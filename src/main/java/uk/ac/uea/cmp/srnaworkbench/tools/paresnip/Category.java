/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.awt.Color;
import java.util.*;

/**
 * Enumeration of categories used by PARESnip (defined by CleaveLand v2).<br/>
 * The colours used in output are defined here but could easily be user-defined.
 *
 * @author prb07qmu
 */
public enum Category
{
  UNDEFINED( Integer.MAX_VALUE, Color.black ),
  CATEGORY_0( 0, Color.red ),
  CATEGORY_1( 1, Color.magenta ),
  CATEGORY_2( 2, Color.orange ),
  CATEGORY_3( 3, Color.green ),
  CATEGORY_4( 4, Color.pink );

  private final int _category;
  private final Color _color;

  private Category( int category, Color col )
  {
    _category = category;
    _color = col;
  }

  public int getCategoryValue()
  {
    return _category;
  }

  public Color getCategoryColor()
  {
    return _color;
  }

  @Override
  public String toString()
  {
    if ( this == UNDEFINED )
    {
      return "UNDEFINED CATEGORY";
    }

    return "Category " + _category;
  }

  // Static variables and methods
  private static final Map< Integer, Category> _lookup = new HashMap< Integer, Category>();
  private static final EnumSet< Category> _definedCategories = EnumSet.< Category>range( CATEGORY_0, CATEGORY_4 );

  static
  {
    for ( Category c : Category.values() )
    {
      _lookup.put( c.getCategoryValue(), c );
    }
  }

  public static EnumSet< Category> definedCategories()
  {
    return _definedCategories;
  }

  public static Category getCategory( int categoryValue )
  {
    Category c = _lookup.get( categoryValue );

    return c == null ? UNDEFINED : c;
  }

  public static String concatenateValues( Set<Category> categories )
  {
    String result = "";

    for ( Category cat : categories )
    {
      result += cat.getCategoryValue() + ", ";
    }

    if ( result.endsWith( ", ") )
    {
      result = result.substring( 0, result.length() - 2 );
    }

    return result;
  }
}
