
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.util.Arrays;
import java.util.Random;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;

/**
 *A Chart representing a transcript and all interactions with said transcript.
 *@author Leighton Folkes (l.folkes@uea.ac.uk)
 **/
final class Chart {

    /**Transcript id - i.e. >id.**/
    protected String ID;

    /**The index of each element is the position on the chart/transcript.**/
    private int[] abundance;
    private float[] weightedAbundance;
    protected byte[] sequence;
    protected int[] compactPositions;
    protected int[] compactAbundance;
    protected float[] compactWeightedAbundance;
    protected int[] compactCategory;
    private int count;
    protected boolean printMe = false;
    protected float medianValue;

    /**
     * Constructs a new instance of Chart.
     * @param ID The string ID for the transcript.
     * @param sequence The nucleotide sequence of the transcript.
     */
    public Chart(String ID, byte[] sequence){
        this.ID = ID;
        this.sequence = sequence;
        abundance = new int[sequence.length];
        weightedAbundance = new float[sequence.length];
        count = 0;
    }//end constructor.

    /**
     * Add a degradome hit to to the transcript chart.
     * @param position The position of the hit.
     * @param weightedAbundance The weighted abundance of the degradome hit.
     * @param abundance The raw abundance of the degradome hit.
     */
    public void addHit(int position, float weightedAbundance, int abundance){
        if(this.weightedAbundance[position] == 0.0f){count++;}
        this.weightedAbundance[position] += weightedAbundance;
        this.abundance[position] += abundance;
    }//end method.

    /**
     * Compact the information relating to this transcript chart.
     */
    public void compact(){
        compactPositions = new int[count];
        compactWeightedAbundance = new float[count];
        compactAbundance = new int[count];
        int next = 0;
        for(int i = 0; i < weightedAbundance.length;i++){
            if(weightedAbundance[i] > 0.0f){
                compactPositions[next] = i;
                compactWeightedAbundance[next] = weightedAbundance[i];
                compactAbundance[next] = abundance[i];
                next++;
            }
        }
        weightedAbundance = null;
        abundance = null;
    }//end method.

    /**
     * Calculate the categories for the hits on this transcript chart.
     */
    public void calculateCategories(boolean isUsingWeightedAbundance){
        if(isUsingWeightedAbundance){
            //Carry out a sort to obtain the median and the largest values.
            float[] c = new float[compactWeightedAbundance.length];
            System.arraycopy(compactWeightedAbundance, 0, c, 0, compactWeightedAbundance.length);
            Arrays.sort(c);
            boolean c0;
            int m = c.length/2;
            float median;
            if(c.length%2==1){
                median = c[m];
            }else{
                median = c[m-1];
            }
            this.medianValue = median;
            //Test to see if there is a c0.
            float max = c[c.length-1];
            float nextMax;
            if(c.length > 1){
                nextMax = c[c.length-2];
            }else{
                nextMax = max;
            }
            if(max > nextMax){
                c0 = true;
            }else{
                c0 = false;
            }

            compactCategory = new int[count];
            for(int i = 0; i < compactWeightedAbundance.length; i++){
                if(compactAbundance[i] == 1.0f) { //Category 4: Only one raw read at position.
                    compactCategory[i] = 4;
                }else if(compactWeightedAbundance[i] == max){
                    if(c0){//Category 0: > 1 raw read at position, abundance on the trascript is equal to the maximum and there is only one maximum
                        compactCategory[i] = 0;
                    }else{//Category 1: > 1 raw read at position, abundance on the transcript is equal to the maximum and there is more than one maximum.
                        compactCategory[i] = 1;
                    }
                }else if(compactWeightedAbundance[i] >= median){//Category 2: > 1 raw read at position, abundance on the transcript is less than the maximum but greather than the median for the transcript.
                    compactCategory[i] = 2;
                }else{//Category 3: > 1 raw read at position, abundance is equal to or less than the median.
                    compactCategory[i] = 3;
                }
            }//end for.
        }else{
            int[] c = new int[compactAbundance.length];
            System.arraycopy(compactAbundance, 0, c, 0, compactAbundance.length);
            Arrays.sort(c);
            float median;
            boolean c0 = true;
            int m = c.length/2;
            //median = c[m];
            if(c.length%2==1){
                median = c[m];
            }else{
                median = c[m-1];
            }

            //Test to see if there is a c0.
            float max = c[c.length-1];
            float nextMax;
            if(c.length > 1){
                nextMax = c[c.length-2];
            }else{
                nextMax = max;
            }
            if(max > nextMax){
                c0 = true;
            }else{
                c0 = false;
            }

            compactCategory = new int[count];
            for(int i = 0; i < compactAbundance.length; i++){
                if(compactAbundance[i] == 1.0f) { //Category 4: Only one raw read at position.
                    compactCategory[i] = 4;
                }else if(compactAbundance[i] == max){
                    if(c0){//Category 0: > 1 raw read at position, abundance on the trascript is equal to the maximum and there is only one maximum
                        compactCategory[i] = 0;
                    }else{//Category 1: > 1 raw read at position, abundance on the transcript is equal to the maximum and there is more than one maximum.
                        compactCategory[i] = 1;
                    }
                }else if(compactAbundance[i] >= median){//Category 2: > 1 raw read at position, abundance on the transcript is less than the maximum but greather than the median for the transcript.
                    compactCategory[i] = 2;
                }else{//Category 3: > 1 raw read at position, abundance is equal to or less than the median.
                    compactCategory[i] = 3;
                }
            }//end for.
        }//end else.
    }//end method.

