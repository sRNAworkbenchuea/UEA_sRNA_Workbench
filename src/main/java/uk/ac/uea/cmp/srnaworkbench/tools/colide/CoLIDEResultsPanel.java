/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
//import org.apache.commons.math.MathException;
//import org.apache.commons.math.stat.inference.ChiSquareTestImpl;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.io.FastaFileReader;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.CoLIDERenderDataProvider;
import uk.ac.uea.cmp.srnaworkbench.utils.NumberUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 *
 * @author w0445959
 */
public class CoLIDEResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus;
  private SparseExpressionMatrix myExpressionMatrix;
  private CoLIDEParams params;

  /**
   * Creates new form CoLIDEResultsPanel
   */
  public CoLIDEResultsPanel()
  {
    initComponents();


  }

  public void display( HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus, SparseExpressionMatrix expressionMatrix,
                       CoLIDEParams params )
  {


    this.params = params;
    this.chromoToLocus = chromoToLocus;
    CoLIDETableRowModel mrm = new CoLIDETableRowModel( expressionMatrix.getFileNames().size() );

    this.setName( "CoLIDEResultsPanel" );
    JTree tree = generateTreeTable( chromoToLocus, expressionMatrix );
    tree.setBackground( Color.WHITE );

    this.outline = generateResultsTable( tree, mrm );



    updateTable();


  }

  private Outline newOutline()
  {
    Outline o = new Outline();
    o.setRenderDataProvider( new CoLIDERenderDataProvider() );
    o.setDefaultRenderer( String.class, new CoLIDERenderDataProvider() );
    o.setRootVisible( false );
    o.setBackground( new java.awt.Color( 120, 120, 120 ) );
    o.setForeground( new java.awt.Color( 255, 255, 255 ) );
    o.setFillsViewportHeight( true );
    o.setRowHeight( 30 );
    o.setGridColor( new java.awt.Color( 153, 204, 255 ) );
    o.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
    o.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
    o.setCellSelectionEnabled( true );
    o.setRowSelectionAllowed( true );
    o.setColumnSelectionAllowed( false );
    //o.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_OFF );
    o.addMouseListener( new java.awt.event.MouseAdapter()
    {
      @Override
      public void mouseClicked( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }

      @Override
      public void mousePressed( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }

      @Override
      public void mouseReleased( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }
    } );
    o.addKeyListener( new java.awt.event.KeyListener()
    {
      @Override
      public void keyTyped( KeyEvent e )
      {
        keyUpdate( e );
      }

      @Override
      public void keyPressed( KeyEvent e )
      {
        keyUpdate( e );
      }

      @Override
      public void keyReleased( KeyEvent e )
      {
        keyUpdate( e );
      }
    } );

    return o;
  }

  private void checkPopup( java.awt.event.MouseEvent evt )
  {
    if ( evt.isPopupTrigger() )
    {
      this.popupMenu.show( this.outline, evt.getX(), evt.getY() );
      Point mousePoint = evt.getPoint();
      this.popupMenu.show( this.outline, evt.getX(), evt.getY() );
      int row = this.outline.rowAtPoint( mousePoint );
      int column = outline.columnAtPoint( mousePoint );
      if ( !outline.isRowSelected( row ) )
      {
        outline.changeSelection( row, column, false, false );

      }

    }
  }

  /**
   * Generate the table containing the results from the CoLIDE run
   *
   * @param chromoToLocus the data as a map, Chromosome -> ArrayList of loci
   * (each locus is another array list of expression elements)
   * @return The tree table for use in the GUI
   */
  private JTree generateTreeTable( HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus, SparseExpressionMatrix expressionMatrix )
  {

    if ( chromoToLocus == null )
    {
      return new JTree( new OutlineNode( new ArrayList<String>() ) );
    }

    myExpressionMatrix = expressionMatrix;

//
    ArrayList<String> rootList = new ArrayList<String>();
    rootList.add( "root" );
    OutlineNode rootNode = new OutlineNode( rootList );
//
//    // data - one block per organism (which could be "all combined")
    //for ( Map.Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : chromoToLocus.entrySet() )
    for ( Map.Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry :
      new TreeMap<String, ArrayList<ArrayList<ExpressionElement>>>( chromoToLocus ).entrySet() )
    {
      int locusIndex = 0;
      ArrayList<String> newChromo = new ArrayList<String>();
      newChromo.add( "Chromosome: " + entry.getKey() );
      OutlineNode child1 = new OutlineNode( newChromo );
      for ( ArrayList<ExpressionElement> locus : entry.getValue() )
      {


        ArrayList<String> locusData = new ArrayList<String>();
        Integer currentMinCoord = Integer.MAX_VALUE;
        Integer currentMaxCoord = Integer.MIN_VALUE;
        locusData.add( "Locus: " + locusIndex );

        //ArrayList<String> sequences = new ArrayList<String>();
        //create array the size of the amount of samples
        //no need to initlise because it is primitive type
        double expressionSeries[] = new double[expressionMatrix.getFileNames().size()];

        for ( ExpressionElement element : locus )
        {
          //System.out.println( "Element: " + element );
          //sequences.add(element.sequence);
          HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( element.sequence );
          ArrayList<ArrayList<String>> fileNames = expressionMatrix.getFileNames();

          int sampleIndex = 0;
          for ( ArrayList<String> sampleList : fileNames )
          {
            if ( sampleList.size() == 1 )//no replicates
            {
              if ( originalRow.containsKey( sampleList.get( 0 ) ) )
              {
                //expressionSeries[sampleIndex] += element.normalisedAbundance;
                expressionSeries[sampleIndex] += originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
              }
            }
            else
            {
              double mean = 0.0;
              for ( String replicateName : sampleList )
              {
                if ( originalRow.containsKey( replicateName ) )
                {
                  //mean += element.normalisedAbundance;
                  mean += originalRow.get( replicateName ).normalisedAbundance;
                }
              }
              mean /= sampleList.size();
              expressionSeries[sampleIndex] += mean;
            }
            sampleIndex++;
          }

          if ( element.startCoord.get( 0 ) < currentMinCoord )
          {
            currentMinCoord = element.startCoord.get( 0 );
          }
          if ( element.endCoord.get( 0 ) > currentMaxCoord )
          {
            currentMaxCoord = element.endCoord.get( 0 );
          }
        }

        //System.out.println( "locus: " + i + " contains " + lociList.size() + " sequences, min coord: " + currentMinCoord + " max coord: " + currentMaxCoord );
        locusIndex++;
        double p_val = calculateP_Val( locus );
        double roundToSignificantFigures = NumberUtils.roundToSignificantFigures( p_val, 3 );
        locusData.add( "" + currentMinCoord );
        locusData.add( "" + currentMaxCoord );
        locusData.add( "" + ( currentMaxCoord - currentMinCoord ) );
        if(roundToSignificantFigures >= 0.01)
        {
          locusData.add( String.valueOf( roundToSignificantFigures ) );
        }
        else
        {
          locusData.add( "<= 0.01" );
        }

//        String expressionString = "";
//        for(double expressionValue : expressionSeries)
//          expressionString += expressionValue + " ";
//        locusData.add( expressionString );

        for ( double expressionValue : expressionSeries )
        {
          locusData.add( "" + NumberUtils.format2DP( expressionValue, RoundingMode.HALF_UP ) );
        }

        //double minimumExpression = Collections.min( Arrays.asList( expressionSeries ));
        Arrays.sort( expressionSeries );
        double minVal = ( expressionSeries[0] + params.getOffsetChiSq() );
        double maxVal = ( expressionSeries[1] + params.getOffsetChiSq() );

        //double numerator
        double diffExpression = Math.log( ( maxVal / minVal ) ) / Math.log( 2.0 );

        locusData.add( entry.getKey() );
        locusData.add( String.valueOf( NumberUtils.format2DP( diffExpression, RoundingMode.HALF_UP) ) );
        //locusData.add( String.valueOf(  diffExpression ) );

        OutlineNode child2 = new OutlineNode( locusData );

        //OutlineNode child3 = new OutlineNode( sequences );
        //child2.add( child3 );
        child1.add( child2 );

      }
      rootNode.add( child1 );
    }
    return new JTree( rootNode );
  }

  public double calculateP_Val( ArrayList<ExpressionElement> locus )
  {
    int currentMinLength = Integer.MAX_VALUE;
    int currentMaxLength = Integer.MIN_VALUE;

    double p_val = 0.0;
    if ( locus.size() == 1 )
    {
      return p_val;
    }

    //determine max and min length 
    for ( ExpressionElement element : locus )
    {
      if ( element.sequence.length() < currentMinLength )
      {
        currentMinLength = element.sequence.length();
      }
      if ( element.sequence.length() > currentMaxLength )
      {
        currentMaxLength = element.sequence.length();
      }
    }

    HashMap<Integer, Double> intervalList;

    //loop again to find abundance of each length
    intervalList = new HashMap<Integer, Double>();
    intervalList.put( params.getSizeValue1(), 0.0 );
    intervalList.put( params.getSizeValue2(), 0.0 );
    intervalList.put( params.getSizeValue3(), 0.0 );
    intervalList.put( params.getSizeValue4(), 0.0 );


    double totalAbundance = 0.0;
    for ( ExpressionElement element : locus )
    {

      HashMap<String, ExpressionElement> originalRow = myExpressionMatrix.get( element.sequence );
      for ( ArrayList<String> sampleList : this.myExpressionMatrix.getFileNames() )
      {
        if ( sampleList.size() == 1 )//no replicates
        {
          if ( originalRow.containsKey( sampleList.get( 0 ) ) )
          {

            if ( intervalList.containsKey( element.sequence.length() ) )
            {
              Double value = intervalList.get( element.sequence.length() )
                + originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;

              totalAbundance += originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
              intervalList.put( element.sequence.length(), value );
            }
          }
        }
        else
        {
          double mean = 0.0;
          for ( String replicateName : sampleList )
          {
            if ( originalRow.containsKey( replicateName ) )
            {
              //mean += element.normalisedAbundance;
              mean += originalRow.get( replicateName ).normalisedAbundance;
            }
          }
          mean /= sampleList.size();
          if ( intervalList.containsKey( element.sequence.length() ) )
          {
            Double value = intervalList.get( element.sequence.length() )
              + mean;
            totalAbundance += mean;
            intervalList.put( element.sequence.length(), value );
          }
        }
      }
    }
    int totalAmountOfSizeClass = intervalList.values().size();
    double[] expected = new double[totalAmountOfSizeClass];
    long[] observed = new long[totalAmountOfSizeClass];


    expected[0] = ( 1.0 / (double) totalAmountOfSizeClass ) * 100.0;
    double abundAsPerc = ( ( intervalList.get( params.getSizeValue1() ) + params.getOffsetChiSq() ) / ( totalAbundance + ( params.getOffsetChiSq() * 4.0 ) ) ) * 100.0;
    observed[0] = (long) abundAsPerc;

    expected[1] = ( 1.0 / (double) totalAmountOfSizeClass ) * 100.0;
    abundAsPerc = ( ( intervalList.get( params.getSizeValue2() ) + params.getOffsetChiSq() ) / ( totalAbundance + ( params.getOffsetChiSq() * 4.0 ) ) ) * 100.0;
    observed[1] = (long) abundAsPerc;

    expected[2] = ( 1.0 / (double) totalAmountOfSizeClass ) * 100.0;
    abundAsPerc = ( ( intervalList.get( params.getSizeValue3() ) + params.getOffsetChiSq() ) / ( totalAbundance + ( params.getOffsetChiSq() * 4.0 ) ) ) * 100.0;
    observed[2] = (long) abundAsPerc;

    expected[3] = ( 1.0 / (double) totalAmountOfSizeClass ) * 100.0;
    abundAsPerc = ( ( intervalList.get( params.getSizeValue4() ) + params.getOffsetChiSq() ) / ( totalAbundance + ( params.getOffsetChiSq() * 4.0 ) ) ) * 100.0;
    observed[3] = (long) abundAsPerc;

    ChiSquareTest chiSQ = new ChiSquareTest();
    try
    {
      p_val = chiSQ.chiSquareTest( expected, observed );
    }
    catch ( Exception ex )
    {
      LOGGER.log( Level.SEVERE, "Illegal CHI sq argument{0}", ex.getMessage() );
    }
    return p_val;
  }

  private void keyUpdate( KeyEvent e )
  {
    int id = e.getID();

    if ( id == KeyEvent.KEY_RELEASED )
    {
      int c = e.getKeyCode();
      int mod = e.getModifiersEx();

      if ( c == KeyEvent.VK_C && mod == KeyEvent.CTRL_DOWN_MASK )
      {
        copySelectedRow();
      }
    }
  }

  private Outline generateResultsTable( JTree results, CoLIDETableRowModel mrm )
  {
    Outline o = newOutline();

    final TreeModel tm = results.getModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "ID" );


    o.setModel( mdl );

    MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
    Enumeration e = o.getColumnModel().getColumns();
    while ( e.hasMoreElements() )
    {
      TableColumn mod = ( (TableColumn) e.nextElement() );
      mod.setHeaderRenderer( renderer );
      mod.setPreferredWidth( 250 );
    }
    //o.removeColumn( o.getColumn( 7 ) );


    return o;
  }

  private void copySelectedRow()
  {
    //TODO Need to work out how to get the selected row into the clipboard
    // navigating the organismGroup structure keyed from row index is hard because
    // the user can expand and collapse nodes in the tree.  Maybe need to just copy
    // the text in the row rather than try to get the actual object again
    int r = this.outline.getSelectedRow();

    StringBuilder sb = new StringBuilder();

    for ( int i = 0; i < this.outline.getColumnCount(); i++ )
    {
      String s = this.outline.getModel().getValueAt( r, i ).toString().trim();
      sb.append( s );
      sb.append( "\t" );
    }

    String copyText = sb.toString().trim();

    StringSelection ss = new StringSelection( copyText );

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( ss, null );
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        popupMenu = new javax.swing.JPopupMenu();
        outputIndividualFastaMenu = new javax.swing.JMenuItem();
        outputEntireLocusFasta = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        renderSingleLocus = new javax.swing.JMenuItem();
        zoomToAggregate = new javax.swing.JMenuItem();
        resultsScroll = new javax.swing.JScrollPane();

        outputIndividualFastaMenu.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        outputIndividualFastaMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        outputIndividualFastaMenu.setText(org.openide.util.NbBundle.getMessage(CoLIDEResultsPanel.class, "CoLIDEResultsPanel.outputIndividualFastaMenu.text")); // NOI18N
        outputIndividualFastaMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputIndividualFastaMenuActionPerformed(evt);
            }
        });
        popupMenu.add(outputIndividualFastaMenu);

        outputEntireLocusFasta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        outputEntireLocusFasta.setText(org.openide.util.NbBundle.getMessage(CoLIDEResultsPanel.class, "CoLIDEResultsPanel.outputEntireLocusFasta.text")); // NOI18N
        outputEntireLocusFasta.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputEntireLocusFastaActionPerformed(evt);
            }
        });
        popupMenu.add(outputEntireLocusFasta);
        popupMenu.add(jSeparator1);

        renderSingleLocus.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        renderSingleLocus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        renderSingleLocus.setText(org.openide.util.NbBundle.getMessage(CoLIDEResultsPanel.class, "CoLIDEResultsPanel.renderSingleLocus.text")); // NOI18N
        renderSingleLocus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                renderSingleLocusActionPerformed(evt);
            }
        });
        popupMenu.add(renderSingleLocus);

        zoomToAggregate.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        zoomToAggregate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        zoomToAggregate.setText(org.openide.util.NbBundle.getMessage(CoLIDEResultsPanel.class, "CoLIDEResultsPanel.zoomToAggregate.text")); // NOI18N
        zoomToAggregate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                zoomToAggregateActionPerformed(evt);
            }
        });
        popupMenu.add(zoomToAggregate);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

  private void outputIndividualFastaMenuActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_outputIndividualFastaMenuActionPerformed
  {//GEN-HEADEREND:event_outputIndividualFastaMenuActionPerformed
    //    JTable source = (JTable) evt.getSource();
    if ( outline.getSelectedRowCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "Output not possible", "No selected loci...", JOptionPane.INFORMATION_MESSAGE );
    }
    else
    {
      File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
      if ( outputFile != null )
      {
        int[] selectedRows = outline.getSelectedRows();

        for ( int row : selectedRows )
        {
          row = outline.convertRowIndexToModel( row );

          String chromo = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
          int startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
          int endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
          exportLocusToFasta( outputFile, chromo, startCoord, endCoord );
        }
      }
    }

  }//GEN-LAST:event_outputIndividualFastaMenuActionPerformed

  private void zoomToAggregateActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_zoomToAggregateActionPerformed
  {//GEN-HEADEREND:event_zoomToAggregateActionPerformed
    generateVissrAggregate( false );

  }//GEN-LAST:event_zoomToAggregateActionPerformed

  private void outputEntireLocusFastaActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_outputEntireLocusFastaActionPerformed
  {//GEN-HEADEREND:event_outputEntireLocusFastaActionPerformed
    if ( outline.getSelectedRowCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "Output not possible", "No selected loci...", JOptionPane.INFORMATION_MESSAGE );
    }
    else
    {
      File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
      if ( outputFile != null )
      {
        int[] selectedRows = outline.getSelectedRows();

        FastaFileReader genReader = new FastaFileReader( this.myExpressionMatrix.getGenomeFile() );

        boolean success = genReader.processFile();
        if ( !success )
        {
          LOGGER.log( Level.WARNING, "Failed to load genome file. No Sequences output" );
        }
        else
        {
          FastaMap fastaMap = genReader.getFastaMap( true );

          for ( int row : selectedRows )
          {
            row = outline.convertRowIndexToModel( row );

            String chromo = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
            int startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
            int endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
            exportEntireLocusToFasta( outputFile, chromo, startCoord, endCoord, fastaMap );
          }
        }
      }
    }
  }//GEN-LAST:event_outputEntireLocusFastaActionPerformed

  private void renderSingleLocusActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_renderSingleLocusActionPerformed
  {//GEN-HEADEREND:event_renderSingleLocusActionPerformed
    generateVissrArrow( false );
  }//GEN-LAST:event_renderSingleLocusActionPerformed

  public void generateVissrArrow( boolean completeModel )
  {
    int row = -1;// = outline.convertRowIndexToModel( outline.getSelectedRow() );
    String chromo = "";// = outline.getValueAt( row, outline.getColumnCount() - 1 ).toString();
    int startCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
    int endCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );

    if ( completeModel )
    {
      startCoord = -1;
      endCoord = -1;
    }
    else
    {
      row = outline.convertRowIndexToModel( outline.getSelectedRow() );
      chromo = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
      startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
      endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );

    }
    int lowRange = 0;
    int highRange = 0;
    String chromoID = "";
    HashMap<String, Patman> locusItems = new HashMap<String, Patman>();
    final byte PHASE = 0;
    HashMap<String, TierParameters> tp_list = new HashMap<String, TierParameters>();


    double totalAbundance = 0.0;
    chromoID = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
    //lowRange = source.getValueAt( row,  )
    lowRange = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
    highRange = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );

    ArrayList<ArrayList<ExpressionElement>> lociForChromo = chromoToLocus.get( chromoID );
    ArrayList<ArrayList<ExpressionElement>> lociInRange = LocusUtils.getLociInRange( lociForChromo, lowRange, highRange );

    int sampleNumber = 1;
    for ( ArrayList<String> fileNames : this.myExpressionMatrix.getFileNames() )
    {

      locusItems.put( "Sample " + sampleNumber, new Patman() );

      sampleNumber++;
    }
   
    for ( ArrayList<ExpressionElement> locus : lociInRange )
    {
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;

      for ( ExpressionElement element : locus )
      {

        if ( element.startCoord.get( 0 ) < currentMinCoord )
        {
          currentMinCoord = element.startCoord.get( 0 );
        }
        if ( element.endCoord.get( 0 ) > currentMaxCoord )
        {
          currentMaxCoord = element.endCoord.get( 0 );
        }
      }
      if ( currentMinCoord >= lowRange && currentMaxCoord <= highRange )
      {
        for ( ExpressionElement ele : locus )
        {
          double value = 0.0;
          int startToPrint = 0;
          int endToPrint = 0;
          for ( int start : ele.startCoord )
          {
            if ( ( start <= highRange ) && start >= lowRange )
            {
              startToPrint = start;
            }
          }
          for ( int end : ele.endCoord )
          {
            if ( ( end <= highRange ) && end >= lowRange )
            {
              endToPrint = end;
            }
          }
          HashMap<String, ExpressionElement> originalRow = myExpressionMatrix.get( ele.sequence );
           //reset sample ID for use later in method
          sampleNumber = 1;
          for ( ArrayList<String> sampleList : this.myExpressionMatrix.getFileNames() )
          {
            if ( sampleList.size() == 1 )//no replicates
            {
              if ( originalRow.containsKey( sampleList.get( 0 ) ) )
              {

                value =
                  originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
                PatmanEntry newPat = new PatmanEntry( ele.chromosomeID, ele.sequence, value, startToPrint, endToPrint, SequenceStrand.NEGATIVE, 0 );

                locusItems.get( "Sample " + sampleNumber ).add( newPat );
                totalAbundance += value;


              }
            }
            else
            {
              double mean = 0.0;
              for ( String replicateName : sampleList )
              {
                if ( originalRow.containsKey( replicateName ) )
                {
                  //mean += element.normalisedAbundance;
                  mean += originalRow.get( replicateName ).normalisedAbundance;
                }
              }
              if ( mean > 0.0 )
              {
                mean /= sampleList.size();
                value = mean;
                PatmanEntry newPat = new PatmanEntry( ele.chromosomeID, ele.sequence, value, startToPrint, endToPrint, SequenceStrand.NEGATIVE, 0 );

                locusItems.get( "Sample " + sampleNumber ).add( newPat );
                totalAbundance += value;
              }
            }
            sampleNumber++;
          }

        }
      }
    }

    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( myExpressionMatrix.getGenomeFile(), true, (TierParameters) null );

    for ( Map.Entry<String, Patman> entry : new TreeMap<String, Patman>(locusItems ).entrySet() )
    {
      entry.getValue().setGlyphHeights( (int) totalAbundance );
      String tierName =  entry.getKey();

      //create tier params for this sample
      TierParameters tp = new TierParameters.Builder( tierName ).build();//glyphClass( StripedSolidRectGlyph.class ).build();
      if ( !entry.getValue().isEmpty() )
      {
        tp.addList( locusItems.get( entry.getKey() ) );
      }
      vissr.addTier( tp );

    }
    vissr.displaySequenceRegion( chromoID, lowRange - 5, highRange + 5 );
    AppUtils.INSTANCE.activateFrame( vissr );
  }

  public void generateVissrAggregate( boolean completeModel )
  {

    int row;// = outline.convertRowIndexToModel( outline.getSelectedRow() );
    String chromo = "";// = outline.getValueAt( row, outline.getColumnCount() - 1 ).toString();
    int startCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
    int endCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );

    if ( completeModel )
    {
      startCoord = -1;
      endCoord = -1;
    }
    else
    {
      row = outline.convertRowIndexToModel( outline.getSelectedRow() );
      chromo = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
      startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
      endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );

    }
    SequenceVizMainFrame vissr = LocusUtils.setupAndDisplayExpressionElementAggregate( myExpressionMatrix, true, startCoord, endCoord,
      chromoToLocus.get( chromo ) );

    vissr.displaySequenceRegion( chromo, startCoord, endCoord );
    AppUtils.INSTANCE.activateFrame( vissr );
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem outputEntireLocusFasta;
    private javax.swing.JMenuItem outputIndividualFastaMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem renderSingleLocus;
    private javax.swing.JScrollPane resultsScroll;
    private javax.swing.JMenuItem zoomToAggregate;
    // End of variables declaration//GEN-END:variables

  private void updateTable()
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        resultsScroll.setViewportView( outline );
      }
    } );
  }

  private void exportEntireLocusToFasta( File outputFile, String chromo, int startCoord, int endCoord, FastaMap fastaMap )
  {
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      ArrayList<ArrayList<ExpressionElement>> lociForChromo = chromoToLocus.get( chromo );
      ArrayList<ArrayList<ExpressionElement>> lociInRange = LocusUtils.getLociInRange( lociForChromo, startCoord, endCoord );

      for ( ArrayList<ExpressionElement> locus : lociInRange )
      {
        int currentMinCoord = Integer.MAX_VALUE;
        int currentMaxCoord = Integer.MIN_VALUE;

        for ( ExpressionElement element : locus )
        {

          if ( element.startCoord.get( 0 ) < currentMinCoord )
          {
            currentMinCoord = element.startCoord.get( 0 );
          }
          if ( element.endCoord.get( 0 ) > currentMaxCoord )
          {
            currentMaxCoord = element.endCoord.get( 0 );
          }
        }
        if ( currentMinCoord >= startCoord && currentMaxCoord <= endCoord )
        {



          FastaRecord get = fastaMap.get( chromo );
          out.println( ">" + chromo + "-Start:" + currentMaxCoord + "-End:" + currentMaxCoord );
          out.println( get.getSubSequenceAsString( currentMinCoord, currentMaxCoord, SequenceStrand.POSITIVE ) );


        }
      }

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outFASTAFile.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }

  private void exportLocusToFasta( File outputFile, String chromo, int startCoord, int endCoord )
  {
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      ArrayList<ArrayList<ExpressionElement>> lociForChromo = chromoToLocus.get( chromo );
      ArrayList<ArrayList<ExpressionElement>> lociInRange = LocusUtils.getLociInRange( lociForChromo, startCoord, endCoord );

      for ( ArrayList<ExpressionElement> locus : lociInRange )
      {
        int currentMinCoord = Integer.MAX_VALUE;
        int currentMaxCoord = Integer.MIN_VALUE;

        for ( ExpressionElement element : locus )
        {

          if ( element.startCoord.get( 0 ) < currentMinCoord )
          {
            currentMinCoord = element.startCoord.get( 0 );
          }
          if ( element.endCoord.get( 0 ) > currentMaxCoord )
          {
            currentMaxCoord = element.endCoord.get( 0 );
          }
        }
        if ( currentMinCoord >= startCoord && currentMaxCoord <= endCoord )
        {
          for ( ExpressionElement ele : locus )
          {
            int startToPrint = 0;
            int endToPrint = 0;
            for ( int start : ele.startCoord )
            {
              if ( ( start <= endCoord ) && start >= startCoord )
              {
                startToPrint = start;
              }
            }
            for ( int end : ele.endCoord )
            {
              if ( ( end <= endCoord ) && end >= startCoord )
              {
                endToPrint = end;
              }
            }

            out.println( ">Chromosome-" + ele.chromosomeID + "-Start-" + startToPrint + "-Stop-" + endToPrint + "-strand-" + ele.strand + ele.printableSampleName );
            out.println( ele.sequence );
          }
        }
      }

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outFASTAFile.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }

  public Outline getOutline()
  {
    return this.outline;
  }

  public void resetAll()
  {
    if ( this.outline != null )
    {
      //this.outline.setModel( null );
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          resultsScroll.setViewportView( newOutline() );
        }
      } );
      this.outline.invalidate();
    }
  }
}
