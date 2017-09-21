/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.rnaannotation;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author w0445959
 */
public class RNA_AnnotationGeneration
{
  public RNA_AnnotationGeneration()
  {
  }

  public void generateImage( String hairpinSequence, String hairpinDotBracket, ArrayList<int[]> miRNA_Index, Color colors[] ) throws IOException
  {

    /*
     * miRNA
    1 cmark 106 126 8 1 0.156862745098039 0.156862745098039 omark
    
    miRNA and miRNA*
    1 cmark 106 126 8 1 0.156862745098039 0.156862745098039 omark 28 48 8 1 0 1 omark
    
    ATGAGGGTTAAGCTATTTCAGTTGAGGGGAATGTTGTCTGGATCGAGGATATTATAGATATATACATGTGTATGTTAATGATTCAAGTGATCATAGAGAGTATCCTCGGACCAGGCTTCATCCCCCCCAACATGTTATTGCCTCTGATCACCAT
    TCGGACCAGGCTTCATCCCCC
    GGAATGTTGTCTGGATCGAGGATATT
     *
     * ATGAGGGTTAAGCTATTTCAGTTGAGGGGAATGTTGTCTGGATCGAGGATATTATAGATATATACATGTGTATGTTAATGATTCAAGTGATCATAGAGAGTATCCTCGGACCAGGCTTCATCCCCCCCAACATGTTATTGCCTCTGATCACCAT
    (((..(((((.((.((..((((((.((((.(((..((((((.(((((((((((...((((((((....)))))))).((((((.....))))))....))))))))))).))))))..))).)))).)))).))..)).))...)))))..)))
    
    
    RNAplot --pre "1 cmark 106 126 8 1 0.156862745098039 0.156862745098039 omark 28 53 8 1 0 1 omark " < LOCATION
    
     * C:\Users\w0445959\LocalDisk\PostDoc\NetBeansProjects\NewWorkbench\ExeFiles\win>gn64c.exe -q -dBATCH -dNOPAUSE -sDEVICE=png16m -r300 -sOutputFile=temp.png LOCATION/rna.ps
     */
    //System.out.println("miRNA: " + miRNA_Index[0] + " " + miRNA_Index[1] + " miRNASTAR" + miRNA_STAR_Index[0] + " " + miRNA_STAR_Index[1]);
    String instruction;
    if ( !miRNA_Index.isEmpty() )
    {

      instruction = "\" 1 cmark ";
      int[] index;
      for ( int i = 0; i < miRNA_Index.size(); i++ )
      {
        Color currentColor = colors[i];
        index = miRNA_Index.get( i );
        float red = ((float)currentColor.getRed())/255.0f;
        float green = ((float)currentColor.getGreen())/255.0f;
        float blue = ((float)currentColor.getBlue())/255.0f;
        if(index[0] <= 1)
          index[0] = 1;
        if(index[1] >= hairpinSequence.length())
          index[1] = hairpinSequence.length();
        instruction += ( index[0]) + " " + ( index[1] ) + " 8 " + red + " " +  
          green + " " +
          blue + " " +
          " omark ";
      }
      instruction += "\"" + Tools.TAB + ">HAIRPIN" + Tools.TAB + hairpinSequence + Tools.TAB + hairpinDotBracket;

    }
    else
    {

      instruction = "\" 1 cmark \"" + Tools.TAB + ">HAIRPIN"
        + Tools.TAB + hairpinSequence + Tools.TAB + hairpinDotBracket;

      //System.out.println( "instruction: " + instruction );
    }

    BinaryExecutor exe = AppUtils.INSTANCE.getBinaryExecutor();
    String plotResult = exe.execRNAPlot( instruction );
    //System.out.println( "RNAplot result: " + plotResult );
//        instruction = "-q -dBATCH -dNOPAUSE -sDEVICE=png16m -r300 -sOutputFile=" + Tools.RNA_Annotation_dataPath + Tools.FILE_SEPARATOR + "HAIRPIN.png " +
//                Tools.curDir + Tools.FILE_SEPARATOR + "HAIRPIN_ss.ps";
//        System.out.println("GSINST: " + instruction);
    //-g1240x1754 -dEPSFitPage 1240 1754
    String GSinstruction = "-q  -dEPSFitPage -dBATCH -dNOPAUSE -sDEVICE=jpeg -r300x300 -sOutputFile=" + Tools.RNA_Annotation_dataPath + DIR_SEPARATOR + "HAIRPIN.jpg HAIRPIN_ss.ps";
    //instruction = "-q -dBATCH -dNOPAUSE -sDEVICE=png16m -r300 -sOutputFile=HAIRPIN.png HAIRPIN_ss.ps";

    String GSresult = exe.execGhostScript( GSinstruction );

    
    
  }
}
