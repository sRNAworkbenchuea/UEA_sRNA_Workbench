/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.io.File;
import java.util.*;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;

/**
 *
 * @author w0445959
 */
public class CoLIDEParams extends ToolParameters
{
  public static final double ROOT_2_OVER_2 = 1.41421356/2.0;

  
  /**
     * An enumeration containing a list of definitions for each CoLIDE parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum SERIES_TYPE_ENUM
    {
      ORDERED,
      UNORDERED;
    }
    public enum CONFIDENCE_INTERVAL
    {
      MIN_MAX//This formula for only min max standard mode with replicates
      {
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
                                             ArrayList<ArrayList<String>> samples,
                                             double percentageNonReplicate )
        {
          buildConfidenceInterval(row, samples);
        }
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
        ArrayList<ArrayList<String>> samples)
        {
          int sampleNumber = 0;
          for ( ArrayList<String> replicateList : samples )
          {
            //System.out.println( "Current Sample: " + sampleNumber );
            sampleNumber++;
            double currentMin = Double.MAX_VALUE;
            double currentMax = Double.MIN_VALUE;
            for ( String key : replicateList )
            {
              ExpressionElement firstElement = row.get( key );
              
              
              if ( firstElement != null )//found an entry for this file so update Confidence Interval 
              {
                if(firstElement.normalisedAbundance > currentMax)
                  currentMax = firstElement.normalisedAbundance;
                else if(firstElement.normalisedAbundance < currentMin)
                  currentMin = firstElement.normalisedAbundance;
              }
            }
            for ( String key : replicateList )
            {
              ExpressionElement firstElement = row.get( key );
              
              
              if ( firstElement != null )//found an entry for this file so update Confidence Interval 
              {
                firstElement.confidenceInterval[0] = currentMin;
                firstElement.confidenceInterval[1] = currentMax;
              }
            }
          }
        }
        @Override
        public void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples )
        {
          boolean swapped = true;
          int j = 0;
          while ( swapped )
          {
            swapped = false;
            j++;

            for ( int i = 0; i < samples.size() - j; i++ )
            {
              ArrayList<String> replicateSet1 = samples.get( i );
              ArrayList<String> replicateSet2 = samples.get( i+1 );
              double firstNormalisedAbun = calculateMean(row, replicateSet1);
              double secondNormalisedAbun = calculateMean(row, replicateSet2);
      
              if (firstNormalisedAbun > secondNormalisedAbun) 
              {
                ArrayList<String> tmp = samples.set( i, samples.get( i +1 ) );
                samples.set( i + 1, tmp);
    
                swapped = true;
              }
            }
          }
        }
      },
      PERCENTAGE_CI//standard mode for non replicate data
      {
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
                                             ArrayList<ArrayList<String>> samples,
                                             double percentageNonReplicate )
        {
          for ( int i = 0; i < samples.size(); i++ )
          {

            String firstKey = samples.get( i ).get(0);

            ExpressionElement firstElement = row.get( firstKey );

            percentageNonReplicate /= 2.0;
            if ( firstElement != null )//found an entry for this file so update Confidence Interval 
            {
              double percentage =  (percentageNonReplicate/100.0);
              if ((percentage * firstElement.normalisedAbundance ) > 10.0 )
              {
                firstElement.confidenceInterval[0] = firstElement.normalisedAbundance * (1.0-percentage);
                firstElement.confidenceInterval[1] = firstElement.normalisedAbundance * (1.0+percentage);
              }
              else
              {
                firstElement.confidenceInterval[0] = firstElement.normalisedAbundance - 10;
                firstElement.confidenceInterval[1] = firstElement.normalisedAbundance + 10;
                if(firstElement.confidenceInterval[0] < 0)
                  firstElement.confidenceInterval[0] = 0.0;
              }
              
            }

          }
        }
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
        ArrayList<ArrayList<String>> samples)
        {
          
          throw new UnsupportedOperationException( "This method should not be called for this type of CI" );
        }
        @Override
        public void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples )
        {

          boolean swapped = true;
          int j = 0;
          while ( swapped )
          {
            swapped = false;
            j++;

            for ( int i = 0; i < samples.size() - j; i++ )
            {
              String firstKey = samples.get( i ).get( 0);
              String secondKey = samples.get( i + 1 ).get( 0 );
              ExpressionElement firstElement = row.get( firstKey );
              ExpressionElement secondElement = row.get( secondKey );
              double firstNormalisedAbun = (firstElement == null) ? 0.0 : firstElement.normalisedAbundance;
              double secondNormalisedAbun = (secondElement == null) ? 0.0 : secondElement.normalisedAbundance;
              
              if (firstNormalisedAbun > secondNormalisedAbun) 
              {
                //String tempKey = firstKey;
                
                ArrayList<String> tmp = samples.set( i, samples.get( i +1 ) );
                samples.set( i + 1, tmp);
    
                swapped = true;
              }
            }
          }

        }
        
      },
      P_M_1_SD
      {
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
                                             ArrayList<ArrayList<String>> samples,
                                             double percentageNonReplicate )
        {
          buildConfidenceInterval(row, samples);
        }
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
        ArrayList<ArrayList<String>> samples)
        {
          
          
          for ( ArrayList<String> replicateList : samples )
          {
            double[] meanSD = this.calculateMeanSD( row, replicateList );
            for ( String key : replicateList )
            {
              ExpressionElement firstElement = row.get( key );
              
              
              if ( firstElement != null )//found an entry for this file so update Confidence Interval 
              {
                firstElement.confidenceInterval[0] = meanSD[0]- meanSD[1];
                firstElement.confidenceInterval[1] = meanSD[0]+ meanSD[1];
              }
            }
          }
        }
        @Override
        public void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples )
        {
          boolean swapped = true;
          int j = 0;
          while ( swapped )
          {
            swapped = false;
            j++;

            for ( int i = 0; i < samples.size() - j; i++ )
            {
              ArrayList<String> replicateSet1 = samples.get( i );
              ArrayList<String> replicateSet2 = samples.get( i+1 );
              double firstNormalisedAbun = calculateMean(row, replicateSet1);
              double secondNormalisedAbun = calculateMean(row, replicateSet2);
              if (firstNormalisedAbun > secondNormalisedAbun) 
              {
                ArrayList<String> tmp = samples.set( i, samples.get( i +1 ) );
                samples.set( i + 1, tmp);
    
                swapped = true;
              }
            }
          }
        }
      },
      P_M_2_SD
      {
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
                                             ArrayList<ArrayList<String>> samples,
                                             double percentageNonReplicate )
        {
          buildConfidenceInterval(row, samples);
        }
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
        ArrayList<ArrayList<String>> samples)
        {
          for ( ArrayList<String> replicateList : samples )
          {
            double[] meanSD = this.calculateMeanSD( row, replicateList );
            for ( String key : replicateList )
            {
              ExpressionElement firstElement = row.get( key );


              if ( firstElement != null )//found an entry for this file so update Confidence Interval 
              {
                firstElement.confidenceInterval[0] = meanSD[0] - (2*meanSD[1]);
                firstElement.confidenceInterval[1] = meanSD[0] + (2*meanSD[1]);
              }
            }
          }
        }
        @Override
        public void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples )
        {
          boolean swapped = true;
          int j = 0;
          while ( swapped )
          {
            swapped = false;
            j++;

            for ( int i = 0; i < samples.size() - j; i++ )
            {
              ArrayList<String> replicateSet1 = samples.get( i );
              ArrayList<String> replicateSet2 = samples.get( i+1 );
              double firstNormalisedAbun = calculateMean(row, replicateSet1);
              double secondNormalisedAbun = calculateMean(row, replicateSet2);
              if (firstNormalisedAbun > secondNormalisedAbun) 
              {
                //String tempKey = firstKey;
                
                ArrayList<String> tmp = samples.set( i, samples.get( i +1 ) );
                samples.set( i + 1, tmp);
    
                swapped = true;
              }
            }
          }
        }
      },
      P_M_R_2_DEV2_X_SD
      {
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
                                             ArrayList<ArrayList<String>> samples,
                                             double percentageNonReplicate )
        {
          buildConfidenceInterval(row, samples);
        }
        @Override
        public void buildConfidenceInterval( HashMap<String, ExpressionElement> row,
        ArrayList<ArrayList<String>> samples)
        {
          for ( ArrayList<String> replicateList : samples )
          {
            double[] meanSD = this.calculateMeanSD( row, replicateList );
            for ( String key : replicateList )
            {
              ExpressionElement firstElement = row.get( key );


              if ( firstElement != null )//found an entry for this file so update Confidence Interval 
              {
                firstElement.confidenceInterval[0] = meanSD[0] - (ROOT_2_OVER_2 * meanSD[1]);
                firstElement.confidenceInterval[1] = meanSD[0] + (ROOT_2_OVER_2 * meanSD[1]);
              }
            }
          }
        }
        @Override
        public void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples )
        {
          boolean swapped = true;
          int j = 0;
          while ( swapped )
          {
            swapped = false;
            j++;

            for ( int i = 0; i < samples.size() - j; i++ )
            {
              ArrayList<String> replicateSet1 = samples.get( i );
              ArrayList<String> replicateSet2 = samples.get( i+1 );
              double firstNormalisedAbun = calculateMean(row, replicateSet1);
              double secondNormalisedAbun = calculateMean(row, replicateSet2);
             
              if (firstNormalisedAbun > secondNormalisedAbun) 
              {

                ArrayList<String> tmp = samples.set( i, samples.get( i +1 ) );
                samples.set( i + 1, tmp);
    
                swapped = true;
              }
            }
          }
        }
      }
      ;

      public abstract void buildConfidenceInterval(HashMap<String, ExpressionElement> row,
                                                   ArrayList<ArrayList<String>> samples);
      
      public abstract void buildConfidenceInterval(HashMap<String, ExpressionElement> row,
                                                   ArrayList<ArrayList<String>> samples,
                                                   double percentageNonReplicate);
      public abstract void sortRow( HashMap<String, ExpressionElement> row, ArrayList<ArrayList<String>> samples );
      
      public double[] calculateMeanSD( HashMap<String, ExpressionElement> row,
                                        ArrayList<String> fileNames )
      {
        double results[] = new double[2];
        /*
         * SAMPLE DISTRIBUTION:
         *
         * S = (SQRT(SUM((value-mean)^2)))/N
         *
         */
     
