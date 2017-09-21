/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import javax.swing.JButton;
import javax.swing.JMenuItem;

/**
 * Manages a two buttons and optionally two menu items, which are intended for starting
 * and stopping a threaded process.  The components enabled flag is automatically
 * set depending on whether or not the process is currently running.
 * @author Daniel Mapleson
 */
public class GoController
{
  private JButton cmdStart;
  private JButton cmdCancel;
  private JMenuItem mnuStart;
  private JMenuItem mnuCancel;

  /**
   * Creates a new GoController object which manages two buttons for starting and
   * cancelling a threaded process.
   * @param cmdStart The button which initiates the task
   * @param cmdCancel The button which cancels the task
   */
  public GoController( JButton cmdStart, JButton cmdCancel )
  {
    this( cmdStart, cmdCancel, null, null );
  }

  /**
   * Creates a new GoController object which manages two buttons and two menu items 
   * for starting and cancelling a threaded process.
   * @param cmdStart The button which initiates the task
   * @param cmdCancel The button which cancels the task
   * @param mnuStart The menu item which initiates the task
   * @param mnuCancel The menu item which cancels the task
   */
  public GoController( JButton cmdStart, JButton cmdCancel, JMenuItem mnuStart, JMenuItem mnuCancel )
  {
    this.cmdStart = cmdStart;
    this.cmdCancel = cmdCancel;
    this.mnuStart = mnuStart;
    this.mnuCancel = mnuCancel;
  }

  /**
   * Puts the GoController into a running or not running state, which determines
   * which of the components should be enabled or disabled
   * @param running Whether the task is currently running or not
   */
  public void setRunning( boolean running )
  {
    if ( running )
    {
      this.cmdStart.setEnabled( false );
      this.cmdCancel.setEnabled( true );

      if ( this.mnuStart != null && this.mnuCancel != null )
      {
        this.mnuStart.setEnabled( false );
        this.mnuCancel.setEnabled( true );
      }
    }
    else
    {
      this.cmdStart.setEnabled( true );
      this.cmdCancel.setEnabled( false );

      if ( this.mnuStart != null && this.mnuCancel != null )
      {
        this.mnuStart.setEnabled( true );
        this.mnuCancel.setEnabled( false );
      }
    }
  }
}
