/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 *
 * @author w0445959
 */
public class LimitedLinesDocument extends DefaultStyledDocument 
{    

  private int maxLines;

  public LimitedLinesDocument(int maxLines) 
  {    
    this.maxLines = maxLines;
  }
  
  /**
   * Allowing max lines to be changed without creating a new object
   * @param maxLines 
   */
  public void setMaxLines(int maxLines){
    this.maxLines = maxLines;
  }

  @Override
  public void insertString(int offs, String str, AttributeSet attribute) throws BadLocationException 
  {        
    if (!LINE_SEPARATOR.equals(str) || StringUtils.occurs(getText(0, getLength()), LINE_SEPARATOR) < maxLines - 1) 
    {    
      super.insertString(offs, str, attribute);
    }
  }
}