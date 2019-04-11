package stringIndexof;

public class SimpleMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private final String pattern;
        
        public MatcherImpl  (String pattern)
        {
            this.pattern = pattern;
        }

        @Override
        public int indexOf (String text, int fromIdx)
        {
            int pattern_len = pattern.length();
            int buf_len = text.length();
            for (int i = fromIdx; i + pattern_len <= buf_len; i++) {
                if (compare (text, i, pattern, pattern_len)) {
                    return i;
                }
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
