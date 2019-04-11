package stringIndexof;

public abstract class Matcher
{
    public static boolean DEBUG = false;
    
    public abstract int indexOf (String text, int fromIdx);
    
    public static boolean compare (String text, int offset, String pattern, int pat_offset, int length)
    {
        return text.regionMatches (offset, pattern, pat_offset, length);
    }

    public static boolean compare (String text, int offset, String pattern, int length)
    {
        return text.regionMatches (offset, pattern, 0, length);
    }
}
