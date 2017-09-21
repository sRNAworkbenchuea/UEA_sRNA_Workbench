/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.normalise;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;

/**
 *
 * @author w0445959
 */
public enum NormalisationType
{
  NONE("Raw Abundance", "RAW"),
  TOTAL_COUNT("per total", "TC"),
  UPPER_QUARTILE("upper quartile", "UQ"),
  TRIMMED_MEAN("trimmed mean", "TMM"),
  QUANTILE("quantile", "Q"),
  BOOTSTRAP("bootstrap", "B"),
  DESEQ("deseq", "DE");
  
  private String full_name;
  private String abbreviation;
  NormalisationType(String full, String abbrev)
  {
      this.full_name = full;
      this.abbreviation = abbrev;
  }
  
  public String getAbbrev()
  {
      return this.abbreviation;
  }
  
  public String getFullName()
  {
      return this.full_name;
  }
  
  /**
   * Incase we need to extract results of a query
   * that did not return an entity, we can use this method
   * to cast the unknown return type to the correct return type
   * @param o the result found after querying with this NormalisationType
   * @return the result abundance as a double
   */
  public double resolveDatabaseAbundance(Object o)
  {
      if(this.equals(NormalisationType.NONE))
      {
          return ((Integer) o).doubleValue();
      }
      else
      {
          return (double) o;
      }
  }

  /**
   * This method returns the integer that gets stored in the database when storing as an enum type
   * @return 
   */
    public int getDatabaseReference()
    {
        switch (this)
        {
            case NONE:
                return 0;

            case TOTAL_COUNT:
                return 1;

            case UPPER_QUARTILE:
                return 2;

            case TRIMMED_MEAN:
                return 3;

            case QUANTILE:
                return 4;

            case BOOTSTRAP:
                return 5;

            case DESEQ:
                return 6;

            default:
                throw new IllegalArgumentException("No such normalisation"
                        + "implemented to for the right column in Sequence_Entity?");

        }
    }
    
      /**
   * This method returns the Abbreviation code that gets stored in the database when storing as an numeric type
   * @return 
   */
    public String codeToAbbrev(int code)
    {
        switch (code)
        {
            
            case 0:
                return "RAW";

            case 1:
                return "TC";

            case 2:
                return "UQ";

            case 3:
                return "TMM";

            case 4:
                return "Q";

            case 5:
                return "B";

            case 6:
                return "DE";

            default:
                throw new IllegalArgumentException("No such normalisation"
                        + "implemented to for the right column in Sequence_Entity?");

        }
    }
  
//  public double getDatabaseAbundance(Sequence_Entity se)
//  {
//      switch(this)
//      {
//          case NONE:
//              return se.getAbundance();
//              
//          case TOTAL_COUNT:
//              return se.getPER_TOTAL();
//              
//          case UPPER_QUARTILE:
//              return se.getUPPER_QUARTILE();
//              
//          case TRIMMED_MEAN:
//              return se.getTRIMMED_MEAN();
//          
//          case QUANTILE:
//              return se.getQUANTILE();
//              
//          case BOOTSTRAP:
//              return se.getBOOTSTRAP();
//              
//          case DESEQ:
//              return se.getDESEQ();
//          
//          default:
//              throw new IllegalArgumentException("No such normalisation"
//                      + "implemented to for the right column in Sequence_Entity?");
//              
//      }    
//  }
}
