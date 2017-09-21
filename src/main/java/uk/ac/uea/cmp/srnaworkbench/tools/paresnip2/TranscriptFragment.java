package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author Josh
 */
public class TranscriptFragment extends Sequence {

    int cleavagePosition;
    String geneName;
    int missingNuleotides;
    int category;
    int abundance;
    int weightedAbundance;
    int normWeightedAbundance;
    boolean invalidBaseInRegion1To7;
    boolean invalidBaseInRegion7To13;
    boolean invalidBaseInRegionTail;
    int regionTarget1to7;
    int regionTarget7to13;
    int regionTargetTail;

    public int getMissingNuleotides() {
        return missingNuleotides;
    }

    public void setMissingNuleotides(int missingNuleotides) {
        this.missingNuleotides = missingNuleotides;
    }

    public boolean isInvalidBaseInRegion1To7() {
        return invalidBaseInRegion1To7;
    }

    public void setInvalidBaseInRegion1To7(boolean invalidBaseInRegion1To7) {
        this.invalidBaseInRegion1To7 = invalidBaseInRegion1To7;
    }

    public boolean isInvalidBaseInRegion7To13() {
        return invalidBaseInRegion7To13;
    }

    public void setInvalidBaseInRegion7To13(boolean invalidBaseInRegion7To13) {
        this.invalidBaseInRegion7To13 = invalidBaseInRegion7To13;
    }

    public boolean isInvalidBaseInRegionTail() {
        return invalidBaseInRegionTail;
    }

    public void setInvalidBaseInRegionTail(boolean invalidBaseInRegionTail) {
        this.invalidBaseInRegionTail = invalidBaseInRegionTail;
    }

    public String getGeneName() {
        return geneName;
    }

    public int getCategory() {
        return category;
    }

    public int getAbundance() {
        return abundance;
    }

    public int getWeightedAbundance() {
        return weightedAbundance;
    }

    public int getNormWeightedAbundance() {
        return normWeightedAbundance;
    }

    public TranscriptFragment(String seq, String gName) {

        cleavagePosition = 16;
        geneName = gName;
        stringSeq = new StringBuilder(seq).reverse().toString();
        sequence = super.toBits(stringSeq);
        missingNuleotides = 0;
        
        regionTarget1to7 = (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));
        regionTarget7to13 = (int) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        regionTargetTail = (int) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));
        
    }

    public TranscriptFragment(String seq, String gName, int cleavePos) {

        cleavagePosition = cleavePos;
        geneName = gName;
        stringSeq = new StringBuilder(seq).reverse().toString();
        sequence = super.toBits(stringSeq);
        missingNuleotides = 0;
        
        regionTarget1to7 = (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));
        regionTarget7to13 = (int) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        regionTargetTail = (int) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));
    }

    public TranscriptFragment(String seq, String gName, int missingNuleotides, int cleavePos) {

        cleavagePosition = cleavePos;
        geneName = gName;
        stringSeq = new StringBuilder(seq).reverse().toString();
        sequence = super.toBits(stringSeq);
        this.missingNuleotides = missingNuleotides;
        
        regionTarget1to7 = (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));
        regionTarget7to13 = (int) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        regionTargetTail = (int) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));
    }

    @Override
    protected long setBit(int bit, long target, char c) {
        switch (c) {
            case 'T':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'G':
                target |= (byte) 1 << bit | (byte) 0 << bit + 1;
                break;
            case 'A':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'U':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'C':
                target |= (byte) 0 << bit | (byte) 1 << bit + 1;
                break;
            default: {
                //unknown base so can't encode it

                int missingNucleotideBits = missingNuleotides + missingNuleotides;
                //Set flags so that we can deal with it later
                if (bit < (26 - missingNucleotideBits) && bit > 10 - missingNucleotideBits) {
                    invalidBaseInRegion1To7 = true;
                } else if (bit < 40 - missingNucleotideBits) {
                    invalidBaseInRegion7To13 = true;
                } else if (bit < 40 + (SmallRNA.getSimilarTailRegionSize() * 2) - missingNucleotideBits){
                    invalidBaseInRegionTail = true;
                }

                //just set it to an A and we'll deal with it later
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
            }
        }

        return target;
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
            return 'T';
        } else if (l == 1) {
            return 'G';
        } else if (l == 2) {
            return 'C';
        } else {
            return 'A';
        }
    }

    @Override
    public String toString() {

        return stringSeq;
    }

    public int getCleavagePosition() {
        return cleavagePosition;
    }

    //TEST THESE!!!!
    public int getTargetRegion1to7() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));

        //   return (int) getWord(7, cleavagePosition - 8);
        
        return regionTarget1to7;
    }

    public int getTargetRegion7to13() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        return regionTarget7to13;
        //  return (int) getWord(7, cleavagePosition - 2);
    }

    public int getTargetTailRegion()  {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));
        return regionTargetTail;
    }

    public int getTargetTailRegionSize() {

        //DONT ACTUALLY THINK THIS IS CORRECT
        int size = Engine.MIN_SRNA_SIZE;

        if (size >= 19) {
            return 7 - missingNuleotides;
        } else {
            size = size - 7 - 5;
            return size - missingNuleotides;
        }

    }

}
