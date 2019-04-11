package byteIndexof;

public class RegexByteMatcher2 extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        private final int [] shifts;
        private final int [] suffix_shifts; // index is the suffix start position
        
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
            suffix_shifts [pattern.length-1] = 0;
            int atzero_len = 0;
            
            for (int suffix_len = 1; suffix_len < pattern.length; suffix_len ++) {
                int pos = find (pattern, suffix_len);
                int suffix_shift;
                
                if (pos < 0) {
                    suffix_shift = len - atzero_len;
                } else {
                    suffix_shift = len - pos - suffix_len;
                }
                suffix_shifts [pattern.length - 1 - suffix_len] = suffix_shift;

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
            int [] shifts = this.shifts;
            int [] suffix_shifts = this.suffix_shifts;
            byte last = pattern [len-1];
            
            int pos = fromIndex;
        NEXT:
            while (pos <= text.length - len) {
                byte b = text [pos + len-1];
                if (b != last) {
                    int shift = shifts [b & 0xFF];
                    if (DEBUG) {
                        ++ shift_count;
                        shift_sum += shift;
                    }
                    pos += shift;
                    continue NEXT;
                }
                for (int i = len-2; i >= 0; i--) {
                    b = text [pos + i];
                    if (b != pattern [i]) {
                        int shift = Math.max (shifts [b & 0xFF] - (len - 1 - i), suffix_shifts [i]);
                        if (DEBUG) {
                            ++ shift_count;
                            shift_sum += shift;
                        }
                        pos += shift;
                        continue NEXT;
                    }
                }
                return pos;
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
        
        String s = "; avg shift = " + avg + ": " + shift_sum + "/" + shift_count;
        shift_count = shift_sum = 0;
        return s;
    }
}
