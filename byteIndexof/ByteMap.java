package byteIndexof;

import java.util.Arrays;
import java.util.Iterator;

public class ByteMap<T> implements Iterable<ByteMap.Entry<T>>
{
    public static class Entry<T>
    {
        byte b;
        T n;
        
        Entry (byte b, T n)
        {
            this.b = b;
            this.n = n;
        }
    }
    
    private byte [] bytes = null;
    private int size = 0;
    private Object [] nodes = null;

    @SuppressWarnings ("unchecked")
    public T get (byte b)
    {
        if (bytes == null) return null;
        for (int i = 0; i < size; i++) {
            if (bytes [i] == b) {
                return (T) nodes [i];
            }
        }
        return null;
    }
    
    public void put (byte b, T n)
    {
        if (get (b) != null) return;
        if (bytes == null) {
            bytes = new byte [4];
            nodes = new Object [bytes.length];
        } else if (bytes.length == size) {
            bytes = Arrays.copyOf (bytes, size * 2);
            nodes = Arrays.copyOf (nodes, size * 2);
        }
        bytes [size] = b;
        nodes [size] = n;
        ++ size;
    }
    
    public int size ()
    {
        return size;
    }
    
    public Iterator<Entry<T>> iterator ()
    {
        return new Iterator<Entry<T>> () {
            
            int n = 0;

            public boolean hasNext ()
            {
                return n < size;
            }

            @SuppressWarnings ("unchecked")
            public Entry<T> next ()
            {
                ++ n;
                return new Entry<T> (bytes [n-1], (T)nodes [n-1]);
            }
        };
    }
    
    @Override
    public String toString ()
    {
        StringBuilder b = new StringBuilder ("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) b.append (';');
            b.append ((char) bytes [i]);
            b.append ("->");
            b.append (nodes [i]);
        }
        b.append (']');
        return b.toString ();
    }
}
