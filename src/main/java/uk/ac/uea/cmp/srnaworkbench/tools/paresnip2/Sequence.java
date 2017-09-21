package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author Josh
 */
public abstract class Sequence {

    protected long sequence;
    protected String stringSeq = null;

    protected abstract long setBit(int bit, long target, char c);

    public long getSequence() {
        return sequence;
    }

    

    protected long toBits(String s) {
        long l = 0L;

        for (int i = 0; i < s.length() * 2; i += 2) {
            if (i == 0) {
                l = setBit((i), l, s.charAt(i));
            } else {
                l = setBit((i), l, s.charAt(i >> 1));
            }
        }
        return l;
    }
    
    protected long extract(int begin, int end) {

        if (end > stringSeq.length() * 2) {
            end = stringSeq.length() * 2;
        }

        long mask = (1L << (end - begin)) - 1;

        return (sequence >> begin) & mask;
    }

    protected long getWord(int size, int position) {
        
        if(position != 0)
            position--;
        
        return extract(position * 2, position * 2 + size * 2);
    }


}
