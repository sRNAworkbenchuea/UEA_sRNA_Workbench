/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.Objects;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;

/**
 *
 * @author Josh
 */
public class SmallRNA extends Sequence {

    public static int similarTailRegionSize;
    int region1to7;
    int region7to13;
    int regionTail;
    byte tailRegionLength;
    boolean discard;
    double perfect_mfe;

    public SmallRNA(String seq) {

        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        
        if ((byte) (config.getMinSmallRNALenth() - 7 - 5) > 7) {
            tailRegionLength = 7;
        } else {
            tailRegionLength = (byte) (config.getMinSmallRNALenth() - 7 - 5);
        }

        sequence = super.toBits(seq);

        length = seq.length();
        //stringSeq = seq;

        region1to7 = (int) super.toBits(seq.substring(0, 7));
        region7to13 = (int) super.toBits(seq.substring(6, 13));
        regionTail = (int) super.toBits(seq.substring(12, tailRegionLength + 12));
        abundance = 1;

//        System.out.println("SmallRNA: " + seq);
//        System.out.println(new StringBuilder(longToString(region1to7, 7)).toString() + ":" + region1to7);
//        System.out.println(new StringBuilder(longToString(region7to13, 7)).toString() + ":" + region7to13);
//        System.out.println(new StringBuilder(longToString(regionTail, 7)).toString() + ":" + regionTail);
//        System.out.println("End SmallRNA");
    }

    @Override
    protected long setBit(int bit, long target, char c) {
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
                discard = true;
        }

        return target;
    }

    public double getPerfect_mfe() {
        return perfect_mfe;
    }

    public void setPerfect_mfe(double perfect_mfe) {
        this.perfect_mfe = perfect_mfe;
    }

    
    
    public static int getSimilarTailRegionSize() {
        return similarTailRegionSize;
    }

    public int getRegion1to7() {
        return region1to7;
    }

    public int getRegion7to13() {
        return region7to13;
    }

    public int getRegionTail() {
        return regionTail;
    }

    public byte getTailRegionLength() {
        return tailRegionLength;
    }

    private void setSimilarTailRegionSize(int i) {
        similarTailRegionSize = i;
    }

    public static String longToString(long l, int n) {

        char[] s = new char[n];

        int index = 0;
        for (int i = 0; i < n * 2; i += 2) {
            long mask = (1L << (i - i + 2)) - 1;
            s[index] = getChar((l >> i) & mask);
            index++;
        }

        return new String(s);

    }

    private static char getChar(long l) {

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

    @Override
    public int hashCode() {
        if (comment.split(">")[0].equals(toString())) {
            return super.hashCode();
        } else {
            return (toString() + comment).hashCode();
        }
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
        if (!Objects.equals(this.toString() + comment, other.toString() + comment)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return SmallRNA.longToString(sequence, length);
    }
}
