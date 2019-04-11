package stringIndexof;
public class StandardMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private final String pattern;
        
        public MatcherImpl  (String pattern)
        {
            this.pattern = pattern;
        }

        @Override
        public int indexOf (String buf, int fromIdx)
        {
            return buf.indexOf (pattern, fromIdx);
        }
    }

    @Override
    public Matcher createMatcher (String pattern)
    {
        return new MatcherImpl (pattern);
    }
}
