package uk.ac.uea.cmp.srnaworkbench.tools.shortreadaligner;

import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;

public class ShortRead extends Sequence implements Comparable<ShortRead> {

    int abundance;
    int numAlignments;

    public int getAbundance() {
        return abundance;
    }

    public void incrementAbundance() {
        abundance++;
    }

    public ShortRead(String s) throws SequenceContainsInvalidBaseException {
        s = s.toUpperCase();
        sequence = super.toBits(s);
        abundance = 1; 
        numAlignments = 0;
        stringSeq = s;

    }

    public ShortRead(String s, int abund) throws SequenceContainsInvalidBaseException {
        this(s);
        abundance = abund;

    }

    @Override
    protected long setBit(int bit, long target, char c) throws SequenceContainsInvalidBaseException {

        switch (c) {
            case 'A':
                target |= 0L << bit | 0L << bit + 1;
                break;
            case 'C':
                target |= 1L << bit | 0L << bit + 1;
                break;
            case 'T':
                target |= 1L << bit | 1L << bit + 1;
                break;
            case 'U':
                target |= 1L << bit | 1L << bit + 1;
                break;
            case 'G':
                target |= 0L << bit | 1L << bit + 1;
                break;
            default:
                throw new SequenceContainsInvalidBaseException();
        }

        return target;

    }

    @Override
    public int compareTo(ShortRead o) {
        return Long.compare(this.sequence, o.sequence);
    }

    @Override
    public String toString() {
//        char[] s = new char[arr[7]];
//
//        s[0] = getChar(extract(0, 2));
//
//        int index = 1;
//        for (int i = 2; i < arr[7] * 2; i += 2) {
//            s[index] = getChar(extract(i, i + 2));
//            index++;
//        }
//
//        String result = new String(s);
        return stringSeq;
    }

    public void incrementNumAlignments() {
        numAlignments++;
    }

    public void setAbundance(int abundance) {
        this.abundance = abundance;
    }
    
    
}
