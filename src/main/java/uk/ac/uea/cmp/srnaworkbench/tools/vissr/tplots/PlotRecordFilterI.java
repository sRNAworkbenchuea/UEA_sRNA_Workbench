/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.PlotRecord;

import javax.swing.JComponent;

/**
 *
 * @author prb07qmu
 */
public interface PlotRecordFilterI
{
  String getDescription();
  String getOperator();
  JComponent getComponent();

  void setFilterValue( Object value );
  Object getFilterValue();

  boolean include( PlotRecord pr );
}
