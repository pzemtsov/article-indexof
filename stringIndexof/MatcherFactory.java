package stringIndexof;

public abstract class MatcherFactory
{
    public abstract Matcher createMatcher (String pattern);

    @Override
    public String toString ()
    {
        return getClass ().getSimpleName ();
    }
}
