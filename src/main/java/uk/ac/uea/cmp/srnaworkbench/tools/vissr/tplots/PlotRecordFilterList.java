/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;

/**
 *
 * @author prb07qmu
 */
public class PlotRecordFilterList
{
  private final List< PlotRecordFilterI > _filters = CollectionUtils.newArrayList();
  private final JPanel _panel;

  public PlotRecordFilterList()
  {
    _filters.add( createDegradomeHitAbundanceFilter() );
    _filters.add( createSmallRNAHitAbundanceFilter() );
    _filters.add( createDegradomeHitCountFilter() );
    _filters.add( createSmallRNAHitCountFilter() );
    _filters.add( createSmallRNAAlignmentScoreFilter() );
    _filters.add( createPValueFilter() );
    _filters.add( createKeywordFilter() );
    _filters.add( createIncludeMirbaseMatchesFilter() );

    _panel = createPanel();
  }

  public boolean doFiltering( Component parent, PlotRecordCollection allPlotRecords, PlotRecordCollection filteredPlotRecords )
  {
    int rtc = JOptionPane.showConfirmDialog( parent,
      _panel,
      "PARESnip results filter",
      JOptionPane.OK_CANCEL_OPTION );

    if ( rtc != JOptionPane.OK_OPTION )
      return false;

    for ( Category cat : Category.definedCategories() )
    {
      List< PlotRecord > l = allPlotRecords.getPlotRecordsForCategory( cat );

      for ( PlotRecord pr : l )
      {
        if ( include( pr ) )
        {
          filteredPlotRecords.add( pr );
        }
      }
    }

    filteredPlotRecords.createIndex();

    return true;
  }

  private JPanel createPanel()
  {
    // Create the panel for the JOptionPane
    //
    JPanel panel = new JPanel();
    panel.setLayout( new GridLayout( _filters.size(), 2, 10, 10 ) );

    for ( PlotRecordFilterI f : _filters )
    {
      JLabel lblDescription = new JLabel( f.getDescription() + "  " + f.getOperator(), SwingConstants.RIGHT );
      JComponent ui         = f.getComponent();

      panel.add( lblDescription );
      panel.add( ui );
    }

    return panel;
  }

  private boolean include( PlotRecord pr )
  {
    for ( PlotRecordFilterI f : _filters )
    {
      if ( ! f.include( pr ) )
        return false;
    }

    return true;
  }

  /*
   * Static stuff **************************************************************
   */

  private static final DefaultFormatterFactory DECIMAL_FORMATTER_FACTORY;
  private static final DefaultFormatterFactory INTEGER_FORMATTER_FACTORY;

  static
  {
    Pattern decimalPattern = Pattern.compile( "[0-9]*(\\.)*([0-9])*" );
    RegexPatternFormatter decimalRegexFormatter = new RegexPatternFormatter( decimalPattern );
    decimalRegexFormatter.setAllowsInvalid( false );

    DECIMAL_FORMATTER_FACTORY = new DefaultFormatterFactory( decimalRegexFormatter );

    Pattern integerPattern = Pattern.compile( "[0-9]*" );
    RegexPatternFormatter integerRegexFormatter = new RegexPatternFormatter( integerPattern );
    integerRegexFormatter.setAllowsInvalid( false );

    INTEGER_FORMATTER_FACTORY = new DefaultFormatterFactory( integerRegexFormatter );
  }

