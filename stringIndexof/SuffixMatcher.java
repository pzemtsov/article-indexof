package stringIndexof;

public class SuffixMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private static final class Node
        {
            Node [] nodes = null;
            boolean boundary = false;
        }

        private final String pattern;
        private final Node root;
        
        public MatcherImpl (String pattern)
        {
            this.pattern = pattern;
            int pattern_len = pattern.length ();
            root = new Node ();

            for (int prefix_len = 1; prefix_len <= pattern_len; prefix_len++) {
                Node node = root;
                for (int i = prefix_len - 1; i >= 0; --i) {
                    int b = pattern.charAt (i) & 0xFF;
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
        public int indexOf (String text, int fromIndex)
        {
            int len = pattern.length();
            int text_len = text.length ();
            
            for (int pos = fromIndex; pos < text_len - len + 1;) {
                Node node = root;
                int shift = len;
                int i;

                for (i = len - 1; ; --i) {
                    node = node.nodes [text.charAt (pos + i) & 0xFF];
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
