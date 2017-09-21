/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * A status tracker provides an easy to use mechanism for tracking the progress of
 * a task.  It manages calls to a progress bar and a label for status messages.
 * @author Daniel Mapleson
 */
public class StatusTracker
{
  private JProgressBar progBar;
  private JLabel status;

  /**
   * Creates a new StatusTracker
   * @param progBar A progress bar to use
   * @param status A label for status messages
   */
  public StatusTracker( JProgressBar progBar, JLabel status )
  {
    this.progBar = progBar;
    this.status = status;
  }

  /**
   * Puts tracker into a indeterminate state, and displays given message
   * @param message Current status message
   */
  public void initUnknownRuntime( final String message )
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setIndeterminate( true );
          progBar.setValue( 0 );
        }
      });
    }

    if ( this.status != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          status.setText( "Status: " + message + "..." );
        }
      });
    }
  }

  /**
   * Puts tracker into a determinate task state and puts bar at the beginning, 
   * and displays the given message
   * @param message Current status message
   * @param length Number of units to process until completion
   */
  public void initKnownRuntime( final String message, final int length )
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setMaximum( length );
          progBar.setValue( 0 );
        }
      });
    }

    if ( this.status != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          status.setText( "Status: " + message + "..." );
        }
      });
    }
  }

  /**
   * Puts tracker into a finished state (either success or fail)
   * @param success Whether the task completed successfully or not
   */
  public void setFinished( final boolean success )
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setIndeterminate( false );
          progBar.setValue( 0 );
        }
      });
    }

    if ( this.status != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          status.setText( "Status: " + ( success ? "completed successfully" : "failed" ) );
        }
      });
    }
  }

  /**
   * Puts tracker into default state.
   */
  public void reset()
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setIndeterminate( false );
          progBar.setValue( 0 );
        }
      });
    }

    if ( this.status != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          status.setText( "Status: Idle" );
        }
      });
    }
  }

  /**
   * For known runtime tasks, increments the progress bar 1 unit.
   */
  public void increment()
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setValue( progBar.getValue() + 1 );
        }
      });
    }
  }
   /**
   * For known runtime tasks, increments the progress bar to value.
   */
  public void increment(final int value)
  {
    if ( this.progBar != null )
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          progBar.setValue( value );
        }
      });
    }
  }

  /**
   * Retrieves the progress bar managed by this StatusTracker
   * @return GUI Progress Bar
   */
  public JProgressBar getProgressBar()
  {
    return this.progBar;
  }

  /**
   * Retrieves the status label managed by this StatusTracker
   * @return GUI Label
   */
  public JLabel getStatusLabel()
  {
    return this.status;
  }
}
