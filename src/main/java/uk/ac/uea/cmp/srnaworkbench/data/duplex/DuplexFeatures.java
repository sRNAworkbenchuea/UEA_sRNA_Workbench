/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

/**
 *
 * @author ezb11yfu
 */
public class DuplexFeatures
{
    public enum MirnaClass 
    {
        IS_MIRNA(1), NOT_MIRNA(-1), UNKNOWN(0);
        
        private int val;
        
        private MirnaClass(int val)
        {
            this.val = val;
        }
        
        public int getVal()
        {
            return this.val;
        }
        
        public void setVal(int val)
        {
            this.val = val;
        }
    }
    
    private LengthFeatures lf;
    private AlignmentFeatures af;
    private NucleotideCompositionFeatures ncf;
        
    // Class
    private MirnaClass is_mirna;
    
    
    public DuplexFeatures()
    {
        this(null, null, null, MirnaClass.UNKNOWN);
        
        setLengthFeatures(new LengthFeatures());
        setNucleotideCompositionFeatures(new NucleotideCompositionFeatures());
        setAlignmentFeatures(new AlignmentFeatures());
    }
    
    public DuplexFeatures(LengthFeatures lf, NucleotideCompositionFeatures ncf, AlignmentFeatures af, MirnaClass is_mirna)
    {
        this.lf = lf;
        this.af = af;
        this.ncf = ncf;
        this.is_mirna = is_mirna;
    }
    
    public final void setLengthFeatures(LengthFeatures lf)
    {
        this.lf = lf;
    }
    
    public final void setAlignmentFeatures(AlignmentFeatures af)
    {
        this.af = af;
    }
    
    public final void setNucleotideCompositionFeatures(NucleotideCompositionFeatures ncf)
    {
        this.ncf = ncf;
    }
    
    public LengthFeatures getLengthFeatures()
    {
        return this.lf;
    }
    
    public NucleotideCompositionFeatures getNucleotideCompositionFeatures()
    {
        return this.ncf;
    }
    
    public AlignmentFeatures getAlignmentFeatures()
    {
        return this.af;
    }
    
    public MirnaClass isMirna()
    {
        return this.is_mirna;
    }
}
