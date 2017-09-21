/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.expressionCalculator;

import java.io.IOException;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author w0445959
 */
public class ExpressionCalculatorRunner extends ToolRunner
{
  public ExpressionCalculatorRunner(ToolHost host)
  {
    super(host);
  }
  
  public void runExpressionCalculator(ExpressionCalculatorParams params, StatusTracker tracker)
  {
    try
    {
      ExpressionCalculator exp_calc_engine = new ExpressionCalculator(  params, tracker );
        this.run( exp_calc_engine );
    }
    catch ( IOException ioe )
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }
}
