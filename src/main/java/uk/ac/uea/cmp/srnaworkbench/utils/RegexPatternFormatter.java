/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import javax.swing.text.DefaultFormatter;

/**
 *
 * @author Matt
 */
public class RegexPatternFormatter extends DefaultFormatter {

  protected java.util.regex.Matcher matcher;

  public RegexPatternFormatter(java.util.regex.Pattern regex) {
    setOverwriteMode(false);
    matcher = regex.matcher(""); // create a Matcher for the regular
                   // expression
  }

    @Override
  public Object stringToValue(String string) throws java.text.ParseException {
    if (string == null)
      return null;
    matcher.reset(string); // set 'string' as the matcher's input

    
    if (!matcher.matches()) // Does 'string' match the regular expression?
    {
        throw new java.text.ParseException("does not match regex", 0);
    }

 
    // If we get this far, then it did match.
    return super.stringToValue(string); // will honor the 'valueClass'
                      // property
  }

}
