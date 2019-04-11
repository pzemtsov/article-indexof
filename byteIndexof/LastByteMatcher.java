package byteIndexof;

public class LastByteMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    static long compare_count = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        private final int [] shifts;
        
        public MatcherImpl (byte [] pattern)
        {
            this.pattern = pattern;
        
            int len = pattern.length;
            shifts = new int [256];
            for (int i = 0; i < 256; i++) {
                shifts [i] = len;
            }
            for (int i = 0; i < len-1; i++) {
                shifts [pattern[i] & 0xFF] = len - i - 1;
            }
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int pattern_len = pattern.length;
            byte last = pattern [pattern_len-1];
            int [] shifts = this.shifts;
            
            for (int pos = fromIndex; pos < text.length - pattern_len + 1;) {
                byte b = text [pos + pattern_len - 1];
                if (b == last) {
                    if (compare (text, pos, pattern, pattern_len-1)) {
                        return pos;
                    }
                    if (DEBUG) {
                        compare_count ++;
                    }
                }
                int shift = shifts [b & 0xFF];
                if (DEBUG) {
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
        return new MatcherImpl (pattern);
    }
    
    @Override
    public String stats ()
    {
        if (shift_count == 0) {
            return "";
        }
        double avg = shift_sum * 1.0 / shift_count;
        double compare_ratio = compare_count * 100.0 / shift_count;
        shift_count = shift_sum = compare_count = 0;
        return String.format ("; avg shift = %5.2f; compare ratio = %5.2f %%", avg, compare_ratio);
    }
}
