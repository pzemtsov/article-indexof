package byteIndexof;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CompileMatcher extends MatcherFactory
{
    private static int class_count = 0;
  
    static File classDir;
    static ClassLoader classLoader;
    
    static
    {
        final String tempDir = System.getProperty("java.io.tmpdir");
        classDir = new File (tempDir, "classdir");
        if (!classDir.isDirectory () && !classDir.mkdirs()) throw new RuntimeException ("Can not create class directory for classes");
        
        try {
            classLoader = new URLClassLoader (new URL [] {classDir.toURI().toURL ()}, CompileMatcher.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException ("Can't set up the class loader");
        }
    }
    
    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        int len = pattern.length;
        
        ++ class_count;
        String className = "CompiledMatcher" + class_count;
        String packageName = "compiled_matcher";
        File outDir = new File (classDir, packageName);
        outDir.mkdirs ();
        File javaFile = new File (outDir, className + ".java");

        try {
            PrintWriter w = new PrintWriter (new FileWriter (javaFile));
            w.println ("package " + packageName + ";");
            w.println ();
            w.println ("import byteIndexof.Matcher;");
            w.println ();
            w.println ("public final class " + className + " extends Matcher");
            w.println ("{");
            w.println ("    @Override");
            w.println ("    public int indexOf (byte[] text, int fromIdx)");
            w.println ("    {");
            w.println ("        for (int i = fromIdx; i < text.length - " + (len - 1) + "; i++) {");
            for (int j = 0; j < len; j++) {
                w.println ("            if (text [i+" + j + "] != " + pattern [j] + ") continue;");
            }
            w.println ("            return i;");
            w.println ("        }");
            w.println ("        return -1;");
            w.println ("    }");
            w.println ("}");
            w.close ();
        } catch (IOException e) {
            throw new RuntimeException ("Can not create file: " + e);
        }
        try {
            ProcessBuilder p = new ProcessBuilder (
                                                   "javac",
                                                   "-classpath", ".",
                                                   javaFile.getAbsolutePath ()
                                                   );
            Process proc = p.start ();
            InputStream is = proc.getErrorStream ();
            int b;
            while ((b = is.read ()) >= 0) System.out.write (b);
            proc.waitFor ();
        } catch (IOException e) {
            throw new RuntimeException ("Can not invoke javac: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException ("Wait for javac interrupted");
        }
        
        try {
            Class<?> clazz = Class.forName (packageName + "." + className, true, classLoader);
            return (Matcher) clazz.newInstance ();
        } catch (Exception e) {
            throw new RuntimeException ("Can not create class", e);
        }
    }
}
