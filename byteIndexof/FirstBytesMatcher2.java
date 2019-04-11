package byteIndexof;

public class FirstBytesMatcher2 extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        
        public MatcherImpl  (byte [] pattern)
        {
            this.pattern = pattern;
        }

        private int indexOf (byte[] text, int fromIdx, byte a)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len; i++) {
                if (text [i] == a) {
                    return i;
                }
            }
            return -1;
        }

        private int indexOf (byte[] text, int fromIdx, byte a, byte b)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 1; i++) {
                if (text [i] == a && text [i+1] == b) {
                    return i;
                }
            }
            return -1;
        }

        private int indexOf (byte[] text, int fromIdx, byte a, byte b, byte c)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 2; i++) {
                if (text [i] == a && text [i+1] == b && text [i+2] == c) {
                    return i;
                }
            }
            return -1;
        }

        private int indexOf (byte[] text, int fromIdx, byte a, byte b, byte c, byte d)
        {
            int text_len = text.length;
            for (int i = fromIdx; i < text_len - 3; i++) {
                if (text [i] == a && text [i+1] == b && text [i+2] == c && text [i+3] == d) {
                    return i;
                }
            }
            return -1;
        }

        private int indexOf (byte[] text, int fromIdx, byte [] pattern)
        {
            int text_len = text.length;
            int pattern_len =  pattern.length;
            if (text_len < pattern_len) {
                return -1;
            }
            byte a = pattern [0];
            byte b = pattern [1];
            byte c = pattern [2];
            byte d = pattern [3];
            byte A = text [fromIdx];
            byte B = text [fromIdx+1];
            byte C = text [fromIdx+2];

            for (int i = fromIdx; i <= text_len - pattern_len; i++) {
                byte D = text [i+3];
                if (A == a && B == b && C == c && D == d && compare (text, i+4, pattern, 4, pattern_len - 4)) {
                    return i;
                }
                A = B;
                B = C;
                C = D;
            }
            return -1;
        }
        
        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            switch (pattern.length) {
            case 1 : return indexOf (text, fromIdx, pattern [0]);
            case 2 : return indexOf (text, fromIdx, pattern [0], pattern [1]);
            case 3 : return indexOf (text, fromIdx, pattern [0], pattern [1], pattern [2]);
            case 4 : return indexOf (text, fromIdx, pattern [0], pattern [1], pattern [2], pattern [3]);
            default:
                return indexOf (text, fromIdx, pattern);
            }
        }
        
    }

    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        return new MatcherImpl (pattern);
    }
}
