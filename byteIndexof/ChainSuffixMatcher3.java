package byteIndexof;

import java.util.Arrays;

public class ChainSuffixMatcher3 extends MatcherFactory
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
                    Node node = e.n.convert ();
                    node = node.addOne (boundary, e.b);
                    return node;
                }
                ArrayNode n = new ArrayNode (boundary);
                for (ByteMap.Entry<BuildNode> e : map) {
                    Node node = e.n.convert ();
                    n.put (e.b, node);
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
            Node addOne (boolean boundary, byte b)
            {
                return boundary ? addBoundaryOne (b) : addOne (b);
            }

            Node addOne (byte b)
            {
                return new SingleNode (b, this);
            }

            Node addBoundaryOne (byte b)
            {
                return new BoundarySingleNode (b, this);
            }
            
            abstract int match (byte [] text, int pos, int i, int shift);
        }
        
        private static class EmptyNode extends Node
        {
            @Override
            Node addOne (byte b)
            {
                return new LastSingleNode (b);
            }

            @Override
            Node addBoundaryOne (byte b)
            {
                return new LastBoundarySingleNode (b);
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
            
            SingleNode (byte b, Node node)
            {
                this.b = b;
                this.node = node == terminal ? null : node;
            }

            @Override
            Node addOne (byte b)
            {
                return new ChainNode (new byte [] {this.b, b}, node);
            }

            @Override
            Node addBoundaryOne (byte b)
            {
                return new BoundaryChainNode (new byte [] {this.b, b}, node);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                -- i;
                if (text [pos + i] != b) {
                    return shift;
                }
                return node.match (text, pos, i, shift);
            }
        }

        private static final class BoundarySingleNode extends Node
        {
            private final byte b;
            private final Node node;
            
            BoundarySingleNode (byte b, Node node)
            {
                this.b = b;
                this.node = node == terminal ? null : node;
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                shift = i;
                -- i;
                if (text [pos + i] != b) {
                    return shift;
                }
                return node.match (text, pos, i, shift);
            }
        }

        private static final class LastSingleNode extends Node
        {
            private final byte b;
            
            LastSingleNode (byte b)
            {
                this.b = b;
            }

            @Override
            Node addOne (byte b)
            {
                return new LastChainNode (new byte [] {this.b, b});
            }

            @Override
            Node addBoundaryOne (byte b)
            {
                return new LastBoundaryChainNode (new byte [] {this.b, b});
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                -- i;
                if (text [pos + i] != b) {
                    return shift;
                }
                return i;
            }
        }

        private static final class LastBoundarySingleNode extends Node
        {
            private final byte b;
            
            LastBoundarySingleNode (byte b)
            {
                this.b = b;
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                shift = i;
                -- i;
                if (text [pos + i] != b) {
                    return shift;
                }
                return i;
            }
        }
        
        private static final class ArrayNode extends Node
        {
            private final Node [] nodes = new Node [256];
            private final boolean boundary;
            
            ArrayNode (boolean boundary)
            {
                this.boundary = boundary;
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
            
            ChainNode (byte [] chain, Node node)
            {
                this.chain = chain;
                this.node = node;
            }

            @Override
            Node addOne (byte b)
            {
                byte [] new_chain = Arrays.copyOf (chain,  chain.length + 1);
                new_chain [chain.length] = b;
                return new ChainNode (new_chain, node);
            }

            @Override
            Node addBoundaryOne (byte b)
            {
                byte [] new_chain = Arrays.copyOf (chain,  chain.length + 1);
                new_chain [chain.length] = b;
                return new BoundaryChainNode (new_chain, node);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                i -= chain.length;
                for (int j = 0; j < chain.length; j++) {
                    if (text [pos + i + j] != chain [j]) {
                        return shift;
                    }
                }
                return node.match (text, pos, i, shift);
            }
        }

        private static final class BoundaryChainNode extends Node
        {
            private final byte [] chain;
            private final Node node;
            
            BoundaryChainNode (byte [] chain, Node node)
            {
                this.chain = chain;
                this.node = node;
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                shift = i;
                i -= chain.length;
                for (int j = 0; j < chain.length; j++) {
                    if (text [pos + i + j] != chain [j]) {
                        return shift;
                    }
                }
                return node.match (text, pos, i, shift);
            }
        }

        private static final class LastChainNode extends Node
        {
            private final byte [] chain;
            
            LastChainNode (byte [] chain)
            {
                this.chain = chain;
            }

            @Override
            Node addOne (byte b)
            {
                byte [] new_chain = Arrays.copyOf (chain,  chain.length + 1);
                new_chain [chain.length] = b;
                return new LastChainNode (new_chain);
            }

            @Override
            Node addBoundaryOne (byte b)
            {
                byte [] new_chain = Arrays.copyOf (chain,  chain.length + 1);
                new_chain [chain.length] = b;
                return new LastBoundaryChainNode (new_chain);
            }
            
            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                i -= chain.length;
                for (int j = 0; j < chain.length; j++) {
                    if (text [pos + i + j] != chain [j]) {
                        return shift;
                    }
                }
                return i;
            }
        }

        private static final class LastBoundaryChainNode extends Node
        {
            private final byte [] chain;
            
            LastBoundaryChainNode (byte [] chain)
            {
                this.chain = chain;
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                shift = i;
                i -= chain.length;
                for (int j = 0; j < chain.length; j++) {
                    if (text [pos + i + j] != chain [j]) {
                        return shift;
                    }
                }
                return i;
            }
        }
        
        private final byte [] pattern;
        private final Node root;
        
        public MatcherImpl (byte [] pattern)
        {
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
            root = build_root.convert ();
        }
        
        @Override
        public int indexOf (byte[] text, int fromIndex)
        {
            int len = pattern.length;
            
            for (int pos = fromIndex; pos < text.length - len + 1;) {
                int shift = root.match (text, pos, len, len);
                if (shift == 0) {
                    return pos;
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
        return "";
    }
}
