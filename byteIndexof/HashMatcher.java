package byteIndexof;
public class HashMatcher extends MatcherFactory
{
    static long compare_count = 0;
    static long count = 0;

    private static final class IndexOfImpl extends Matcher
    {
        private final byte [] pattern;
        private final int hash;
        
        public IndexOfImpl  (byte [] pattern)
        {
            this.pattern = pattern;
            hash = hashCode (pattern, 0, pattern.length);
        }

        private static int hashCode (byte[] array, int fromIdx, int length)
        {
            int hash = 0;
            for (int i = fromIdx; i < fromIdx + length; i++){
                hash += array[i];
            }
            
            return hash;
        }

        @Override
        public int indexOf (byte[] text, int fromIdx)
        {
            byte [] pattern = this.pattern;
            int pattern_len = pattern.length;
            int text_len = text.length;

            if (fromIdx + pattern_len > text_len) return -1;
            int pattern_hash = this.hash;
            
            int hash = hashCode (text, fromIdx, pattern_len);

            for (int i = fromIdx; ; i++) {
                if (DEBUG) ++ count;
                if (hash == pattern_hash){
                    if (DEBUG) ++ compare_count;
                    if (compare(text, i, pattern, pattern_len)) return i;
                }
                if (i + pattern_len >= text_len) return -1;
                hash -= text [i];
                hash += text [i + pattern_len];
            }
        }
    }

    @Override
    public Matcher createMatcher (byte[] pattern)
    {
        return new IndexOfImpl (pattern);
    }

    @Override
    public String stats ()
    {
        if (count == 0) {
            return "";
        }
        double compare_ratio = compare_count * 100.0 / count;
        return String.format ("; compare ratio = %5.2f %%", compare_ratio);
    }
}
