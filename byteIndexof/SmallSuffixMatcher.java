package byteIndexof;

public class SmallSuffixMatcher extends MatcherFactory
{
    static long shift_sum = 0;
    static long shift_count = 0;
    static long shift_cnt_sum = 0;

    private static final class MatcherImpl extends Matcher
    {
        static final EmptyNode terminal = new EmptyNode ();
    
        private static final class BuildNode
        {
            boolean boundary = false;
            ByteMap<BuildNode> map = null;
            
            BuildNode add (byte b)
            {
                if (map == null) {
                    map = new ByteMap<> ();
                }
                BuildNode n = map.get (b);
                if (n == null) {
                    n = new BuildNode ();
                    map.put (b, n);
                }
                return n;
            }
            
            Node convert ()
            {
                if (map == null) {
                    return terminal;
                }
                if (map.size () == 1) {
                    ByteMap.Entry<BuildNode> e = map.iterator ().next ();
                    return new SingleNode (boundary, e.b, e.n.convert ());
                }
                ArrayNode n = new ArrayNode (boundary);
                for (ByteMap.Entry<BuildNode> e : map) {
                    n.put (e.b, e.n.convert ());
                }
                return n;
            }
        }

        private static abstract class Node
        {
            public final boolean boundary;
            
            Node (boolean boundary) {
                this.boundary = boundary;
            }
            
            abstract Node get (byte b);

            boolean terminal () { return false;}
        }
        
        private static class EmptyNode extends Node
        {
            EmptyNode () {super (true);}
            @Override Node get (byte b)   {return null;}
            @Override boolean terminal () {return true;}
        }

        private static final class SingleNode extends Node
        {
            private final int b;
            private final Node node;
            
            SingleNode (boolean boundary, int b, Node node)
            {
                super (boundary);
                this.b = b;
                this.node = node;
            }
            
            @Override
            Node get (byte b)
            {
                return b == this.b ? node : null;
            }
        }
        
        private static final class ArrayNode extends Node
        {
            private final Node [] nodes = new Node [256];
            
            ArrayNode (boolean boundary)
            {
                super (boundary);
            }

            void put (byte b, Node node)
            {
                nodes [b & 0xFF] = node;
            }
            
            @Override
            Node get (byte b)
            {
                return nodes [b & 0xFF];
            }
        }

        private final byte [] pattern;
        private final Node root;
        
        public MatcherImpl (byte [] pattern)
        {
            this.pattern = pattern;
            int pattern_len = pattern.length;
            BuildNode buildRoot = new BuildNode ();

            for (int prefix_len = 1; prefix_len <= pattern_len; prefix_len++) {
                BuildNode node = buildRoot;
                for (int i = prefix_len - 1; i >= 0; --i) {
                    node = node.add (pattern [i]);
                }
                node.boundary = true;
            }
            root = buildRoot.convert ();
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
                    node = node.get (text [pos + i]);
                    if (node == null) {
                        break;
                    }
                    if (node.boundary) {
                        shift = i;
                        if (node.terminal ()) {
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
