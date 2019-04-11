package byteIndexof;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CompileSuffixMatcher3 extends MatcherFactory
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
    
    private final int limit;

    public CompileSuffixMatcher3 (int limit)
    {
        this.limit = limit;
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
        int [] suffix = new int [pattern_len];

        try {
            PrintWriter w = new PrintWriter (new FileWriter (javaFile));
            w.println ("package " + packageName + ";");
            w.println ();
            w.println ("import byteIndexof.Matcher;");
            w.println ();
            w.println ("public final class " + className + " extends Matcher");
            w.println ("{");
            w.println ("    private static final class Node");
            w.println ("    {");
            w.println ("        Node [] nodes = null;");
            w.println ("        int shift;");
            w.println ("");
            w.println ("        public Node (int shift)");
            w.println ("        {");
            w.println ("            this.shift = shift;");
            w.println ("        }");
            w.println ("    }");
            w.println ("");
            w.print ("    private static byte [] pattern = new byte [] ");
            String delim = "{";
            for (byte b : pattern) {
                w.print (delim);
                w.print (b);
                delim = ", ";
            }
            w.println ("};");
            w.println ("");
            w.println ("    private static Node createRoot ()");
            w.println ("    {");
            w.println ("        Node root = new Node (" + pattern_len + ");");
            w.println ("");
            w.println ("        for (int prefix_len = 1; prefix_len <= " + pattern_len + "; prefix_len++) {");
            w.println ("            Node node = root;");
            w.println ("            for (int i = prefix_len - 1; i >= 0; --i) {");
            w.println ("                int b = pattern [i] & 0xFF;");
            w.println ("                if (node.nodes == null) {");
            w.println ("                    node.nodes = new Node [256];");
            w.println ("                }");
            w.println ("                if (node.nodes [b] == null) {");
            w.println ("                    node.nodes [b] = new Node (" + pattern_len + ");");
            w.println ("                }");
            w.println ("                node = node.nodes [b];");
            w.println ("            }");
            w.println ("            node.shift = " + pattern_len + " - prefix_len;");
            w.println ("        }");
            w.println ("        for (int end = " + pattern_len + "; end > 0; end --) {");
            w.println ("            Node node = root;");
            w.println ("            int shift = " + pattern_len + ";");
            w.println ("            for (int i = end - 1; i >= 0; i--) {");
            w.println ("                node = node.nodes [pattern [i] & 0xFF];");
            w.println ("                node.shift = shift = Math.min (node.shift, shift);");
            w.println ("            }");
            w.println ("        }");
            w.println ("        return root;");
            w.println ("    }");
            w.println ("");
            w.println ("    private static final Node root = createRoot ();");
            w.println ("");
            generate_nodes (w, root, suffix, 0);
            w.println ("");
            w.println ("    private static Node find (int ... bytes)");
            w.println ("    {");
            w.println ("        Node n = root;");
            w.println ("        for (int b : bytes) {");
            w.println ("            n = n.nodes [b];");
            w.println ("        }");
            w.println ("        return n;");
            w.println ("    }");
            w.println ("");
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
            w.println ("    private static int match (byte [] text, int pos)");
            w.println ("    {");

            generate (w, root, suffix, 0, "        ", pattern_len-1);
            w.println ("    }");
            w.println ("");
            w.println ("    private static int match (byte [] text, int pos, Node node)");
            w.println ("    {");
            w.println ("        while (true) {");
            w.println ("            Node n = node.nodes [text [pos] & 0xFF];");
            w.println ("            if (n == null) return node.shift;");
            w.println ("            if (n.nodes == null) return n.shift;");
            w.println ("            node = n;");
            w.println ("            pos --;");
            w.println ("        }");
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

    private void generate_nodes (PrintWriter w, Node node, int [] suffix, int suffix_len) throws IOException
    {
        if (suffix_len >= limit) {
            w.print ("    private static final Node " + node_name (suffix, suffix_len-1) + " = find ");
            for (int i = 0; i < suffix_len; i++) {
                w.print (i == 0 ? "(" : ", ");
                w.print (suffix [i]);
            }
            w.println (");");
        } else {
            if (node.nodes != null) {
                for (int i = 0; i < 256; i++) {
                    if (node.nodes [i] != null && node.nodes [i].nodes != null) {
                        suffix [suffix_len] = i;
                        generate_nodes (w, node.nodes [i], suffix, suffix_len + 1);
                    }
                }
            }
        }
    }
    
    private void generate (PrintWriter w, Node node, int [] suffix, int suffix_len, String indent, int pos) throws IOException
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
                suffix [suffix_len] = single_byte;
                if (suffix_len >= limit-1) {
                    w.println (" return match (text, pos + " + (pos - 1) + ", " + node_name (suffix, suffix_len) + ");");
                } else {
                    w.println (" {");
                    generate (w, n, suffix, suffix_len + 1, indent + "    ", pos - 1);
                    w.println (indent + "}");
                }
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
                        suffix [suffix_len] = i;
                        if (suffix_len >= limit-1) {
                            w.println ("return match (text, pos + " + (pos - 1) + ", " + node_name (suffix, suffix_len) + ");");
                        } else {
                            w.println ();
                            generate (w, n, suffix, suffix_len + 1, indent + "    ", pos - 1);
                        }
                    }
                }
            }
            w.println (indent + "}");
            w.println (indent + "return " + node.shift + ";");
        }
    }
    
    private static String node_name (int [] suffix, int suffix_len)
    {
        StringBuilder b = new StringBuilder ("node");
        for (int i = 0; i <= suffix_len; i++) {
            b.append ("_").append (String.format ("%02X", suffix [i]));
        }
        return b.toString ();
    }

    @Override
    public String toString ()
    {
        return getClass ().getSimpleName () + "(" + limit + ")";
    }
}
