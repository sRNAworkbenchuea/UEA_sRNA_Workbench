/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.rnaannotation.RNAannotationMainFrame;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author Matt
 */
public class RenderHairpinMenu extends JMenuItem
{
  MouseEvent myMouseEvent;
  Container MC_parent;

  public RenderHairpinMenu( String menuText )
  {
    super( menuText );
    this.setIcon( new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/newHAIRPIN.jpg")));
    this.addActionListener( new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed( java.awt.event.ActionEvent evt )
      {
        renderHairpinEvent( evt );
      }
    } );
  }

  public void addTableRef( MouseEvent e, Container parent )
  {

    myMouseEvent = e;
    MC_parent = parent;
  }

  public void renderHairpinEvent( java.awt.event.ActionEvent evt )
  {
    JTable source = (JTable) myMouseEvent.getSource();

    int row = source.rowAtPoint( myMouseEvent.getPoint() );
    int column = source.columnAtPoint( myMouseEvent.getPoint() );
    // System.out.println("PARENT " + this.getParent().getParent().getClass());
    if ( source.getModel().getRowCount() >= row )
    {
      row = source.convertRowIndexToModel( row );

      String hairpinSequenceHTML = source.getModel().getValueAt( row, 3 ).toString();
      String hairpinDotBracketHTML = source.getModel().getValueAt( row, 12 ).toString();
      int miRNALength = source.getModel().getValueAt( row, 1 ).toString().length();

      int miRNASTAR_Start = Integer.parseInt( source.getModel().getValueAt( row, 17 ).toString() );
      int miRNASTAR_End = Integer.parseInt( source.getModel().getValueAt( row, 18 ).toString() );
      String MFE = source.getModel().getValueAt( row, 14 ).toString();

      LOGGER.log( Level.INFO, "get Seq: {0}", hairpinSequenceHTML );
      LOGGER.log( Level.INFO, "get DB: {0}", hairpinDotBracketHTML );
      RNAannotationMainFrame HA_tool = new RNAannotationMainFrame( hairpinSequenceHTML, hairpinDotBracketHTML, miRNALength, miRNASTAR_End - miRNASTAR_Start, MFE,
        source.getModel().getValueAt( row, 6 ).toString() );

      HA_tool.setVisible( true );
      ToolManager.getInstance().addTool( HA_tool );
      ( (MDIDesktopPane) MC_parent ).add( HA_tool );
    }
  }
}
