/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.expressionCalculator;

import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author w0445959
 */
public class ExpressionCalculator extends RunnableTool
{
  private final static String TOOL_NAME = "Expression_Calculator";
  public ExpressionCalculator(ExpressionCalculatorParams params, StatusTracker tracker)
  {
    super(TOOL_NAME, tracker);
  }
  
  private void build_mRNA_ExpressionMatrix()
  {
    //map reads
    
  }
  
  private void build_sRNA_ExpressionMatrix()
  {
    
  }

  @Override
  protected void process() throws Exception
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
  
}
