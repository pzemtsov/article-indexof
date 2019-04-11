package byteIndexof;

public class LightChainSuffixMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        static final EmptyNode terminal = new EmptyNode ();
        
        private final class BuildNode
        {
            boolean boundary = false;
            ByteMap<BuildNode> map = null;
            int position = 0;
            
            BuildNode add (int pos, byte b)
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
            
            void set_position ()
            {
                if (map != null) {
                    for (ByteMap.Entry<BuildNode> e : map) {
                        e.n.set_position ();
                        position = e.n.position + 1;
                    }
                }
            }
            
            Node convert ()
            {
                if (map == null) {
                    return terminal;
                }
                if (map.size () == 1) {
                    ByteMap.Entry<BuildNode> e = map.iterator ().next ();
                    Node n = e.n.convert ();
                    return n.addOne (pattern, position, boundary);
                }
                ArrayNode n = new ArrayNode (boundary);
                for (ByteMap.Entry<BuildNode> e : map) {
                    n.put (e.b, e.n.convert ());
                }
                return n;
            }
            
            @Override
            public String toString ()
            {
                return boundary + ": " + map;
            }
        }
        
        private static abstract class Node
        {
            public final boolean boundary;
            
            Node (boolean boundary) {
                this.boundary = boundary;
            }

            Node addOne (byte [] pattern, int position, boolean boundary)
            {
                return new SingleNode (boundary, pattern [position-1], this);
            }
            
            abstract int match (byte [] text, int pos, int i, int shift);
        }
        
        private static class EmptyNode extends Node
        {
            EmptyNode ()
            {
                super (true);
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                return i;
            }
        }

        private static final class SingleNode extends Node
        {
            private final byte b;
            private final Node node;
            
            SingleNode (boolean boundary, byte b, Node node)
            {
                super (boundary);
                this.b = b;
                this.node = node == terminal ? null : node;
            }

            @Override
            Node addOne (byte [] pattern, int position, boolean boundary)
            {
                return this.boundary ? super.addOne (pattern, position, boundary) : new ChainNode (pattern, position, 2, boundary, node);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                if (boundary) shift = i;
                -- i;
                if (text [pos + i] != b) {
                    return shift;
                }
                if (node == null) {
                    return i;
                }
                return node.match (text, pos, i, shift);
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
            int match (byte [] text, int pos, int i, int shift)
            {
                if (boundary) shift = i;
                -- i;
                Node n = nodes [text [pos + i] & 0xFF];
                if (n == null) {
                    return shift;
                }
                return n.match (text, pos, i, shift);
            }
        }
        
        private static final class ChainNode extends Node
        {
            private final byte [] pattern;
            private final int start;
            private final int len;
            private final Node node;
            
            ChainNode (byte [] pattern, int position, int len, boolean boundary, Node node)
            {
                super (boundary);
                this.pattern = pattern;
                this.start = position - len;
                this.len = len;
                this.node = node;
            }
            
            @Override
            Node addOne (byte [] pattern, int position, boolean boundary)
            {
                if  (this.boundary) return super.addOne (pattern, position, boundary);
                return new ChainNode (pattern, position, len + 1, boundary, node);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                if (boundary) shift = i;
                i -= len;
                for (int j = 0; j < len; j++) {
                    if (text [pos + i + j] != pattern [start + j]) {
                        return shift;
                    }
                }
                if (node == null) {
                    return i;
                }
                return shift = node.match (text, pos, i, shift);
            }
        }
        
        private final byte [] pattern;
        private final Node root;
        
        public MatcherImpl (byte [] pattern)
        {
            long t1 = 0;
            long mem1 = 0;
            if (DEBUG) {
                System.gc ();
                mem1 = Runtime.getRuntime ().totalMemory () - Runtime.getRuntime ().freeMemory ();
                t1 = System.currentTimeMillis ();
            }
            
            this.pattern = pattern;
            int pattern_len = pattern.length;
            BuildNode build_root = new BuildNode ();

            for (int prefix_len = 1; prefix_len <= pattern_len; prefix_len++) {
                BuildNode node = build_root;
                for (int i = prefix_len - 1; i >= 0; --i) {
                    node = node.add (i, pattern [i]);
                }
                node.boundary = true;
            }
            build_root.set_position ();
            if (DEBUG) {
                long t2 = System.currentTimeMillis ();
                System.gc ();
                long mem2 = Runtime.getRuntime ().totalMemory () - Runtime.getRuntime ().freeMemory ();
                System.out.println ("Root Memory: " + pattern_len + ": " + (mem2 - mem1));
                System.out.println ("Time building: " + pattern_len + ": " + (t2 - t1));
            }
            
            root = build_root.convert ();
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            int len = pattern.length;
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                int shift = root.match (text, pos, len, len);
                if (shift == 0) return pos;
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
        return "";
    }
}
