package byteIndexof;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CompileSuffixMatcher2 extends MatcherFactory
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

    private static final class Node
    {
        Node [] nodes = null;
        int shift = 0;
        
        public Node (int shift)
        {
            this.shift = shift;
        }
    }
    
    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        int pattern_len = pattern.length;

        Node root = new Node (pattern_len);

        for (int prefix_len = 1; prefix_len <= pattern_len; prefix_len++) {
            Node node = root;
            for (int i = prefix_len - 1; i >= 0; --i) {
                int b = pattern [i] & 0xFF;
                if (node.nodes == null) {
                    node.nodes = new Node [256];
                }
                if (node.nodes [b] == null) {
                    node.nodes [b] = new Node (pattern_len);
                }
                node = node.nodes [b];
            }
            node.shift = pattern_len - prefix_len;
        }
        for (int end = pattern_len; end > 0; end --) {
            Node node = root;
            int shift = pattern_len;
            for (int i = end - 1; i >= 0; i--) {
                int b = pattern [i] & 0xFF;
                node = node.nodes [b];
                node.shift = shift = Math.min (node.shift, shift);
            }
        }
        
        ++ class_count;
        String className = "CompiledSuffixMatcher" + class_count;
        String packageName = "compiled_suffix_matcher";
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
            w.println ("        for (int pos = fromIdx; pos < text.length - " + (pattern_len - 1) + "; ) {");
            w.println ("            int shift = match (text, pos);");
            w.println ("            if (shift == 0) return pos;");
            w.println ("            pos += shift;");
            w.println ("        }");
            w.println ("        return -1;");
            w.println ("    }");
            w.println ();
            w.println ("    private int match (byte [] text, int pos)");
            w.println ("    {");
            generate (w, root, "        ", pattern_len-1);
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
    
    private void generate (PrintWriter w, Node node, String indent, int pos) throws IOException
    {
        int count = 0;
        int single_byte = 0;
        for (int i = 0; i < 256; i++) {
            if (node.nodes [i] != null) {
                ++ count;
                single_byte = i;
            }
        }
        if (count == 1) {
            w.print (indent + "if (text [pos + " + pos + "] == " + (byte) single_byte + ")");
            Node n = node.nodes [single_byte];
            if (n.nodes == null) {
                w.print (" return " + n.shift + ";");
            } else {
                w.println (" {");
                generate (w, n, indent + "    ", pos - 1);
                w.println (indent + "}");
            }
            w.println (indent + "return " + node.shift + ";");
        } else {
            w.println (indent + "switch (text [pos + " + pos + "]) {");
            for (int i = 0; i < 256; i++) {
                Node n = node.nodes [i];
                if (n != null) {
                    w.print (indent + "case " + (byte) i + ": ");
                    if (n.nodes == null) {
                        w.println ("return " + n.shift + ";");
                    } else {
                        w.println ();
                        generate (w, n, indent + "    ", pos - 1);
                    }
                }
            }
            w.println (indent + "}");
            w.println (indent + "return " + node.shift + ";");
        }
    }
}
