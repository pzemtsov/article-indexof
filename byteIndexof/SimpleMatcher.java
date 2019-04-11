package byteIndexof;

public class SimpleMatcher extends MatcherFactory
{
    static long neq_ind_sum = 0;
    static long compare_count = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private final byte [] pattern;
        
        public MatcherImpl  (byte [] pattern)
        {
            this.pattern = pattern;
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            int pattern_len = pattern.length;
            int text_len = text.length;
            for (int i = fromIdx; i + pattern_len <= text_len; i++) {
                if (DEBUG) {
                    ++ compare_count;
                    neq_ind_sum += neq_index (text, i, pattern, pattern_len);
                }
                if (compare (text, i, pattern, pattern_len)) {
                    return i;
                }
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
        if (compare_count == 0) {
            return "";
        }
        double avg = neq_ind_sum * 1.0 / compare_count;
        compare_count = neq_ind_sum = 0;
        return String.format ("; avg neq index = %5.2f", avg);
    }
}
