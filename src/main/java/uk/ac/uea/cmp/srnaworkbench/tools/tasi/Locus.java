/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * Describes a TAS gene locus on the genome.
 * @author Dan Mapleson
 */
public class Locus
{
  private String chromosome;
  private int start;
  private int end;
  private int nb_srnas;
  private Patman srnas;
  private int nb_phased_srnas;
  private Patman phased_srnas;
  private double p_value;
  private int phase_register;

  private Locus( String chromosome, int start, int end, int nb_srnas, int nb_phased_srnas, double p_value, int phase_register )
  {
    this.chromosome = chromosome;
    this.start = start;
    this.end = end;
    this.nb_srnas = nb_srnas;
    this.nb_phased_srnas = nb_phased_srnas;
    this.p_value = p_value;
    this.srnas = null;
    this.phased_srnas = null;
    this.phase_register = phase_register;
  }

  public Locus( String chromosome, int start, SequenceStrand strand, int nb_srnas, int nb_phased_srnas, double p_value, int phase_register )
  {
    int new_end = start + TasiParams.REGION_SIZE + phase_register - 1;
    int new_start = start;
    this.chromosome = chromosome;
    this.start = new_start;
    this.end = new_end;
    this.nb_srnas = nb_srnas;
    this.nb_phased_srnas = nb_phased_srnas;
    this.p_value = p_value;
    this.srnas = null;
    this.phased_srnas = null;
    this.phase_register = phase_register;
  }

  public String getChromosome()
  {
    return chromosome;
  }

  public int getEnd()
  {
    return end;
  }

  public int getNbPhasedSrnas()
  {
    return nb_phased_srnas;
  }

  public int getNbSrnas()
  {
    return nb_srnas;
  }

  public double getPValue()
  {
    return p_value;
  }

  public int getStart()
  {
    return start;
  }
  
  public Patman getAllSrnas()
  {
    return srnas;
  }

  public void setAllSrnas( Patman srnas )
  {
    this.srnas = srnas;
  }

  public Patman getPhasedSrnas()
  {
    return phased_srnas;
  }

  public void setPhasedSrnas( Patman phased_srnas )
  {
    this.phased_srnas = phased_srnas;
  }

  @Override
  public String toString()
  {
    return this.chromosome + "," + this.start + "," + this.end + "," + this.nb_srnas + "," + this.nb_phased_srnas + "," + this.p_value;
  }
  
  public VissrData buildVissrData()
  {
    Patman posPhasedItems = new Patman();
    List<GFFRecord> locusItems = CollectionUtils.newArrayList();
    Patman negPhasedItems = new Patman();
    Patman unphasedItems = new Patman();
    
    final byte PHASE = 0;
    
    // Move 1 based coords to 0 based.
    int plusStart = getStart() - 1;
    int plusEnd = getEnd() - 1;      
    GFFRecord plusTasGFF = new GFFRecord( getChromosome(), "tasi-tool", "Loci", plusStart, plusEnd, (float) getPValue(), '+', PHASE );
    plusTasGFF.addAttribute( "Distinct Unphased sRNAs", Integer.toString( getNbSrnas() ) );
    plusTasGFF.addAttribute( "Distinct Phased sRNAs", Integer.toString( getNbPhasedSrnas() ) );
    
    for(int i = plusStart + phase_register; i < plusEnd; i += phase_register )
    {
      plusTasGFF.addChild( new GFFRecord( getChromosome(), "tasi-tool", "Loci", i, i, 0.0f, '+', PHASE ) );
    }

    // Move 1 based coords to 0 based and move back two coords for neg strand.
    int minusStart = getStart() - TasiParams.NEG_STRAND_OFFSET - 1;
    int minusEnd = getEnd() - TasiParams.NEG_STRAND_OFFSET - 1;
    GFFRecord minusTasGFF = new GFFRecord( getChromosome(), "tasi-tool", "Loci", minusStart, minusEnd, (float) getPValue(), '-', PHASE );
    minusTasGFF.addAttribute( "Distinct Unphased sRNAs", Integer.toString( getNbSrnas() ) );
    minusTasGFF.addAttribute( "Distinct Phased sRNAs", Integer.toString( getNbPhasedSrnas() ) );
    for(int i = minusStart + phase_register; i < minusEnd; i += phase_register )
    {
      minusTasGFF.addChild( new GFFRecord( getChromosome(), "tasi-tool", "Loci", i, i, 0.0f, '-', PHASE ) );
    }
    
    locusItems.add( plusTasGFF );
    locusItems.add( minusTasGFF );
    Patman allSrnas = getAllSrnas();
    Patman phasedSrnas = getPhasedSrnas();
    // Iterate through phased items
    for ( PatmanEntry pe :  phasedSrnas)
    {
      if ( pe.getSequenceStrand() == SequenceStrand.POSITIVE )
      {
        posPhasedItems.add( pe );
      }
      else
      {
        if ( pe.getSequenceStrand() == SequenceStrand.NEGATIVE )
        {
          negPhasedItems.add( pe );
        }
        else
        {
          // Something went wrong here!  Unexpected position string.
        }
      }

    }
    //loop through all sRNAs to build unphased list
    for(PatmanEntry pe : allSrnas)
    {
      if(!phasedSrnas.contains( pe ) )
      {
        unphasedItems.add( pe );
      }
    }
    
    return new VissrData( locusItems, posPhasedItems, negPhasedItems, getAllSrnas(), unphasedItems );
  }


  
  public final class VissrData
  {
    private List<GFFRecord> locusItems = CollectionUtils.newArrayList();
    private Patman posPhasedItems = new Patman();
    private Patman negPhasedItems = new Patman();
    private Patman allItems = new Patman();
    private Patman unphasedItems = new Patman();
    
    public VissrData( List<GFFRecord> locusItems, 
                      Patman posPhasedItems, 
                      Patman negPhasedItems, 
                      Patman allItems,
                      Patman unphasedItems) 
    {
      this.locusItems = locusItems;
      this.posPhasedItems = posPhasedItems;
      this.negPhasedItems = negPhasedItems;
      this.allItems = allItems;
      this.unphasedItems = unphasedItems;
    }
    
    public Patman getPosPhasedItems()
    {
      return this.posPhasedItems;
    }

    public List<GFFRecord> getLocusItems()
    {
      return locusItems;
    }

    public Patman getNegPhasedItems()
    {
      return negPhasedItems;
    }

    public Patman getAllItems()
    {
      return allItems;
    }
    public Patman getUnphasedItems()
    {
      return unphasedItems;
    }
  }  
}
