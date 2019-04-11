package stringIndexof;

import regex.Pattern;

public class RegexMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private final Pattern pattern;
        
        public MatcherImpl  (String pattern)
        {
            this.pattern = Pattern.compile (pattern);
        }

        @Override
        public int indexOf (String text, int fromIdx)
        {
            regex.Matcher matcher = pattern.matcher (text);
//            java.util.regex.Matcher matcher = pattern.matcher (text);
            if (! matcher.find (fromIdx)) return -1;
            return matcher.start ();
        }
    }

    @Override
    public Matcher createMatcher (String pattern)
    {
        return new MatcherImpl (pattern);
    }
}
