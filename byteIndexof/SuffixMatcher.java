package byteIndexof;

public class SuffixMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    static long shift_cnt_sum = 0;
    
    private static final class MatcherImpl extends Matcher
    {
        private static final class Node
        {
            Node [] nodes = null;
            boolean boundary = false;
        }

        private final byte [] pattern;
        private final Node root;
        
        public MatcherImpl (byte [] pattern)
        {
            this.pattern = pattern;
            int pattern_len = pattern.length;
            root = new Node ();

            for (int prefix_len = 1; prefix_len <= pattern_len; prefix_len++) {
                Node node = root;
                for (int i = prefix_len - 1; i >= 0; --i) {
                    int b = pattern [i] & 0xFF;
                    if (node.nodes == null) {
                        node.nodes = new Node [256];
                    }
                    if (node.nodes [b] == null) {
                        node.nodes [b] = new Node ();
                    }
                    node = node.nodes [b];
                }
                node.boundary = true;
            }
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            int len = pattern.length;
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                Node node = root;
                int shift = len;
                int i;

                for (i = len - 1; ; --i) {
                    node = node.nodes [text [pos + i] & 0xFF];
                    if (node == null) {
                        break;
                    }
                    if (node.boundary) {
                        shift = i;
                        if (node.nodes == null) {
                            break;
                        }
                    }
                }
                if (shift == 0) return pos;
                
                if (DEBUG) {
                    shift_cnt_sum += len - i;
                    shift_count ++;
                    shift_sum += shift; 
                }
                pos += shift;
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
        if (shift_count == 0) {
            return "";
        }
        double avg = shift_sum * 1.0 / shift_count;
        double avg_cnt = shift_cnt_sum * 1.0 / shift_count;
        shift_count = shift_sum = shift_cnt_sum = 0;
        return String.format ("; avg shift = %5.2f; avg shift count = %5.2f", avg, avg_cnt);
    }
}
