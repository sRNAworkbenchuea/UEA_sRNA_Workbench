/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.util.*;
import javax.swing.JOptionPane;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeI;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.StripedSolidRectGlyph;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows.SequenceWindowHelper;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 *
 * @author w0445959
 */
public class LocusUtils
{
  public static ArrayList<ArrayList<ExpressionElement>> getLociInRange( ArrayList<ArrayList<ExpressionElement>> allLoci, int start, int end )
  {
    ArrayList<ArrayList<ExpressionElement>> newChunk = new ArrayList<ArrayList<ExpressionElement>>();
    
    Comparator<ArrayList<ExpressionElement>> cStart = new Comparator<ArrayList<ExpressionElement>>() {
      @Override
      public int compare(ArrayList<ExpressionElement> u1, ArrayList<ExpressionElement> u2) {
        //return u1.compareTo(u2.getId());
        int u1MinCoord = Integer.MAX_VALUE;
        int u1MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u1 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u1MinCoord )
          {
            u1MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u1MaxCoord )
          {
            u1MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        int u2MinCoord = Integer.MAX_VALUE;
        int u2MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u2 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u2MinCoord )
          {
            u2MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u2MaxCoord )
          {
            u2MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        Integer start1 = new Integer(u1MinCoord );
        Integer start2 = new Integer(u2MinCoord);
        return start1.compareTo( start2 );
      }
    };
    Comparator<ArrayList<ExpressionElement>> cEnd = new Comparator<ArrayList<ExpressionElement>>() {
      @Override
      public int compare(ArrayList<ExpressionElement> u1, ArrayList<ExpressionElement> u2) {
        //return u1.compareTo(u2.getId());
            int u1MinCoord = Integer.MAX_VALUE;
        int u1MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u1 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u1MinCoord )
          {
            u1MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u1MaxCoord )
          {
            u1MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        int u2MinCoord = Integer.MAX_VALUE;
        int u2MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u2 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u2MinCoord )
          {
            u2MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u2MaxCoord )
          {
            u2MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        Integer start1 = new Integer(u1MaxCoord );
        Integer start2 = new Integer(u2MaxCoord);
        return start1.compareTo( start2 );
      }
    };
    
    ExpressionElement findMeEE = new ExpressionElement();
    findMeEE.startCoord.add( start);
    findMeEE.endCoord.add( end);
    ArrayList<ExpressionElement> findMe = new ArrayList<ExpressionElement>();
    findMe.add( findMeEE );
    int index = Collections.binarySearch(allLoci, findMe, cStart);
    //System.out.println(index); 
    
    int endIndex = Collections.binarySearch(allLoci, findMe, cEnd);
    //System.out.println(Math.abs( endIndex )); 

    int startInsertionPoint = Math.abs( index );
    int endInsertionPoint = Math.abs( endIndex );
    for(int valueIndex = Math.max(0 ,startInsertionPoint-2) ; valueIndex <  Math.min( allLoci.size(), endInsertionPoint+1) ; valueIndex++ )
    {
     
      newChunk.add( allLoci.get(valueIndex) );
    }
    
    return newChunk;
  }
  
