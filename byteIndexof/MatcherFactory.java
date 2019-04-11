package byteIndexof;

public abstract class MatcherFactory
{
    public abstract Matcher createMatcher (byte [] pattern);

    public String stats ()
    {
        return "";
    }
    
    @Override
    public String toString ()
    {
        return getClass ().getSimpleName ();
    }
    
}
