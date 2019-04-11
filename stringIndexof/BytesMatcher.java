package stringIndexof;
public class BytesMatcher extends MatcherFactory
{
    private static final class MatcherImpl_1 extends Matcher
    {
        private final char a;
        
        public MatcherImpl_1  (String pattern)
        {
            a = pattern.charAt (0);
        }

        @Override
        public int indexOf (String text, int fromIdx)
        {
            int text_len = text.length();
            for (int i = fromIdx; i < text_len; i++) {
                if (text.charAt (i) == a) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class MatcherImpl_2 extends Matcher
    {
        private final char a, b;
        
        public MatcherImpl_2  (String pattern)
        {
            a = pattern.charAt (0);
            b = pattern.charAt (1);
        }

        @Override
        public int indexOf (String buf, int fromIdx)
        {
            int buf_len = buf.length();
            for (int i = fromIdx; i < buf_len - 1; i++) {
                if (buf.charAt (i) == a && buf.charAt (i+1) == b) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private static final class MatcherImpl_3 extends Matcher
    {
        private final char a, b, c;
        
        public MatcherImpl_3  (String pattern)
        {
            a = pattern.charAt (0);
            b = pattern.charAt (1);
            c = pattern.charAt (2);
        }

        @Override
        public int indexOf (String buf, int fromIdx)
        {
            int buf_len = buf.length();
            for (int i = fromIdx; i < buf_len - 2; i++) {
                if (buf.charAt (i) == a && buf.charAt (i+1) == b && buf.charAt (i+2) == c) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class MatcherImpl_4 extends Matcher
    {
        private final char a, b, c, d;
        
        public MatcherImpl_4  (String pattern)
        {
            a = pattern.charAt (0);
            b = pattern.charAt (1);
            c = pattern.charAt (2);
            d = pattern.charAt (3);
        }

        @Override
        public int indexOf (String buf, int fromIdx)
        {
            int buf_len = buf.length();
            for (int i = fromIdx; i < buf_len - 3; i++) {
                if (buf.charAt (i) == a && buf.charAt (i+1) == b && buf.charAt (i+2) == c && buf.charAt (i+3) == d) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private static final class MatcherImpl_long extends Matcher
    {
        private final char a, b, c, d;
        private final String pattern;
        
        public MatcherImpl_long  (String pattern)
        {
            a = pattern.charAt (0);
            b = pattern.charAt (1);
            c = pattern.charAt (2);
            d = pattern.charAt (3);
            this.pattern = pattern;
        }

        @Override
        public int indexOf (String buf, int fromIdx)
        {
            int buf_len = buf.length();
            int text =  pattern.length();
            for (int i = fromIdx; i <= buf_len - text; i++) {
                if (buf.charAt (i) == a && buf.charAt (i+1) == b && buf.charAt (i+2) == c && buf.charAt (i+3) == d
                    && compare (buf, i + 4, pattern, 4, text - 4))
                {
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    public Matcher createMatcher (String pattern)
    {
        switch (pattern.length()) {
        case 1 : return new MatcherImpl_1 (pattern);
        case 2 : return new MatcherImpl_2 (pattern);
        case 3 : return new MatcherImpl_3 (pattern);
        case 4 : return new MatcherImpl_4 (pattern);
        default:
            return new MatcherImpl_long (pattern);
        }
    }
}
