/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author prb07qmu
 */
public final class VisSRGlobals
{
  private VisSRGlobals() {}

  private static final Map< Integer, Color > DEFAULT_LENGTH_COLOR_MAP = CollectionUtils.newHashMap();
  private static  Map< Integer, Color > LENGTH_COLOR_MAP = CollectionUtils.newHashMap();
  private static final int MIN_LENGTH = 15;

  enum ViewMode
  {
    NORMAL   { @Override boolean isNormal()   { return true; } },
    PARESNIP { @Override boolean isParesnip() { return true; } };

    // shortcuts
    boolean isParesnip() { return false; }
    boolean isNormal()   { return false; }
  }

  static
  {
    // Load the length -> colour map
    //
    // shades of pink
    DEFAULT_LENGTH_COLOR_MAP.put( 15, new Color( 0xDDA0DD ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 16, new Color( 0xFFB6C1 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 17, new Color( 0xEEA2AD ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 18, new Color( 0xFF69b4 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 19, new Color( 0xFF6EC7 ) );

    DEFAULT_LENGTH_COLOR_MAP.put( 20, Color.red );
    DEFAULT_LENGTH_COLOR_MAP.put( 21, Color.red.darker() );

    DEFAULT_LENGTH_COLOR_MAP.put( 22, Color.green );
    DEFAULT_LENGTH_COLOR_MAP.put( 23, Color.green.darker() );

    DEFAULT_LENGTH_COLOR_MAP.put( 24, Color.blue );
    DEFAULT_LENGTH_COLOR_MAP.put( 25, Color.blue.darker() );

    DEFAULT_LENGTH_COLOR_MAP.put( 26, new Color( 0, 120, 120 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 27, new Color( 0, 140, 140 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 28, new Color( 0, 160, 160 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 29, new Color( 0, 180, 180 ) );
    DEFAULT_LENGTH_COLOR_MAP.put( 30, new Color( 0, 200, 200 ) );
    
    cloneColourMap();
  }

  private static void cloneColourMap()
  {
    for(Map.Entry<Integer, Color> e : new TreeMap<>(DEFAULT_LENGTH_COLOR_MAP).entrySet())
    {
      LENGTH_COLOR_MAP.put( e.getKey(), e.getValue());
    }
  }
  public static void setColourForLength(int length, Color newColor)
  {
    Color c = LENGTH_COLOR_MAP.get( length );

    if ( c != null )
    {
      LENGTH_COLOR_MAP.put( length, newColor );
    }
    else
    {
      LOGGER.log(Level.WARNING, "An attempted colour setting has resulted in an error, size not present in list");
    }
  }
  public static Map<Integer, Color> getColourMap()
  {
    return LENGTH_COLOR_MAP;
  }
  public static void resetToDefaults()
  {
    cloneColourMap();
    
  }
  public static Color getColourForLength( int length )
  {
    Color c = LENGTH_COLOR_MAP.get( length );

    if ( c != null )
    {
      return c;
    }

    if ( length < MIN_LENGTH )
    {
      return Color.lightGray;
    }

    return Color.gray;
  }
}

