package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.Objects;

/**
 *
 * @author Josh
 */
public abstract class Sequence {

    protected long sequence;
    //protected String stringSeq = null;
    protected String comment;
    protected int abundance;
    protected double normalisedAbundance;
    int length;

    public double getNormalisedAbundance() {
        return normalisedAbundance;
    }

    public void setNormalisedAbundance(double normalisedAbundance) {
        this.normalisedAbundance = normalisedAbundance;
    }

    public void incrementAbundance() {
        abundance++;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    protected abstract long setBit(int bit, long target, char c);

    public long getSequence() {
        return sequence;
    }

    protected long toBits(String s) {
        long l = 0L;

        int arrPos = 0;
        for (int i = 0; i < s.length() * 2; i += 2) {
            if (i == 0) {
                l = setBit((i), l, s.charAt(arrPos++));
            } else {
                l = setBit((i), l, s.charAt(arrPos++));

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

    protected long getWord(int size, int position) {

        if (position != 0) {
            position--;
        }

        return extract(position * 2, position * 2 + size * 2);
    }

    public String getStringSeq() {
        return toString();
    }

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

    public void setAbundance(int abundance) {
        this.abundance = abundance;
    }

    public int getAbundance() {
        return abundance;
    }

}
