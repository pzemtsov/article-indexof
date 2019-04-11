package byteIndexof;

public class LastByteSuffixMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    static long shift_count_one = 0;
    static long shift_sum_one = 0;
    static long shift_count_bigger_than_one = 0;
    static long shift_sum_bigger_than_one = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        private final int [] shifts;
        private final int [] suffix_shifts; // index is the suffix length
        
        private int find (byte [] pattern, int suffix_len)
        {
            int len = pattern.length;
            
            for (int pos = len - suffix_len - 1; pos >= 0; pos --) {
                if (compare (pattern, pos, pattern, len - suffix_len, suffix_len)) {
                    return pos;
                }
            }
            return -1;
        }

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
            
            suffix_shifts = new int [pattern.length];
            suffix_shifts [0] = shifts [pattern [len-1] & 0xFF];
            int atzero_len = 0;
            
            for (int suffix_len = 1; suffix_len < pattern.length; suffix_len ++) {
                int pos = find (pattern, suffix_len);
                int suffix_shift;
                
                if (pos < 0) {
                    suffix_shift = len - atzero_len;
                } else {
                    suffix_shift = len - pos - suffix_len;
                }
                suffix_shifts [suffix_len] = suffix_shift;

                if (compare (pattern, len - suffix_len, pattern, 0, suffix_len)) {
                    atzero_len = suffix_len;
                }
            }
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            byte [] pattern = this.pattern;
            int len = pattern.length;
            byte last = pattern [len-1];
            int [] shifts = this.shifts;
            
            for (int pos = fromIndex; pos <= text.length - len;) {
                byte b = text [pos + len - 1];
                int shift;
                if (b != last) {
                    shift = shifts [b & 0xFF];
                } else {
                    int i = len-2;
                    while (true) {
                        if (text [pos + i] != pattern [i]) {
                            break;
                        }
                        if (i == 0) {
                            return pos;
                        }
                        -- i;
                    }
                    int suffix_len = len - i - 1;
                    shift = suffix_shifts [suffix_len];
                    if (DEBUG) {
                        if (suffix_len == 1) {
                            shift_count_one ++; 
                            shift_sum_one += shift;
                        } else {
                            shift_count_bigger_than_one ++;
                            shift_sum_bigger_than_one += shift;
                        }
                    }
                }
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
        if (pattern.length <= 2) {
            return new FirstBytesMatcher ().createMatcher (pattern);
        }
        return new MatcherImpl (pattern);
    }
    
    @Override
    public String stats ()
    {
        if (shift_count == 0) {
            return "";
        }
        double avg = shift_sum * 1.0 / shift_count;
        double avg1 = shift_sum_one * 1.0 / shift_count_one;
        double avgm1 = shift_sum_bigger_than_one * 1.0 / shift_count_bigger_than_one;
        
        
        shift_count = shift_sum = shift_count_one = shift_sum_one = shift_count_bigger_than_one = shift_sum_bigger_than_one  = 0;
        return String.format ("; avg shift = %5.2f; one: %5.2f; more than one: %5.2f %%", avg, avg1, avgm1);
    }
}
