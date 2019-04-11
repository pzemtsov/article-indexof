package byteIndexof;

import java.util.Arrays;

public class ChainSuffixMatcher extends MatcherFactory
{
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
                    Node n = e.n.convert ();
                    n = n.addOne (boundary, e.b);
                    return n;
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

            Node addOne (boolean boundary, byte b)
            {
                return new SingleNode (boundary, b, this);
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
            Node addOne (boolean boundary, byte b)
            {
                return this.boundary ? super.addOne (boundary,  b) : new ChainNode (boundary, new byte [] {this.b, b}, node);
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
            private final byte [] chain;
            private final Node node;
            
            ChainNode (boolean boundary, byte [] chain, Node node)
            {
                super (boundary);
                this.chain = chain;
                this.node = node;
            }

            @Override
            Node addOne (boolean boundary, byte b)
            {
                if  (this.boundary) return super.addOne (boundary,  b);
                byte [] new_chain = Arrays.copyOf (chain,  chain.length + 1);
                new_chain [chain.length] = b;
                return new ChainNode (boundary, new_chain, node);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                if (boundary) shift = i;
                i -= chain.length;
                for (int j = 0; j < chain.length; j++) {
                    if (text [pos + i + j] != chain [j]) {
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
            long mem1 = 0, t1 = 0;
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
                    node = node.add (pattern [i]);
                }
                node.boundary = true;
            }

            if (DEBUG) {
                long t2 = System.currentTimeMillis ();
                System.gc ();
                long mem2 = Runtime.getRuntime ().totalMemory () - Runtime.getRuntime ().freeMemory ();
                System.out.println ("Root Memory: " + pattern_len + ": " + (mem2 - mem1));
                System.out.println ("Time building: " + pattern_len + ": " + (t2 - t1));
            }

            root = build_root.convert ();
            if (DEBUG) {
                long t3 = System.currentTimeMillis ();
                System.out.println ("Time converting: " + pattern_len + ": " + (t3 - t1));
            }
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
