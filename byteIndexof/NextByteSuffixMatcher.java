package byteIndexof;

public class NextByteSuffixMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        private final int [][] shifts;
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
            shifts = new int [pattern.length][256];
            for (int pos = len-1; pos >= 0; pos --) {
                for (int i = 0; i < 256; i++) {
                    shifts [pos][i] = pos+1;
                }
                for (int i = 0; i < pos; i++) {
                    shifts [pos][pattern[i] & 0xFF] = pos - i;
                }
            }
            
            suffix_shifts = new int [pattern.length];
            suffix_shifts [0] = 0;
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
            int [][] shifts = this.shifts;
            int [] last_shifts = shifts [len-1];
            
            for (int pos = fromIndex; pos <= text.length - len;) {
                byte b = text [pos + len - 1];
                int shift;
                if (b != last) {
                    shift = last_shifts [b & 0xFF];
                } else {
                    int i = len-2;
                    while (true) {
                        b = text [pos + i];
                        if (b != pattern [i]) {
                            break;
                        }
                        if (i == 0) {
                            return pos;
                        }
                        -- i;
                    }
                    int suffix_len = len - i - 1;
                    shift = Math.max (shifts [i][b & 0xFF], suffix_shifts [suffix_len]);
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
        
        String s = "; avg shift = " + avg + ": " + shift_sum + "/" + shift_count;
        shift_count = shift_sum = 0;
        return s;
    }
}
