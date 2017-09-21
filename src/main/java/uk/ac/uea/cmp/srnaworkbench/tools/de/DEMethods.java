/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionData;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionLevels;

/**
 *
 * @author ezb11yfu
 */
public enum DEMethods
{
  OFFSET_FOLD_CHANGE
  {
    @Override
    public ExpressionLevels compute( ExpressionData expData, DEParams params )
    {
      return OffsetFoldChange.calcOFC( expData.getData(), params.getOffset(), params.getOrdered() );
    }
  },
  UNUSUAL_RATIO
  {
    @Override
    public ExpressionLevels compute( ExpressionData expData, DEParams params )
    {
      return UnusualRatio.calcUnusualRatio( expData.getData(), params.getOffset() );
    }
  },
  MODIFIED_SAM
  {
    @Override
    public ExpressionLevels compute( ExpressionData expData, DEParams params )
    {
      return ModifiedSAM.calcModifiedSAM( expData.getData(), params.getReplicateCounts(), params.getOffset() );
    }
  };
  
  public abstract ExpressionLevels compute( ExpressionData expData, DEParams params );
}
