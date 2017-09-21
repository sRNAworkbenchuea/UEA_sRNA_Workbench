
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Central Data holder.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
final class Data {

    /**The number of queries remaining to be processed.**/
    private int queriesRemaining = 0;

    /**DEBUG FOR CAT TREE: Should we print out lots of information about the category tree. **/
    protected static final boolean CAT_TREE_VERY_VERBOSE = false;
    /**DEBUG FOR CAT TREE: Should we print out lots of information about the srna tree. **/
    protected static final boolean VERY_VERBOSE = false;
    /**Flag to generate sRNAs from cleavage positions.**/
    protected static final boolean GENERATE_SRNAS = false;
    /**Flag for output of debug information relating to categories.**/
    protected static final boolean CATEGORY_VERBOSE = false;
    /**Should the command line be in verbose mode?**/
    protected static final boolean cmdVerbose = true;
    /**Should the tool make and output plot records?**/
    protected static final boolean makePlotRecords = true;
    /** Constants for the ASCII code in byte form.**/
    private static final byte A = 'A';
    /** Constants for the ASCII code in byte form.**/
    private static final byte C = 'C';
    /** Constants for the ASCII code in byte form.**/
    private static final byte G = 'G';
    /** Constants for the ASCII code in byte form.**/
    private static final byte T = 'T';
    /** Constants for the ASCII code in byte form.**/
    private static final byte U = 'U';
    /** Constants for the ASCII code in byte form.**/
    private static final byte N = 'N';
    /**The normalisation multiplier.**/
    protected static final int NORMALISATION_MULTIPLIER = 1000000;
    /**A regular expression marking the end of a field - "&&"**/
    protected static final String END_OF_FIELD = "&&";
    /**A regular expression marking the end of a record - "##"**/
    protected static final String END_OF_RECORD = "##";
    /**Is the tool to be run in table mode? Default is true. **/
    protected static final boolean IS_TABLE_MODE = true;
    /**The number of consecutive nucleotides found to remove sequence.**/
    private static final int REPEAT_NTS = 7;
    /**Maximum di-nucleotide repeats allowed.**/
    private static final int DI_NTS_ALLOWED = 5;
    /**Constant value for rules broken index. **/
    protected static final int RULES_BROKEN = 32;
    /**Constant value for level index - index.**/
    protected static final int TREE_LEVEL_INDEX = 33;
    /**Legacy value for gap scoring.**/
    protected static final float GAP_SCORE = 1.0f;
    /** Encode + decode constant. **/
    protected static final int ENCODED_SOURCE_IS_NO_GAP = 1;
    /** Encode + decode constant. **/
    protected static final int ENCODED_SOURCE_IS_S_GAP = 2;
    /** Encode + decode constant. **/
    protected static final int ENCODED_SOURCE_IS_M_GAP = 3;
    /** Encode + decode constant. **/
    protected static final byte EMPTY_CELL = 0;
    /** Encode + decode constant. **/
    protected static final int SRNA_INDEX = 0;
    /** Encode + decode constant. **/
    protected static final int POSITIONS_INDEX = 1;
    /** Encode + decode constant. **/
    protected static final int MRNA_INDEX = 2;
    /** Encode + decode constant. **/
    protected static final byte NG_MATCH = 1;
    /** Encode + decode constant. **/
    protected static final byte SG_EXACT_MATCH = 2;
    /** Encode + decode constant. **/
    protected static final byte MG_EXACT_MATCH = 3;
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_HALF = 1;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_HALF_A = NG_MISMATCH_HALF+'A';//1+65 = 66
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_HALF_C = NG_MISMATCH_HALF+'C';//1+67 = 68
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_HALF_G = NG_MISMATCH_HALF+'G';//1+71 = 72
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_HALF_T = NG_MISMATCH_HALF+'T';//1+84 = 85
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_FULL = 2;//NEVER A SWITCH CASE
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_FULL_A = NG_MISMATCH_FULL+'A';//2+65 = 67
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_FULL_C = NG_MISMATCH_FULL+'C';//2+67 = 69
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_FULL_G = NG_MISMATCH_FULL+'G';//2+71 = 73
    /** Encode + decode constant. **/
    protected static final byte NG_MISMATCH_FULL_T = NG_MISMATCH_FULL+'T';//2+84 = 86
    /** Encode + decode constant. **/
    protected static final byte SG_GAP = 4;
    /** Encode + decode constant. **/
    protected static final byte MG_GAP = -10;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte MG_GAP_A = MG_GAP+'A';//-10+65 = 55
    /** Encode + decode constant. **/
    protected static final byte MG_GAP_C = MG_GAP+'C';//-10+67 = 57
    /** Encode + decode constant. **/
    protected static final byte MG_GAP_G = MG_GAP+'G';//-10+71 = 61
    /** Encode + decode constant. **/
    protected static final byte MG_GAP_T = MG_GAP+'T';//-10+84 = 74
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_HALF = -20;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_HALF_A = SG_MISMATCH_HALF+'A';//-20+65 = 45
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_HALF_C = SG_MISMATCH_HALF+'C';//-20+67 = 47
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_HALF_G = SG_MISMATCH_HALF+'G';//-20+71 = 51
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_HALF_T = SG_MISMATCH_HALF+'T';//-20+84 = 64
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_FULL = -30;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_FULL_A = SG_MISMATCH_FULL+'A';//-30+65 = 35
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_FULL_C = SG_MISMATCH_FULL+'C';//-30+67 = 37
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_FULL_G = SG_MISMATCH_FULL+'G';//-30+71 = 41
    /** Encode + decode constant. **/
    protected static final byte SG_MISMATCH_FULL_T = SG_MISMATCH_FULL+'T';//-30+84 = 54
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_HALF = -40;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_HALF_A = MG_MISMATCH_HALF+'A';//-40+65 = 25#
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_HALF_C = MG_MISMATCH_HALF+'C';//-40+67 = 27
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_HALF_G = MG_MISMATCH_HALF+'G';//-40+71 = 31
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_HALF_T = MG_MISMATCH_HALF+'T';//-40+84 = 44
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_FULL = -50;//NEVER A SWITCH/CASE
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_FULL_A = MG_MISMATCH_FULL+'A';//-50+65 = 15
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_FULL_C = MG_MISMATCH_FULL+'C';//-50+67 = 17
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_FULL_G = MG_MISMATCH_FULL+'G';//-50+71 = 21
    /** Encode + decode constant. **/
    protected static final byte MG_MISMATCH_FULL_T = MG_MISMATCH_FULL+'T';//-50+84 = 34
    /**Features indexer for mismatch at eleven**/
    protected static final int MM_ELEVEN = 0;
    /**Features indexer for adjacent mismatches**/
    protected static final int ADJACENT_MM = 1;

