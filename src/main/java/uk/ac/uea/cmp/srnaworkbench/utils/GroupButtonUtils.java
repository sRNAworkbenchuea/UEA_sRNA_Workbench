/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 *
 * @author w0445959
 */
import java.awt.Color;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

public class GroupButtonUtils {

    public static String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }
    
  // This method returns the selected radio button in a button group
  // Also takes a list of colours and sets the correct color to the supplied color
  // if optional params (colours and toSet) are null just the button is returned
  public static JRadioButton getSelectedRadioButton( ButtonGroup group, Color colours[], Color toSet )
  {
    int i = 0;
    for ( Enumeration e = group.getElements(); e.hasMoreElements(); )
    {
      JRadioButton b = (JRadioButton) e.nextElement();
      if ( b.getModel() == group.getSelection() )
      {
        if ( colours != null && toSet != null )
        {
          colours[i] = toSet;
        }
        return b;
      }
      i++;
    }
    return null;
  }
   
}