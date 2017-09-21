/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

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
    int abundance;

    public SmallRNA(String seq) {

        if ((byte) (Engine.MIN_SRNA_SIZE - 7 - 5) > 7) {
            tailRegionLength = 7;
        } else {
            tailRegionLength = (byte) (Engine.MIN_SRNA_SIZE - 7 - 5);
        }

        sequence = super.toBits(seq);
        stringSeq = seq;

        region1to7 = (int) super.toBits(stringSeq.substring(0, 7));
        region7to13 = (int) super.toBits(stringSeq.substring(6, 13));
        regionTail = (int) super.toBits(stringSeq.substring(12, tailRegionLength+12));
        
//        System.out.println("SmallRNA: " + stringSeq);
//        System.out.println(new StringBuilder(longToString(region1to7, 7)).toString() + ":" + region1to7);
//        System.out.println(new StringBuilder(longToString(region7to13, 7)).toString() + ":" + region7to13);
//        System.out.println(new StringBuilder(longToString(regionTail, 7)).toString() + ":" + regionTail);
//        System.out.println("End SmallRNA");

    }

    @Override
    protected long setBit(int bit, long target, char c) {
        switch (c) {
            case 'A':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'C':
                target |= (byte) 1 << bit | (byte) 0 << bit + 1;
                break;
            case 'T':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'U':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'G':
                target |= (byte) 0 << bit | (byte) 1 << bit + 1;
                break;
            default:
                discard = true;
        }

        return target;
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
}
