/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FileSelectorPanel.java
 *
 * Created on 02-Dec-2011, 16:14:04
 */
package uk.ac.uea.cmp.srnaworkbench.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.filechooser.FileNameExtensionFilter;
import uk.ac.uea.cmp.srnaworkbench.history.HistoryBrowser;
import uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * A component for easily selecting a file path
 * @author Dan Mapleson
 */
public final class FileSelectorPanel extends javax.swing.JPanel
{
  private static final int FILE_SELECTOR_WIDTH = 36;
   
  private FileSelector type;
  private FileNameExtensionFilter[] filters;

  private HistoryFileType historyType = HistoryFileType.NONE;
  private String toolName;
  private boolean historySingleMode = false;
  
  // The Document is exposed through a method so that we can react to the 
  // any changes in text
  private LimitedLinesDocument txtFileDocument;

  /** Creates new form FileSelectorPanel */
  public FileSelectorPanel()
  {
    initComponents();
    
    this.txtFileDocument = new LimitedLinesDocument(1);
    this.txtFilePath.setDocument(txtFileDocument);
    setHistoryType(historyType);
  }

  /**
   * Sets the label
   * @param label The text to display in the label
   */
  public void setLabel( String label )
  {
    this.lblFile.setText( label );
    
    this.setLabelWidth( this.lblFile.getPreferredSize().width );
    //this.lblFile.revalidate();
  }
  public void setFileLineAmount(int amountOfFiles)
  {
    //this.txtFilePath.setDocument(new LimitedLinesDocument(amountOfFiles));
    this.txtFileDocument.setMaxLines( amountOfFiles);
  }
  
  public void setHistoryType(HistoryFileType type)
  {
    historyType = type;
    if(historyType == HistoryFileType.NONE)
    {
      this.cmdSrnaFileHistory.setEnabled( false );
    }
    else
    {
      this.cmdSrnaFileHistory.setEnabled( true );
    }
  }
  
  public HistoryFileType getHistoryType()
  {
    return historyType;
  }
  
  public LimitedLinesDocument getDocument()
  {
    return this.txtFileDocument;
  }

  /**
   * Gets the label
   * @param label The text in the label
   */
  public String getLabel()
  {
    return this.lblFile.getText();
  }

  public Color getCompositeBackground()
  {
    return getBackground();
  }

  public void setCompositeBackground( Color bg )
  {
    setBackground( bg );

    cmdSelectFile.setBackground( bg );
    lblFile.setBackground( bg );

    invalidate();
  }

  public Color getLabelForeground()
  {
    return lblFile.getForeground();
  }

  public void  setLabelForeground( Color fg )
  {
    lblFile.setForeground( fg );

    repaint();
  }

  
  
  @Override
  public void revalidate()
  {
    super.revalidate();
    
//    if ( lblFile != null && txtFilePath != null && cmdSelectFile != null )
//    {
//      layoutComponents();
//    }
  }
  
  @Override
  public void invalidate()
  {
    super.invalidate();
    
    
  }
  
  @Override
  public void repaint()
  {
    super.repaint();
    
    if ( lblFile != null && txtFilePath != null && cmdSelectFile != null )
    {
      //layoutComponents();
      
      lblFile.repaint();
      txtFilePath.repaint();
      cmdSelectFile.repaint();
    }
  }
  
  

  /**
   * Sets the type of file selector this component should be
   * @param type The type of file selector this component should be
   */
  public void setSelector( FileSelector type )
  {
    this.type = type;
  }

  /**
   * Sets the filter list to use for the file dialog box
   * @param filters
   */
  public void setFilters( FileNameExtensionFilter[] filters )
  {
    this.filters = filters;
  }
  
  /**
   * Retrieves the current set of file filters used for this component.
   * @return The current set of file filters.
   */
  public FileNameExtensionFilter[] getFilters()
  {
    return this.filters;
  }


  /**
   * Gets a File object represented by the path described in the input file text box
   * @return A file object represented by the path described in the input file text box
   */
  public File getFile()
  {
    String path = this.txtFilePath.getText().trim();
    if(!path.isEmpty())
      this.historyType.writeHistory( path + Tools.TAB + toolName + Tools.TAB + Tools.getDateTime() );
    File f = path.isEmpty() ? null : new File( path );
    return f;
  }
  public ArrayList<File> getFiles()
  {
    ArrayList<File> files = new ArrayList<File>();
    String path = this.txtFilePath.getText().trim();
    String[] paths = path.split( LINE_SEPARATOR);
    String historyInput = "";
    for(String toAdd : paths)
    {
      if ( !toAdd.isEmpty() )
      {
        historyInput = toAdd + Tools.TAB + toolName + Tools.TAB + Tools.getDateTime();
        File toAddFile = new File( toAdd );
        if ( toAddFile.exists() )
        {
          files.add( toAddFile );
          this.historyType.writeHistory(historyInput);
        }
      }
    }
    
    return files;
  }
  
  public void addToHistory(String toAdd)
  {
    File toAddFile = new File( toAdd );
    if ( toAddFile.exists() )
    {
      this.historyType.writeHistory( toAdd + Tools.TAB + toolName + Tools.TAB + Tools.getDateTime() );
    }
  }
  public void addToHistory(ArrayList<String> listToAdd)
  {
    for(String toAdd : listToAdd)
    {
      if ( !toAdd.isEmpty() )
      {

        File toAddFile = new File( toAdd );
        if ( toAddFile.exists() )
        {
          this.historyType.writeHistory(toAdd + Tools.TAB + toolName + Tools.TAB + Tools.getDateTime());
        }
      }
    }
  }
  /**
   * Clears the text box of any file information.
   */
  public void clear()
  {
    this.txtFilePath.setText( "" );
    this.txtFilePath.setCaretPosition( 0);
  }
  
