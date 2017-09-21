/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Subclass of {@link JComboBox} which automatically sizes the popup to be greater
 * than the width of the longest element in the combo.
 *
 * @author prb07qmu
 */
public class WideJComboBox<E extends Object> extends javax.swing.JComboBox
{
  private boolean _isLayingOut = false;

  /**
   * Calls super-class constructor (see {@link JComboBox#JComboBox()})
   */
  public WideJComboBox()
  {
    super();
  }

  /**
   * Calls super-class constructor (see {@link JComboBox#JComboBox(java.util.Vector)})
   */
  public WideJComboBox( @SuppressWarnings( "UseOfObsoleteCollectionType" ) java.util.Vector<E> items )
  {
    super( items );
  }

  /**
   * Calls super-class constructor (see {@link JComboBox#JComboBox(Object[])})
   */
  public WideJComboBox( E[] items )
  {
    super( items );
  }

  /**
   * Calls super-class constructor (see {@link JComboBox#JComboBox(ComboBoxModel)})
   */
  public WideJComboBox( ComboBoxModel aModel )
  {
    super( aModel );
  }

  /**
   * {@inheritDoc}
   *
   * When the component is not being laid out the width will be slightly greater
   * than the widest element in the list so that the popup can be sized accordingly.
   */
  @Override
  public Dimension getSize()
  {
    Dimension dim = super.getSize();

    if ( _isLayingOut )
    {
      // Combo is being laid out so leave the dimension alone.
      // If we changed it here then its size on the parent frame would change.
    }
    else
    {
      /*
       * When we're here a request has been made for the size before showing the popup.
       * So return a size where the width is greater then the widest item in the list.
       * The width is recalculated each time this method is called even when the list
       * has not changed. This may seem slightly unnecessary but it is simpler than
       * the alternative.
       *
       * i.e. Use the ListDataListener interface and its implementation in JComboBox
       * to set a 'recalculate popup width' flag. However the documentation in JComboBox
       * says not to override the interface's method. Adding a listener directly to the
       * model would mean having to track changes to the data model.
       * These problems are avoided by this simple approach.
       */

      // add a bit to account for the possible presence of a vertical scroll-bar
      int requiredWidth = 25 + getWidestItemWidth();

      dim.width = Math.max( requiredWidth, dim.width );
    }

    return dim;
  }

  /**
   * Calculate the width of the widest item in the combo's list
   *
   * @return the greatest width
   */
  private int getWidestItemWidth()
  {
    FontMetrics fm = this.getFontMetrics( this.getFont() );

    int widest = 0;

    for ( int i = 0, num = this.getItemCount(); i < num; i++ )
    {
      Object o = this.getItemAt( i );

      if ( o == null )
        continue;

      widest = Math.max( widest, fm.stringWidth( o.toString() ) );
    }

    return widest;
  }

  @Override
  public void doLayout()
  {
    try
    {
      _isLayingOut = true;
      super.doLayout();
    }
    finally
    {
      _isLayingOut = false;
    }
  }

  public static void main( String[] args )
  {
    String title = "Combo Test";
    javax.swing.JFrame frame = new javax.swing.JFrame( title );
    frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );

    String[] items =
    {
      "I need lot of width to be visible , oh am I visible now",
      "I need lot of width to be visible , oh am I visible now",
      "hello",
      "hello again",
      "hello, again !"
    };

    final WideJComboBox<String> simpleCombo = new WideJComboBox<String>( items );

    simpleCombo.setMaximumRowCount( 4 );
    //simpleCombo.setMaximumRowCount( 24 );

    simpleCombo.setPreferredSize( new Dimension( 180, 20 ) );
    javax.swing.JLabel label = new javax.swing.JLabel( "Wider Drop Down Demo" );

    javax.swing.JButton btn = new javax.swing.JButton( "Add 1");
    btn.addActionListener( new ActionListener()
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        simpleCombo.addItem( "new item ------------------------------------------------------------------" );
      }
    } );

    frame.getContentPane().add( simpleCombo, BorderLayout.NORTH );
    frame.getContentPane().add( btn );
    frame.getContentPane().add( label, BorderLayout.SOUTH );

    int width = 200;
    int height = 150;
    frame.setSize( width, height );
    frame.setVisible( true );
  }
}
