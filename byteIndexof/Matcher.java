package byteIndexof;

public abstract class Matcher
{
    public static boolean DEBUG = false;
    
    public abstract int indexOf (byte [] text, int fromIdx);
    
    public static boolean compare (byte [] text, int offset, byte [] pattern, int pat_offset, int pattern_len)
    {
        for (int i = 0; i < pattern_len; i++) {
            if (text [offset + i] != pattern [pat_offset + i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean compare (byte [] text, int offset, byte [] pattern, int pattern_len)
    {
        for (int i = 0; i < pattern_len; i++) {
            if (text [offset + i] != pattern [i]) {
                return false;
            }
        }
        return true;
    }

    public static int neq_index (byte [] text, int offset, byte [] pattern, int pattern_len)
    {
        for (int i = 0; i < pattern_len; i++) {
            if (text [offset + i] != pattern [i]) {
                return i;
            }
        }
        return pattern_len;
    }
}