  private static PlotRecordFilterI createDegradomeHitAbundanceFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "Max. degradome hit abundance", ">=", true, 0 )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          return pr.getMaxDegradomeHitAbundance() >= getFloatValue();
        }
      };

    return f;
  }

  private static PlotRecordFilterI createSmallRNAHitAbundanceFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "sRNA hit abundance", ">=", true, 0 )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          for ( PlotRecord.SmallRNAHit sh : pr.getSmallRNAHits() )
          {
            if ( sh.getAbundance() >= getFloatValue() )
              return true;
          }

          return false;
        }
      };

    return f;
  }

  private static PlotRecordFilterI createDegradomeHitCountFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "Number of degradome hits", ">=", true, 0 )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          return pr.getDegradomeHits().size() >= getFloatValue();
        }
      };

    return f;
  }

  private static PlotRecordFilterI createSmallRNAHitCountFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "Number of sRNA hits", ">=", false, 0 )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          return pr.getSmallRNAHits().size() >= getFloatValue();
        }
      };

    return f;
  }

  private static PlotRecordFilterI createSmallRNAAlignmentScoreFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "Alignment score", "<=", true, 4 )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          float alignmentScoreFilter = getFloatValue();

          for ( PlotRecord.SmallRNAHit sh : pr.getSmallRNAHits() )
          {
            if ( Float.compare( sh.getScore(), alignmentScoreFilter ) <= 0 )
              return true;
          }

          return false;
        }
      };

    return f;
  }

  private static PlotRecordFilterI createPValueFilter()
  {
    PlotRecordFilterI f = new NumericFilter( "p-value", "<=", true, 0.04f )
      {
        @Override
        public boolean include( PlotRecord pr )
        {
          float pValueFilter = getFloatValue();

          // If one of the sRNA hits is OK then the whole PlotRecord is OK
          for ( PlotRecord.SmallRNAHit sh : pr.getSmallRNAHits() )
          {
            if ( 0 <= sh.getPValue() && sh.getPValue() <= pValueFilter )
              return true;
          }

          return false;
        }
      };

    return f;
  }

  private static PlotRecordFilterI createKeywordFilter()
  {
    PlotRecordFilterI f = new PlotRecordFilterI()
    {
      private String _value = "";

      @Override
      public String getDescription()
      {
        return "Key-word";
      }

      @Override
      public String getOperator()
      {
        return "";
      }

      @Override
      public JComponent getComponent()
      {
        final JTextField tf = new JTextField( _value );

        tf.addKeyListener( new KeyListener()
        {
          @Override
          public void keyTyped( KeyEvent e )
          {
            setFilterValue( tf.getText() );
          }

          @Override
          public void keyPressed( KeyEvent e ) {}

          @Override
          public void keyReleased( KeyEvent e ) {}
        } );

        return tf;
      }

      @Override
      public Object getFilterValue()
      {
        return _value;
      }

      @Override
      public void setFilterValue( Object value )
      {
        if ( value == null )
          return;

        _value = value.toString();
      }

      @Override
      public boolean include( PlotRecord pr )
      {
        if ( _value.isEmpty() )
          return true;

        return pr.containsSearchKey( _value );
      }
    };

    return f;
  }

  private static PlotRecordFilterI createIncludeMirbaseMatchesFilter()
  {
    PlotRecordFilterI f = new BooleanFilter( "Include exact miRBase matches", true )
    {
      @Override
      public boolean include( PlotRecord pr )
      {
        // If we're including mirbase matches then we're including everything
        if ( getBooleanValue() )
          return true;

        boolean hasMirbaseMatch = false;

        for ( PlotRecord.SmallRNAHit sh : pr.getSmallRNAHits() )
        {
          if ( ! sh.getMirbaseId().isEmpty() )
          {
            hasMirbaseMatch = true;
            break;
          }
        }

        return ! hasMirbaseMatch;
      }
    };

    return f;
  }


  private static JComponent createNumericTextField( final PlotRecordFilterI prf, boolean allowDecimals )
  {
    final JFormattedTextField ftf = new JFormattedTextField();

    ftf.setFormatterFactory( allowDecimals ? DECIMAL_FORMATTER_FACTORY : INTEGER_FORMATTER_FACTORY );

    if ( allowDecimals )
    {
      ftf.setText( prf.getFilterValue().toString() );
    }
    else
    {
      // Need to do this to avoid the decimal point causing the 'bing' sound which is played
      // when an invalid character is input into the text field.
      int value = (int)StringUtils.safeFloatParse( prf.getFilterValue().toString(), 0 );
      ftf.setText( "" + value );
    }

    ftf.addFocusListener( new FocusListener()
    {
      @Override public void focusGained( FocusEvent e )
      {
        ftf.select( 0, Integer.MAX_VALUE );
      }

      @Override
      public void focusLost( FocusEvent e )
      {
        try
        {
          float f = Float.parseFloat( ftf.getText() );
          prf.setFilterValue( f );
        }
        catch ( NumberFormatException ex )
        {
          prf.setFilterValue( null );
        }
      }
    } );

    return ftf;
  }


  /**
   * Helper class for numeric filters.
   */
  private abstract static class NumericFilter implements PlotRecordFilterI
  {
    private final String _description;
    private final String _operator;
    private final boolean _allowDecimals;

    private float _value = 0;

    NumericFilter( String description, String operator, boolean allowDecimals, float initialValue )
    {
      _description = description;
      _operator = operator;
      _allowDecimals = allowDecimals;

      _value = initialValue;
    }

    @Override
    public String getDescription()
    {
      return _description;
    }

    @Override
    public String getOperator()
    {
      return _operator;
    }

    @Override
    public JComponent getComponent()
    {
      return createNumericTextField( this, _allowDecimals );
    }

    @Override
    public void setFilterValue( Object value )
    {
      if ( value instanceof Number )
      {
        _value = ((Number)value).floatValue();
      }
    }

    @Override
    public Object getFilterValue()
    {
      return new Float( _value );
    }

    public float getFloatValue()
    {
      return _value;
    }
  }


  /**
   * Helper class for boolean filters.
   */
  private abstract static class BooleanFilter implements PlotRecordFilterI
  {
    private final String _description;

    private boolean _value = true;

    BooleanFilter( String description, boolean initialValue )
    {
      _description = description;

      _value = initialValue;
    }

    @Override
    public String getDescription()
    {
      return _description;
    }

    @Override
    public String getOperator()
    {
      return "";
    }

    @Override
    public JComponent getComponent()
    {
      final JCheckBox chk = new JCheckBox( "", _value );
      chk.setSelected( _value );

      chk.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          _value = chk.isSelected();
        }
      } );

      return chk;
    }

    @Override
    public void setFilterValue( Object value )
    {
      if ( value instanceof Boolean )
      {
        _value = ((Boolean)value).booleanValue();
      }
    }

    @Override
    public Object getFilterValue()
    {
      return Boolean.valueOf( _value );
    }

    public boolean getBooleanValue()
    {
      return _value;
    }
  }
}
