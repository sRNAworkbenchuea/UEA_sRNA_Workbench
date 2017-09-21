/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.*;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

/**
 *
 * @author w0445959
 */
public class GenerateSingleGFFMenu extends JMenuItem
{
    MouseEvent myMouseEvent;



    JTable myHairpinTable;
    Container MC_parent;
    File myGenomeFile = null;

    public GenerateSingleGFFMenu(String menuText)
    {
        super(menuText);
        this.setIcon( new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg")));
        this.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateGFFEvent(evt);
            }
        });
    }
     public void addTableRef(MouseEvent e, Container parent, File genomeFile)
    {

        myMouseEvent = e;
        MC_parent=  parent;
        myGenomeFile = genomeFile;
    }
    public void generateGFFEvent(java.awt.event.ActionEvent evt)
    {
         JTable source = (JTable) myMouseEvent.getSource();

         int row = source.rowAtPoint(myMouseEvent.getPoint());
        //int column = source.columnAtPoint(myMouseEvent.getPoint());
       // System.out.println("PARENT " + this.getParent().getParent().getClass());
        if (source.getModel().getRowCount() >= row)
        {
            /*create seq viz object
             * invoke loadSequenceFromFile with genome fasta
             * ionvoke display sequenceRanges(String label, Color labelColour, Map< String, List<? extends SequenceRange> > sequenceIdToSequenceRangesMap)
             * label is the label on the left of the frame (tier)
             * colour is the background colour or null for default
             * Map is a map key String = chromoID that I created the GFF record with. List object constructed with any GFF records to be displayed
             *  //chromo header from FASTA
  //source is "TAIR" usually we can set it to mircat
  //type is miRNA
  //start and end coordinates
  //score set to 0
  //phase set to 0
  //attribues map <HairpinSequence, DOTbracket> is an example
  public GFFRecord( String seqId, String source, String type, int startIndex, int endIndex,
    float score, char strand, byte phase, Map<String,String> attributesMap )
//             */
//            GFFRecord miRNA_record = generateRecord(source, row, 4, 5);
//            GFFRecord miRNA_STAR_record = generateRecord(source, row, 17, 18);
//            GFFRecord hairpinRecord = generateRecord(source, row, 10, 11);
//
            row = source.convertRowIndexToModel(row);

            String ID = source.getModel().getValueAt(row, 0).toString();
            int start = Integer.parseInt(source.getModel().getValueAt(row, 4).toString());
            int end = Integer.parseInt(source.getModel().getValueAt(row, 5).toString());
            char strand = source.getModel().getValueAt(row, 6).toString().charAt(0);

            String hairpinSequenceHTML = source.getModel().getValueAt(row, 3).toString();
            String hairpinDotBracketHTML = source.getModel().getValueAt(row, 12).toString();

            String hairpinSequence = hairpinSequenceHTML.replace("<HTML>", "").replace("</HTML>", "").replace("<u><font color=#0000FF>", "").replace("</u></font>", "").replace("<u><font color=#FF0000>", "").replace("<font color=#FFFFFF>", "").replace("</u>", "");
            String hairpinDotBracket = hairpinDotBracketHTML.replace("<HTML>", "").replace("</HTML>", "");
            //String finalHairpinDotBracket = (((((hairpinDotBracket.replace("-", ".")).replace("<", "(")).replace(">", ")")).replace("{", "(")).replace("}", ")")).replace("=", ".");

            GFFRecord record_miRNA = new GFFRecord(ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte)0);
            record_miRNA.addAttribute("hairpin_element", "miRNA");

            int hairpinStart = Integer.parseInt(source.getModel().getValueAt(row, 10).toString());
            int hairpinEnd = Integer.parseInt(source.getModel().getValueAt(row, 11).toString());

            GFFRecord recordHairpin = new GFFRecord(ID, "miRCat", "miRNA", hairpinStart, hairpinEnd, 0.0f, strand, (byte) 0);
            recordHairpin.addAttribute("HairpinSequence", hairpinSequence);
            recordHairpin.addAttribute("Marked_up_Dot-Bracket", hairpinDotBracket);

            ArrayList<SequenceRange> newList = new ArrayList<SequenceRange>();
            newList.add(recordHairpin);
            newList.add(record_miRNA);

            start = Integer.parseInt(source.getModel().getValueAt(row, 17).toString());
            end = Integer.parseInt(source.getModel().getValueAt(row, 18).toString());

            if(start >=0 && end > 0)
            {
                GFFRecord recordSTAR = new GFFRecord(ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte)0);
                recordSTAR.addAttribute("hairpin_element", "miRNA*");
                newList.add(recordSTAR);
            }

            TierParameters tp = new TierParameters.Builder( "miRCat" ).tierLabelBackgroundColour(Color.yellow).glyphBackgroundColour(Color.GREEN).build();
            tp.addListForId( ID, newList );
            

            SequenceVizMainFrame newSeqVissr = SequenceVizMainFrame.createVisSRInstance( myGenomeFile, false, tp );
            newSeqVissr.displaySequenceRegion( source.getModel().getValueAt(row, 0).toString(), hairpinStart-20, hairpinEnd+20 );
        }
    }
}