        double totalSamples = fileNames.size();
  
        results[0] = calculateMean(row, fileNames);//mean value
        double sum_value_minus_mean_sq = 0.0;
        
        for ( int i = 0; i < fileNames.size(); i++ )
        {

          String firstKey = fileNames.get( i );

          ExpressionElement firstElement = row.get( firstKey );

          if ( firstElement != null )//found an entry for this file so update Confidence Interval 
          {
             sum_value_minus_mean_sq += (firstElement.normalisedAbundance - results[0]) *
                                        (firstElement.normalisedAbundance - results[0]);
          }
          else
          {
            
             sum_value_minus_mean_sq += (0 - results[0]) *
                                        (0 - results[0]);
          
          }

        }
        
        results[1] = (Math.sqrt ( sum_value_minus_mean_sq ) )/totalSamples;


        return results;
      }
      public double calculateMean(HashMap<String, ExpressionElement> row,
                                        ArrayList<String> fileNames)
      {
        double totalSamples = fileNames.size();
        double totalAbundance = 0.0;
        
        for ( int i = 0; i < fileNames.size(); i++ )
        {

          String firstKey = fileNames.get( i );

          ExpressionElement firstElement = row.get( firstKey );

          if ( firstElement != null )//found an entry for this file so update Confidence Interval 
          {
             totalAbundance += firstElement.normalisedAbundance;
          }

        }
        return totalAbundance / totalSamples;//mean value
      }
    }

    public enum Definition
    {
        MINIMUM_LENGTH              ("min_length",          Integer.valueOf(16),      Integer.valueOf(16),    Integer.valueOf(49)),
        MAXIMUM_LENGTH              ("max_length",          Integer.valueOf(49),      Integer.valueOf(17),    Integer.valueOf(50)),
        PERCENTAGE_CI_VALUE         ("percentage_ci_value", Double.valueOf(10.0),     Double.valueOf(0.0),   Double.valueOf(10.0)),
        SERIES_TYPE                 ("series_type",         SERIES_TYPE_ENUM.ORDERED),
        REPLICATE                   ("replicate",           Boolean.FALSE),
        SIZE_VALUE_1                ("size_val_1",          Integer.valueOf(21),      Integer.valueOf(10),    Integer.valueOf(Integer.MAX_VALUE)),
        SIZE_VALUE_2                ("size_val_2",          Integer.valueOf(22),      Integer.valueOf(10),    Integer.valueOf(Integer.MAX_VALUE)),
        SIZE_VALUE_3                ("size_val_3",          Integer.valueOf(23),      Integer.valueOf(10),    Integer.valueOf(Integer.MAX_VALUE)),
        SIZE_VALUE_4                ("size_val_4",          Integer.valueOf(24),      Integer.valueOf(10),    Integer.valueOf(Integer.MAX_VALUE)),
        OFFSET_CHI_SQ               ("offset_chi_sq",       Double.valueOf(10.0),      Double.valueOf(10.0),    Double.valueOf(50.0)),
        OVERLAP_PERCENTAGE          ("overlap_perc",        Double.valueOf(50.0),     Double.valueOf(0.0),    Double.valueOf(100.0)),
        CONF_INTER                  ("conf_inter",          CONFIDENCE_INTERVAL.MIN_MAX), 
        MAX_CHUNK                   ("max_chunk",           Integer.valueOf(1000),    Integer.valueOf(10),    Integer.valueOf(Integer.MAX_VALUE)),
        MIN_CHUNK                   ("min_chunk",           Integer.valueOf(200),     Integer.valueOf(10),    Integer.valueOf(500)),
        THRESHOLD                   ("threshold",           Double.valueOf(-3.0),     Double.valueOf(-5.0),      Double.valueOf(-2.0));
        
        private ParameterDefinition definition;

        private Definition(String name, Boolean default_value)                                  {this.definition = new ParameterDefinition<Boolean>( name, default_value );}
        private Definition(String name, String default_value)                                   {this.definition = new ParameterDefinition<String>( name, default_value );}
        private Definition(String name, File default_value)                                     {this.definition = new ParameterDefinition<File>( name, default_value );}
        private Definition(String name, Integer default_value, Integer lower, Integer upper)    {this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper );}
        private Definition(String name, Float default_value, Float lower, Float upper)          {this.definition = new ParameterDefinition<Float>( name, default_value, lower, upper );}
        private Definition(String name, Double default_value, Double lower, Double upper)       {this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper );}
        private Definition(String name, SERIES_TYPE_ENUM default_series)                        {this.definition = new ParameterDefinition<SERIES_TYPE_ENUM>( name,  default_series);}
        private Definition(String name, CONFIDENCE_INTERVAL default_series)                     {this.definition = new ParameterDefinition<CONFIDENCE_INTERVAL>( name,  default_series);}
        
        // ***** Shortcuts to defaults and limits *****
        public String   getName()                       {return this.definition.getName();}
        public <T> T    getDefault(Class<T> type)       {return type.cast(this.definition.getDefaultValue());}
        public <T> T    getLowerLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getLower());}
        public <T> T    getUpperLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getUpper());}
        
        public ParameterDefinition getDefinition()      {return this.definition;}
    }
