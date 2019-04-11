package stringIndexof;
import java.io.File;
import java.io.FileInputStream;

public class Indexof
{
    static String text;
    static String full_pattern;
    static int ITERS;
    static int [] lengths;

    private static void compare (Matcher ref, Matcher indexof, int length, int start_ind)
    {
        int index = 0;
        while (true) {
            int ref_index = ref.indexOf (text, index);
            int test_index = indexof.indexOf (text, index);
            if (test_index != ref_index) {
                System.out.println ("INDEX NOT EQUAL from " + index + "; text=" + text + "; start_ind=" + start_ind);
                System.out.println ("ref=" + ref_index);
                System.out.println ("test=" + test_index);
                System.out.println (ref);
                System.out.println (indexof);
                System.exit (0);
            }
            if (ref_index < 0) break;
            index = ref_index + 1;
        }
    }

    private static void test_one_length (MatcherFactory factory, int ITERS, int pattern_len)
    {
        Matcher [] matchers = new Matcher [full_pattern.length() - pattern_len + 1];
        for (int start_ind = 0; start_ind < matchers.length; start_ind ++) {
            String pattern = full_pattern.substring (start_ind, start_ind + pattern_len);
            Matcher matcher = factory.createMatcher (pattern);
            compare (new SimpleMatcher ().createMatcher (pattern), matcher, pattern_len, start_ind);
            matchers [start_ind] = matcher;
        }
        
        int NTESTS = Matcher.DEBUG ? 1 : 3;
        int match_count = 0;
        System.out.printf ("%s: %3d: ", factory.toString (), pattern_len);
        long min_time = Long.MAX_VALUE;
        for (int iter = 0; iter < NTESTS; iter ++) {
            int matcher_ind = 0;
            long t1 = System.currentTimeMillis ();
            for (int i = 0; i < ITERS; i ++) {
                Matcher matcher = matchers [matcher_ind];
                if (++matcher_ind == matchers.length) matcher_ind = 0;
                int index = 0;
                while (true) {
                    index = matcher.indexOf (text, index);
                    if (index < 0) break;
                    ++ match_count;
                    ++ index;
                }
            }
            long t2 = System.currentTimeMillis ();
            long time = t2 - t1;
            System.out.printf ("%5d ", time);
            if (time < min_time) min_time = time;
        }
        double ns = min_time * 1000000.0 / ITERS / text.length();
        System.out.printf ("; count=%10d; ns/byte=%6.2f\n", match_count, ns);
    }
    
    private static void test (MatcherFactory factory)
    {
        System.out.println ("Test: " + factory.getClass ().getSimpleName ());
        
        for (int pattern_len : lengths) {
            test_one_length (factory, ITERS, pattern_len);
        }
    }
    
    private static void read_source (String name) throws Exception
    {
        File f = new File (name);
        FileInputStream in = new FileInputStream (f);
        int length = (int) f.length ();
        byte [] buf = new byte [length];
        in.read (buf);
        in.close ();
        int j = 0;
        for (byte b : buf) {
            if (b < 0) continue;
            if (b == '\n') b = (byte) ' ';
            if (b >= 'A' && b <= 'Z') b |= 0x20;
            if (b >= 'a' && b <= 'z' || b == ' ') {
                buf [j++] = b;
            }
        }
        text = new String (buf, 0, j);
    }
    
    static void define_line ()
    {
        String line = "doubt thou the stars are fire " +
                      "doubt that the sun doth move " +
                      "doubt truth to be a liar " +
                      "but never doubt i love";

        full_pattern = line;
        lengths = new int [] {4, 8, 16, 32, 64, 96, full_pattern.length ()};
    }
    
    public static void main (String [] args) throws Exception
    {
        read_source ("text"); define_line (); ITERS = 100000;
        
        test (new StandardMatcher ());
        test (new SimpleMatcher ());
        test (new BytesMatcher ());
        test (new IndexofMatcher ());
        test (new RegexMatcher ());
        test (new LastByteMatcher ());
        test (new SuffixMatcher ());
    }
}
