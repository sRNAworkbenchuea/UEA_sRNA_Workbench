/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner;

/**
 *
 * @author rew13hpu
 */
public class CircularArray {

    int size;
    int longReadPositionPositive;
    int longReadPositionNegative;
    int currentStartPositive;
    int currentEndPositive;
    int currentStartNegative;
    int currentEndNegative;
    long currentLongPositive;
    long currentLongNegative;
    char[] positiveSeq;
    char[] negativeSeq;
    boolean setUpPositive;
    boolean setUpNegative;
    boolean containsInvalidBase;

    public int getLongReadPositionPositive() {
        return longReadPositionPositive;
    }

    public int getLongReadPositionNegative() {
        return longReadPositionNegative;
    }

    public int getSize() {
        return size;
    }

    public CircularArray(int size, int pos) {
        this.size = size;
        positiveSeq = new char[size];
        negativeSeq = new char[size];
        currentStartPositive = 0;
        currentEndPositive = 0;
        currentStartNegative = size - 1;
        currentEndNegative = size - 1;
        longReadPositionPositive = pos;
        longReadPositionNegative = pos;
        setUpPositive = false;
        containsInvalidBase = false;
    }

    public void add(char c) {

        if (currentEndPositive < size) {
            positiveSeq[currentEndPositive] = c;
            currentEndPositive++;
        } else {
            longReadPositionPositive++;
            positiveSeq[currentStartPositive] = c;
            currentStartPositive++;
            if (currentStartPositive == size) {
                currentStartPositive = 0;
            }
        }

        if (currentEndNegative >= 0) {
            negativeSeq[currentEndNegative] = reverseComplement(c);
            currentEndNegative--;
        } else {
            longReadPositionNegative++;
            negativeSeq[currentStartNegative] = reverseComplement(c);
            currentStartNegative--;

            if (currentStartNegative < 0) {
                currentStartNegative = size - 1;
            }
        }

        if (containsInvalidBase) {
            c = Character.toUpperCase(c);
            boolean valid = true;
            for (Character ch : positiveSeq) {
                if (ch != 'A' && ch != 'C' && ch != 'T' && ch != 'G' && ch != 'U') {
                    valid = false;
                }
            }

            if (c != 'A' && c != 'C' && c != 'T' && c != 'G' && c != 'U') {
                containsInvalidBase = true;
            } else {
                containsInvalidBase = !valid;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size * 2 + 2);
        sb.append(toStringPositive());
        return sb.toString();
    }

    public String toStringPositive() {
        StringBuilder sb = new StringBuilder(size);

        int overHang = Math.abs(0 - currentStartPositive);

        for (int i = currentStartPositive; i < size; i++) {
            sb.append(positiveSeq[i]);
        }

        for (int i = 0; i < overHang; i++) {
            sb.append(positiveSeq[i]);
        }

        return sb.toString();

    }

    public String toStringNegative() {
        StringBuilder sb = new StringBuilder(size);

        if (currentStartNegative == size - 1) {
            for (int i = currentStartNegative; i >= 0; i--) {
                sb.append(negativeSeq[i]);
            }
            return sb.toString();
        } else {
            int overHang = size - currentStartNegative - 1;

            for (int i = currentStartNegative; i >= 0; i--) {
                sb.append(negativeSeq[i]);
            }

            for (int i = size - 1; i >= size - overHang; i--) {
                sb.append(negativeSeq[i]);
            }
            return sb.toString();
        }
    }

    private static char reverseComplement(char c) {

        switch (c) {
            case 'A':
                return 'T';
            case 'T':
                return 'A';
            case 'C':
                return 'G';
            case 'G':
                return 'C';
            case 'U':
                return 'A';
            default:
                return Character.toUpperCase(c);
        }

    }

    public long toLong() {

        long l = 0L;
        if (!setUpPositive) {
            int arrPos = 0;
            for (int i = 0; i < size * 2; i += 2) {
                l = setBit((i), l, positiveSeq[arrPos++]);
            }
            setUpPositive = true;
            currentLongPositive = l;
            if (containsInvalidBase) {
                return -1;
            }
            return l;
        }

        if (currentStartPositive == 0) {
            currentLongPositive = currentLongPositive >> 2;
            currentLongPositive = setBit((size * 2) - 2, currentLongPositive, positiveSeq[size - 1]);
            if (containsInvalidBase) {
                return -1;
            }
            return currentLongPositive;
        } else {

            currentLongPositive = currentLongPositive >> 2;
            currentLongPositive = setBit((size * 2) - 2, currentLongPositive, positiveSeq[currentStartPositive - 1]);
            if (containsInvalidBase) {
                return -1;
            }
            return currentLongPositive;
        }

    }

    public long toLongReverseComplement() {
        long l = 0L;
        if (!setUpNegative) {
            int arrPos = 0;
            for (int i = 0; i < size * 2; i += 2) {
                l = setBit((i), l, negativeSeq[arrPos++]);
            }
            currentLongNegative = l;
            setUpNegative = true;
            return l;

        }
        if (currentStartNegative == size - 1) {
            currentLongNegative = currentLongNegative << 2;
            currentLongNegative &= ~(1L << (size * 2));
            currentLongNegative &= ~(1L << (size * 2 + 1));
            currentLongNegative = setBit(0, currentLongNegative, negativeSeq[0]);
            return currentLongNegative;
        } else {
            currentLongNegative = currentLongNegative << 2;
            currentLongNegative &= ~(1L << (size * 2));
            currentLongNegative &= ~(1L << (size * 2 + 1));
            currentLongNegative = setBit(0, currentLongNegative, negativeSeq[currentStartNegative + 1]);

//            System.out.println(SmallRNA.longToString(currentLongNegative, size));
//            System.out.println(toStringNegative());
//            System.out.println(longReadPositionNegative);
//            System.out.println(Long.toBinaryString(currentLongNegative));
//            System.out.println("");
            return currentLongNegative;
        }
    }

    private long setBit(int bit, long target, char c) {
        c = Character.toUpperCase(c);

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
                containsInvalidBase = true;
                //Just put an A there as it will be discarded anyway
                target |= 0L << bit | 0L << bit + 1;
        }

        return target;

    }
}
