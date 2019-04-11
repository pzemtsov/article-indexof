package byteIndexof;

public class UnrolledLimitedMultiByteMatcher extends MatcherFactory
{
    private int n;

    public UnrolledLimitedMultiByteMatcher (int n)
    {
        this.n = n;
    }
    
    private static abstract class MatcherImpl extends Matcher
    {
        protected final byte [] pattern;
        protected final int [][] shifts;
        
        public MatcherImpl (byte [] pattern, int n)
        {
            this.pattern = pattern;

            int len = pattern.length;
            shifts = new int [pattern.length][256];
            for (int pos = len-1; pos >= len-n; pos --) {
                for (int i = 0; i < 256; i++) {
                    shifts [pos][i] = pos+1;
                }
                for (int i = 0; i < pos; i++) {
                    shifts [pos][pattern[i] & 0xFF] = pos - i;
                }
            }
        }
    }
    
    private static final class MatcherImpl2 extends MatcherImpl
    {
        public MatcherImpl2 (byte [] pattern)
        {
            super (pattern, 2);
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int len = pattern.length;
            int [] shifts1 = shifts[len-1];
            int [] shifts2 = shifts[len-2];
            byte last = pattern [len-1];
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                byte b1 = text [pos + len-1];
                if (b1 == last && compare (text, pos, pattern, len-1)) {
                    return pos;
                }
                
                int shift = shifts1 [b1 & 0xFF];
                if (shift < len) {
                    byte b2 = text [pos + len-2];
                    int sh = shifts2 [b2 & 0xFF];
                    if (sh > shift) shift = sh;
                }
                pos += shift;
            }
            return -1;
        }
    }

    private static final class MatcherImpl3 extends MatcherImpl
    {
        public MatcherImpl3 (byte [] pattern)
        {
            super (pattern, 3);
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int len = pattern.length;
            int [] shifts1 = shifts[len-1];
            int [] shifts2 = shifts[len-2];
            int [] shifts3 = shifts[len-3];
            byte last = pattern [len-1];
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                byte b1 = text [pos + len-1];
                if (b1 == last && compare (text, pos, pattern, len-1)) {
                    return pos;
                }
                
                int shift = shifts1 [b1 & 0xFF];
                if (shift < len) {
                    byte b2 = text [pos + len-2];
                    int sh = shifts2 [b2 & 0xFF];
                    if (sh > shift) shift = sh;
                    if (shift < len) {
                        byte b3 = text [pos + len-3];
                        int sh3 = shifts3 [b3 & 0xFF];
                        if (sh3 > shift) shift = sh3;
                    }
                }
                pos += shift;
            }
            return -1;
        }
    }

    private static final class MatcherImpl4 extends MatcherImpl
    {
        public MatcherImpl4 (byte [] pattern)
        {
            super (pattern, 4);
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int len = pattern.length;
            int [] shifts1 = shifts[len-1];
            int [] shifts2 = shifts[len-2];
            int [] shifts3 = shifts[len-3];
            int [] shifts4 = shifts[len-4];
            byte last = pattern [len-1];
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                byte b1 = text [pos + len-1];
                if (b1 == last && compare (text, pos, pattern, len-1)) {
                    return pos;
                }
                
                int shift = shifts1 [b1 & 0xFF];
                if (shift < len) {
                    byte b2 = text [pos + len-2];
                    int sh = shifts2 [b2 & 0xFF];
                    if (sh > shift) shift = sh;
                    if (shift < len) {
                        byte b3 = text [pos + len-3];
                        int sh3 = shifts3 [b3 & 0xFF];
                        if (sh3 > shift) shift = sh3;
                        if (shift < len) {
                            byte b4 = text [pos + len-4];
                            int sh4 = shifts4 [b4 & 0xFF];
                            if (sh4 > shift) shift = sh4;
                        }
                    }
                }
                pos += shift;
            }
            return -1;
        }
    }
    
    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        if (pattern.length < n) {
            return new FirstBytesMatcher ().createMatcher (pattern);
        }
        switch (n) {
            case 2 : return new MatcherImpl2 (pattern);
            case 3 : return new MatcherImpl3 (pattern);
            case 4 : return new MatcherImpl4 (pattern);
        }
        return new LimitedMultiByteMatcher (n).createMatcher (pattern);
    }
    
    @Override
    public String toString ()
    {
        return getClass ().getSimpleName () + " (" + n + ")";
    }

}
