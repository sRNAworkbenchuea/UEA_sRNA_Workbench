package uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner;


import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;

public class ShortRead extends Sequence implements Comparable<ShortRead>{

    int abundance;
    double weightedAbundancePositive;
    int numAlignmentsPositive;
    int numAlignmentsNegative;
    double adjustedWeightedAbundance;

    public int getAbundance() {
        return abundance;
    }

    public void incrementAbundance() {
        abundance++;
    }
    
    public double getWeightedPositiveAbundance()
    {
        return weightedAbundancePositive;
    }
    
    public void calculateWeightedPositiveAbundance()
    {
        weightedAbundancePositive = (double)abundance/(double)numAlignmentsPositive;
        adjustedWeightedAbundance = weightedAbundancePositive;
    }
    
    public int getNumAlignments()
    {
        return numAlignmentsPositive;
    }

    public ShortRead(String s) throws SequenceContainsInvalidBaseException {
        s = s.toUpperCase();
        sequence = super.toBits(s);
        abundance = 1; 
        numAlignmentsPositive = 0;
        length = s.length();
        weightedAbundancePositive = 0;
        adjustedWeightedAbundance = 0;
//        System.out.println(s);
//        System.out.println(toString());

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
    public String toString() {
        char[] s = new char[length];

        s[0] = getChar(extract(0, 2));

        int index = 1;
        for (int i = 2; i < length * 2; i += 2) {
            s[index] = getChar(extract(i, i + 2));
            index++;
        }

        String result = new String(s);
        return result;
    }

    public void incrementNumAlignmentsPositive() {
        numAlignmentsPositive++;
    }
    
    public void incrementNumAlignmentsNegative() {
        numAlignmentsNegative++;
    }

    public void setAbundance(int abundance) {
        this.abundance = abundance;
    }

    @Override
    public int compareTo(ShortRead o) {
        
        //THESE NEED TO BE SORTED BASED ON THEIR SEQUENCE VALUE!!!!!!!
        //IF NOT THEY WONT BE IN ASCENEDING ORDER
        
        return Long.compare(this.sequence, o.sequence);

    }

    public void setNumAlignmentsPositive(int numAlignmentsPositive) {
        this.numAlignmentsPositive = numAlignmentsPositive;
    }

    public void setNumAlignmentsNegative(int numAlignmentsNegative) {
        this.numAlignmentsNegative = numAlignmentsNegative;
    }

    public void setWeightedAbundancePositive(double weightedAbundancePositive) {
        this.weightedAbundancePositive = weightedAbundancePositive;
    }

    public double getAdjustedWeightedAbundance() {
        return adjustedWeightedAbundance;
    }

    public void setAdjustedWeightedAbundance(double adjustedWeightedAbundance) {
        this.adjustedWeightedAbundance = adjustedWeightedAbundance;
    }
    
    
    
    
    
    
    
}
