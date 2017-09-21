
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import javax.swing.JTable;

/**
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 *
 * @deprecated Searching the results table takes place in Degradome.java
 */
public class WordSearch
{
  private int start;
  private String lastSearchedFor;
  private JTable table;

  /**
   * Create a new instance.
   * @param t The JTable to be searched.
   */
  public WordSearch( JTable t )
  {
    table = t;
  }

  /**
   * Search for the given string highlighting the search result if found.
   * @param text The text to search for.
   */
  public void search( String text )
  {
    System.out.println( "Search for: " + text );

    if ( Data.IS_TABLE_MODE && table != null )
    {
      if ( !text.equalsIgnoreCase( lastSearchedFor ) )
      {
        start = 0;
      }

      lastSearchedFor = text;
      int columns = table.getModel().getColumnCount();
      int rows = table.getModel().getRowCount();

      for ( int row = start; row < rows; row++ )
      {
        for ( int column = 0; column < columns; column++ )
        {
          String content = String.valueOf( table.getModel().getValueAt( row, column ) );

          if ( content.contains( text ) || content.equals( text ) )
          {
            System.out.println( "Cell content: " + content );
            table.scrollRectToVisible( table.getCellRect( row, column, true ) );
            table.getSelectionModel().setSelectionInterval( row, row );
            start = row + 1;
            return;
          }
        }
      }

      for ( int row = 0; row < start; row++ )
      {
        for ( int column = 0; column < columns; column++ )
        {
          String content = String.valueOf( table.getModel().getValueAt( row, column ) );

          if ( content.contains( text ) || content.equals( text ) )
          {
            System.out.println( "Cell content: " + content );
            table.scrollRectToVisible( table.getCellRect( row, column, true ) );
            table.getSelectionModel().setSelectionInterval( row, row );
            start = row + 1;
            return;
          }
        }
      }

    }
  }
}