    /**
     * Print the degradome hits for this chart.
     * @return The degradome hits.
     */
    public String printDegradomeAlignments(){
        String s = "Position:\tAbundance:\tCategory:"+LINE_SEPARATOR;
        String format = "%-9d\t%-10f\t%-1d"+LINE_SEPARATOR;
        for(int i=0; i < compactWeightedAbundance.length; i++){
            s += String.format(format, compactPositions[i], compactWeightedAbundance[i], compactCategory[i]);
        }
        return s;
    }//end method.

    /**
     * Make the category trees (add to the trees) for this chart.
     */
    public void makeCategoryTrees(OriginalTreeBox treeBox, ParesnipParams params)
    {
        // Iterate through the compact*** arrays - assume they are the same size !
        //
        // The arrays are:
        //   compactWeightedAbundance
        //   compactPositions
        //   compactCategory
        //
        for(int i = 0; i < compactWeightedAbundance.length;i++)
        {
            int end = compactPositions[i] + 9;

            if(end <= sequence.length-1)
            {
                int start = compactPositions[i] - 16;

                int length = 0;
                boolean addToTree = false;

                // Get the category tree for the given category
                int cat = compactCategory[i];

                if(start >=0)
                {
                    addToTree = wantToAddToTree( cat, params );

                    if ( addToTree )
                    {
                      length = 26;
                    }
                }
                else
                {
                    //do best length possible!!
                    for(int j = 16; j >=0 ; j--)
                    {
                        if(compactPositions[i]-j >= 0)
                        {
                            addToTree = wantToAddToTree( cat, params );

                            if ( addToTree )
                            {
                              start = compactPositions[i] - j;
                              length = j+9+1;
                            }

                            break;
                        }
                    }
                }

                if ( addToTree )
                {
                    byte[] b = Data.reverseCompliment( sequence, start, end, length );
                    CategoryTree catTree = treeBox.getOriginalCategoryTree( cat );
                    catTree.addToTree( b, params );
                }
            }
        }
    }//END METHOD.

   /**
     * Do we actually want to add this category to a tree ?
     *
     * @param cat
     * @param params
     * @return
     */
    private boolean wantToAddToTree( int cat, ParesnipParams params )
    {
      boolean result = false;

      switch ( cat )
      {
          case 0: result = params.isCategory0(); break;
          case 1: result = params.isCategory1(); break;
          case 2: result = params.isCategory2(); break;
          case 3: result = params.isCategory3(); break;
          case 4: result = params.isCategory4(); break;
      }

      return result;
    }

    /**
     * Makes a plot record and adds it to the Data stucture held in Data.
     * This method must be called AFTER all chart construction has been done.
     */
    public PlotRecord makePlotRecord( boolean useWeightedFragmentAbundance )
    {
        PlotRecord r = new PlotRecord();

        r.setGeneId( ID );
        r.setMedianDegradomeHitAbundance( medianValue );
        r.setGeneLength(sequence.length);

        for(int i = 0; i < this.compactWeightedAbundance.length; i++)
        {
            r.addD_Hit(this.compactPositions[i], useWeightedFragmentAbundance ? this.compactWeightedAbundance[i] : this.compactAbundance[i] );
        }

        return r;
    }

    /**
     * Random sRNA generation used for obtaining srnas to be used in benchmarking
     * the performance of the software.
     * @param rand A random object with its seed already set.
     * @return A cleavage centred srna sequence.
     */
    public String getRandomSrnaFromTranscriptPeakOfRandomLength(Random rand){
        try{
            int randomSite = getRandomCleavagePos(rand);
            //We know that we only want 10 nt downstream of cleavage site (cleavage index + 9 = 10 nt)...
            int downstream = 9;
            //We want between 10 and 14 nucleotides upstream of cleavage site.
            //So get a random number between 0 - 4 and add 10.
            int upstream = rand.nextInt(5) + 10;
            //The start index is cleavage site - upstream.
            int start = randomSite - upstream;
            //The end index is cleavage site + 9
            int end = randomSite + downstream;
            //List the random cleavage centered sequence from the transcript.
            String s = "";
            for(int i = start; i <= end; i++){
                s += (char)sequence[i];
            }
            //reverse compliment the sequence lifted
            //Change the string lifted to byte array to use convenience method..
            byte[] b = StringUtils.changeStringToByteArray(s);
            //Reverse compliment the lifted sequence...
            byte[] c = Data.reverseCompliment(b, 0,b.length-1,b.length);
            //Return a string of the reverse complimented sequence.
            return new String(c);
        }catch(Exception e){
            return null;
        }
    }//end method.

    public int getRandomCleavagePos(Random rand){
        //Get a random index of the cleaveage positions.
        int randomIndex = rand.nextInt(this.compactPositions.length);
        //Get a random cleavage site.
        return this.compactPositions[randomIndex];
    }

}//END CLASS


