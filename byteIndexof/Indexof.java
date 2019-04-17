package byteIndexof;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Random;

public class Indexof
{
    static byte [] [] texts;
    static byte [] full_pattern;
    static int ITERS;
    static int [] lengths;
    static int max_matcher_count = Integer.MAX_VALUE;
    static boolean DUMP_MEMORY = false;

    private static void compare (Matcher ref, Matcher indexof, int pattern_len, int start_ind)
    {
        int index = 0;
        while (true) {
            int ref_index = ref.indexOf (texts[0], index);
            int test_index = indexof.indexOf (texts[0], index);
            if (test_index != ref_index) {
                System.out.println ("INDEX NOT EQUAL from " + index + "; pattern_len=" + pattern_len + "; start_ind=" + start_ind);
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
        Matcher [] matchers = new Matcher [Math.min (full_pattern.length - pattern_len + 1, max_matcher_count)];
        boolean dumped = false;
        for (int start_ind = 0; start_ind < matchers.length; start_ind ++) {
            byte [] pattern = Arrays.copyOfRange (full_pattern, start_ind, start_ind + pattern_len);
            Matcher matcher;
            if (DUMP_MEMORY) {
                System.gc ();
                long mem1 = Runtime.getRuntime ().totalMemory () - Runtime.getRuntime ().freeMemory ();
                matcher = factory.createMatcher (pattern);
                System.gc ();
                long mem2 = Runtime.getRuntime ().totalMemory () - Runtime.getRuntime ().freeMemory ();
                if (!dumped)
                    System.out.println ("Memory: " + factory + ":" + pattern_len + ": " + (mem2 - mem1));
                dumped = true;
            } else {
                matcher = factory.createMatcher (pattern);
            }
            compare (new SimpleMatcher ().createMatcher (pattern), matcher, pattern_len, start_ind);
            matchers [start_ind] = matcher;
        }
        
        int NTESTS = Matcher.DEBUG ? 1 : 3;
        int match_count = 0;
        System.out.printf ("%s: %3d: ", factory.toString (), pattern_len);
        long min_time = Long.MAX_VALUE;
        long total_bytes = 0;
        int text_ind = 0;
        for (int iter = 0; iter < NTESTS; iter ++) {
            total_bytes = 0;
            int matcher_ind = 0;
            long t1 = System.currentTimeMillis ();
            for (int i = 0; i < ITERS; i ++) {
                byte [] text = texts [text_ind];
                Matcher matcher = matchers [matcher_ind];
                if (++matcher_ind == matchers.length) matcher_ind = 0;
                int index = 0;
                while (true) {
                    index = matcher.indexOf (text, index);
                    if (index < 0) break;
                    ++ match_count;
                    ++ index;
                }
                total_bytes += text.length;
                text_ind ++;
                if (text_ind >= texts.length) text_ind = 0;
            }
            long t2 = System.currentTimeMillis ();
            long time = t2 - t1;
            System.out.printf ("%5d ", time);
            if (time < min_time) min_time = time;
        }
        double ns = min_time * 1000000.0 / total_bytes;
        System.out.printf ("; count=%10d; bytes=%d; ns/byte=%8.4f%s\n", match_count, total_bytes, ns, factory.stats ());
    }
    
    private static void test (MatcherFactory factory)
    {
        String test_name = factory.toString ();
        System.out.println ("Test: " + test_name);

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
        texts = new byte[][] {Arrays.copyOf (buf,  j)};
    }

    static void define_line ()
    {
        String line = "doubt thou the stars are fire " +
                      "doubt that the sun doth move " +
                      "doubt truth to be a liar " +
                      "but never doubt i love";

        full_pattern = line.getBytes ();
        lengths = new int [] {4, 8, 16, 32, 64, 96, full_pattern.length};
    }

    private static void read_utf (String name) throws Exception
    {
        File f = new File (name);
        FileInputStream in = new FileInputStream (f);
        int length = (int) f.length ();
        byte [] buf = new byte [length];
        in.read (buf);
        in.close ();
        texts = new byte[][] {buf};
    }

    static void define_line_utf ()
    {
        
        byte [] text = texts [0];
        int ind = text.length * 2 / 3;
        while (text[ind] != (byte) '\n') ++ ind;
        while ((text[ind] & 0xFF) < 0x80) ++ ind;
        int i = 0;
        while (text [ind+i] != '\r') ++ i;
        System.out.println ("Pattern Length = " + i);
        full_pattern = Arrays.copyOfRange (text, ind, ind + i);
        lengths = new int [] {4, 8, 16, 32, 64, 96, full_pattern.length};
        for (int j = 0; j < lengths.length - 1; j++) {
            while ((full_pattern [lengths[j]] & 0xC0) == 0x80) ++ lengths[j];
        }
    }
    
    static void generate_source (int size, int ntexts)
    {
        Random rand = new Random (0);
        texts = new byte [ntexts][];
        for (int i = 0; i < ntexts; i++) {
            texts [i] = new byte [size];
            rand.nextBytes (texts [i]);
            System.out.println ("Allocated " + i);
        }
    }
    
    static void generate_line (int len)
    {
        full_pattern = new byte [len];
        byte [] text = texts [texts.length/2];
        System.arraycopy (text, text.length / 2,  full_pattern,  0,  full_pattern.length);
    }
    
    public static void main (String [] args) throws Exception
    {
        String test_type = args.length == 0 ? "text" : args [0];
        System.out.println ("test type: " + test_type);
        switch (test_type) {
        case "text":
            read_source ("text");
            define_line ();
            ITERS = 50000;
            break;
        case "utf":
            read_utf ("Book-23950.txt");
            define_line_utf ();
            max_matcher_count = 1;
            ITERS = 2000;
            break;
        case "random":
            generate_source (4 * 1024 * 1024, 1);
            generate_line (512);
            max_matcher_count = 4;
            lengths = new int [] {4, 8, 16, 32, 64, 128, 256};
            ITERS = 500;
            break;
        case "medium":
            generate_source (4 * 1024 * 1024, 1);
            generate_line (8192);
            max_matcher_count = 1;
            lengths = new int [] {512, 1024, 2048, 4096, 8192};
            ITERS = 500;
            break;
        case "big":
            generate_source (4 * 1024 * 1024, 1);
            generate_line (1 << 20);
            max_matcher_count = 1;
            lengths = new int [] {1<<14, 1<<15, 1<<16, 1<<17, 1<<18, 1<<19, 1<<20};
            ITERS = 500;
            break;
        case "huge":
            generate_source (1024 * 1024 * 1024, 16);
            generate_line (1 << 20);
            max_matcher_count = 1;
            lengths = new int [] {1<<14, 1<<15, 1<<16, 1<<17, 1<<18, 1<<19, 1<<20};
            ITERS = 16;
            break;
        default:
            System.out.println ("Usage: java byteIndexof.Indexof text | random | medium | big | huge");
            return;
        }

        test (new SimpleMatcher ());
        test (new FirstByteMatcher ());
        test (new FirstBytesMatcher ());
        test (new FirstBytesMatcher2 ());
        test (new CompileMatcher ());
        test (new HashMatcher ());
        test (new LastByteMatcher ());
        test (new MultiByteMatcher ());
        test (new LimitedMultiByteMatcher (2));
        test (new LimitedMultiByteMatcher (3));
        test (new LimitedMultiByteMatcher (4));
        test (new UnrolledLimitedMultiByteMatcher (2));
        test (new UnrolledLimitedMultiByteMatcher (3));
        test (new UnrolledLimitedMultiByteMatcher (4));
        test (new LastByteSuffixMatcher ());
        test (new NextByteSuffixMatcher ());
        test (new RegexByteMatcher ());
        test (new RegexByteMatcher2 ());
        test (new SuffixMatcher ());
        test (new CompileSuffixMatcher ());
//        test (new CompileSuffixMatcher2 ());
        test (new CompileSuffixMatcher3 (1));
        test (new CompileSuffixMatcher3 (2));
        test (new CompileSuffixMatcher3 (3));
        test (new CompileSuffixMatcher3 (4));
        test (new CompileSuffixMatcher3 (5));
        test (new CompileSuffixMatcher3 (6));
        test (new CompileSuffixMatcher3 (7));
        test (new CompileSuffixMatcher3 (8));
        test (new SmallSuffixMatcher ());
        test (new ChainSuffixMatcher ());
        test (new ChainSuffixMatcher2 ());
        test (new ChainSuffixMatcher3 ());
        test (new LightChainSuffixMatcher ());
        test (new LightestChainSuffixMatcher ());
        test (new TwoByteHashShiftMatcher ());
    }
}
