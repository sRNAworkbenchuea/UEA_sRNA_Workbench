/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.matrix;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;

/**
 * Handles building a SparseExpressionMatrix as a stand-alone process
 * @author Matthew Beckers
 */
public class SparseExpressionMatrixRunner extends ToolRunner
{
  
  public SparseExpressionMatrixRunner(ToolHost host){
    super(host);
  }
  
  public void buildSparseExpressionMatrix(SparseExpressionMatrix matrix) throws Exception{
    this.engine = matrix;
    this.run( engine );
  }
  
}
