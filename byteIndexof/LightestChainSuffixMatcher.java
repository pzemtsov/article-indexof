package byteIndexof;

public class LightestChainSuffixMatcher extends MatcherFactory
{
    private static final class MatcherImpl extends Matcher
    {
        private static EmptyNode terminal = new EmptyNode ();
        
        private static abstract class Node
        {
            boolean boundary;
            
            abstract Node add (byte [] pattern, int start, int len);
            abstract int match (byte [] text, int pos, int i, int shift);
            abstract void dump (String level);
        }
        
        private static class EmptyNode extends Node
        {
            EmptyNode ()
            {
                boundary = true;
            }
            
            @Override
            Node add (byte [] pattern, int start, int len)
            {
                Node n = new ChainNode (pattern, start, len, null);
                n.boundary = true;
                return n;
            }

            @Override
            int match (byte [] text, int pos, int i, int shift)
            {
                return i;
            }
            
            @Override
            public String toString ()
            {
                return "E";
            }
            
            @Override
            void dump (String level)
            {
                System.out.print ("E");
            }
        }
        
        private static final class ArrayNode extends Node
        {
            private final Node [] nodes = new Node [256];
            
            @Override
            Node add (byte [] pattern, int start, int len)
            {
                byte b = pattern [start +-- len];
                Node n = nodes [b & 0xFF];
                if (n != null) {
                    if (len == 0) {
                        n.boundary = true;
                    } else {
                        nodes [b & 0xFF] = n.add (pattern, start, len);
                    }
                } else {
                    nodes [b & 0x0FF] = len == 0 ? terminal : new ChainNode (pattern, start, len, null);
                }
                return this;
            }
            
            void put (byte b, Node node)
            {
                if (node == null) node = terminal;
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

            @Override
            public String toString ()
            {
                StringBuilder b = new StringBuilder (boundary ? "*[" : "[");
                String delim = "";
                for (int i = 0; i < 256; i++) {
                    if (nodes [i] != null) {
                        b.append (delim).append ('\'').append ((char) i).append ("':").append (nodes [i]);
                        delim = ", ";
                    }
                }
                b.append ("]");
                return b.toString ();
            }

            @Override
            void dump (String level)
            {
                System.out.println ();
                System.out.print (level);
                if (boundary) System.out.print ("*");
                System.out.println ("[");
                String new_level = level + "  ";
                for (int i = 0; i < 256; i++) {
                    if (nodes [i] != null) {
                        System.out.print (new_level);
                        System.out.print ("'" + (char) i + "': ");
                        nodes [i].dump (new_level);
                        System.out.println ();
                    }
                }
                System.out.print (level + "]");
            }
            
            @Override
            public boolean equals (Object obj)
            {
                if (! (obj instanceof ArrayNode)) return false;
                ArrayNode a = (ArrayNode) obj;
                if (boundary != a.boundary) return false;
                for (int i = 0; i < 256; i++) {
                    if ((nodes [i] == null) != (a.nodes[i] == null)) {
                        return false;
                    }
                    if (nodes [i] != null && ! nodes [i].equals (a.nodes [i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        private static final class ChainNode extends Node
        {
            private final byte [] pattern;
            private int start;
            private int len;
            private Node node;
            
            ChainNode (byte [] pattern, int start, int len, Node node)
            {
                this.pattern = pattern;
                this.start = start;
                this.len = len;
                this.node = node;
            }

            @Override
            Node add (byte [] pattern, int start, int len)
            {
                Node result = this;
                int end = start + len - 1;
                int this_end = this.start + this.len - 1;
                
                int min_len = Math.min (len,  this.len);
                int i;
                for (i = 0; i < min_len; i++) {
                    if (pattern [end - i] != pattern [this_end - i]) {
                        break;
                    }
                }
                if (i == this.len) {
                    if (this.len == len) {
                        if (node != null) {
                            node.boundary = true;
                        }
                    } else {
                        if (node != null) {
                            node = node.add (pattern, start, len - i);
                        } else {
                            node = new ChainNode (pattern, start, len - i, null);
                            node.boundary = true;
                        }
                    }
                } else if (i == len) {
                    node = new ChainNode (pattern, this.start, this.len - len, node);
                    node.boundary = true;
                    this.start = this.start + this.len - i;
                    this.len = i;
                } else {
                    byte chain_byte = pattern [this_end - i];
                    ArrayNode array = new ArrayNode ();
                    if (i == 0) {
                        array.boundary = boundary;
                        boundary = false;
                        -- this.len;
                        array.put (chain_byte, this.len == 0 ? node : this);
                        result = array;
                    } else if (i == this.len - 1) {
                        ++ this.start;
                        -- this.len;
                        array.put (chain_byte, node);
                        node = array;
                    } else {
                        Node new_chain = new ChainNode (pattern, this.start, this.len-i-1, node);
                        this.start = this.start + this.len - i;
                        this.len = i;
                        array.put (chain_byte, new_chain);
                        node = array;
                    }
                    array.add (pattern, start, len - i);
                }
                return result;
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
            
            @Override
            public String toString ()
            {
                return (boundary ? "*\"" : "\"") + new String (pattern, start, len) + "\"" + (node == null ? "" : "->" + node);
            }

            @Override
            void dump (String level)
            {
                if (boundary) System.out.print ("*");
                System.out.print ('"');
                System.out.print (new String (pattern, start, len));
                System.out.print ('"');
                if (node != null) {
                    System.out.print ("->");
                    node.dump (level + "  ");
                }
            }

            @Override
            public boolean equals (Object obj)
            {
                if (! (obj instanceof ChainNode)) return false;
                ChainNode c = (ChainNode) obj;
                if (len != c.len || boundary != c.boundary) {
                    return false;
                }
                if (start != c.start) {
                    if (! compare (pattern, start, pattern, c.start, c.len)) {
                        return false;
                    }
                }
                if ((node == null) != (c.node == null)) {
                    return false;
                }
                if (node != null && ! node.equals (c.node)) {
                    return false;
                }
                return true;
            }
        }

        private final byte [] pattern;
        private final Node root;
        
        public MatcherImpl (byte [] pattern)
        {
            this.pattern = pattern;
            this.root = build_root ();
        }
        
        private Node build_root ()
        {
            Node root = terminal;
            for (int prefix_len = pattern.length; prefix_len >= 1; prefix_len--) {
                root = root.add (pattern, 0,  prefix_len);
            }
            return root;
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