    // Package-private ctor - constants and static methods, no need to instantiate
    Data() {}

    /**
     * Set the count down for number of queries remaining to be processed.
     * @param i The number of queries remaining to be processed.
     */
    void setCountDown(int i) {
        queriesRemaining = i;
    }

    /**
     * Decrements the total number of queries remaining to be processed.
     * @param decrement The number of queries processed.
     * @return the number of queries left to be processed.
     */
    synchronized int accessCountDown(boolean decrement) {
        if(decrement){
            queriesRemaining--;
            return queriesRemaining;
        }else{
            return queriesRemaining;
        }
    }

    /**
     * Decodes the upper partition of an encoded duplex.
     * @param encoded The encoded duplex.
     * @param sequence The sequence being tested for complementarity.
     * @return The duplex decoded.
     */
    protected static byte[][] U_decode(byte[] encoded, byte[] sequence) {
        int emptyCell = 0;
        byte[][] decoded = new byte[3][32];
        for(int i = 14; i >= 0; i--){
            switch(encoded[i]){
                case NG_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_EXACT_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case MG_EXACT_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i+1];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case NG_MISMATCH_HALF_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_HALF_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_HALF_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_HALF_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_FULL_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_FULL_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_FULL_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case NG_MISMATCH_FULL_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_GAP:{
                    decoded[SRNA_INDEX][i] = '-';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case MG_GAP_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyCell = 1;
                    break;
                }case MG_GAP_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyCell = 1;
                    break;
                }case MG_GAP_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyCell = 1;
                    break;
                }case MG_GAP_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyCell = 1;
                    break;
                }case SG_MISMATCH_HALF_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_HALF_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_HALF_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_HALF_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_FULL_A :{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_FULL_C :{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_FULL_G :{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case SG_MISMATCH_FULL_T :{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    break;
                }case MG_MISMATCH_HALF_A :{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_HALF_C :{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_HALF_G :{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_HALF_T :{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_FULL_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_FULL_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_FULL_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case MG_MISMATCH_FULL_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i+1];
                    break;
                }case EMPTY_CELL:{
                    if(emptyCell == 0){
                        decoded[MRNA_INDEX][i] = sequence[i];
                    }else if(emptyCell == 1){
                        decoded[MRNA_INDEX][i] = sequence[i+1];
                    }
                    break;
                }
            }//end switch.
        }//end for.
        Data.compliment(decoded[MRNA_INDEX]);
        return decoded;
   }//end method.

    /**
     * Decodes the lower partition of an encoded duplex.
     * @param encoded The encoded duplex.
     * @param sequence The sequence being tested for complementarity.
     * @return The duplex decoded.
     **/
    protected static byte[][] D_decode(byte[] encoded, byte[] sequence, boolean[] features) {
        boolean one = false;
        boolean two = false;
        boolean three = false;
        int emptyType = 0;
        byte[][] decoded = new byte[3][32];
        for(int i = 14; i < sequence.length; i++){
            switch(encoded[i]){
                case NG_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    one = false; two = false; three = false;
                    break;
                }case SG_EXACT_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    one = false; two = false; three = false;
                    break;
                }case MG_EXACT_MATCH:{
                    decoded[SRNA_INDEX][i] = sequence[i-1];
                    decoded[POSITIONS_INDEX][i] = '|';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    one = false; two = false; three = false;
                    break;
                }case NG_MISMATCH_HALF_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_HALF_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_HALF_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_HALF_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_FULL_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_FULL_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_FULL_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case NG_MISMATCH_FULL_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_GAP:{
                    decoded[SRNA_INDEX][i] = '-';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_GAP_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyType = 1;
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_GAP_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyType = 1;
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_GAP_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyType = 1;
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_GAP_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = '-';
                    emptyType = 1;
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_HALF_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_HALF_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_HALF_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_HALF_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_FULL_A :{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_FULL_C :{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_FULL_G :{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case SG_MISMATCH_FULL_T :{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_HALF_A :{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_HALF_C :{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_HALF_G :{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_HALF_T :{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = 'o';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_FULL_A:{
                    decoded[SRNA_INDEX][i] = 'A';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_FULL_C:{
                    decoded[SRNA_INDEX][i] = 'C';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_FULL_G:{
                    decoded[SRNA_INDEX][i] = 'G';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case MG_MISMATCH_FULL_T:{
                    decoded[SRNA_INDEX][i] = 'T';
                    decoded[POSITIONS_INDEX][i] = ' ';
                    decoded[MRNA_INDEX][i] = sequence[i-1];
                    if(!one){one = true;}
                    else if(one){two = true;}
                    else if(two){three = true;}
                    break;
                }case EMPTY_CELL:{
                    if(emptyType == 0){
                        decoded[MRNA_INDEX][i] = sequence[i];
                    }else if(emptyType == 1){
                        decoded[MRNA_INDEX][i] = sequence[i-1];
                    }
                    break;
                }
            }//end switch.
            if(one && two && three){features[Data.ADJACENT_MM] = true;}
        }//end for.
        Data.compliment(decoded[MRNA_INDEX]);
        if(encoded[14] != NG_MATCH || encoded[14] != SG_EXACT_MATCH || encoded[14] != MG_EXACT_MATCH){features[Data.MM_ELEVEN] = true;}

        return decoded;
   }//end method.

    /**
    * join two arrays into one array.
    * Note: This function will not leave default array creation value of 0, but will
    * replace 0's with ASCII white space.
    * @param a The first array.
    * @param b The second array.
    * @return The joined array.
    */
    protected static byte[] join(byte[] a, byte[] b) {
       byte[] r = new byte[32];
       System.arraycopy(a, 0, r, 0, 14);
       System.arraycopy(b, 14, r, 14, 18);
       //Replace any 0s in the array with proper white space (ASCII 32).
       for(int i = 0; i < r.length; i++){
           if(r[i] == 0){
               r[i]=32;
           }
       }
       return r;
   }//end method.

    /**
     * Compliment a sequence.
     * @param b The sequence to be complimented.
     */
    protected static void compliment(byte[] b) {

        for(int i = 0; i < b.length; i++){
            byte c = b[i];
            switch(c){
                case U :  {b[i] = A; break;}
                case T :  {b[i] = A; break;}
                case A :  {b[i] = T; break;}
                case G :  {b[i] = C; break;}
                case C :  {b[i] = G; break;}
                case N :  {b[i] = N; break;}
                default  :  {b[i] = b[i]; break;}
            }//end switch
        }//END FOR.
    }//end method.

    /**
     * Filter for sequences containing repeat nucleotides e.g. AAAAAAA and repeat
     * di-nucleotides e.g. ATATATATAT.
     * @param sequence The sequence to be tested if it needs to be filtered.
     * @return True if the sequence should be filtered.
     */
    protected static boolean filter(byte[] sequence) {
        //If there is a repetitive 5Mer - filter it out.
        if(kMerFilter(sequence, 5)){
            return true;
        }
        int ha = 0;
        int hc = 0;
        int hg = 0;
        int ht = 0;
        for(int i = 0; i < sequence.length; i++){
            if(sequence[i] == 'A'){
                ha = 1;
            }else if(sequence[i] == 'C'){
                hc = 1;
            }else if(sequence[i] == 'G'){
                hg = 1;
            }else if(sequence[i] == 'T'){
                ht = 1;
            }
        }
        if(ha+hc+hg+ht < 3){
            return true;
        }
        String sequ = new String(sequence);
        String s = "";
        String[] combos = {"AC","AG","AT","CA","CG","CT","GA","GC","GT","TA","TC","TG"};
        for(int i = 0; i < combos.length; i++){
            for(int j = 0; j < DI_NTS_ALLOWED; j++){
                s+=combos[i];
            }
            if(sequ.contains(s)){
                return true;
            }
            s = "";
        }
        String allA = "";
        String allC = "";
        String allG = "";
        String allT = "";
        for(int i = 0; i < REPEAT_NTS; i++){
            allA +="A";
            allC +="C";
            allG +="G";
            allT +="T";
        }
        if(sequ.contains(allA)){
            return true;
        }
        if(sequ.contains(allC)){
            return true;
        }
        if(sequ.contains(allG)){
            return true;
        }
        if(sequ.contains(allT)){
            return true;
        }
        return false;
    }//end method.

    /**
     * Filters for repetitive kMers. The default for k is 5 and the maximum number
     * of kMer repeats is 10.  See document "filterInvestigation.xls".
     * @param sequence The sequence to be tested for filtering.
     * @param kMerLength The length of the kMer.
     * @return True if this sequence should be filtered.
     */
    protected static boolean kMerFilter(byte[] sequence, int kMerLength) {
        String in = new String(sequence);
        HashMap<String,Integer> kMerCount = new HashMap<String,Integer>();
        for(int i = 1; i < in.length(); i++ ){
            int start = i;
            int end = i+kMerLength-1;
            if(end < in.length()){
                String kMer = in.substring(start,end);
                if(kMerCount.containsKey(kMer)){
                    Integer tempCount = kMerCount.get(kMer)+1;
                    kMerCount.put(kMer, tempCount);
                }else{
                    kMerCount.put(kMer, 1);
                }
            }
        }
        int longest = 0;
        Iterator<String> itr = kMerCount.keySet().iterator();
        while(itr.hasNext()){
            String next = itr.next();
            if(kMerCount.get(next) > longest){
                longest = kMerCount.get(next);
            }
            if(longest >= 10){
                return true;
            }
        }
        return false;
    }//end method.

    /**
     * Convenience method for making the reverse complement of a string (in byte form ASCII)
     * @param b The sequence to be reverse complimented.
     * @param start The start position of the sequence.
     * @param end The end position of the sequence.
     * @param length The length of the sequence.
     * @return A reverse complimented sequence.
     */
    protected static byte[] reverseCompliment(byte[] b, int start, int end, int length) {
        byte[] rc = new byte[length];
        for(int i = end, j = 0 ; i >= start; i--, j++){
            byte c = b[i];
            switch(c){
                case U :  {rc[j] = A; break;}
                case T :  {rc[j] = A; break;}
                case A :  {rc[j] = T; break;}
                case G :  {rc[j] = C; break;}
                case C :  {rc[j] = G; break;}
                case N :  {rc[j] = N; break;}
                //default  :  {rc[j] = 'x'; break;}
            }//end switch
        }//end for.
        return rc;
    }//end method.

    /**
     * Reverse a sequence.
     * @param b The sequence to be reversed.
     * @return The sequence reversed.
     * @deprecated No longer used anywhere.
     */
    protected static byte[] reverse(byte[] b) {
        byte[] r = new byte[b.length];
        for(int i = b.length-1, j = 0 ; i >= 0; i--, j++){
            byte c = b[i];
            switch(c){
                case U :  {r[j] = U; break;}
                case T :  {r[j] = T; break;}
                case A :  {r[j] = A; break;}
                case G :  {r[j] = G; break;}
                case C :  {r[j] = C; break;}
                case N :  {r[j] = N; break;}
                default  :  {r[j] = 'x'; break;}
            }//end switch
        }//END FOR.
        return r;
    }//end method.

    /**
     * Get the index of a p-value based on mismatches.
     * @param mm The mismatch sum.
     * @return an index into the p-value list.
     * @deprecated No longer used anywhere.
     */
    protected static int getPValueIndex(float mm) {

             if(mm == 0.0){return 0;}
        else if(mm == 0.5){return 1;}
        else if(mm == 1.0){return 2;}
        else if(mm == 1.5){return 3;}
        else if(mm == 2.0){return 4;}
        else if(mm == 2.5){return 5;}
        else if(mm == 3.0){return 6;}
        else if(mm == 3.5){return 7;}
        else if(mm == 4.0){return 8;}
        else if(mm == 4.5){return 9;}
        else if(mm == 5.0){return 10;}
        else if(mm == 5.5){return 11;}
        else if(mm == 6.0){return 12;}
        else if(mm == 6.5){return 13;}
        else if(mm == 7.0){return 14;}
        else if(mm == 7.5){return 15;}
        else if(mm == 8.0){return 16;}
        else if(mm == 8.5){return 17;}
        else if(mm == 9.0){return 18;}
        else if(mm == 9.5){return 19;}
        else if(mm == 10.0){return 20;}
        else if(mm == 10.5){return 21;}
        else if(mm == 11.0){return 22;}
        else if(mm == 11.5){return 23;}
        else if(mm == 12.0){return 24;}
        else if(mm == 12.5){return 25;}
        else if(mm == 13.0){return 26;}
        else if(mm == 13.5){return 27;}
        else if(mm == 14.0){return 28;}
        return 0;
    }//end method.

    /**
     * Hugh's method for resetting static variables.
     * @deprecated Moved all of the statics out of here.
     */
    static void performStaticReset() {
    //Make an array to store the sequence distributions.
    //non_redundant_sequence_distribution_srnaome = new int[ maximumSequenceLength+1];
    //redundant_sequence_distribution_srnaome = new int[ maximumSequenceLength+1];
    //Deg_Tool_degradome_non_redundant_read_count = 0;
    //Deg_Tool_degradome_redundant_read_count = 0;
    //Deg_Tool_degradome_total_unique_hits_on_transcriptome = 0;
    //non_redundant_sequences_srnaome = 0;
    //redundant_sequence_count_srnaome = 0;
    //litListCount = 0;
    //srnaNodeCount = 0;
    // = 0;
    //totalRawShortReads_AF = 0;
  }//end method.

}//end class.
