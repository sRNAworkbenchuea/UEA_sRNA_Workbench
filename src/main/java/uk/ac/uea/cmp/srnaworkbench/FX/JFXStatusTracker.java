/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.FX;

import javafx.scene.web.WebEngine;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author w0445959
 */
public class JFXStatusTracker extends StatusTracker
{

    private WebEngine webEngine;
    private String progressWheelName;

    /**
     * Creates a new JFXStatusTracker
     *
     * @param engine A web engine containing the radial progress script 
     * @param progressWheelName the ID for the HTML element used to display
     * progress
     */
    public JFXStatusTracker(WebEngine engine, String progressWheelName)
    {   
        super(null, null);
        webEngine = engine;
        this.progressWheelName = progressWheelName;
    }
    
    public JFXStatusTracker()
    {
        this(null, null);
    }

    /**
     * Puts tracker into a indeterminate state, and displays given message
     *
     * @param message Current status message
     */
    @Override
    public void initUnknownRuntime(final String message)
    {
        System.out.println("init unknown");
        webEngine.executeScript(" showSingleDiv(' " + progressWheelName + " ') ");

//    if ( this.status != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          status.setText( "Status: " + message + "..." );
//        }
//      });
//    }
    }

    /**
     * Puts tracker into a determinate task state and puts bar at the beginning,
     * and displays the given message
     *
     * @param message Current status message
     * @param length Number of units to process until completion
     */
    @Override
    public void initKnownRuntime(final String message, final int length)
    {
//    if ( this.progBar != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          progBar.setMaximum( length );
//          progBar.setValue( 0 );
//        }
//      });
//    }
//
//    if ( this.status != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          status.setText( "Status: " + message + "..." );
//        }
//      });
//    }
    }

    /**
     * Puts tracker into a finished state (either success or fail)
     *
     * @param success Whether the task completed successfully or not
     */
    @Override
    public void setFinished(final boolean success)
    {
//    if ( this.progBar != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          progBar.setIndeterminate( false );
//          progBar.setValue( 0 );
//        }
//      });
//    }
//
//    if ( this.status != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          status.setText( "Status: " + ( success ? "completed successfully" : "failed" ) );
//        }
//      });
//    }
    }

    /**
     * Puts tracker into default state.
     */
    @Override
    public void reset()
    {
        webEngine.executeScript(" hideSingleDiv(' " + progressWheelName + " ') ");
//    if ( this.progBar != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          progBar.setIndeterminate( false );
//          progBar.setValue( 0 );
//        }
//      });
//    }
//
//    if ( this.status != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          status.setText( "Status: Idle" );
//        }
//      });
//    }
    }

    /**
     * For known runtime tasks, increments the progress bar 1 unit.
     */
    @Override
    public void increment()
    {
//    if ( this.progBar != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          progBar.setValue( progBar.getValue() + 1 );
//        }
//      });
//    }
    }

    /**
     * For known runtime tasks, increments the progress bar to value.
     *
     * @param value
     */
    @Override
    public void increment(final int value)
    {
//    if ( this.progBar != null )
//    {
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
//          progBar.setValue( value );
//        }
//      });
//    }
    }

    public WebEngine getWebEngine()
    {
        return webEngine;
    }

    public void setWebEngine(WebEngine webEngine)
    {
        this.webEngine = webEngine;
    }

    public String getProgressWheelName()
    {
        return progressWheelName;
    }

    public void setProgressWheelName(String progressWheelName)
    {
        this.progressWheelName = progressWheelName;
    }
    
    
    

}
