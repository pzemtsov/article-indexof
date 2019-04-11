package byteIndexof;

public class FirstByteMatcher extends MatcherFactory
{
    static long compare_count = 0;
    static long count = 0;
    
    private static final class MatcherImpl_1 extends Matcher
    {
        private final byte a;
        
        public MatcherImpl_1  (byte [] pattern)
        {
            a = pattern [0];
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len; i++) {
                if (text [i] == a) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class MatcherImpl_long extends Matcher
    {
        private final byte a;
        private final byte [] pattern;
        
        public MatcherImpl_long  (byte [] pattern)
        {
            a = pattern [0];
            this.pattern = pattern;
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            int pattern_len =  pattern.length;
            for (int i = fromIdx; i <= text_len - pattern_len; i++) {
                if (DEBUG) ++ count;
                if (text [i] == a) {
                    if (DEBUG) ++ compare_count;
                    if (compare (text, i + 1, pattern, 1, pattern_len - 1)) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        if (pattern.length == 1) {
            return new MatcherImpl_1 (pattern);
        } else {
            return new MatcherImpl_long (pattern);
        }
    }
    
    @Override
    public String stats ()
    {
        if (count == 0) {
            return "";
        }
        double avg = compare_count * 100.0 / count;
        compare_count = count = 0;
        return String.format ("; long compare = %5.2f %%", avg);
    }
}
