package byteIndexof;

public class FirstBytesMatcher extends MatcherFactory
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

    private static final class MatcherImpl_2 extends Matcher
    {
        private final byte a, b;
        
        public MatcherImpl_2  (byte [] pattern)
        {
            a = pattern [0];
            b = pattern [1];
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 1; i++) {
                if (text [i] == a && text [i+1] == b) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private static final class MatcherImpl_3 extends Matcher
    {
        private final byte a, b, c;
        
        public MatcherImpl_3  (byte [] pattern)
        {
            a = pattern [0];
            b = pattern [1];
            c = pattern [2];
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 2; i++) {
                if (text [i] == a && text [i+1] == b && text [i+2] == c) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class MatcherImpl_4 extends Matcher
    {
        private final byte a, b, c, d;
        
        public MatcherImpl_4  (byte [] pattern)
        {
            a = pattern [0];
            b = pattern [1];
            c = pattern [2];
            d = pattern [3];
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 3; i++) {
                if (text [i] == a && text [i+1] == b && text [i+2] == c && text [i+3] == d) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private static final class MatcherImpl_long extends Matcher
    {
        private final byte a, b, c, d;
        private final byte [] pattern;
        
        public MatcherImpl_long  (byte [] pattern)
        {
            a = pattern [0];
            b = pattern [1];
            c = pattern [2];
            d = pattern [3];
            this.pattern = pattern;
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int text_len = text.length;
            int pattern_len =  pattern.length;
            for (int i = fromIdx; i <= text_len - pattern_len; i++) {
                if (DEBUG) ++ count;
                if (text [i] == a && text [i+1] == b && text [i+2] == c && text [i+3] == d) {
                    if (DEBUG) ++ compare_count;
                    if (compare (text, i + 4, pattern, 4, pattern_len - 4)) {
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
        switch (pattern.length) {
        case 1 : return new MatcherImpl_1 (pattern);
        case 2 : return new MatcherImpl_2 (pattern);
        case 3 : return new MatcherImpl_3 (pattern);
        case 4 : return new MatcherImpl_4 (pattern);
        default:
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