  /**
   * Sets the path to the given parameter
   * @param file_path The file path the set.
   */
  public void setFilePath( String file_path )
  {
    if ( file_path != null )
    {
      this.txtFilePath.setText( file_path );
      this.txtFilePath.setCaretPosition( 0);
    }
    else
    {
      clear();
    }
  }
  
  /**
   * Sets the path to the given parameter
   * @param file_path The file path the set.
   */
  public void setFilePath( File f )
  {
    if ( f != null )
    {
      setFilePath( f.getPath() );
      
    }
    else
    {
      clear();
    }
  }


  @Override
  public void setEnabled( boolean enabled )
  {
    this.lblFile.setEnabled( enabled );
    this.txtFilePath.setEnabled( enabled );
    this.cmdSelectFile.setEnabled( enabled );
    this.cmdSrnaFileHistory.setEnabled( enabled );
  }

  public int getLabelWidth()
  {
    
    return this.lblFile.getPreferredSize().width;
    
    
  }
  public void setLabelWidth(int width)
  {

    lblFile.setMinimumSize( new Dimension (width, lblFile.getHeight() ) );
    lblFile.setPreferredSize( new Dimension (width, lblFile.getHeight() ) );

    revalidate();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        lblFile = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtFilePath = new javax.swing.JTextArea();
        cmdSelectFile = new javax.swing.JButton();
        cmdSrnaFileHistory = new javax.swing.JButton();

        setBackground(new java.awt.Color(120, 120, 120));
        setMinimumSize(new java.awt.Dimension(158, 37));
        setPreferredSize(new java.awt.Dimension(301, 37));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        lblFile.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblFile.setForeground(new java.awt.Color(255, 255, 255));
        lblFile.setText("File Path Label:");
        add(lblFile);

        txtFilePath.setColumns(20);
        txtFilePath.setRows(1);
        txtFilePath.addInputMethodListener(new java.awt.event.InputMethodListener()
        {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt)
            {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt)
            {
                txtFilePathInputMethodTextChanged(evt);
            }
        });
        jScrollPane1.setViewportView(txtFilePath);

        add(jScrollPane1);

        cmdSelectFile.setBackground(new java.awt.Color(120, 120, 120));
        cmdSelectFile.setText("...");
        cmdSelectFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cmdSelectFile.setMaximumSize(new java.awt.Dimension(32, 25));
        cmdSelectFile.setMinimumSize(new java.awt.Dimension(32, 25));
        cmdSelectFile.setPreferredSize(new java.awt.Dimension(32, 25));
        cmdSelectFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdSelectFileActionPerformed(evt);
            }
        });
        add(cmdSelectFile);

        cmdSrnaFileHistory.setBackground(new java.awt.Color(120, 120, 120));
        cmdSrnaFileHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/Clock16.png"))); // NOI18N
        cmdSrnaFileHistory.setToolTipText("Specify a sRNA file from the history browser");
        cmdSrnaFileHistory.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cmdSrnaFileHistory.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdSrnaFileHistoryActionPerformed(evt);
            }
        });
        add(cmdSrnaFileHistory);
    }// </editor-fold>//GEN-END:initComponents

  private void cmdSelectFileActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdSelectFileActionPerformed
  {//GEN-HEADEREND:event_cmdSelectFileActionPerformed
    if ( filters == null )
      filters = new FileNameExtensionFilter[] {};

    if(this.historySingleMode)
      clear();
    this.txtFilePath.append( this.type.select( filters, this.historyType, toolName ));
    this.txtFilePath.setCaretPosition( 0);  
  }//GEN-LAST:event_cmdSelectFileActionPerformed

  
  private void cmdSrnaFileHistoryActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdSrnaFileHistoryActionPerformed
  {//GEN-HEADEREND:event_cmdSrnaFileHistoryActionPerformed
// //     File shortReadFile = FileDialogUtils.showSingleFileOpenDialog( FileExtFilter.toFilterArray( FileExtFilter.PATMAN, FileExtFilter.FASTA ), this );

    JFrame temp = new JFrame();
    temp.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    HistoryBrowser history = new HistoryBrowser( temp, "File History", "Please select File from history", historyType);
    history.setSingleSelectionMode(historySingleMode);
    
    history.setVisible( true );
    if(this.historySingleMode)
      clear();
    if ( history.getValue() == JOptionPane.OK_OPTION )
    {
      for(String shortReadFile : history.getSelectedFiles())
      {
          this.txtFilePath.append( shortReadFile );
          this.txtFilePath.setCaretPosition( 0);
      }

      

      //updatePipelinePaths();
    }
  }//GEN-LAST:event_cmdSrnaFileHistoryActionPerformed

    private void txtFilePathInputMethodTextChanged(java.awt.event.InputMethodEvent evt)//GEN-FIRST:event_txtFilePathInputMethodTextChanged
    {//GEN-HEADEREND:event_txtFilePathInputMethodTextChanged
      this.dispatchEvent( evt );        // TODO add your handling code here:
    }//GEN-LAST:event_txtFilePathInputMethodTextChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdSelectFile;
    private javax.swing.JButton cmdSrnaFileHistory;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFile;
    private javax.swing.JTextArea txtFilePath;
    // End of variables declaration//GEN-END:variables

  public void setToolName( String name )
  {
    toolName = name;
  }

  public void setPath( String path )
  {
    this.txtFilePath.setText( path );
  }

  public void setHistorySingleMode( boolean b )
  {
    historySingleMode = b;
  }
}