//
//
//    /**
//     * Default constructor.  Produces a CoLIDEParams instance with default parameters.
//     */
    public CoLIDEParams()
    {
        this(new Builder());
    }
//
    /**
     * Assignment constructor using the Builder design pattern.  Should a client
     * wish to use this constructor directly, they are required to pass a CoLIDEParams.Builder
     * object as an argument, which they can use to pick and choose parameters that
     * are not default.
     * All other constructors must go through this constructor.
     * @param builder A FilterParams.Builder object containing the FilterParams to
     * use.
     */
    private CoLIDEParams(Builder builder)
    {
        setLengthRange(builder.getMinLength(), builder.getMaxLength());
        setSeriesType( builder.getSeriesType() );
        setReplicate(builder.getReplicate());
        setConfInter(builder.getConfInter());
        setPercentageOverlapValue(builder.getOverlapPercentage());
        setPercentageCIValue(builder.getPercentageCIValue());
        setMinChunk(builder.getMinChunk());
        setMaxChunk(builder.getMaxChunk());
        setThreshold(builder.getThreshold());
        List<Integer> values = new ArrayList<Integer>(4);
        values.add( builder.getSizeValue1() );
        values.add( builder.getSizeValue2() );
        values.add( builder.getSizeValue3() );
        values.add( builder.getSizeValue4() );
        setSizeClassRanges(values);
        setOffsetChiSq(builder.getOffsetChiSq());

    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int                   getMinLength()              {return getParameterValue(Integer.class, Definition.MINIMUM_LENGTH.getName());}
    public int                   getMaxLength()              {return getParameterValue(Integer.class, Definition.MAXIMUM_LENGTH.getName());}
    public SERIES_TYPE_ENUM      getSeriesType()             {return getParameterValue(SERIES_TYPE_ENUM.class, Definition.SERIES_TYPE.getName());}
    public boolean               getReplicate()              {return getParameterValue(Boolean.class, Definition.REPLICATE.getName());}
    public CONFIDENCE_INTERVAL   getConfInter()              {return getParameterValue(CONFIDENCE_INTERVAL.class, Definition.CONF_INTER.getName());}
    public double                getPercentageOverlapValue() {return getParameterValue(Double.class, Definition.OVERLAP_PERCENTAGE.getName());}
    public double                getPercentageCIValue()      {return getParameterValue(Double.class, Definition.PERCENTAGE_CI_VALUE.getName());}
    public int                   getMaxChunk()               {return getParameterValue(Integer.class, Definition.MAX_CHUNK.getName());}
    public int                   getMinChunk()               {return getParameterValue(Integer.class, Definition.MIN_CHUNK.getName());}
    public double                getThreshold()              {return getParameterValue(Double.class, Definition.THRESHOLD.getName()); }
    public int                   getSizeValue1()             {return getParameterValue(Integer.class, Definition.SIZE_VALUE_1.getName());}
    public int                   getSizeValue2()             {return getParameterValue(Integer.class, Definition.SIZE_VALUE_2.getName());}
    public int                   getSizeValue3()             {return getParameterValue(Integer.class, Definition.SIZE_VALUE_3.getName());}
    public int                   getSizeValue4()             {return getParameterValue(Integer.class, Definition.SIZE_VALUE_4.getName());}
    public double                getOffsetChiSq()            {return getParameterValue(Double.class, Definition.OFFSET_CHI_SQ.getName());}
//
//    // **** Setters ****
    private void setMinLength(int min_length)                       {setParam(Definition.MINIMUM_LENGTH, min_length);}
    private void setMaxLength(int max_length)                       {setParam(Definition.MAXIMUM_LENGTH, max_length);}
    public  void setSeriesType(SERIES_TYPE_ENUM seriesType)         {setParam(Definition.SERIES_TYPE, seriesType);}
    public  void setReplicate(boolean replicate)                    {setParam(Definition.REPLICATE, replicate);}
    public  void setConfInter(CONFIDENCE_INTERVAL confInter)        {setParam(Definition.CONF_INTER, confInter);}
    public  void setPercentageCIValue(double perc)                  {setParam(Definition.PERCENTAGE_CI_VALUE, perc);}
    public  void setPercentageOverlapValue(double overlapPerc)      {setParam(Definition.OVERLAP_PERCENTAGE, overlapPerc);}
    public  void setMinChunk(int minChunk)                          {setParam(Definition.MIN_CHUNK, minChunk);}
    public  void setMaxChunk(int maxChunk)                          {setParam(Definition.MAX_CHUNK, maxChunk);}
    public  void setThreshold(double threshold)                     {setParam(Definition.THRESHOLD, threshold);}
    public  void setSizeValue1(int value)                           {setParam(Definition.SIZE_VALUE_1, value);}
    public  void setSizeValue2(int value)                           {setParam(Definition.SIZE_VALUE_2, value);}
    public  void setSizeValue3(int value)                           {setParam(Definition.SIZE_VALUE_3, value);}
    public  void setSizeValue4(int value)                           {setParam(Definition.SIZE_VALUE_4, value);}
    public  void setOffsetChiSq(double value)                       {setParam(Definition.OFFSET_CHI_SQ, value);}
    
    public void setLengthRange(int min_length, int max_length)
    {
        if (min_length > max_length)
        {
            throw new IllegalArgumentException("Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinLength(min_length);
        setMaxLength(max_length);
    }
    public void setSizeClassRanges(List<Integer> values)
    {


      Collections.sort(values);
      
      Set<Integer> set = new HashSet<Integer>(values);

      if(set.size() != values.size())
        throw new IllegalArgumentException("Illegal size class range cannot contain duplicate values.");
      
      setSizeValue1(values.get( 0 ) );
      setSizeValue2(values.get( 1 ));
      setSizeValue3(values.get( 2 ));
      setSizeValue4(values.get( 3 ));
    }


    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at CoLIDEParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int min_length                 = Definition.MINIMUM_LENGTH.getDefault(Integer.class);
        private int max_length                 = Definition.MAXIMUM_LENGTH.getDefault(Integer.class);
        private SERIES_TYPE_ENUM series_type   = Definition.SERIES_TYPE.getDefault(SERIES_TYPE_ENUM.class);
        private boolean replicate              = Definition.REPLICATE.getDefault( Boolean.class);
        private CONFIDENCE_INTERVAL confInter  = Definition.CONF_INTER.getDefault(CONFIDENCE_INTERVAL.class);
        private double percentage_ci_value     = Definition.PERCENTAGE_CI_VALUE.getDefault(Double.class);
        private double overlapPercentage       = Definition.OVERLAP_PERCENTAGE.getDefault( Double.class );
        private int minChunk                   = Definition.MIN_CHUNK.getDefault( Integer.class);
        private int maxChunk                   = Definition.MAX_CHUNK.getDefault( Integer.class);
        private double threshold               = Definition.THRESHOLD.getDefault( Double.class );
        private int size_value_1               = Definition.SIZE_VALUE_1.getDefault( Integer.class);
        private int size_value_2               = Definition.SIZE_VALUE_2.getDefault( Integer.class);
        private int size_value_3               = Definition.SIZE_VALUE_3.getDefault( Integer.class);
        private int size_value_4               = Definition.SIZE_VALUE_4.getDefault( Integer.class);
        private double offset_chi_sq           = Definition.OFFSET_CHI_SQ.getDefault( Double.class);

        // **** Getters ****
        public int                   getMinLength()              {return min_length;}
        public int                   getMaxLength()              {return max_length;}
        public SERIES_TYPE_ENUM      getSeriesType()             {return series_type;}
        public CONFIDENCE_INTERVAL   getConfInter()              {return confInter;}
        public boolean               getReplicate()              {return replicate;}
        public double                getPercentageCIValue()      {return percentage_ci_value;}
        public double                getOverlapPercentage()      {return overlapPercentage;}
        public int                   getMinChunk()               {return minChunk;}
        public int                   getMaxChunk()               {return maxChunk;}
        public double                getThreshold()              {return threshold;}
        public int                   getSizeValue1()             {return size_value_1;}
        public int                   getSizeValue2()             {return size_value_2;}
        public int                   getSizeValue3()             {return size_value_3;}
        public int                   getSizeValue4()             {return size_value_4;}
        public double                getOffsetChiSq()            {return offset_chi_sq;}

        // **** Setters ****
        public Builder setMinLength(int min_length)                    {this.min_length = min_length;   return this;}
        public Builder setMaxLength(int max_length)                    {this.max_length = max_length;   return this;}
        public Builder setSeriesType(SERIES_TYPE_ENUM series_type)     {this.series_type = series_type; return this;}
        public Builder setReplicate(boolean replicate)                 {this.replicate = replicate;     return this;}
        public Builder setConfInter(CONFIDENCE_INTERVAL confInter)     {this.confInter = confInter;     return this;}
        public Builder setPercentageCIValue(double perc)               {this.percentage_ci_value = perc; return this;}
        public Builder setOverlapPercentage(double overlapPercentage)  {this.overlapPercentage = overlapPercentage;   return this;}
        public Builder setMinChunk(int minChunk)                       {this.minChunk = minChunk;   return this;}
        public Builder setMaxChunk(int maxChunk)                       {this.maxChunk = maxChunk;   return this;}
        public Builder setThreshold(double threshold)                  {this.threshold = threshold; return this;}
        public Builder setSizeValue1(int value)                        {this.size_value_1 = value; return this;}
        public Builder setSizeValue2(int value)                        {this.size_value_2 = value; return this;}
        public Builder setSizeValue3(int value)                        {this.size_value_3 = value; return this;}
        public Builder setSizeValue4(int value)                        {this.size_value_4 = value; return this;}
        public Builder setOffsetChiSq(double value)                    {this.offset_chi_sq = value; return this;}


        /**
         * Constructs the CoLIDEParams object
         * @return
         */
        public CoLIDEParams build()
        {
          return new CoLIDEParams( this );
        }
    }
}
