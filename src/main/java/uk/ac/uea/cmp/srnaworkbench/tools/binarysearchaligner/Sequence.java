package uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner;


import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;
import java.util.Objects;
/**
 *
 * @author Josh
 */
public abstract class Sequence {

    protected long sequence;
    //protected String stringSeq = null;
    int length;

    protected abstract long setBit(int bit, long target, char c) throws SequenceContainsInvalidBaseException;

    public long getSequence() {
        return sequence;
    }

    

    protected long toBits(String s) throws SequenceContainsInvalidBaseException {
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

        if (end > length * 2) {
            end = length * 2;
        }

        long mask = (1L << (end - begin)) - 1;

        return (sequence >> begin) & mask;
    }
    
    protected static char getChar(long l) {

        if (l == 0) {
            return 'A';
        } else if (l == 1) {
            return 'C';
        } else if (l == 2) {
            return 'G';
        } else {
            return 'T';
        }
    }
//
//    protected long getWord(int size, int position) {
//        
//        if(position != 0)
//            position--;
//        
//        return extract(position * 2, position * 2 + size * 2);
//    }

     @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sequence other = (Sequence) obj;
        if (!Objects.equals(this.toString(), other.toString())) {
            return false;
        }
        return true;
    }

}