  public static ArrayList<Patman> getPatmanLociInRange( ArrayList<Patman> allLoci, int start, int end )
  {
    ArrayList<Patman> newChunk = new ArrayList<Patman>();
    
    Comparator<Patman> cStart = new Comparator<Patman>() {
      @Override
      public int compare(Patman u1, Patman u2) {
        //return u1.compareTo(u2.getId());
        int u1MinCoord = Integer.MAX_VALUE;
        int u1MaxCoord = Integer.MIN_VALUE;
        for ( PatmanEntry flankingElement : u1 )
        {
          if ( flankingElement.getStart() < u1MinCoord )
          {
            u1MinCoord = flankingElement.getStart();
          }
          if ( flankingElement.getEnd() > u1MaxCoord )
          {
            u1MaxCoord = flankingElement.getEnd();
          }
          

        }
        int u2MinCoord = Integer.MAX_VALUE;
        int u2MaxCoord = Integer.MIN_VALUE;
        for ( PatmanEntry flankingElement : u2 )
        {
          if ( flankingElement.getStart() < u2MinCoord )
          {
            u2MinCoord = flankingElement.getStart();
          }
          if ( flankingElement.getEnd() > u2MaxCoord )
          {
            u2MaxCoord = flankingElement.getEnd();
          }
          

        }
        Integer start1 = new Integer(u1MinCoord );
        Integer start2 = new Integer(u2MinCoord);
        return start1.compareTo( start2 );
      }
    };
    Comparator<Patman> cEnd = new Comparator<Patman>() {
      @Override
      public int compare(Patman u1, Patman u2) {
        //return u1.compareTo(u2.getId());
            int u1MinCoord = Integer.MAX_VALUE;
        int u1MaxCoord = Integer.MIN_VALUE;
        for ( PatmanEntry flankingElement : u1 )
        {
          if ( flankingElement.getStart() < u1MinCoord )
          {
            u1MinCoord = flankingElement.getStart();
          }
          if ( flankingElement.getEnd() > u1MaxCoord )
          {
            u1MaxCoord = flankingElement.getEnd();
          }
          

        }
        int u2MinCoord = Integer.MAX_VALUE;
        int u2MaxCoord = Integer.MIN_VALUE;
        for ( PatmanEntry flankingElement : u2 )
        {
          if ( flankingElement.getStart() < u2MinCoord )
          {
            u2MinCoord = flankingElement.getStart();
          }
          if ( flankingElement.getEnd() > u2MaxCoord )
          {
            u2MaxCoord = flankingElement.getEnd();
          }
          

        }
        Integer start1 = new Integer(u1MaxCoord );
        Integer start2 = new Integer(u2MaxCoord);
        return start1.compareTo( start2 );
      }
    };
    
    PatmanEntry findMeEE = new PatmanEntry(start, end);
    
    Patman findMe = new Patman();
    findMe.add( findMeEE );
    int index = Collections.binarySearch(allLoci, findMe, cStart);
    //System.out.println(index); 
    
    int endIndex = Collections.binarySearch(allLoci, findMe, cEnd);
    //System.out.println(Math.abs( endIndex )); 

    int startInsertionPoint = Math.abs( index );
    int endInsertionPoint = Math.abs( endIndex );
    for(int valueIndex = Math.max(0 ,startInsertionPoint-2) ; valueIndex <  Math.min( allLoci.size(), endInsertionPoint+1) ; valueIndex++ )
    {
     
      newChunk.add( allLoci.get(valueIndex) );
    }
    
    return newChunk;
  }
  public static SequenceVizMainFrame setupAndDisplayExpressionElementAggregate(SparseExpressionMatrix myExpressionMatrix, boolean normalised,
                                                                    int start, int stop,
                                                                    ArrayList<ArrayList<ExpressionElement>> lociForChromo)
  {
    
    ArrayList<Integer> lengthRanges = myExpressionMatrix.getLengthRanges();
    SequenceVizMainFrame newSeqVissr = SequenceVizMainFrame.createVisSRInstance( myExpressionMatrix.getGenomeFile(), normalised, (TierParameters) null);
    List<SequenceRangeI> getAllReferenceSequences = newSeqVissr.getAllReferenceSequence();
    
    displayAggregatedExpressionElementData(lengthRanges, getAllReferenceSequences, newSeqVissr, myExpressionMatrix, start, stop,
      lociForChromo);
    return newSeqVissr;
  }
  public static SequenceVizMainFrame setupAndDisplayPatmanAggregate(SparseExpressionMatrix myExpressionMatrix, boolean normalised,
                                                                    int start, int stop,
                                                                    ArrayList<Patman> lociForChromo)
  {
    
    ArrayList<Integer> lengthRanges = myExpressionMatrix.getLengthRanges();
    SequenceVizMainFrame newSeqVissr = SequenceVizMainFrame.createVisSRInstance( myExpressionMatrix.getGenomeFile(), normalised, (TierParameters) null);
    List<SequenceRangeI> getAllReferenceSequences = newSeqVissr.getAllReferenceSequence();
    
    displayAggregatedPatmanData(lengthRanges, getAllReferenceSequences, newSeqVissr, myExpressionMatrix, start, stop,
      lociForChromo);
    return newSeqVissr;
  }
    private static void displayAggregatedPatmanData( List< Integer> lengths, List<SequenceRangeI> _currentReferenceSequences,
                                            SequenceVizMainFrame vissr,  SparseExpressionMatrix expressionMatrix, int startIndex, int stopIndex, 
                                            ArrayList<Patman> lociForChromo)
  {

    
    HashMap<String, SequenceWindowHelper> seqHelpers = new HashMap<String, SequenceWindowHelper>();
    for(SequenceRangeI range : _currentReferenceSequences)
    {
      SequenceWindowHelper swh = new SequenceWindowHelper( range, lengths );
      
      swh.setReferenceSequence( _currentReferenceSequences.get( 0 ).getSequenceId() );

      if(startIndex < 0)
        startIndex = 0;

      if(stopIndex < 0)
        stopIndex = _currentReferenceSequences.get( 0 ).getSequenceLength();

      double numWindows =  ((double)(stopIndex - startIndex)) / 100.0;
      swh.setNumWindows( (int)numWindows +1 );
      //swh.setNumWindows( numWindows );
      
      swh.setStartStopIndex( startIndex, stopIndex );
      seqHelpers.put( range.getSequenceId(), swh);
    }

    HashMap<String, Patman> locusItems = new HashMap<String, Patman>();
    ArrayList<Patman> lociInRange = LocusUtils.getPatmanLociInRange( lociForChromo, startIndex, stopIndex );

    for ( ArrayList<String> fileNames : expressionMatrix.getFileNames() )
    {
      if ( fileNames.size() == 1 )
      {
        locusItems.put( fileNames.get( 0 ), new Patman() );
      }
    }
     for ( Patman locus : lociInRange )
    {
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;

      for ( PatmanEntry element : locus )
      {

        if ( element.getStart() < currentMinCoord )
        {
          currentMinCoord = element.getStart();
        }
        if ( element.getEnd() > currentMaxCoord )
        {
          currentMaxCoord = element.getEnd();
        }
      }
      if ( currentMinCoord >= startIndex && currentMaxCoord <= stopIndex )
      {
        double value = 0.0;
        for ( PatmanEntry ele : locus )
        {
          

          HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( ele.getSequence() );
          for ( ArrayList<String> sampleList : expressionMatrix.getFileNames() )
          {

            if ( originalRow.containsKey( sampleList.get( 0 ) ) )
            {
              value =
                  originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
              PatmanEntry clone = new PatmanEntry(ele);
              clone.setAbundance(value);
              locusItems.get( sampleList.get( 0 ) ).add( clone );
            }
          }
        }
      }
    }
    
    int sampleNumber = 0;
    for ( ArrayList<String> sample : expressionMatrix.getFileNames() )
    {
      String tierName = "Sample " + sampleNumber;
      sampleNumber ++;
      
      //create tier params for this sample
      TierParameters tp = new TierParameters.Builder( tierName ).glyphClass( StripedSolidRectGlyph.class ).build();
      // Aggregate the length-filtered Patman data into 'windows'
      for ( SequenceRangeI _currentReferenceSequence : _currentReferenceSequences )
      {
        //allSequenceWindows.add( swh.generateSequenceWindows( p_list.get( _currentReferenceSequence.getSequenceId()) ) );
        tp.addListForId( _currentReferenceSequence.getSequenceId(), 
          seqHelpers.get( _currentReferenceSequence.getSequenceId() ).generateSequenceWindows( locusItems.get( sample.get(0)) ));
        
      }
      vissr.addTier( tp );
      
    }

  }
  private static void displayAggregatedExpressionElementData( List< Integer> lengths, List<SequenceRangeI> _currentReferenceSequences,
                                            SequenceVizMainFrame vissr,  SparseExpressionMatrix expressionMatrix, int startIndex, int stopIndex, 
                                            ArrayList<ArrayList<ExpressionElement>> lociForChromo)
  {

    
    HashMap<String, SequenceWindowHelper> seqHelpers = new HashMap<String, SequenceWindowHelper>();
    for(SequenceRangeI range : _currentReferenceSequences)
    {
      SequenceWindowHelper swh = new SequenceWindowHelper( range, lengths );
      
      swh.setReferenceSequence( _currentReferenceSequences.get( 0 ).getSequenceId() );

      if(startIndex < 0)
        startIndex = 0;

      if(stopIndex < 0)
        stopIndex = _currentReferenceSequences.get( 0 ).getSequenceLength();

      double numWindows =  ((double)(stopIndex - startIndex)) / 100.0;
      swh.setNumWindows( (int)numWindows +1 );
      //swh.setNumWindows( numWindows );
      
      swh.setStartStopIndex( startIndex, stopIndex );
      seqHelpers.put( range.getSequenceId(), swh);
    }

    double totalAbundance = 0.0;
    HashMap<String, Patman> locusItems = new HashMap<String, Patman>();
    ArrayList<ArrayList<ExpressionElement>> lociInRange = LocusUtils.getLociInRange( lociForChromo, startIndex, stopIndex );

    int sampleNumber = 1;
    for ( ArrayList<String> fileNames : expressionMatrix.getFileNames() )
    {
      locusItems.put( "Sample " + sampleNumber, new Patman() );

      sampleNumber++;
    }
    for ( ArrayList<ExpressionElement> locus : lociInRange )
    {
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;

      for ( ExpressionElement element : locus )
      {

        if ( element.startCoord.get( 0 ) < currentMinCoord )
        {
          currentMinCoord = element.startCoord.get( 0 );
        }
        if ( element.endCoord.get( 0 ) > currentMaxCoord )
        {
          currentMaxCoord = element.endCoord.get( 0 );
        }
      }
      if ( currentMinCoord >= startIndex && currentMaxCoord <= stopIndex )
      {
        for ( ExpressionElement ele : locus )
        {
          double value = 0.0;
          
          int startToPrint = 0;
          int endToPrint = 0;
          for ( int start : ele.startCoord )
          {
            if ( ( start <= stopIndex ) && start >= startIndex )
            {
              startToPrint = start;
            }
          }
          for ( int end : ele.endCoord )
          {
            if ( ( end <= stopIndex ) && end >= startIndex )
            {
              endToPrint = end;
            }
          }
          HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( ele.sequence );
          sampleNumber = 1;
          for ( ArrayList<String> sampleList : expressionMatrix.getFileNames() )
          {
            if ( sampleList.size() == 1 )//no replicates
            {
              if ( originalRow.containsKey( sampleList.get( 0 ) ) )
              {

                value =
                  originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
                PatmanEntry newPat = new PatmanEntry( ele.chromosomeID, ele.sequence, value, startToPrint, endToPrint, SequenceStrand.NEGATIVE, 0 );

                locusItems.get( "Sample " + sampleNumber ).add( newPat );
                totalAbundance += value;


              }
            }
            else
            {
              double mean = 0.0;
              for ( String replicateName : sampleList )
              {
                if ( originalRow.containsKey( replicateName ) )
                {
                  //mean += element.normalisedAbundance;
                  mean += originalRow.get( replicateName ).normalisedAbundance;
                }
              }
              mean /= sampleList.size();
              value = mean;
              PatmanEntry newPat = new PatmanEntry( ele.chromosomeID, ele.sequence, value, startToPrint, endToPrint, SequenceStrand.NEGATIVE, 0 );

               locusItems.get(  "Sample " + sampleNumber ).add( newPat );
              totalAbundance += value;
            }
            sampleNumber++;
          }
        }
      }
    }
    
    sampleNumber = 1;
    for (Map.Entry<String, Patman> entry : new TreeMap<String, Patman>(locusItems ).entrySet() )
    {
      entry.getValue().setGlyphHeights( (int) totalAbundance * 100 );
      String tierName =  entry.getKey();
      
      //create tier params for this sample
      TierParameters tp = new TierParameters.Builder( tierName ).glyphClass( StripedSolidRectGlyph.class ).build();
      // Aggregate the length-filtered Patman data into 'windows'
      for ( SequenceRangeI _currentReferenceSequence : _currentReferenceSequences )
      {
        //allSequenceWindows.add( swh.generateSequenceWindows( p_list.get( _currentReferenceSequence.getSequenceId()) ) );
        tp.addListForId( _currentReferenceSequence.getSequenceId(), 
          seqHelpers.get( _currentReferenceSequence.getSequenceId() ).generateSequenceWindows( entry.getValue() ));
        
      }
      vissr.addTier( tp );
      
    }

  }
}
