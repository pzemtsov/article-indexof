package stringIndexof;

public class LastByteMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private final String pattern;
        private final int [] shifts;
        
        public MatcherImpl (String pattern)
        {
            this.pattern = pattern;
        
            int len = pattern.length();
            shifts = new int [256];
            for (int i = 0; i < 256; i++) {
                shifts [i] = len;
            }
            for (int i = 0; i < len-1; i++) {
                shifts [pattern.charAt (i) & 0xFF] = len - i - 1;
            }
        }
        
        @Override
        public int indexOf (String text, int fromIndex)
        {
            int text_len = text.length ();
            String pattern = this.pattern;
            int pattern_len = pattern.length ();
            char last = pattern.charAt (pattern_len-1);
            int [] shifts = this.shifts;
            
            for (int pos = fromIndex; pos < text_len - pattern_len + 1;) {
                char b = text.charAt (pos + pattern_len - 1);
                if (b == last && compare (text, pos, pattern, pattern_len-1)) {
                    return pos;
                }
                int shift = shifts [b & 0xFF];
                pos += shift;
            }
            return -1;
        }
    }

    @Override
    public Matcher createMatcher (String pattern)
    {
        return new MatcherImpl (pattern);
    }
}
