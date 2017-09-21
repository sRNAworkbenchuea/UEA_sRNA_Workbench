
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * This class contains all building and searching functionality for creating a
 * cumulative frequency of pValues.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class CategoryTree {

    /**Are gapped alignments allowed?**/
    private boolean gapped;
    /**
     * byte representation of char A
     */
    private final byte A = 'A';
    /**
     * byte representation of char C
     */
    private final byte C = 'C';
    /**
     * byte representation of char G
     */
    private final byte G = 'G';
    /**
     * byte representation of char T
     */
    private final byte T = 'T';
    /**
     * Did the previous comparison of nucleotides a match? (NG only)
     **/
    private boolean previous;
    /**
     * The score for upwards no gapped alignments.
     */
    private float U_NG_SCORE;
    /**
     * Holder for the sequence which has been passed to this tree for searching.
     */
    private byte[] sequence;
    /**
     * The number of entry nodes held in this tree.
     */
    private int splitListCount;
    private void setSplitListCount(int splitListCount){this.splitListCount = splitListCount;}
    /**
     * The list of entry points.
     * AA AC AG AT  CC CA CG CT GG GA GC GT TT TA TC TG
     */
    private ArrayList<Node>[] start;
    private void setStart(ArrayList<Node>[] start){this.start = start;}
    /**
     * The absolute root of this category tree.
     */
    private Node root;
    private void setRoot(Node root){this.root = root;}
    /**
     * The index into the search sequence up the tree.
     */
    private int U_INDEX;
    /**
     * Counter for indexing into the gapped mrna alignment list.
     */
    private int U_MG_LENGTH = 0;
    /**
     * An array holding the scores for mrna gapped alignments found up the tree.
     */
    private ArrayList<Float> U_MG_SCORE;
    /**
     * A rule breakage mirror for U_MG_SCORE.
     */
    private ArrayList<Boolean> U_MG_RULES_UNBROKEN;
    /**
     * Counter for indexing into the gapped srna alignment list.
     */
    private int U_SG_LENGTH = 0;
    /**
     * An array holding the scores for srna gapped alignments found up the tree.
     */
    private ArrayList<Float> U_SG_SCORE;
    /**
     * A rule breakage mirror for U_SG_SCORE.
     */
    private ArrayList<Boolean> U_SG_RULES_UNBROKEN;
    /**
     * Node holder - holds the node visited within the tree before the current focus node.
     */
    private Node previousNode;
    /**
     * Flag for putting back a particular state after a test is made.
     * @see U_SG()
     */
    private boolean putItBack = false;
    /**
     * Was the previous nucleotide comparison between sequence and gapped mrna a match?
     * Note: set to true initially as positions 10 and 11 will always match.
     */
    private boolean U_MG_previous = true;
    /**
     * Was the previous nucleotide comparison between sequence and gapped srna a match?
     * Note: set to true initially as positions 10 and 11 will always match.
     */
    private boolean U_SG_previous = true;
    /**
     * Have the rules been broken when making an un-gapped alignment up the tree?
     */
    private boolean U_NG_RULES_UNBROKEN = true;
    /**
     * Special case node for making sequence v gapped mrna alignments up the tree.
     */
    private Node U_MG_MATCHING_NODE;
    /**
     * Flag used by <b>D_gapFound()</b> for holding the result of testing for a gap
     * on either the srna or mrna. Saves local variable memory allocation call a few
     * hundred million times.
     */
    boolean D_GAP_FOUND;
    /**
     * Flag for allowing adjacent mismatches;
     */
    //boolean adjacentMM = false;
    /**
     * For debugging only.
     **/
    private String U_NG_srna = "";
    private String U_NG_pos = "";
    private String U_NG_mrna = "";
    private ArrayList<String> U_SG_srna = new ArrayList<String>();
    private ArrayList<String> U_SG_pos = new ArrayList<String>();
    private ArrayList<String> U_SG_mrna = new ArrayList<String>();
    private ArrayList<String> U_MG_srna = new ArrayList<String>();
    private ArrayList<String> U_MG_pos = new ArrayList<String>();
    private ArrayList<String> U_MG_mrna = new ArrayList<String>();
    private byte[] D_NG_srna = new byte[26];
    private byte[] D_NG_pos = new byte[26];
    private byte[] D_NG_mrna = new byte[26];
    private ArrayList<String> D_SG_srna = new ArrayList<String>();
    private ArrayList<String> D_SG_pos = new ArrayList<String>();
    private ArrayList<String> D_SG_mrna = new ArrayList<String>();
    private ArrayList<String> D_MG_srna = new ArrayList<String>();
    private ArrayList<String> D_MG_pos = new ArrayList<String>();
    private ArrayList<String> D_MG_mrna = new ArrayList<String>();
    //////////////////////////////////////////////////////////////

    /**
     * The current score for the no gapped alignment down the tree.
     **/
    private float D_NG_SCORE = 0.0f;
    /**
     * The current state of rule breakage for the down no gapped alignment.
     */
    private boolean D_NG_RULES_UNBROKEN = true;
    /**
     * Array of undefined length holding scores for downwards <b>srna</b> gapped alignments.
     * TODO: This structure could be swapped out for something more efficient.
     */
    private ArrayList<Float> D_SG_SCORE = new ArrayList<Float>();
    /**
     * Array of undefined length holding rule breakage state which mirrors the srna scores.
     */
    private ArrayList<Boolean> D_SG_RULES_UNBROKEN = new ArrayList<Boolean>();
    /**
     * Array of undefined length holding scores for downwards <b>mrna</b> gapped alignments.
     * TODO: This structure could be swapped out for something more efficient.
     */
    private ArrayList<Float> D_MG_SCORE = new ArrayList<Float>();
    /**
     * Array of undefined length holding rule breakage state which mirrors the mrna scores.
     */
    private ArrayList<Boolean> D_MG_RULES_UNBROKEN = new ArrayList<Boolean>();
    /**
     * The current node of focus when navigating up the tree.
     */
    private Node U_NG_NODE;
    /**
     * A flag which is true if a valid <b>srna</b> gapped alignment is available for updating.
     * This flag is updated as a time saving mechanism when testing for broken rules
     * on gapped srna alignments. Note: The U is for Up the tree.
     */
    private boolean U_SG_VALID = false;
    /**
     * A flag which is true if a valid <b>mrna</b> gapped alignment is available for updating.
     * This flag is updated as a time saving mechanism when testing for broken rules
     * on gapped srna alignments. Note: The U is for Up the tree.
     */
    private boolean U_MG_VALID = false;
    /**
     * The current focus node when navigating down the tree.
     */
    private Node D_FOCUS_NODE;
    /**
     * The current sequence index when navigating down the tree.
     */
    private int D_INDEX;
    /**
     * A stack where each entry is a level within the tree. This stack mirrors the nodes stack.
     */
    private Stack<Integer> treeLevel = new Stack<Integer>();
    /**
     * A stack of nodes pushed and popped when navigating paths down the tree.
     * @see D_pushAndPop()
     */
    private Stack<Node> nodes = new Stack<Node>();
    /**
     * A holder for the best scored alignment found a call to search.
     */
    private float bestScored;
    /**
     * Scratch space just to save continued local allocation of memory. (helps the garbage collector)
     */
    private float scratchScore;
    /**
     * An array of scores from un-gapped alignments down the tree.
     */
    private Stack<Float> D_NG_SCORES = new Stack<Float>();
    /**
     * An array of rule breakage states which mirrors the D_NG_SCORES
     */
    private Stack<Boolean> D_NG_RULES_UNBROKEN_stack = new Stack<Boolean>();
    /**
     * An array list holding the level in the tree which a gapped srna alignment came
     * into existence. This array mirrors the d_srna scores.
     */
    private ArrayList<Integer> D_SG_LEVEL_INTO_EXISTENCE = new ArrayList<Integer>();
    /**
     * An array list holding the level in the tree which a gapped mrna alignment came
     * into existence. This array mirrors the d_srna scores.
     */
    private ArrayList<Integer> D_MG_LEVEL_INTO_EXISTENCE = new ArrayList<Integer>();
    /**
     * Holds the number of nodes pushed onto the stack in any one call to D_pushAndPop().
     * @see D_pushAndPop()
     */
    private int pushed;
    /**
     * The best gapped score down either on srna or mrna.
     **/
    private float bestGapScoreDown;
    /**
     * Identifier for which the best gap score was found.
     */
    private int bestGapDownStrand;
    /**
     * The index of which the best gap score down was found in the score array.
     */
    private int bestGapScoreDownIndex;
    /**
     * A value that can be used to say a particular state is currently undefined.
     * @see U_NG_setBestScore()
     */
    private final int UNIDENTIFIED = 0;
    /**
     * Value to identify srna strand.
     */
    private final int SRNA_STRAND = 1;
    /**
     * Value to identify mrna strand.
     */
    private final int MRNA_STRAND = 2;
    /**
     * Set to true if nothing was found when searching up the tree.
     * If this flag is true, then execution will move to the next subtree and not search any further in this subtree.
     * @see setAdvanceLogic()
     */
    private boolean NOTHING_VALID;
    /**
     * Set to true if the only valid alignment up the tree was an un-gapped alignment.
     * @see setAdvanceLogic()
     */
    private boolean NO_GAPPED_UP_ONLY;
    /**
     * Set to true if the only valid alignment up the tree was a gapped alignment on either the srna or mrna.
     * @see setAdvanceLogic()
     */
    private boolean GAPPED_UP_ONLY;
    /**
     * Set to true if both a valid alignment was found when searching up the tree for both a gapped alignment and
     * an un-gapped alignment.
     * @see setAdvanceLogic()
     */
    private boolean BOTH_UP;
    /**
     * Flag which is set after testing which is the best alignment up the tree.  A comparison of
     * U_NG v U_SG v U_MG is made and U_NG takes precedence. If U_NG is the best then we will be
     * using no gap alignment up the tree. therefore = true; else false;
     * @see D_NG_setBestScore()
     * @see getBestBOTH_UP()
     */
    private boolean usingNoGapForUP_for_noGapDown;
    /**
     * Holds the value for the best score found when searching upwards, which makes half
     * of the score for NG Down. This could be either a gapped or un gapped score.
     * @see setBestGappedDown()
     **/
    private float U_NG_BEST_SCORE_FOUND;
    /**
     * Holds the identifier for the strand on which the best gap was found. i.e. mrna or srna.
     */
    private int BEST_SCORE_UP_STRAND;
    /**
     * The index of the best gapped score within the score array.
     */
    private int bestGapScoreUpIndex;
    /**
     * A scratch space for the alignment score to be held during calculation.
     * @see selectBestAlignmentScoreFound()
     */
    private float outAlignmentScore;
    /**
     * The maximum score which an alignment cannot exceed.
     * The max is the original duplex score.
     */
    private float max;
    /**
     * A scratch space so as not to repeatedly create a local variable (helps garbage collector).
     */
    private float scratchSpace;
    /**
     * The number of nodes in the tree.
     */
    public int nodeCount = 0;
    /**
     * Is there a valid alignment with gap on mRNA strand?
     */
    private boolean HAS_VALID_MG_DOWN;
    /**
     * Is there a valid alignment with gap on sRNA strand?
     */
    private boolean HAS_VALID_SG_DOWN;
    /**
     * Allow mismatch at position eleven.
     */
    private boolean allowEleven = false;//false
    /**
     * Use less than 2.5 mismatches in seed rule.
     */
    private boolean useLess = true;//true
    /**
     * Use no adjacent mismatches in seed region rule.
     */
    private boolean useUpAm = true;//true
    /**
     * Use adjacent mismatches down rule.
     */
    private boolean useDownAm = false;//false

    /**
     * Constructs a new instance of CategoryTree.
     **/
    @SuppressWarnings("unchecked")
    public CategoryTree() {
        root = Node.createRoot();
        start = new ArrayList[16];  // unchecked warning - not much we can do when mixing arrays and generics
        for (int i = 0; i < 16; i++) {
            start[i] = new ArrayList<Node>();
        }
    }//end constructor.

    /**
     * Makes a shallow copy of this Category Tree object.
     * @return
     */
    public CategoryTree copyCategoryTree() {
        CategoryTree c = new CategoryTree();
        c.setStart(start);
        c.setSplitListCount(splitListCount);
        c.setRoot(root);
        return c;
    }//end method.

    /**
     * Adds a sequence to the category tree for searching.
     * @param sequence length must be greater than 19 and less than 26 bytes
     * and contain the ASCII code in byte form for A G C T ONLY.
     */
    public void addToTree(byte[] sequence, ParesnipParams params) {
        //Test to see if the sequence requested to be put in the tree is of the correct length.
        if(sequence.length < 21 || sequence.length > 26) {
            return;
        }//end if.

        //Test for un known characters - return if any unknowns are found.
        for (int i = 0; i < sequence.length; i++) {
            switch (sequence[i]) {
                case A:
                    break;
                case C:
                    break;
                case G:
                    break;
                case T:
                    break;
                default:
                    return;
            }//end switch.
        }//end for.
        if(params.isDiscardLowComplexityCandidates() && Data.filter(sequence)){
            return;
        }

        Node currentNode = root;
        for (int i = 0; i < sequence.length; i++) {
            //Look at the edge needed for the nt at the level of this node.
            switch (sequence[i]) {
                case A: {
                    if (currentNode.edgeA == null) {
                        //If the edge of A is null and we need it then make it.
                        currentNode.edgeA = new Node( currentNode, A );
                        //currentNode.edgeA.parent = currentNode;
                        //currentNode.edgeA.edge = A;
                        currentNode = currentNode.edgeA;
                        nodeCount++;
                    } else {
                        //Else we have the edge, so recurse down that path.
                        currentNode = currentNode.edgeA;
                    }
                    break;
                }//end case.
                case C: {
                    if (currentNode.edgeC == null) {
                        //If the edge of C is null and we need it then make it.
                        currentNode.edgeC = new Node( currentNode, C );
                        //currentNode.edgeC.parent = currentNode;
                        //currentNode.edgeC.edge = C;
                        currentNode = currentNode.edgeC;
                        nodeCount++;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeC;
                    }
                    break;
                }//end case.
                case G: {
                    if (currentNode.edgeG == null) {
                        //If the edge of G is null and we need it then make it.
                        currentNode.edgeG = new Node( currentNode, G );
                        //currentNode.edgeG.parent = currentNode;
                        //currentNode.edgeG.edge = G;
                        currentNode = currentNode.edgeG;
                        nodeCount++;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeG;
                    }//end else.
                    break;
                }//end case.
                case T: {
                    if (currentNode.edgeT == null) {
                        //If the edge of T is null and we need it then make it.
                        currentNode.edgeT = new Node( currentNode, T );
                        //currentNode.edgeT.parent = currentNode;
                        //currentNode.edgeT.edge = T;
                        currentNode = currentNode.edgeT;
                        nodeCount++;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeT;
                    }//end else.
                    break;
                }//end case.
            }//end switch.
        }//end for
        currentNode.isTerminator = true;
        if (currentNode.terminator == null) {
            currentNode.terminator = new Terminator();
        }
        currentNode.terminator.abundance++;
    }//end method.

    /**
     * List the entry points into the tree. The entry points will match the
     * 10th and 11th nt of the query sequence.
     */
    public void listStartPositions() {
        Stack<Node> s = new Stack<Node>();
        Stack<Integer> l = new Stack<Integer>();
        s.push(root);
        l.push(0);
        Node focusNode;
        int level;
        while (!s.isEmpty()) {
            focusNode = s.pop();
            level = l.pop();
            if (focusNode.edgeA != null) {
                s.push(focusNode.edgeA);
                l.push(level + 1);
                if (level == 11) {//10
                    if (focusNode.edge == A) {
                        getStartPositionList(A, A).add(focusNode.edgeA);
                        splitListCount++;
                    }
                    if (focusNode.edge == C) {
                        getStartPositionList(C, A).add(focusNode.edgeA);
                        splitListCount++;
                    }
                    if (focusNode.edge == G) {
                        getStartPositionList(G, A).add(focusNode.edgeA);
                        splitListCount++;
                    }
                    if (focusNode.edge == T) {
                        getStartPositionList(T, A).add(focusNode.edgeA);
                        splitListCount++;
                    }
                }
            }
            if (focusNode.edgeC != null) {
                s.push(focusNode.edgeC);
                l.push(level + 1);
                if (level == 11) {
                    if (focusNode.edge == A) {
                        getStartPositionList(A, C).add(focusNode.edgeC);
                        splitListCount++;
                    }
                    if (focusNode.edge == C) {
                        getStartPositionList(C, C).add(focusNode.edgeC);
                        splitListCount++;
                    }
                    if (focusNode.edge == G) {
                        getStartPositionList(G, C).add(focusNode.edgeC);
                        splitListCount++;
                    }
                    if (focusNode.edge == T) {
                        getStartPositionList(T, C).add(focusNode.edgeC);
                        splitListCount++;
                    }
                }
            }
            if (focusNode.edgeG != null) {
                s.push(focusNode.edgeG);
                l.push(level + 1);
                if (level == 11) {//10
                    if (focusNode.edge == A) {
                        getStartPositionList(A, G).add(focusNode.edgeG);
                        splitListCount++;
                    }
                    if (focusNode.edge == C) {
                        getStartPositionList(C, G).add(focusNode.edgeG);
                        splitListCount++;
                    }
                    if (focusNode.edge == G) {
                        getStartPositionList(G, G).add(focusNode.edgeG);
                        splitListCount++;
                    }
                    if (focusNode.edge == T) {
                        getStartPositionList(T, G).add(focusNode.edgeG);
                        splitListCount++;
                    }
                }
            }
            if (focusNode.edgeT != null) {
                s.push(focusNode.edgeT);
                l.push(level + 1);
                if (level == 11) {//10
                    if (focusNode.edge == A) {
                        getStartPositionList(A, T).add(focusNode.edgeT);
                        splitListCount++;
                    }
                    if (focusNode.edge == C) {
                        getStartPositionList(C, T).add(focusNode.edgeT);
                        splitListCount++;
                    }
                    if (focusNode.edge == G) {
                        getStartPositionList(G, T).add(focusNode.edgeT);
                        splitListCount++;
                    }
                    if (focusNode.edge == T) {
                        getStartPositionList(T, T).add(focusNode.edgeT);
                        splitListCount++;
                    }
                }
            }
        }
    }//end method.

    /**
     * Obtain the entry nodes into the tree when allowing mismatches at position 11 in a duplex.
     * @param ntA position 10 nt.
     * @param ntB position 11 nt.
     * @return List of entry points to the category tree.
     */
    private ArrayList<Node> getStartPositionListMM11(byte ntA, byte ntB) {
          ArrayList<Node> startPositionList = null;
          startPositionList = new ArrayList<Node>();
          switch (ntA) {
              case A: {
                  startPositionList.addAll(start[0]);
                  startPositionList.addAll(start[1]);
                  startPositionList.addAll(start[2]);
                  startPositionList.addAll(start[3]);
                  break;
              }
              case C: {
                  startPositionList.addAll(start[4]);
                  startPositionList.addAll(start[5]);
                  startPositionList.addAll(start[6]);
                  startPositionList.addAll(start[7]);
                  break;
              }
              case G: {
                  startPositionList.addAll(start[8]);
                  startPositionList.addAll(start[9]);
                  startPositionList.addAll(start[10]);
                  startPositionList.addAll(start[11]);
                  break;
              }
              case T: {
                  startPositionList.addAll(start[12]);
                  startPositionList.addAll(start[13]);
                  startPositionList.addAll(start[14]);
                  startPositionList.addAll(start[15]);
                  break;
              }
        }
        return startPositionList;
    }//end method.

    /**
     * Gets the subtree with the root being the start position (entry point) to
     * start the alignment process.
     * @param ntA The 10th nucleotide.
     * @param ntB The 11th nucleotide.
     * @return A list of root nodes for each subtree matching the 10th and 11th nucleotide of the query transcript fragment.
     */
    private ArrayList<Node> getStartPositionList(byte ntA, byte ntB) {

        ArrayList<Node> startPositionList = null;
        switch (ntA) {
            case A: {
                switch (ntB) {
                    case A:
                        startPositionList = start[0];
                        break;
                    case C:
                        startPositionList = start[1];
                        break;
                    case G:
                        startPositionList = start[2];
                        break;
                    case T:
                        startPositionList = start[3];
                        break;
                }
                break;
            }
            case C: {
                switch (ntB) {
                    case C:
                        startPositionList = start[4];
                        break;
                    case A:
                        startPositionList = start[5];
                        break;
                    case G:
                        startPositionList = start[6];
                        break;
                    case T:
                        startPositionList = start[7];
                        break;
                }
                break;
            }
            case G: {
                switch (ntB) {
                    case G:
                        startPositionList = start[8];
                        break;
                    case A:
                        startPositionList = start[9];
                        break;
                    case C:
                        startPositionList = start[10];
                        break;
                    case T:
                        startPositionList = start[11];
                        break;
                }
                break;
            }
            case T: {
                switch (ntB) {
                    case T:
                        startPositionList = start[12];
                        break;
                    case A:
                        startPositionList = start[13];
                        break;
                    case C:
                        startPositionList = start[14];
                        break;
                    case G:
                        startPositionList = start[15];
                        break;
                }
                break;
            }
        }
        return startPositionList;
    }//end method.

    /**
     * Reset fields required for navigation towards the root of the new sub-tree.
     */
    private void U_init(Node startNode) {
        U_NG_NODE = startNode.parent;
        D_FOCUS_NODE = startNode;
        U_INDEX = 9;
        U_MG_MATCHING_NODE = U_NG_NODE;
        U_MG_LENGTH = 0;//replaces the clear function for gapped score arrays.
        U_SG_LENGTH = 0;
        U_NG_SCORE = 0.0f;
        U_NG_RULES_UNBROKEN = true;

        previous = true;
        U_SG_previous = false;
        U_MG_previous = false;

        BOTH_UP = false;
        NOTHING_VALID = false;
        NO_GAPPED_UP_ONLY = false;
        GAPPED_UP_ONLY = false;

        U_MG_RULES_UNBROKEN = new ArrayList<Boolean>();
        U_MG_SCORE = new ArrayList<Float>();
        U_SG_RULES_UNBROKEN = new ArrayList<Boolean>();
        U_SG_SCORE = new ArrayList<Float>();

        if(Data.CAT_TREE_VERY_VERBOSE){
            U_NG_srna = "";
            U_NG_pos = "";
            U_NG_mrna = "";
            U_SG_srna = new ArrayList<String>();
            U_SG_pos = new ArrayList<String>();
            U_SG_mrna = new ArrayList<String>();
            U_MG_srna = new ArrayList<String>();
            U_MG_pos = new ArrayList<String>();
            U_MG_mrna = new ArrayList<String>();
        }
    }//end method.

    /**
     * Tests if this nt pair is an exact match or not.  The test will only be applied
     * if the rules have not been broken on the Upwards No Gap alignment.
     * @return True if either the rules have been broken or there is an exact match.
     */
    private boolean U_NG_exactMatch() {
         /**
         * If the no gap score has already broken the rules, then we can't create any further
         * gapped alignments.  Also, if the rules are broken, we don't want to do anymore work
         * on the un gapped alignment, so return true to stop any further work on  this or the next.
         **/
        if( (useLess && U_NG_SCORE > 2.5) || U_NG_SCORE > max){ U_NG_RULES_UNBROKEN = false; return true;}

        if (U_NG_NODE.edge == sequence[U_INDEX]) {
            if(Data.CAT_TREE_VERY_VERBOSE){
                U_NG_srna = ""+(char)sequence[U_INDEX]+U_NG_srna;
                U_NG_pos = "|"+U_NG_pos;
                U_NG_mrna = ""+(char)U_NG_NODE.edge+U_NG_mrna;
            }
            previous = true;
            return true;
        } else {
            return false;
        }
    }//end method.

    /**
     * Tests to see if a gap can be made on either the srna (sequence) or mrna (tree) after a mismatch
     * has been found.
     * @return True if a gap was found.
     */
    private boolean U_gapFound() {

        if(!gapped){return false;}
        if(!U_NG_RULES_UNBROKEN){return true;}
        boolean gapFound = false;
        if (previous) {
            if (U_INDEX > 0 && U_NG_NODE.edge == sequence[U_INDEX - 1]) {

                if( !useLess || (useLess && U_NG_SCORE + 1.0f <= 2.5F) ){
                    gapFound = true;
                    U_MG_SCORE.add(U_MG_LENGTH, U_NG_SCORE + 1.0f);
                    U_MG_RULES_UNBROKEN.add(U_MG_LENGTH, true);
                    U_MG_LENGTH++;
                    U_MG_VALID = true;

                    if(Data.CAT_TREE_VERY_VERBOSE){
                        String sRNA = U_NG_srna;
                        String positions = U_NG_pos;
                        String mRNA = U_NG_mrna;

                        sRNA = ""+(char)sequence[U_INDEX] + sRNA;
                        positions = " "+positions;
                        mRNA = ""+"-"+mRNA ;
                        U_MG_srna.add(sRNA);
                        U_MG_pos.add(positions);
                        U_MG_mrna.add(mRNA);
                    }//end very verbose.
                }


            }

            if (U_NG_NODE.parent.edge == sequence[U_INDEX]) {

                if(!useLess || (useLess && U_NG_SCORE + 1.0f <= 2.5F)){
                    gapFound = true;
                    U_SG_SCORE.add(U_SG_LENGTH, U_NG_SCORE + 1.0f);
                    U_SG_RULES_UNBROKEN.add(U_SG_LENGTH,  true);
                    U_SG_LENGTH++;
                    U_SG_VALID = true;

                    if(Data.CAT_TREE_VERY_VERBOSE){
                        String sRNA = U_NG_srna;
                        String positions = U_NG_pos;
                        String mRNA = U_NG_mrna;

                        sRNA = "-"+sRNA;
                        positions = " "+positions;
                        mRNA = ""+(char)U_NG_NODE.edge+mRNA ;
                        U_SG_srna.add(sRNA);
                        U_SG_pos.add(positions);
                        U_SG_mrna.add(mRNA);
                    }//end very verbose.
                }

            }
        }
        if (gapFound) {
            U_NG_mismatch();
        }
        return gapFound;
    }//end method.

    /**
     * Tests to see what kind of mismatch has been found (half or full) and apply the rules
     * to the No Gapped upwards alignment process.
     */
    private void U_NG_mismatch() {

        if(Data.CAT_TREE_VERY_VERBOSE){
                U_NG_srna = ""+(char)sequence[U_INDEX]+U_NG_srna;
                U_NG_mrna = ""+(char)U_NG_NODE.edge + U_NG_mrna;
        }

        if ((U_NG_NODE.edge == C && sequence[U_INDEX] == T) || (U_NG_NODE.edge == A && sequence[U_INDEX] == G)) {
            U_NG_SCORE += 0.5f;
            if(Data.CAT_TREE_VERY_VERBOSE){
                U_NG_pos = 'o' +  U_NG_pos;
            }
        } else {
            U_NG_SCORE += 1.0f;
            if(Data.CAT_TREE_VERY_VERBOSE){
                U_NG_pos = ' '+ U_NG_pos;
            }
        }
        /**
         * Here we do the rules check.  Firstly, have we exceeded the 2.5 maximum number of mismatches
         * allowed in U_NG_pos 1-12? Secondly, do we have an adjacent mismatches in U_NG_pos 2-12.
         * We test this by seeing if the previous nt complementarity was a mismatch, and this complementarity
         * is a mismatch (because we are inside the mismatch function)?  We should allow adjacent mismatches
         * in U_NG_pos 1 and 2, so we test that we are not at the first position (U_INDEX > 0).
         */
        if ( (useLess && U_NG_SCORE > 2.5f) || (!previous && U_INDEX > 0)) {
            U_NG_RULES_UNBROKEN = false;
        }else if(U_NG_SCORE > max) {
            U_NG_RULES_UNBROKEN = false;
        }
        //We have recorded the mismatch - so set the flag and let anybody else know that we found
        //and recorded a mismatch on this nt comparision.
        previous = false;
    }//end method.

    /**
     * Sets the logic flags according to what valid alignments were found when traversing
     * up the tree. This method should be called once the root of the tree is reached when
     * searching up the tree for a valid alignment.  This function ONLY tests to see the
     * possible outcomes from searching up the tree and sets the flags as needed. <br><br>
     * NOTE: The possibilities could be:<br><br>
     * <b>flag NOTHING_VALID</b> = true if no valid alignment was found when searching up the tree.<br><br>
     * <b>flag NO_GAPPED_UP_ONLY</b> = true if ONLY a valid un-gapped alignment was found when searching up the tree.<br><br>
     * <b>flag GAPPED_UP_ONLY</b> = true if ONLY a valid gapped alignment in either srna or mrna was found when searching up the tree.<br><br>
     * <b>flag BOTH_UP</b> = true if BOTH valid gapped and un-gapped alignments were found when searching up the tree.<br>
     * Only one of these flags will be set to true at any one point.
     **/
    private void setAdvanceLogic() {
        //RESET THE FLAGS FROM ANY PREVIOUS CALL BY THIS FUNCTION!
        NOTHING_VALID = false;
        NO_GAPPED_UP_ONLY = false;
        GAPPED_UP_ONLY = false;
        BOTH_UP = false;
        //This will be set to true if we have a valid gapped alignment from searching up the tree.
        boolean haveGapped = false;
        //See if we have a valid gapped alignment on the U_NG_srna strand from searching up the tree.
        for(int i = 0; i < U_SG_RULES_UNBROKEN.size(); i++){
            //Do the test to see if the rules are unbroken.
            if(U_SG_RULES_UNBROKEN.get(i)){
                //They are, so set the flag and get outa this loop.
                haveGapped = true;
                break;
            }//end if.
        }//end for.
        //If we didn't find a gapped alignment on the srna then test the mrna.
        if(!haveGapped){
            //Go through any mrna gapped alignments we have.
            for(int i = 0; i < U_MG_RULES_UNBROKEN.size(); i++){
                //Test to see if we have a valid alignment.
                if(U_MG_RULES_UNBROKEN.get(i)){
                    //We do, so set the flag to true and get outa this loop.
                    haveGapped = true;
                    break;
                }//end if.
            }//end for.
        }//end if.
        //Set the logic for advancing down the tree.
        //If we don't have a gapped and we dont have an ungapped - then there is nothing valid
        //to search for down the tree.
        if(haveGapped == false && U_NG_RULES_UNBROKEN == false){
            NOTHING_VALID = true;
            return;
        }//end if.
        //If we don't have a gapped alignment, but we do have an ungapped alignment up the tree.
        if(haveGapped == false && U_NG_RULES_UNBROKEN == true){
            //Then set the flag for no gaps up only.
            NO_GAPPED_UP_ONLY = true;
        }
        //Else if we have a gapped alingment upwards and no gapped upwards.
        if(haveGapped == true && U_NG_RULES_UNBROKEN == false){
            //Then set the gapped up only to be true.
            GAPPED_UP_ONLY = true;
        }//Eles if we have a valid gapped and valid ungapped alignments up the tree.
        if(haveGapped == true && U_NG_RULES_UNBROKEN == true){
            //Then set the both valid alignments flag to true.
            BOTH_UP = true;
        }//end if.
    }//end method.

    /**
     * Process for matches, mismatches and rule application for Upwards Srna Gapped
     * alignments.
     */
    private void U_SG() {
        U_SG_VALID = false;
        if (U_NG_NODE.edge == sequence[U_INDEX + 1]) {
            U_SG_previous = true;
            U_SG_VALID = true;
            if(Data.CAT_TREE_VERY_VERBOSE){
                for (int i = 0; i < U_SG_LENGTH; i++) {
                    U_SG_srna.set(i,""+(char)sequence[U_INDEX+1] + U_SG_srna.get(i));
                    U_SG_pos.set(i, "|"+U_SG_pos.get(i));
                    U_SG_mrna.set(i, ""+(char)U_NG_NODE.edge+U_SG_mrna.get(i));
                }
             }
        } else if ((U_NG_NODE.edge == A && sequence[U_INDEX + 1] == G) || (U_NG_NODE.edge == C && sequence[U_INDEX + 1] == T)) {
            for (int i = 0; i < U_SG_LENGTH; i++) {
                U_SG_SCORE.set(i, U_SG_SCORE.get(i) + 0.5f);
                if( useLess && U_SG_SCORE.get(i) > 2.5f){U_SG_RULES_UNBROKEN.set(i, false); }
                if(U_SG_SCORE.get(i) > max){ U_SG_RULES_UNBROKEN.set(i, false); }
                else if(this.useUpAm && !U_SG_previous && U_INDEX + 1 > 0){ U_SG_RULES_UNBROKEN.set(i, false); }
                if(U_SG_RULES_UNBROKEN.get(i)){U_SG_VALID = true;}
                if(Data.CAT_TREE_VERY_VERBOSE){
                    U_SG_srna.set(i,""+(char)sequence[U_INDEX+1] + U_SG_srna.get(i));
                    U_SG_pos.set(i, "o"+U_SG_pos.get(i));
                    U_SG_mrna.set(i, ""+(char)U_NG_NODE.edge+U_SG_mrna.get(i));
                }
            }
            U_SG_previous = false;
        } else {
            for (int i = 0; i < U_SG_LENGTH; i++) {
                U_SG_SCORE.set(i, U_SG_SCORE.get(i) + 1.0f);
                if( useLess && U_SG_SCORE.get(i) > 2.5f){ U_SG_RULES_UNBROKEN.set(i, false); }
                if(U_SG_SCORE.get(i) > max){ U_SG_RULES_UNBROKEN.set(i, false ); }
                else if(this.useUpAm && !U_SG_previous && U_INDEX + 1 > 0){ U_SG_RULES_UNBROKEN.set(i, false);}
                if(U_SG_RULES_UNBROKEN.get(i)){U_SG_VALID = true;}
                if(Data.CAT_TREE_VERY_VERBOSE){
                   U_SG_srna.set(i,""+(char)sequence[U_INDEX+1] + U_SG_srna.get(i));
                    U_SG_pos.set(i, " "+U_SG_pos.get(i));
                    U_SG_mrna.set(i, ""+(char)U_NG_NODE.edge+U_SG_mrna.get(i));
                }
            }
            U_SG_previous = false;
        }//end else.
        if (U_INDEX == 0) {
            previousNode = U_NG_NODE;
            U_NG_NODE = U_NG_NODE.parent;
            U_INDEX--;
            putItBack = true;
            U_SG();
        }//end if.
        if (putItBack) {
            U_INDEX++;
            U_NG_NODE = previousNode;
            putItBack = false;
        }//end if.
    }//end method.

    /**
     * Process for matches, mismatches and application of rules for Upwards MRNA Gapped
     * alignments.
     */
    private void U_MG() {
        U_MG_VALID = false;
        if (U_MG_MATCHING_NODE.edge == sequence[U_INDEX]) {
            U_MG_previous = true;
            //We have a valid alignment still because we check to see that there was one before
            //We entered into this procedure.
            U_MG_VALID = true;
            if(Data.CAT_TREE_VERY_VERBOSE){
                for (int i = 0; i < U_MG_LENGTH; i++) {
                    U_MG_srna.set(i, ""+(char)sequence[U_INDEX]+U_MG_srna.get(i));
                    U_MG_pos.set(i,"|"+U_MG_pos.get(i));
                    U_MG_mrna.set(i, ""+(char)U_MG_MATCHING_NODE.edge+U_MG_mrna.get(i));
                }
            }
        } else if ((U_MG_MATCHING_NODE.edge == A && sequence[U_INDEX] == G) || (U_MG_MATCHING_NODE.edge == C && sequence[U_INDEX] == T)) {
            for (int i = 0; i < U_MG_LENGTH; i++) {
                U_MG_SCORE.set(i, U_MG_SCORE.get(i) + 0.5f);
                if( useLess && U_MG_SCORE.get(i) > 2.5f){ U_MG_RULES_UNBROKEN.set(i, false); }
                else if(U_MG_SCORE.get(i) > max){U_MG_RULES_UNBROKEN.set(i,false); }
                else if(this.useUpAm && !U_MG_previous && U_INDEX > 0){ U_MG_RULES_UNBROKEN.set(i, false); }
                if(U_MG_RULES_UNBROKEN.get(i)){U_MG_VALID = true;}

                if(Data.CAT_TREE_VERY_VERBOSE){
                   U_MG_srna.set(i, ""+(char)sequence[U_INDEX]+U_MG_srna.get(i));
                   U_MG_pos.set(i,"o"+U_MG_pos.get(i));
                   U_MG_mrna.set(i, ""+(char)U_MG_MATCHING_NODE.edge+U_MG_mrna.get(i));
                }
            }
            U_MG_previous = false;
        } else {
            for (int i = 0; i < U_MG_LENGTH; i++) {
                U_MG_SCORE.set(i,  U_MG_SCORE.get(i) + 1.0f);
                if( useLess && U_MG_SCORE.get(i) > 2.5f){ U_MG_RULES_UNBROKEN.set(i, false); }
                else if(U_MG_SCORE.get(i) > max){U_MG_RULES_UNBROKEN.set(i, false); }
                else if(this.useUpAm && !U_MG_previous && U_INDEX > 0){ U_MG_RULES_UNBROKEN.set(i, false); }
                if(U_MG_RULES_UNBROKEN.get(i)){U_MG_VALID = true;}
                if(Data.CAT_TREE_VERY_VERBOSE){
                   U_MG_srna.set(i, ""+(char)sequence[U_INDEX]+U_MG_srna.get(i));
                   U_MG_pos.set(i," "+U_MG_pos.get(i));
                   U_MG_mrna.set(i, ""+(char)U_MG_MATCHING_NODE.edge+U_MG_mrna.get(i));
                }
            }
            U_MG_previous = false;
        }
    }//end method.

    /**
     * Sets the bestGapScoreDown and connected fields to the best
     * gapped score down.
     */
    private void setBestGappedDown() {
        bestGapScoreDown = 999.999f;
        bestGapDownStrand = 0;
        bestGapScoreDownIndex = -99;

        for(int i = 0; i < D_SG_SCORE.size();i++){
            if(D_SG_RULES_UNBROKEN.get(i) == true){
                if(D_SG_SCORE.get(i) <= bestGapScoreDown){
                    bestGapScoreDown = D_SG_SCORE.get(i);
                    bestGapDownStrand = SRNA_STRAND;
                    bestGapScoreDownIndex = i;
                }
            }
        }
        for(int i = 0; i < D_MG_SCORE.size();i++){
            if(D_MG_RULES_UNBROKEN.get(i) == true){
                if(D_MG_SCORE.get(i) <= bestGapScoreDown){
                    bestGapScoreDown = D_MG_SCORE.get(i);
                    bestGapDownStrand = MRNA_STRAND;
                    bestGapScoreDownIndex = i;
                }
            }
        }
    }//end method.

    /**
     * Sets the partial downwards score to the best upwards score available for D_NG.
     * An un-gapped alignment upwards takes precedence over a gapped alignment upwards.
     */
    private void D_NG_setBestScore() {
        //Set the flag for using ungapped alignment if both a gapped alignment and ungapped alignment was found.
        usingNoGapForUP_for_noGapDown = false;
        //Set the best score upwards for no gap to something we know that it would never possibly be.
        U_NG_BEST_SCORE_FOUND = 99.99f;
        //Set the flag to UNIDENTIFIED.
        BEST_SCORE_UP_STRAND = UNIDENTIFIED;
        //If the only valid alignment is ungapped upwards.
        if(NO_GAPPED_UP_ONLY){
            //Then simply set the ungapped upwards score.
            U_NG_BEST_SCORE_FOUND = U_NG_SCORE;
            usingNoGapForUP_for_noGapDown = true;
        }//Else if we have only a gapped up score or both gapped and ungapped upwards.
        else if(GAPPED_UP_ONLY || BOTH_UP){
            //Find the best score upwards for gapped on the srna.
            for(int i = 0; i < U_SG_RULES_UNBROKEN.size();i++){
                //If we have a gapped and the rules are unbroken.
               if(U_SG_RULES_UNBROKEN.get(i) == true){
                   //If that score is less than the current best score found.
                   if(U_SG_SCORE.get(i) <= U_NG_BEST_SCORE_FOUND){
                       //Set the current best score to this one.
                       U_NG_BEST_SCORE_FOUND = U_SG_SCORE.get(i);
                       BEST_SCORE_UP_STRAND = SRNA_STRAND;
                       bestGapScoreUpIndex = i;
                   }//end if.
               }//end if.
            }//end for.
            //For each of the gapped alignments found on the mrna.
            for(int i = 0; i < U_MG_RULES_UNBROKEN.size();i++){
               //If the rules havn't been broken.
               if(U_MG_RULES_UNBROKEN.get(i) == true){
                   //And, if this score is better than the previous score.
                   if(U_MG_SCORE.get(i) <= U_NG_BEST_SCORE_FOUND){
                       //Set the best score to this one.
                       U_NG_BEST_SCORE_FOUND = U_MG_SCORE.get(i);
                       BEST_SCORE_UP_STRAND = MRNA_STRAND;
                       bestGapScoreUpIndex = i;
                   }//end if.
               }//end if.
            }//end for.
            //If both gapped and ungapped, give priority to an ungapped score.
            if(BOTH_UP){
                //If ungapped score is better than the current gapped score.
               if(U_NG_SCORE <= U_NG_BEST_SCORE_FOUND){
                   //Set the ungapped score to be the best score.
                   U_NG_BEST_SCORE_FOUND = U_NG_SCORE;
                   usingNoGapForUP_for_noGapDown = true;
               }//else leave it as it is.
            }//end if.
        }//end else if.
    }//end method.

    /**
     * Initialises  lists and fields for navigating down the tree.
     */
    private void D_init() {
        if(Data.CAT_TREE_VERY_VERBOSE){
            D_MG_mrna.clear();
            D_MG_srna.clear();
            D_MG_pos.clear();
            D_SG_mrna.clear();
            D_SG_pos.clear();
            D_SG_srna.clear();
            D_NG_srna = new byte[26];
            D_NG_pos = new byte[26];
            D_NG_mrna = new byte[26];
        }

        D_MG_RULES_UNBROKEN.clear();
        D_SG_RULES_UNBROKEN.clear();
        D_SG_SCORE.clear();
        D_MG_SCORE.clear();
        D_SG_LEVEL_INTO_EXISTENCE.clear();
        D_MG_LEVEL_INTO_EXISTENCE.clear();
        nodes.push(D_FOCUS_NODE);
        treeLevel.push(10);

        D_NG_SCORES.push(0.0F);
        D_NG_RULES_UNBROKEN_stack.push(true);
    }//end method.

    /**
     * Perform the push and the pop for traversing down the tree.
     * This function is responsible for all push and pop operations, including testing
     * to see if an alignment is no longer valid because of a push/pop operation.
     */
    private void D_pushAndPop() {
        /**
         * The previous call to this function pushed nodes onto the stack. When we pushed those nodes onto the
         * stack we did not know what the NG alignment score associated with their parent node. Therefore we
         * Push the children of the focus node onto the stack, perform the alignment with the focus node and
         * then push the associated score and rules state onto the stack, therefore associating the children of
         * the focus node with the correct score and rules state of their parent i.e. the focus node.
         **/
        //For each node which was pushed on the previous function call.
        for(int i = 0; i < pushed; i++){
            //Push the score found with the curren node/sequence index onto the stack for the children.
            D_NG_SCORES.push(D_NG_SCORE);
            //Push the rules state of the NG onto the stack for the children.
            D_NG_RULES_UNBROKEN_stack.push(D_NG_RULES_UNBROKEN);
        }//end for.
        //POP! ready for the next level down the tree.
        D_NG_SCORE = D_NG_SCORES.pop();
        D_NG_RULES_UNBROKEN = D_NG_RULES_UNBROKEN_stack.pop();
        D_FOCUS_NODE = nodes.pop();
        D_INDEX = treeLevel.pop();
        //Reset the number of children pushed onto the stack.
        pushed = 0;
        /**
         * TEST:
         * If we have popped up the tree to a level before or equal to a previous root,
         * Then any previous alignments that we may have found with gaps are no longer valid.
         */
        //For each alignment with a gap on the srna which has come into existence.
        for(int i = 0; i < D_SG_LEVEL_INTO_EXISTENCE.size(); i++){
            //If the current level/index is before that gapped alignment existed.
            if(D_INDEX <= D_SG_LEVEL_INTO_EXISTENCE.get(i)){
                //Dont do anything with it as it is not valid and belongs to a previous path.
                D_SG_RULES_UNBROKEN.set(i, false);
            }//end if.
        }//end for.
        //Do the same for gapped alingments found on the mrna.
        for(int i = 0; i < D_MG_LEVEL_INTO_EXISTENCE.size(); i++){
            if(D_INDEX <= D_MG_LEVEL_INTO_EXISTENCE.get(i)){
                D_MG_RULES_UNBROKEN.set(i, false);
            }//end if.
        }//end for.
        //Push the child nodes of the current focus node onto the stack along with their level/index and count how many children we push.
        if(D_INDEX < sequence.length){
            if(D_FOCUS_NODE.edgeA!=null){
                nodes.push(D_FOCUS_NODE.edgeA);
                treeLevel.push(D_INDEX+1);
                pushed++;
            }//end if.
            if(D_FOCUS_NODE.edgeC!=null){
                nodes.push(D_FOCUS_NODE.edgeC);
                treeLevel.push(D_INDEX+1);
                pushed++;
            }//end if.
            if(D_FOCUS_NODE.edgeG!=null){
                nodes.push(D_FOCUS_NODE.edgeG);
                treeLevel.push(D_INDEX+1);
                pushed++;
            }//end if.
            if(D_FOCUS_NODE.edgeT!=null){
                nodes.push(D_FOCUS_NODE.edgeT);
                treeLevel.push(D_INDEX+1);
                pushed++;
            }//end if.
        }//end if.
    }//end method.

    /**
     * Perform a test for <b>Downwards No Gapped</b> alignment to see if the current
     * node nt and sequence nt match exactly. A straight forward comparison of
     * nucleotides for un-gapped downwards alignments.
     * @return True if an exact match is found at the current un-gapped position
     * in the tree and parameter sequence.
     */
    private boolean D_NG_exactMatch() {
        //Dont allow any further work to be done if the alignment has broken the rules.
        if(!D_NG_RULES_UNBROKEN){return true;}
        //Do we have an exact match at this position?
        if(D_FOCUS_NODE.edge == sequence[D_INDEX]){
            if(Data.CAT_TREE_VERY_VERBOSE){
                D_NG_srna[D_INDEX] = sequence[D_INDEX];
                D_NG_pos[D_INDEX] = '|';
                D_NG_mrna[D_INDEX] = D_FOCUS_NODE.edge;
            }
            //Yes we do, get outa here.
            return true;
        }else{
            //No we don't, get outa here.
            return false;
        }//end else.
    }//end method.

    /**
     * Tests for a gap on either srna or mrna if a mismatch has been found along the No Gapped
     * alignment process.  This function should only be called <b>AFTER</b> testing for a mismatch
     * in the un-gapped alignment process using the function D_NG_exactMatch().
     * No potential gapped alignments will be added if the un-gapped alignment has broken
     * the rules.
     * @return true iff a gap is possible on either srna (sequence) or mrna (tree).
     */
    private boolean D_gapFound() {
        if(!gapped){return false;}
        /**
         * If the situation arises where we have not made any gapped alignments yet, but
         * there has been more than 2.5 mismatches in U_NG_pos 1-12 of srna using
         * the BEST NO GAP DOWN + NO GAP UP score combined - there is a chance that this
         * could be missed by the alignment rules when creating a gapped alignment
         * further down the line. Therefore, we do a test at nt 11 in function
         * D_NG_mismatch() and if this situation is true, then we block all further
         * gapped alignment creation as they would not meet the rules.
         */
        if(useLess && D_INDEX <= 11 && U_NG_SCORE + D_NG_SCORE > 2.5F){
            return false;
        }//end if.
        //If the rules are broken..
        if(!D_NG_RULES_UNBROKEN){
            //return true because we don't want to do any further un-necessary processing.
            return true;
        }//end if.
        //Calculate the score we will have if we were to make a gap.
        scratchScore = U_NG_SCORE + D_NG_SCORE + 1.0f;
        //If the we are at or below position 12 in the srna and the score is greater than 2.5.
        if(useLess && D_INDEX <= 11 && scratchScore > 2.5f){
            //Then the rules would be broken - so don't make a new gapped alignment which is already broken.
            return false;
        }//end if.
        if(scratchScore > max || !U_NG_RULES_UNBROKEN){
            //Then don't make a gap which has already broken the rules.
            return false;
        }//end if.
        //Test to see if the previous nt alignment was a match.
        if(D_FOCUS_NODE.parent.edge == sequence[D_INDEX -1]){
            //Prepare for finding a gap - we return this value and it is set to true if a gap is found on either strand.
            D_GAP_FOUND = false;
            //DO D SG
            if(!nodes.isEmpty() && nodes.peek().edge == sequence[D_INDEX]){
                //A gap can be made, so make the gap and add it to the list of gapped alignments for further alignment processing.
                D_SG_SCORE.add(scratchScore);
                D_SG_RULES_UNBROKEN.add(true);
                D_SG_LEVEL_INTO_EXISTENCE.add(D_INDEX);
                D_GAP_FOUND = true;

                if(Data.CAT_TREE_VERY_VERBOSE){
                    System.out.println("S_GAP CREATED: score: "+scratchScore);
                    D_SG_srna.add(new String(D_NG_srna).substring(0, D_INDEX)+"-");
                    D_SG_pos.add(new String(D_NG_pos).substring(0, D_INDEX) +" ");
                    D_SG_mrna.add(new String(D_NG_mrna).substring(0,D_INDEX)+(char)D_FOCUS_NODE.edge);
                }
            }//end if.
            //DO D MG
            //Test to see if we can extend along the sequence.
            if(D_INDEX+1 < sequence.length){
                //We can so test to see if a gap can be made.
                if(D_FOCUS_NODE.edge == sequence[D_INDEX+1] ){
                    //It can, so add the gapped alignment to the list for further alignment processing.
                    D_MG_SCORE.add(scratchScore);
                    D_MG_RULES_UNBROKEN.add(true);
                    D_MG_LEVEL_INTO_EXISTENCE.add(D_INDEX);
                    D_GAP_FOUND = true;

                    if(Data.CAT_TREE_VERY_VERBOSE){
                        System.out.println("M_GAP CREATED: score: "+scratchScore);
                        D_MG_srna.add(new String(D_NG_srna).substring(0,D_INDEX)+(char)sequence[D_INDEX]);
                        D_MG_pos.add(new String(D_NG_pos).substring(0, D_INDEX) + " ");
                        D_MG_mrna.add(new String(D_NG_mrna).substring(0, D_INDEX) + "-");
                    }
                }//end if.
            }//end if.
        }//end if.
        //If a gap was found, then run the NG mismatch procedure as it will not be
        //entered in upon return of this function.
        if(D_GAP_FOUND){
            D_NG_mismatch();
        }
        //Return if we found a gap or not.
        return D_GAP_FOUND;
    }//end method.

    /**
     * Logic to continue advance descendents of entry node.
     * @return
     */
    private boolean advanceDown() {

        boolean stillValidAlignmentPossible = D_NG_RULES_UNBROKEN;

        if(!stillValidAlignmentPossible){
            for(int i = 0; i < D_MG_RULES_UNBROKEN.size(); i++){
                if(D_MG_RULES_UNBROKEN.get(i)){
                    stillValidAlignmentPossible = true;
                }
            }
        }
        if(!stillValidAlignmentPossible){
            for(int i = 0; i < D_SG_RULES_UNBROKEN.size(); i++){
                if(D_SG_RULES_UNBROKEN.get(i)){
                    stillValidAlignmentPossible = true;
                }
            }
        }
      return stillValidAlignmentPossible;
    }//end method.

    /**
     * Process base pairs for Downwards No Gap alignment after a mismatch has been found. This method is responsible for testing
     * for what type of mismatch has been found and adjusting the D_NG score as necessary i.e.
     * full or half (G-U) mismatch.  Alignment rules are applied after testing for the type of mismatch.
     * <br><br><b>Note:<br>This method does not test for a mismatch, it ONLY finds out what kind
     * of mismatch has been found (half or full) and adjusts the score and applies the alignment rules.</b>
     */
    private void D_NG_mismatch() {
        if( (D_FOCUS_NODE.edge == C && sequence[D_INDEX] == T) || (D_FOCUS_NODE.edge == A && sequence[D_INDEX] == G) ){
            D_NG_SCORE += 0.5f;
            scratchSpace = U_NG_BEST_SCORE_FOUND + D_NG_SCORE;
            if(Data.CAT_TREE_VERY_VERBOSE){
                D_NG_srna[D_INDEX] = sequence[D_INDEX];
                D_NG_pos[D_INDEX] = 'o';
                D_NG_mrna[D_INDEX] = D_FOCUS_NODE.edge;
            }
            //Test to see if there has been >2.5 mismatches in U_NG_pos 1-12
            if(useLess && D_INDEX <= 11 && scratchSpace >2.5f){
                D_NG_RULES_UNBROKEN = false;
            }
            else if(scratchSpace > max){
                D_NG_RULES_UNBROKEN = false;
            }
            //Test to see if there has been more than Data.maxAlignmentScore mismatches in total.
            else if(scratchSpace > max){
                D_NG_RULES_UNBROKEN = false;
            }
            //Test to see if there are more than two adjacent mismatches.
            else if(this.useDownAm && D_FOCUS_NODE.parent.edge != sequence[D_INDEX - 1] && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 2]){
                D_NG_RULES_UNBROKEN = false;
            }
        }else{
            D_NG_SCORE += 1.0f;
            scratchSpace = U_NG_BEST_SCORE_FOUND + D_NG_SCORE;
            if(Data.CAT_TREE_VERY_VERBOSE){
                D_NG_srna[D_INDEX] = sequence[D_INDEX];
                D_NG_pos[D_INDEX] = ' ';
                D_NG_mrna[D_INDEX] = D_FOCUS_NODE.edge;
            }
            //Test to see if there has been >2.5 mismatches in U_NG_pos 1-12
            if(useLess && D_INDEX <= 11 && scratchSpace > 2.5f){
                D_NG_RULES_UNBROKEN = false;
            }
            else if(scratchSpace > max){
                D_NG_RULES_UNBROKEN = false;
            }
            //Test to see if there has been more than Data.maxAlignmentScore mismatches in total.
            else if(scratchSpace > max){
                D_NG_RULES_UNBROKEN = false;
            }
            //Test to see if there are more than two adjacent mismatches.
            else if(this.useDownAm && D_FOCUS_NODE.parent.edge != sequence[D_INDEX - 1] && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 2]){
                D_NG_RULES_UNBROKEN = false;
            }
        }//end else.
    }//end method.

    /**
     * Process base pairs for any and all Downwards <b>Mrna</b> Gapped alignments found.
     * This function looks after all processing of Downwards Mrna Gapped alignments
     * including matches, mismatches (half & full) and identifies when any of the
     * alignment rules are broken.
     * <br><br><b>NOTE:<br>D = We are performing alignments as we traverse down the tree.
     * <br>MG = Mrna/transcript Gapped alignments ONLY.</b>
     */
    private void D_MG() {
        HAS_VALID_MG_DOWN = false;
        //If we have an exact match for the Down Mrna Gap alignments.
        if(D_FOCUS_NODE.parent.edge == sequence[D_INDEX]){
            for(int i = 0; i < D_MG_SCORE.size(); i++){
                if(D_MG_RULES_UNBROKEN.get(i)){
                    HAS_VALID_MG_DOWN = true;
                }
                if(Data.CAT_TREE_VERY_VERBOSE){
                    if(D_MG_RULES_UNBROKEN.get(i)){
                        D_MG_srna.set(i, D_MG_srna.get(i)+(char)sequence[D_INDEX]);
                        D_MG_pos.set(i, D_MG_pos.get(i)+"|");
                        D_MG_mrna.set(i, D_MG_mrna.get(i)+(char)D_FOCUS_NODE.parent.edge);
                    }
                }
            }
            //Just get out of here because there is nothing to do.
            return;
        }else{//Else we have a mismatch - so process the mismatch.
            //If we have a G-U (G-T) pair.
            if( (D_FOCUS_NODE.parent.edge == A && sequence[D_INDEX] == G) || (D_FOCUS_NODE.parent.edge == C && sequence[D_INDEX] == T)){
                //Then update every Down Mrna Gapped alignment with a +0.5 mismatch score.
                for(int i = 0; i < D_MG_SCORE.size(); i++) {
                    //If the rules have already been broken - don't do anything because it's a waste of time.
                    if(!D_MG_RULES_UNBROKEN.get(i)){
                        continue;
                    }
                    scratchScore = D_MG_SCORE.get(i)+0.5F;
                    D_MG_SCORE.set(i,scratchScore);
                    if(Data.CAT_TREE_VERY_VERBOSE){
                        D_MG_srna.set(i, D_MG_srna.get(i)+(char)sequence[D_INDEX]);
                        D_MG_pos.set(i, D_MG_pos.get(i)+"o");
                        D_MG_mrna.set(i, D_MG_mrna.get(i)+(char)D_FOCUS_NODE.parent.edge);
                    }
                    //Test to see if there has been more than max mismatches in total.
                    if(scratchScore > max){
                        D_MG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }
                    //Test to see if there are more than two adjacent mismatches.
                    else if(this.useDownAm && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 1] && D_FOCUS_NODE.parent.parent.parent.edge != sequence[D_INDEX - 2]){
                        D_MG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }else{
                        HAS_VALID_MG_DOWN = true;
                    }
                }//end for each gapped alignment.
                return;
            }else{//Else we have a full mismatch.
                //Update every Down Mrna Gapped alignment found with a +1 mismatch score.
                for(int i = 0; i < D_MG_SCORE.size(); i++){
                    //If the rules have already been broken - don't do anything because it's a waste of time.
                    if(!D_MG_RULES_UNBROKEN.get(i)){
                        continue;
                    }
                    scratchScore = D_MG_SCORE.get(i)+1.0F;
                    D_MG_SCORE.set(i,scratchScore);
                    if(Data.CAT_TREE_VERY_VERBOSE){
                        D_MG_srna.set(i, D_MG_srna.get(i)+(char)sequence[D_INDEX]);
                        D_MG_pos.set(i, D_MG_pos.get(i)+" ");
                        D_MG_mrna.set(i, D_MG_mrna.get(i)+(char)D_FOCUS_NODE.parent.edge);
                    }
                    //Test to see if there has been more than Data.maxAlignmentScore mismatches in total.
                    if(scratchScore > max){
                        D_MG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }
                    //Test to see if there are more than two adjacent mismatches.
                    else if(this.useDownAm && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 1] && D_FOCUS_NODE.parent.parent.parent.edge != sequence[D_INDEX - 2]){
                        D_MG_RULES_UNBROKEN.set(i, false);
                    }else{
                        HAS_VALID_MG_DOWN = true;
                    }
                }//end for each gapped alignment.
                return;
            }//end else.
        }//end if.
    }//end method.

    /**
     * Process base pairs for any and all Downwards <b>Srna</b> Gapped alignments found.
     * This function looks after all processing of Downwards <b>Srna</b> Gapped alignments
     * including matches, mismatches (half & full) and identifies when any of the
     * alignment rules are broken.
     * <br><br><b>NOTE:<br> D = We are performing alignments as we traverse down the tree.
     * <br>SG = Srna Gapped alignments ONLY.</b>
     */
    private void D_SG() {

        HAS_VALID_SG_DOWN = false;
        //If we have an exact match for downwards srna gapped alignments.
        if(D_INDEX-1 < sequence.length && D_FOCUS_NODE.edge == sequence[D_INDEX-1]){
            for(int i = 0; i < D_SG_srna.size(); i++){
                if(D_SG_RULES_UNBROKEN.get(i)){
                    HAS_VALID_SG_DOWN = true;
                }
                if(Data.CAT_TREE_VERY_VERBOSE){
                    if(D_SG_RULES_UNBROKEN.get(i)){
                       D_SG_srna.set(i, D_SG_srna.get(i)+(char)sequence[D_INDEX-1]);
                       D_SG_pos.set(i, D_SG_pos.get(i)+"|");
                       D_SG_mrna.set(i, D_SG_mrna.get(i)+(char)D_FOCUS_NODE.edge);
                    }
                }
            }
            //Do nothing and get out of here because all is good and there is nothing to do.
            return;
        }else{//We have a mismatch. What kind of mismatch do we have?
            //Test to see if we have a half mismatch.
            if( ( (D_INDEX-1 < sequence.length) && (D_FOCUS_NODE.edge == A && sequence[D_INDEX-1] == G) ) || ( (D_INDEX-1 < sequence.length) && (D_FOCUS_NODE.edge == C && sequence[D_INDEX-1] == T) ) ){
                //We do, so update the gapped srna alignments by 0.5.
                for(int i = 0; i < D_SG_SCORE.size(); i++){
                    //If the rules have already been broken - don't do anything because it's a waste of time.
                    if(!D_SG_RULES_UNBROKEN.get(i)){
                        continue;
                    }

                    scratchScore = D_SG_SCORE.get(i)+0.5F;
                    D_SG_SCORE.set(i,scratchScore);
                    if(Data.CAT_TREE_VERY_VERBOSE){
                        D_SG_srna.set(i, D_SG_srna.get(i)+(char)sequence[D_INDEX-1]);
                        D_SG_pos.set(i, D_SG_pos.get(i)+"o");
                        D_SG_mrna.set(i, D_SG_mrna.get(i)+(char)D_FOCUS_NODE.edge);
                    }

                    //Test to see if there has been more than Data.maxAlignmentScore mismatches in total.
                    if(scratchScore > max){
                        D_SG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }
                    //Test to see if there are more than two adjacent mismatches.
                    else if(this.useDownAm && D_FOCUS_NODE.parent.edge != sequence[D_INDEX - 2] && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 3]){
                        D_SG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }else{
                        HAS_VALID_SG_DOWN = true;
                    }
                }
                //All done, so get outa here.
                return;
            }else{//Else, we have a full mismatch, so update the srna gapped alignments by 1.
                for(int i = 0; i < D_SG_SCORE.size(); i++){
                    //If the rules have already been broken - don't do anything because it's a waste of time.
                    if(!D_SG_RULES_UNBROKEN.get(i)){
                        continue;
                    }
                    scratchScore = D_SG_SCORE.get(i)+1.0F;
                    D_SG_SCORE.set(i,scratchScore);
                    if(Data.CAT_TREE_VERY_VERBOSE){
                        D_SG_srna.set(i, D_SG_srna.get(i)+(char)sequence[D_INDEX-1]);
                        D_SG_pos.set(i, D_SG_pos.get(i)+" ");
                        D_SG_mrna.set(i, D_SG_mrna.get(i)+(char)D_FOCUS_NODE.edge);
                    }
                    //Test to see if there has been more than Data.maxAlignmentScore mismatches in total.
                    if(scratchScore > max){
                        D_SG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }
                    //Test to see if there are more than two adjacent mismatches.
                    else if(this.useDownAm && D_FOCUS_NODE.parent.edge != sequence[D_INDEX - 2] && D_FOCUS_NODE.parent.parent.edge != sequence[D_INDEX - 3]){
                        D_SG_RULES_UNBROKEN.set(i, false);
                        continue;
                    }else{
                        HAS_VALID_SG_DOWN = true;
                    }
                }//end for.
                //All done, so get outa here.
                return;
            }//end else.
        }//end if.

    }//end function.

    /**
     * Get the constant which represents the best alignment for a situation when we
     * have valid alignments up the tree for both NG and GU.  The possible outcomes
     * could be (NGU & NGD) v (NGU & GD) v (GU & NGD).
     * @return constant representing the best alignment up the tree.
     * <br><b>NOTE: Un-gapped alignments are given preference over gapped alignments.</b>
     * @see setBestGappedDown()
     */
    private int getBestBOTH_UP() {
        //Possibilities = (NGU & NGD) v (NGU & GD) v (GU & NGD)
        //Set the best gapped alignment which was found navigating down the tree to this base case.
        this.setBestGappedDown();
        //Set the value for the first possible outcome.
        float As = U_NG_SCORE + D_NG_SCORE;
        boolean okA = D_NG_RULES_UNBROKEN && As <= max;
        //Set the value for the seccond  possible outcome.
        float Bs = bestGapScoreDown;
        boolean okB = bestGapDownStrand != UNIDENTIFIED && Bs <= max;
        //Grab some memory for the third possible outcome.
        float Cs;
        boolean okC;
        //If we are using no gapped alignment as this was the best alignment found up the tree when compared to gapped srna and mrna alignments.
        if(usingNoGapForUP_for_noGapDown){
            //Then the state is the same as As
            Cs = As;
            okC = okA;
        }else{
            //Else we are using a gapped alignment (which has already been calculated) for the score up the tree so (GU & NGD)
            Cs = U_NG_BEST_SCORE_FOUND + D_NG_SCORE;
            okC  = D_NG_RULES_UNBROKEN && Cs <= max;
        }//end else.

        if(Data.CAT_TREE_VERY_VERBOSE){
            System.out.println("-----------------------------------");
            System.out.println("Tree.getBestBOTH_UP() = ");
            System.out.println("OKA: "+okA);
            System.out.println("As: "+As);
            System.out.println("OKB: "+okB);
            System.out.println("Bs: "+Bs);
            System.out.println("OKC: "+okC);
            System.out.println("Cs: "+Cs);
        }

        //Grab some memory and set it to an initial state.
        float value = 99.99f;
        int state = UNIDENTIFIED;
        //If state 3 is less than value and is okay - then set it.
        if(Cs < value && okC){value = Cs; state = 3;}
        //If state 2 is less than state 3 and is okay - then replace previous.
        if(Bs < value && okB){value = Bs; state = 2;}
        //If state 1 is better than the previous 2 then replace previous (Note: this also ensures precedence for NG alignments)
        if(As <= value && okA){state = 1;}

        if(Data.CAT_TREE_VERY_VERBOSE){
            System.out.println("STATE: "+state);
            System.out.println("-----------------------------------");
        }

        //Tell the caller what the best situation was.
        return state;

    }//end method.

    /**
     * Selects and sets the best alignment possibility from all those found when a base case is reached.<br>
     * The possibilities are:<br>
     * (GU + NGD)<br>
     * (NGU & NGD) v (NGU & GD)<br>
     * (NGU & NGD) v (NGU & GD) v (GU & NGD)<br>
     * @return True if a valid state and alignment was found.
     */
    private boolean selectBestAlignmentScoreFound() {
        outAlignmentScore = 2719.9F;
        //If we have a gapped up only.
        if(GAPPED_UP_ONLY){
            /**
             * LOGIC = (GU + NGD)
             * See logic table.
             */
            //If the D_NG rules are not broken and best gapped score up wads on srna.
            if(D_NG_RULES_UNBROKEN && BEST_SCORE_UP_STRAND == SRNA_STRAND){
                outAlignmentScore = U_SG_SCORE.get(bestGapScoreUpIndex) + D_NG_SCORE;
                return true;
            }else if(D_NG_RULES_UNBROKEN && BEST_SCORE_UP_STRAND == MRNA_STRAND){
                //If the D_NG rules are not broken and best gapped score up wads on mrna.
                outAlignmentScore = U_MG_SCORE.get(bestGapScoreUpIndex) + D_NG_SCORE;
                return true;
            }else {
                return false;
            }
        }else if(NO_GAPPED_UP_ONLY){
            /**
             * LOGIC = (NGU & NGD) v (NGU & GD)
             * See logic table.
             */
            //Find and set the best score for gapped alignment down the tree.
            this.setBestGappedDown();
            //Test: NGD v GD
            if(D_NG_RULES_UNBROKEN && (U_NG_SCORE+D_NG_SCORE) <= bestGapScoreDown){
                //Then (NGU & NGD)
                outAlignmentScore = U_NG_SCORE + D_NG_SCORE;
                return true;
            }else{//We have (NGU & GD)
                if(bestGapDownStrand == SRNA_STRAND){
                    outAlignmentScore = D_SG_SCORE.get(bestGapScoreDownIndex);
                    return true;
                }else if(bestGapDownStrand == MRNA_STRAND){
                    outAlignmentScore = D_MG_SCORE.get(bestGapScoreDownIndex);
                    return true;
                }else{
                    return false;
                }
            }
        }else if(BOTH_UP){
            /**
             * LOGIC = (NGU & NGD) v (NGU & GD) v (GU & NGD)
             */
            switch(this.getBestBOTH_UP()){
                case 1 :{
                    outAlignmentScore = U_NG_SCORE + D_NG_SCORE;
                    return true;
                }
                case 2: {
                    if(bestGapDownStrand == SRNA_STRAND){
                        outAlignmentScore = (D_SG_SCORE.get(bestGapScoreDownIndex));
                        return true;
                    }else if(bestGapDownStrand == MRNA_STRAND){
                        outAlignmentScore = (D_MG_SCORE.get(bestGapScoreDownIndex));
                        return true;

                    }else{
                        return false;
                    }
                }
                case 3:{
                        outAlignmentScore = (U_NG_BEST_SCORE_FOUND) + D_NG_SCORE;
                        return true;
                }//end case
                default: {
                    return false;
                }
            }//end switch
        }//end else if

        return false;
    }//end method.

    /**
     * Debugging - printing everything while traversing towards the root of the tree.
     */
    public void printStuffUp() {


        System.out.println("UP NO GAP");
        System.out.println(U_NG_srna);
        System.out.println(U_NG_pos + "  score: "+ U_NG_SCORE + ",  rules NOT broken: "+ U_NG_RULES_UNBROKEN);
        System.out.println(U_NG_mrna);

        System.out.println("UP SG list");
        for(int i = 0; i < U_SG_srna.size(); i++){
            System.out.println(U_SG_srna.get(i));
            System.out.println(U_SG_pos.get(i) + "  score: "+ U_SG_SCORE.get(i) + ",  rules NOT broken: "+ U_SG_RULES_UNBROKEN.get(i));
            System.out.println(U_SG_mrna.get(i));
        }

        System.out.println("UP MG list");
        for(int i = 0; i < U_MG_srna.size(); i++){
            System.out.println(U_MG_srna.get(i));
            System.out.println(U_MG_pos.get(i) + "  score: "+ U_MG_SCORE.get(i) + ",  rules NOT broken: "+ U_MG_RULES_UNBROKEN.get(i));
            System.out.println(U_MG_mrna.get(i));
        }

        System.out.println("NOTHING_VALID: "+NOTHING_VALID);
        System.out.println("NO GAPPED UP ONLY: "+NO_GAPPED_UP_ONLY);
        System.out.println("GAPPED UP ONLY: "+GAPPED_UP_ONLY);
        System.out.println("BOTH UP: "+BOTH_UP);

    }//end method.

    /**
     * Debugging - printing everything while traversing towards the terminator nodes of the tree.
     */
    public void printStuffDown() {

        System.out.println("-----------------------------------------------------------------------------------------------------------");

        System.out.println("----");
        System.out.println("NO GAP (up)");
        System.out.println(U_NG_srna);
        System.out.println(U_NG_pos + "  score: "+ U_NG_SCORE + ",  rules NOT broken: "+ U_NG_RULES_UNBROKEN);
        System.out.println(U_NG_mrna);
        if(usingNoGapForUP_for_noGapDown){
            System.out.println("This alignment IS being used for NG Down.");
        }else{
            System.out.println("A gaped alignment is being used for NG Down (not this alignment)");
        }
        System.out.println("----");

        System.out.println("DOWN NG     : HAS VALID NG DOWN: "+D_NG_RULES_UNBROKEN);
        System.out.println("DOWN SG list: HAS VALID SG DOWN: "+HAS_VALID_SG_DOWN);
        System.out.println("DOWN MG list: HAS VALID MG DOWN: "+HAS_VALID_MG_DOWN);
        System.out.println("Alignment broken if score exceeds (max): "+max);
        System.out.println("Using no gap up alignment for upstream of no gap down: "+usingNoGapForUP_for_noGapDown);

        System.out.println("NO GAP (down)");
        System.out.println(new String(D_NG_srna));
        System.out.println(new String(D_NG_pos) + "\tscore: "+ U_NG_BEST_SCORE_FOUND+" (up) "+D_NG_SCORE+ " (down),  rules NOT broken: "+ D_NG_RULES_UNBROKEN);
        System.out.println(new String(D_NG_mrna));

        System.out.println("SG GAP (down) size = "+D_SG_RULES_UNBROKEN.size());
        for(int i = 0; i < D_SG_srna.size(); i++){
            System.out.println(D_SG_srna.get(i));
            System.out.println(D_SG_pos.get(i) + "\t\tscore: "+ (D_SG_SCORE.get(i)) + ",  rules NOT broken: "+ D_SG_RULES_UNBROKEN.get(i));
            System.out.println(D_SG_mrna.get(i));
        }

        System.out.println("MG GAP (down) size = "+D_MG_RULES_UNBROKEN.size());
        for(int i = 0; i < D_MG_srna.size(); i++){
            System.out.println(D_MG_srna.get(i));
            System.out.println(D_MG_pos.get(i) + "\t\tscore: "+ (D_MG_SCORE.get(i)) + " rules NOT broken: "+ D_MG_RULES_UNBROKEN.get(i));
            System.out.println(D_MG_mrna.get(i));
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------");
    }//end method.

    /**
     * The key search algorithm for searching the category tree for the given small RNA sequence and
     * incrementing the p-values.
     * @param sequence The small RNA (shuffled) to be searched against the category tree.
     * @param pValues The list of pValues to be incremented.
     * @param max The maximum number of mismatches that an alignment should not exceed i.e. the duplex alignment score.
     */
    public boolean searchTree(byte[] sequence, float max, boolean usingGapped) {
        //Set the gapped alignments flag.
        this.gapped = usingGapped;
        //Set the maximum score so other methods can see it when needed.
        this.max = max;
        //Set the best score to something it could never be.
        bestScored = 9927.999f;
        //Ensure well behaved code!
        if (sequence == null) {return false;}
        if (sequence.length < 19 || sequence.length > 24) { return false; }
        ArrayList<Node> subTreeList = null;
        //Get the list of start positions/subtrees.
        if(this.allowEleven){
            subTreeList = getStartPositionListMM11(sequence[9], sequence[10]);
        }else{
            subTreeList =  getStartPositionList(sequence[9], sequence[10]);
        }
        //Ensure well behaved code.
        if (subTreeList == null) { return false; }
        //Get an iterator for the list.
        Iterator<Node> subTreeIterator = subTreeList.iterator();
        //Set the sequence so other fucntions can use it.
        this.sequence = sequence;
        //Labled - while we have sub-trees to search for alignments in.
        boolean found = false;
        NEXT:
        while(subTreeIterator.hasNext() && !found) {
            //Initialise fields for the navigation up this sub-tree.
            U_init(subTreeIterator.next());
            if(Data.CAT_TREE_VERY_VERBOSE){
                System.out.println("\n\n-------------- NEW PARTITION TREE (starting to search towards the root) -----------------");
            }
            //While we have not reached the root of the tree.
            while (U_INDEX >= 0) {
                if(gapped){
                    //If we have a valid gapped alignment on the srna.
                    if(U_SG_VALID) {
                        //Then process all gapped alignments on srna.
                        U_SG();
                    }//end if.

                    //If we have a valid gapped alignment on the mrna.
                    if(U_MG_VALID) {
                        //Then process all gapped alignments on the mrna.
                        U_MG();
                    }//end if.
                }
                //If the rules have not been broken on the no gapped alignment.
                if(U_NG_RULES_UNBROKEN){
                    //Then test for an exact match.
                    if (U_NG_exactMatch()) {
                    }//If no exact match was found, then test for a gap (note: if a gap was found this function looks after mismatches)
                    else if (U_gapFound()) {
                    }else{//Else we had no gap or exact match, so process the mismatch.
                        U_NG_mismatch();
                    }//end else.
                }//end if.
                //Set the downwards logic.
                setAdvanceLogic();
                 if(Data.CAT_TREE_VERY_VERBOSE){
                    System.out.println("THE SRNA:"+new String(sequence));
                    //Print alignments found to std out.
                    printStuffUp();
                }
                //Test to make sure we have something valid - if we don't then move to the next subtree.
                if(NOTHING_VALID){continue NEXT;}
                //Easy else, set the best score for the no gapped alignments down the tree.
                U_MG_MATCHING_NODE = U_NG_NODE;
                U_NG_NODE = U_NG_NODE.parent;
                U_INDEX--;
            }//END WHILE (GOING UP!)
            //Set up for downwards.
            if(Data.CAT_TREE_VERY_VERBOSE){
                System.out.println("\n\n--------------- ROOT WAS REACHED (Starting down cat tree)----------------");
            }
            D_NG_setBestScore();
            //Initalise lists and fields ready for the tree traversals.
            D_init();
            D_NEXT:
            while(!nodes.isEmpty()){
                //Perform push & pop operations to navigate down the tree.
                D_pushAndPop();
                if(!advanceDown()){
                    continue D_NEXT;
                }
                if(Data.CAT_TREE_VERY_VERBOSE){
                    System.out.println("PUSH AND POP - sRNA Position (zero indexed): "+D_INDEX+"  -  (shuffled sequence =  "+new String(sequence)+" ).");
                }
                if(gapped){
                    //If we have one or more alignments with a gap on the srna.
                    if(D_SG_SCORE.size() > 0){
                        //Do the srna gaped alignments.
                        D_SG();
                    }//end if.
                }
                //If we may have reached the base case - but still wanted to test of srna gapped alignments above, so we
                //protected the following statements as we do not want to perform these procedures if the base case is met.
                if(D_INDEX < sequence.length){
                    if(gapped){
                        //If we have one or more gapped alignments on the mrna.
                        if(D_MG_SCORE.size() > 0){
                            //Process the mrna gapped alignments.
                            D_MG();
                        }//end if.
                    }
                    //Carry out ungapped processing.
                    //If on the NG we have an exact match - then do nothing else.
                    if(D_NG_exactMatch()){
                    }//If we didnt have an exact match, then can we make a gapped alignment? (Note - gapped alignment process looks after mismatches if needed.)
                    else if(D_gapFound()){
                    }else{
                        //If no gap was found, then carry out mismatch procedure.
                        D_NG_mismatch();
                    }//end else.

                }//end if
                if(Data.CAT_TREE_VERY_VERBOSE){
                    printStuffDown();
                }
                /**
                 * If we do not have anything valid after processing the duplex on this tree traversal, then
                 * don't bother to try and find best alignment etc because it's pointless.
                 */
                if(!D_NG_RULES_UNBROKEN && !HAS_VALID_SG_DOWN && !HAS_VALID_MG_DOWN){
                    continue;
                }
                /**
                 * BASE CASE:
                 * If we only have D_NG (no potentially valid D_SG), then the base case is when the integer used to index the shuffled sRNA positions equals
                 * the length of the shuffled sRNA. If we have a potentially valid D_SG, then we must allow for the -1 adjustment because of the allowed gap.
                 * Therefore,the base case in this situation is when the (d index -1) equals the length of the sequence. Therefore (d index) will need to equal
                 * sequence length rather than sequence length -1, because we are accounting for the -1 of the d index.
                 **/
                int baseCaseLength = sequence.length-1;
                if(HAS_VALID_SG_DOWN){
                    baseCaseLength = sequence.length;
                }
                if(D_INDEX == baseCaseLength){
                    if(selectBestAlignmentScoreFound()){
                        //If the best alignment score is less that that what we have already found for this srna.
                        if(outAlignmentScore < bestScored){
                            //Replace the best score.
                            bestScored = outAlignmentScore;
                        }//end if.
                    }else{
                        if(Data.CAT_TREE_VERY_VERBOSE){
                            //If no valid alignment was found - then let me know (TODO: delete this else as not required, only for debugging.)
                            System.out.println("NO VALID ALIGNMENT found - then how did we get here?");
                        }
                    }
                    if(Data.CAT_TREE_VERY_VERBOSE){
                        System.out.println("\n####### ALIGNMENT FOUND #######\n");
                        System.out.println("Sequence Length: "+sequence.length +" , sequence: "+new String(sequence)+", baseCaseLength: "+baseCaseLength
                                +" (same as sRNA position if no gapped down alignments valid and sRNA position+1 if gapped alignment on sRNA is valid)");
                        //Test to see if we have a valid alignment and select the best score.
                        System.out.println("ALIGNMENT FOUND: MAX= "+max+" outAlignmentScore= "+outAlignmentScore + " bestScored= "+bestScored);
                    }
                    //We have found an alignment with the same or better score than the real duplex.
                    if(outAlignmentScore <= max){return true; }
                    //Continue to the next PUSH & POP!
                    continue D_NEXT;
                }//END IF BASECASE.
            }//end while (no more nodes!).
        }//END ALL THE SUBTREES!
        if(Data.CAT_TREE_VERY_VERBOSE){
            System.out.println("----------------------ONE RANDOM SRNA END--------------------------------");
        }
        //We didn't return earlier so are dropping out of the bottom of the algorithm, therefore, tell the caller we found nothing.
        return false;
    }//end method.

}//END CLASS.
