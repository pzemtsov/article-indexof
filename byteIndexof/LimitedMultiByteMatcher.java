package byteIndexof;

public class LimitedMultiByteMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    static long shift_cnt_sum = 0;
    
    private int n;

    public LimitedMultiByteMatcher (int n)
    {
        this.n = n;
    }
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        private final int [][] shifts;
        private final int n;
        
        public MatcherImpl (byte [] pattern, int n)
        {
            this.pattern = pattern;
            this.n = n;

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
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int pattern_len = pattern.length;
            int [][] shifts = this.shifts;
            
            for (int pos = fromIndex; pos < text.length - pattern_len + 1;) {
                if (compare (text, pos, pattern, pattern_len)) {
                    return pos;
                }
                
                int shift = 0;
                int i;
                for (i = pattern_len - 1; i >= pattern_len-n; i--) {
                    int sh = shifts [i] [text [pos + i] & 0xFF];
                    if (sh > shift) shift = sh;
                    if (shift > i) {
                        break;
                    }
                }
                if (DEBUG) {
                    if (i < pattern_len-n) i = pattern_len-n;
                    shift_cnt_sum += pattern_len-i;
                    shift_count ++;
                    shift_sum += shift; 
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
        return new MatcherImpl (pattern, n);
    }
    
    @Override
    public String stats ()
    {
        if (shift_count == 0) {
            return "";
        }
        double avg = shift_sum * 1.0 / shift_count;
        double avg_cnt = shift_cnt_sum * 1.0 / shift_count;
        shift_count = shift_sum = shift_cnt_sum = 0;
        return String.format ("; avg shift = %5.2f; avg shift count = %5.2f", avg, avg_cnt);
    }
    
    @Override
    public String toString ()
    {
        return getClass ().getSimpleName () + " (" + n + ")";
    }
    
}
