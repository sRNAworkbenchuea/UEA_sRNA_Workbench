package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 * Represents the data structure and search algorithms used for mapping candidate
 * messenger RNA fragments against small RNAs in an sRNAome.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
class Tree extends Thread {

    /**Total number of start nodes in middle of tree - shows width of tree.**/
    private int splitListCount = 0;
    /**Total number of nodes in the srna tree.**/
    private int srnaNodeCount = 0;
    /**Redundant sequence count in srnaome - after noise filtering.**/
    private int redundant_sequence_count_srnaome = 0;
    /**Redundant sequence size distribution for reads in srnaome - after noise filter.**/
    private int[] redundant_sequence_distribution_srnaome;
    /**non-redundant sequence count in srnaome. after noise filter**/
    private int non_redundant_sequences_srnaome = 0;
    /**non-redundant size distribution for reads in srnaome. after noise filter**/
    private int[] non_redundant_sequence_distribution_srnaome;
    /**The total number of short reads within this tree.**/
    private int totalRawShortReads;
    /**Total number of degraded fragments**/
    private int totalFragments;



    /**Byte false.**/
    private final byte FALSE = 0;
    /**Byte true.**/
    private final byte TRUE = 1;
    /**Byte A.**/
    private final byte A = 'A';
    /**Byte C.**/
    private final byte C = 'C';
    /**Byte G.**/
    private final byte G = 'G';
    /**Byte T.**/
    private final byte T = 'T';
    /**Short RNA for output.**/
    private byte[] outSrna;
    /**Positions array for output**/
    private byte[] outPositions;
    /**Output alignment score.**/
    private float outAlignmentScore;
    /**Messenger RNA for output.**/
    private byte[] outMrna;
    /**Start nodes: AA AC AG AT CC CA CG CT GG GA GC GT TT TA TC TG (IMPORTANT - DO NOT RE-ORDER THIS!)**/
    private ArrayList<Node>[] start;
    /**The header of the current miRNA/tasiRNA.**/
    protected static String miID;
    /**The root of the short RNA tree.**/
    private Node root;
    /**A stack of nodes for the search algorithm.**/
    private Stack<Node> nodes;
    /**A stack of tree levels for the search algorithm**/
    private Stack<Byte> treeLevel;
    /**The current focus node during the search algorithm**/
    private Node focusNode;
    /**The current tree level during the search algorithm**/
    private byte level;
    /**Search algorithm no gapped short RNA.**/
    private byte[] noGapSRNAD;
    /**Search algorithm no gapped positions.**/
    private byte[] noGapPositionsD;
    /**Search algorithm no gapped messenger RNA(fragment - not degradome)**/
    private byte[] noGapMRNAD;
    /**Search algorithm no gapped score.**/
    private float noGapScoreD;
    /**List of small RNAs with gaps on the sRNA strand.**/
    private ArrayList<byte[]> sGapSRNAD;
    /**List of positions with gaps on the sRNA strand.**/
    private ArrayList<byte[]> sGapPositionsD;
    /**List of mRNAs with gaps on sRNA strand.**/
    private ArrayList<byte[]> sGapMRNAD;
    /**List of scores with gaps on sRNA strand.**/
    private ArrayList<Float> sGapScoreD;
    /**List of sRNAs with gap on mRNA strand.**/
    private ArrayList<byte[]> mGapSRNAD;
    /**List of positions with gap on mRNA strand.**/
    private ArrayList<byte[]> mGapPositionsD;
    /**List of messenger RNAs with gap on mRNA strand.**/
    private ArrayList<byte[]> mGapMRNAD;
    /**List of scores with gap on mRNA strand.**/
    private ArrayList<Float> mGapScoreD;
    /**Index holder for no gap duplex.**/
    private int noGapIndexD;
    /**Index holder for duplex with gap on sRNA strand.**/
    private int sGapIndexD;
    /**Flag for when a gap on an mRNA strand has been found.**/
    private boolean mGapFound;
    /**Index holder for duplex with gap on mRNA strand.**/
    private int mGapIndexD;
    /**Stack of rules - (byte 0 = false)**/
    private Stack<Byte> rulesBroken;
    /**Holder for the best score found when searching towards the root of the tree.**/
    private float bestScoreUpForNoGap;
    /**If gapped - which strand has the best score with a gap**/
    private int BEST_SCORE_UP_STRAND;
    /**Index into the best scored alignment upwards in the tree with a gap.**/
    private int bestGapScoreUpIndex;
    /**Flag for using un-gapped alignment when gaped and un-gapped is possible**/
    private boolean usingNoGapForBOTH_UP;
    /**Score for the best gapped alignment found down.**/
    private float bestGapScoreDown;
    /**Strand which has a gap and offers the best score.**/
    private int bestGapDownStrand;
    /**Index of best scoring gapped alignment.**/
    private int bestGapScoreDownIndex;
    /**Constant expression marking the sRNA gap strand.**/
    private final int SRNA_STRAND = 1;
    /**Constant expression marking the mRNA gap strand.**/
    private final int MRNA_STRAND = 2;
    /**Flag set to true if at any point there is no valid alignment in the search.**/
    private boolean NOTHING_VALID = false;
    /**Holder for the starting node of a search.**/
    private Node startNode;
    /**Flag to say if the previous nucleotides were and exact match.**/
    private boolean previous;
    /**Verbose only.**/
    private byte[] noGapSRNA;
    /**Verbose only.**/
    private byte[] noGapPositions;
    /**Verbose only.**/
    private byte[] noGapMRNA;
    /**Verbose only.**/
    private float noGapScore;
    /**Verbose only.**/
    private boolean sGapPrevious;
    /**Verbose only.**/
    private ArrayList<byte[]> sGapSRNA;
    /**Verbose only.**/
    private ArrayList<byte[]> sGapPositions;
    /**Verbose only.**/
    private ArrayList<byte[]> sGapMRNA;
    /**Verbose only.**/
    private ArrayList<Float> sGapScore = new ArrayList<Float>();
    /**Verbose only.**/
    private boolean mGapPrevious;
    /**Verbose only.**/
    private ArrayList<byte[]> mGapSRNA;
    /**Verbose only.**/
    private ArrayList<byte[]> mGapPositions;
    /**Verbose only.**/
    private ArrayList<byte[]> mGapMRNA;
    /**Verbose only.**/
    private ArrayList<Float> mGapScore = new ArrayList<Float>();
    /**sRNA node UPWARDS.**/
    private Node sNode;
    /**Index into un-gapped duplex**/
    private int noGapIndex;
    /**Gapped duplex sRNA node upwards.**/
    private Node sGapNode;
    /**Index into the gapped duplex - gap on sRNA strand.**/
    private int sGapIndex;
    /**Node for gap on mRNA searching upwards.**/
    private Node mGapNode;
    /**Index into duplex for gapped alignment on mRNA strand.**/
    private int mGapIndex;
    /**The candidate sequence being searched for.**/
    private byte[] sequence;
    /**Only a gapped alignment was found searching up the tree.**/
    private boolean GAPPED_UP_ONLY;
    /**Only an un-gapped alignment was found when searching up the tree.**/
    private boolean NO_GAPPED_UP_ONLY;
    /**Both gapped and un-gapped alignments where found when searching up the tree.**/
    private boolean BOTH_UP;
    /**Encoded un-gapped duplex searching upwards.**/
    private byte[] U_NG_encoded;
    /**List of encoded duplex with gap on sRNA strand when searching up the tree.**/
    private ArrayList<byte[]> U_SG_encoded = new ArrayList<byte[]>();
    /**List of encoded duplex with gap on mRNA strand when searching up the tree.**/
    private ArrayList<byte[]> U_MG_encoded = new ArrayList<byte[]>();
    /**Decoded duplex searching up the tree.**/
    private byte[][] U_decoded;
    /**Decoded duplex searching down the tree.**/
    private byte[][] D_decoded;
    /**Encoded un-gapped duplex searching down the tree.**/
    private byte[] D_NG_encoded;
    /**List of encoded duplex with gap on sRNA strand when searching down the tree.**/
    private ArrayList<byte[]> D_SG_encoded;
    /**List of encoded duplex with gap on mRNA strand when searching down the tree.**/
    private ArrayList<byte[]> D_MG_encoded;
    /**Stack of scores for no-gap mismatch duplex down the tree.**/
    private Stack<Float> NGmismatchDown;
    /**Holder for the number of nodes pushed onto the node stack.**/
    private int pushed;
    /**The edge of the root of the tree has a special value to show it is the root.**/
    private final byte treeRootEdge = -1;
    /**U shuffle instance to calculate p-values.**/
    private UShuffle pValues;
    /**Category Trees 0.**/
    private CategoryTree c0;
    /**Category Trees 1.**/
    private CategoryTree c1;
    /**Category Trees 2.**/
    private CategoryTree c2;
    /**Category Trees 3.**/
    private CategoryTree c3;
    /**Category Trees 4.**/
    private CategoryTree c4;
    /**The manager for the threads.**/
    private SearchTreeThreadPoolManager manager;
    /**Flag set when finalising best alignment. Does the final duplex contain a gap?**/
    private boolean hasGap;
    /**Flag set if a mismatch is present at position 11 in final duplex**/
    private boolean[] features;
    /**Final indexer for decoding.**/
    private int finalIndex = -1;
    /**The user parameters.**/
    private ParesnipParams params;
    /**The index of this threads.**/
    private int threadIndex;

     /**
     * Creates a new instance of Tree.
     **/
    @SuppressWarnings("unchecked")
    public Tree(ParesnipParams params) {
        this.params = params;
        root = Node.createRoot(treeRootEdge);
        //root.edge = treeRootEdge;
        start = new ArrayList[16];   // unchecked warning - not much we can do when mixing arrays and generics
        for (int i = 0; i < 16; i++) {
            start[i] = new ArrayList<Node>();
        }//end for.
        pValues = new UShuffle();
        redundant_sequence_distribution_srnaome = new int[params.getMaxSrnaLength()+1];
        non_redundant_sequence_distribution_srnaome = new int[params.getMaxSrnaLength() +1];
    }//end constructor.

    /**
     * Sets the total number of degradation fragments found in the degradome.
     * @param totalFragments
     */
    public void setTotalFragmentCount(int totalFragments){
      this.totalFragments = totalFragments;
    }//end method.

    /**
     * Get the total number of nodes within this trees split list.
     * @return number of nodes in split lists.
     */
    public int getSplitListCount(){
      return this.splitListCount;
    }//end method.

    /**
     * Get the total number of nodes in this tree.
     * @return total number of nodes.
     */
    public int getSrnaNodeCount(){
      return this.srnaNodeCount;
    }//end method.

    /**
     * Sets the total number of short reads within this tree.
     * @param totalShortReads
     */
    public void setTotalShortReads(int totalShortReads){
      this.totalRawShortReads = totalShortReads;
    }//end method.

    /**
     * Get the redundant sequence count within this tree.
     * @return redundant sequence count.
     */
    public int getRedundantSequenceCount(){
      return this.redundant_sequence_count_srnaome;
    }//end method.

    /**
     * Setter for the root node.
     * @param root
     */
    public void setRoot(Node root) {
        this.root = root;
    }//end method.

    /**
     * Setter for the thread index.
     * @param threadIndex
     */
    public void setThreadIndex(int threadIndex) {
        this.threadIndex = threadIndex;
    }//end method.

    /**
     * Setter for the thread pool manager.
     */
    public void setManager(SearchTreeThreadPoolManager manager) {
        this.manager = manager;
    }//end method.

    /**
     * Set the category trees.
     * @param c0 Category 0
     * @param c1 Category 1
     * @param c2 Category 2
     * @param c3 Category 3
     * @param c4 Category 4
     */
    public void setCategoryTrees(CategoryTree c0, CategoryTree c1, CategoryTree c2, CategoryTree c3, CategoryTree c4) {
        this.c0 = c0;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }//end method.

    /**
     * Use the tree collection (aka box) to set the category trees.
     * @param treebox
     */
    public void setCategoryTrees( OriginalTreeBox treebox )
    {
      this.c0 = treebox.getOriginalCategoryTree( Category.CATEGORY_0 );
      this.c1 = treebox.getOriginalCategoryTree( Category.CATEGORY_1 );
      this.c2 = treebox.getOriginalCategoryTree( Category.CATEGORY_2 );
      this.c3 = treebox.getOriginalCategoryTree( Category.CATEGORY_3 );
      this.c4 = treebox.getOriginalCategoryTree( Category.CATEGORY_4 );
    }

    /**
     * Getter for non redundant sequence count within this tree.
     * @return total number of NR sequences.
     */
    public int getNonRedundantSequencesCount(){
      return this.non_redundant_sequences_srnaome;
    }//end method.

    /**
     * Set the start nodes list.
     * @param s the start nodes (entry points).
     */
    public void setStart(ArrayList<Node>[] s) {
        this.start = s;
    }//end method.

    /**
     * Creates a new tree with shared fields copied.
     * @return New tree with shared fields copied.
     */
    public Tree getTreeCopy() {
        Tree t = new Tree(params);
        t.setRoot(root);
        t.setStart(start);
        t.setCategoryTrees(c0.copyCategoryTree(), c1.copyCategoryTree(), c2.copyCategoryTree(), c3.copyCategoryTree(), c4.copyCategoryTree());
        t.setManager(manager);
        t.setRedundantSequenceCount( redundant_sequence_count_srnaome );
        t.setTotalFragments( totalFragments );
        t.setTotalRawShortReads( totalRawShortReads );
        return t;
    }//end method.

    /**
     * Set the total number of redundant sequences.
     * @param redundantSequenceCount
     */
    private void setRedundantSequenceCount(int redundantSequenceCount){
      this.redundant_sequence_count_srnaome = redundantSequenceCount;
    }//end method.

    /**
     * Set the total number of fragments.
     * @param totalFragments
     */
    private void setTotalFragments(int totalFragments){
      this.totalFragments = totalFragments;
    }//end method.

    /**
     * Set the total number of short reads.
     * @param totalRawShortReads
     */
    private void setTotalRawShortReads(int totalRawShortReads){
      this.totalRawShortReads = totalRawShortReads;
    }//end method.

    @Override
    public void run(){
        Query q = manager.nextQuery();
        while (q != null) {
            try{
                String result = searchTree(q.sequence, q.chart, q.index, q.other, totalFragments);
                if(!result.isEmpty()) {
                    manager.accessResults(result, false);
                }
                q = manager.nextQuery();
            }catch(InterruptedException e){
                System.out.println("Tree algorithm intrupted - run method will exit");
                return;
            }
        }
    }//end method.

    public int[] getRedundantSequenceDistribution(){
      return this.redundant_sequence_distribution_srnaome;
    }//end method.

    /**
     * Adds a sequence to the small RNA search tree.
     * @param sequence The sequence to be added to the search tree.
     **/
    public void addToTree(byte[] sequence) {

        if (sequence.length < params.getMinSrnaLength() || sequence.length > params.getMaxSrnaLength() ) {
            return;
        }

        for (int i = 0; i < sequence.length; i++) {
            switch (sequence[i]) {
                case A:
                case C:
                case G:
                case T:
                    break;
                default:
                    return;
            }//end switch.
        }//end for.

        if(params.isDiscardLowComplexitySRNAs() && Data.filter(sequence)) {
            return;
        }
        //Add the sequence to the statistics about the data.
        redundant_sequence_count_srnaome++;
        redundant_sequence_distribution_srnaome[sequence.length]++;
        Node currentNode = root;

        for (int i = 0; i < sequence.length; i++) {
            //Look at the edge needed for the nt at the level of this node.
            switch (sequence[i]) {
                case A: {
                    if (currentNode.edgeA == null) {
                        //If the edge of A is null and we need it then make it.
                        currentNode.edgeA = new Node(currentNode, A);
                        srnaNodeCount++;
                        //currentNode.edgeA.parent = currentNode;
                        //currentNode.edgeA.edge = A;
                        currentNode = currentNode.edgeA;
                    } else {
                        //Else we have the edge, so recurse down that path.
                        currentNode = currentNode.edgeA;

                    }
                    break;
                }//end case.
                case C: {
                    if (currentNode.edgeC == null) {
                        //If the edge of C is null and we need it then make it.
                        currentNode.edgeC = new Node(currentNode, C);
                        srnaNodeCount++;
                        //currentNode.edgeC.parent = currentNode;
                        //currentNode.edgeC.edge = C;
                        currentNode = currentNode.edgeC;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeC;
                    }
                    break;
                }//end case.
                case G: {
                    if (currentNode.edgeG == null) {
                        //If the edge of G is null and we need it then make it.
                        currentNode.edgeG = new Node(currentNode, G);
                        srnaNodeCount++;
                        //currentNode.edgeG.parent = currentNode;
                        //currentNode.edgeG.edge = G;
                        currentNode = currentNode.edgeG;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeG;
                    }
                    break;
                }//end case.
                case T: {
                    if (currentNode.edgeT == null) {
                        //If the edge of T is null and we need it then make it.
                        currentNode.edgeT = new Node(currentNode, T);
                        srnaNodeCount++;
                        //currentNode.edgeT.parent = currentNode;
                        //currentNode.edgeT.edge = T;
                        currentNode = currentNode.edgeT;
                    } else {
                        //Else we have the edge, so get that node.
                        currentNode = currentNode.edgeT;
                    }
                    break;
                }//end case.
            }//end switch.
        }//end for
        currentNode.isTerminator = true;
        if (currentNode.terminator == null) {
            currentNode.terminator = new Terminator();
            non_redundant_sequence_distribution_srnaome[sequence.length]++;
            non_redundant_sequences_srnaome++;
        }
        currentNode.terminator.id = miID;
        currentNode.terminator.abundance++;
    }//end method.

    /**
     * Get the non redundant sequence distribution for the srnas in this tree.
     * @return length distribution.
     */
    public int[] getNonRedundantSequenceDistribution(){
        return this.non_redundant_sequence_distribution_srnaome;
    }//end method.

    /**
     * List the start nodes which are the entry points into our short RNA search tree.
     */
    public void listStartPositions(){

        Stack<Node> s = new Stack<Node>();
        Stack<Integer> l = new Stack<Integer>();
        s.push(root);
        l.push(0);
        Node focusNodeListStartPos;
        int levelListStartPos;
        while (!s.isEmpty()) {
            focusNodeListStartPos = s.pop();
            levelListStartPos = l.pop();
            if (focusNodeListStartPos.edgeA != null) {
                s.push(focusNodeListStartPos.edgeA);
                l.push(levelListStartPos + 1);
                if (levelListStartPos == 10) {//10
                    if (focusNodeListStartPos.edge == A) {
                        getStartPositionList(A, A).add(focusNodeListStartPos.edgeA);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == C) {
                        getStartPositionList(C, A).add(focusNodeListStartPos.edgeA);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == G) {
                        getStartPositionList(G, A).add(focusNodeListStartPos.edgeA);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == T) {
                        getStartPositionList(T, A).add(focusNodeListStartPos.edgeA);
                        splitListCount++;
                    }
                }//end if.
            }//end if.
            if (focusNodeListStartPos.edgeC != null) {
                s.push(focusNodeListStartPos.edgeC);
                l.push(levelListStartPos + 1);
                if (levelListStartPos == 10) {//10
                    if (focusNodeListStartPos.edge == A) {
                        getStartPositionList(A, C).add(focusNodeListStartPos.edgeC);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == C) {
                        getStartPositionList(C, C).add(focusNodeListStartPos.edgeC);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == G) {
                        getStartPositionList(G, C).add(focusNodeListStartPos.edgeC);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == T) {
                        getStartPositionList(T, C).add(focusNodeListStartPos.edgeC);
                        splitListCount++;
                    }
                }//end if.
            }//end if.
            if (focusNodeListStartPos.edgeG != null) {
                s.push(focusNodeListStartPos.edgeG);
                l.push(levelListStartPos + 1);
                if (levelListStartPos == 10) {//10
                    if (focusNodeListStartPos.edge == A) {
                        getStartPositionList(A, G).add(focusNodeListStartPos.edgeG);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == C) {
                        getStartPositionList(C, G).add(focusNodeListStartPos.edgeG);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == G) {
                        getStartPositionList(G, G).add(focusNodeListStartPos.edgeG);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == T) {
                        getStartPositionList(T, G).add(focusNodeListStartPos.edgeG);
                        splitListCount++;
                    }
                }//end if.
            }//end if.
            if (focusNodeListStartPos.edgeT != null) {
                s.push(focusNodeListStartPos.edgeT);
                l.push(levelListStartPos + 1);
                if (levelListStartPos == 10) {
                    if (focusNodeListStartPos.edge == A) {
                        getStartPositionList(A, T).add(focusNodeListStartPos.edgeT);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == C) {
                        getStartPositionList(C, T).add(focusNodeListStartPos.edgeT);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == G) {
                        getStartPositionList(G, T).add(focusNodeListStartPos.edgeT);
                        splitListCount++;
                    }
                    if (focusNodeListStartPos.edge == T) {
                        getStartPositionList(T, T).add(focusNodeListStartPos.edgeT);
                        splitListCount++;
                    }
                }//end if.
            }//end if
        } //end while.
    }//end method.

    private ArrayList<Node> getStartPositionListMM11(byte ntA, byte ntB) {

        ArrayList<Node> startPositionList = null;
            startPositionList = new ArrayList<Node>();
            switch (ntA) {
                case A: {
                    startPositionList.addAll(start[0]);
                    startPositionList.addAll(start[1]);
                    startPositionList.addAll(start[2]);
                    startPositionList.addAll(start[3]);
                    if (Data.VERY_VERBOSE) {
                        System.out.println("GetStartPositionList() (allow mismatch at eleven) : Case A : listSize: " + startPositionList.size());
                    }
                    break;
                }//end case.
                case C: {
                    startPositionList.addAll(start[4]);
                    startPositionList.addAll(start[5]);
                    startPositionList.addAll(start[6]);
                    startPositionList.addAll(start[7]);
                    if (Data.VERY_VERBOSE) {
                        System.out.println("GetStartPositionList() (allow mismatch at eleven) : Case C : listSize: " + startPositionList.size());
                    }
                    break;
                }//end case.
                case G: {
                    startPositionList.addAll(start[8]);
                    startPositionList.addAll(start[9]);
                    startPositionList.addAll(start[10]);
                    startPositionList.addAll(start[11]);
                    if (Data.VERY_VERBOSE) {
                        System.out.println("GetStartPositionList() (allow mismatch at eleven) : Case G : listSize: " + startPositionList.size());
                    }
                    break;
                }//end case.
                case T: {
                    startPositionList.addAll(start[12]);
                    startPositionList.addAll(start[13]);
                    startPositionList.addAll(start[14]);
                    startPositionList.addAll(start[15]);
                    if (Data.VERY_VERBOSE) {
                        System.out.println("GetStartPositionList() (allow mismatch at eleven) : Case T : listSize: " + startPositionList.size());
                    }
                    break;
                }//end case.
            }//end switch.

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
            }//end case.
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
            }//end case.
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
            }//end case.
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
            }//end case.
        }//end switch.

        return startPositionList;
    }//end method.

    /**
     * Initialises reused memory when starting a new search from a start node
     * up the tree towards the root.
     * @param startNode The entry node into the middle of the tree.
     */
    private void U_init(Node startNode) {

        U_NG_encoded = new byte[33];
        noGapScore = 0.0f;
        U_NG_encoded[Data.RULES_BROKEN] = FALSE;
        U_SG_encoded.clear();
        sGapScore.clear();
        U_MG_encoded.clear();
        mGapScore.clear();
        GAPPED_UP_ONLY = false;
        NO_GAPPED_UP_ONLY = false;
        BOTH_UP = false;
        NOTHING_VALID = false;
        this.startNode = startNode;
        previous = true;
        sGapPrevious = false;
        mGapPrevious = false;
        sNode = startNode;
        noGapIndex = 14;
        sGapNode = startNode;
        sGapIndex = 13;
        mGapNode = startNode.parent;
        mGapIndex = 14;
        if (Data.VERY_VERBOSE) {
            noGapSRNA = new byte[32];
            noGapPositions = new byte[32];
            noGapMRNA = new byte[32];
            sGapSRNA = new ArrayList<byte[]>();
            sGapPositions = new ArrayList<byte[]>();
            sGapMRNA = new ArrayList<byte[]>();
            mGapSRNA = new ArrayList<byte[]>();
            mGapPositions = new ArrayList<byte[]>();
            mGapMRNA = new ArrayList<byte[]>();
        }//end if.
    }//end method.

    /**
     * Apply the exact match criteria in a potential duplex.
     * @return True if there is an exact match at the recognised position.
     */
    private boolean U_NG_exactMatch() {
        if (sNode.edge == sequence[noGapIndex]) {
            previous = true;
            if (U_NG_encoded[Data.RULES_BROKEN] == FALSE) {
                U_NG_encoded[noGapIndex] = Data.NG_MATCH;
                if (Data.VERY_VERBOSE) {
                    noGapPositions[noGapIndex] = '|';
                    noGapSRNA[noGapIndex] = sNode.edge;
                    noGapMRNA[noGapIndex] = sequence[noGapIndex];
                }//end if.
            }//end if.
            return true;
        } else {
            return false;
        }//end else.
    }//end method.

    /**
     * Apply the gap found criteria when traversing up the tree towards the root.
     * @return True if a gap was found.
     */
    private boolean U_gapFound() {
        //Test to see if we are using gaps in the first place.
        if (!params.isAllowSingleNtGap()) {
            return false;
        }
        boolean gapFound = false;
        if (U_NG_encoded[Data.RULES_BROKEN] == FALSE && noGapScore + 1.0f < 2.5f) {
            //GAP IN THE MRNA!!!
            if (sNode.parent != null && previous && sNode.parent.edge == sequence[noGapIndex]) {
                if (Data.VERY_VERBOSE) {
                    //MAKE NEW GAP ALIGNMENT
                    byte[] sRNA = new byte[32];
                    byte[] positions = new byte[32];
                    byte[] mRNA = new byte[32];
                    System.arraycopy(noGapSRNA, 0, sRNA, 0, 32);
                    System.arraycopy(noGapPositions, 0, positions, 0, 32);
                    System.arraycopy(noGapMRNA, 0, mRNA, 0, 32);
                    sRNA[noGapIndex] = sNode.edge;
                    positions[noGapIndex] = ' ';
                    mRNA[noGapIndex] = '-';
                    mGapSRNA.add(sRNA);
                    mGapPositions.add(positions);
                    mGapMRNA.add(mRNA);
                }//end very verbose.
                mGapScore.add(noGapScore + 1.0f);
                gapFound = true;
                byte[] temp_U_MG_encode = new byte[33];
                System.arraycopy(U_NG_encoded, 0, temp_U_MG_encode, 0, 15);
                temp_U_MG_encode[noGapIndex] = (byte) (Data.MG_GAP + sNode.edge);
                U_MG_encoded.add(temp_U_MG_encode);
            }//end if.
            if (previous && sNode.edge == sequence[noGapIndex - 1]) {
                if (Data.VERY_VERBOSE) {
                    byte[] sRNA = new byte[32];
                    byte[] positions = new byte[32];
                    byte[] mRNA = new byte[32];
                    System.arraycopy(noGapSRNA, 0, sRNA, 0, 32);
                    System.arraycopy(noGapPositions, 0, positions, 0, 32);
                    System.arraycopy(noGapMRNA, 0, mRNA, 0, 32);
                    sRNA[noGapIndex] = '-';
                    positions[noGapIndex] = ' ';
                    mRNA[noGapIndex] = sequence[noGapIndex];
                    sGapSRNA.add(sRNA);
                    sGapPositions.add(positions);
                    sGapMRNA.add(mRNA);
                }//end very verbose.
                sGapScore.add(noGapScore + 1.0f);
                gapFound = true;
                byte[] temp_U_SG_encode = new byte[33];
                System.arraycopy(U_NG_encoded, 0, temp_U_SG_encode, 0, 15);
                temp_U_SG_encode[noGapIndex] = Data.SG_GAP;
                U_SG_encoded.add(temp_U_SG_encode);
            }//end if.
        }//end if.
        if (gapFound) {
            U_NG_mismatch();
        }
        return gapFound;
    }//end method.

    /**
     * Apply rules in a mismatch situation to un-gapped alignments when searching
     * up the tree towards the root.
     */
    private void U_NG_mismatch() {
        if (U_NG_encoded[Data.RULES_BROKEN] == FALSE) {
            if (Data.VERY_VERBOSE) {
                noGapSRNA[noGapIndex] = sNode.edge;
                noGapMRNA[noGapIndex] = sequence[noGapIndex];
            }//end if.
            //IF G-U BASE FOR NO-GAP.
            if ((sNode.edge == G && sequence[noGapIndex] == A) || (sNode.edge == T && sequence[noGapIndex] == C)) {
                if (Data.VERY_VERBOSE) {
                    noGapPositions[noGapIndex] = 'o';
                }//end if.
                noGapScore += 0.5f;
                U_NG_encoded[noGapIndex] = (byte) (Data.NG_MISMATCH_HALF + sNode.edge);
            } else {
                if (Data.VERY_VERBOSE) {
                    noGapPositions[noGapIndex] = ' ';
                }//end if.
                noGapScore += 1.0f;
                U_NG_encoded[noGapIndex] = (byte) (Data.NG_MISMATCH_FULL + sNode.edge);
            }//end else.
            if ((!previous && sNode.parent.edge != treeRootEdge) || noGapScore > 2.5f) {
                U_NG_encoded[Data.RULES_BROKEN] = TRUE;
            }//end if.
        }//end big if.
        previous = false;
    }//end method.

    /**
     * Advance the search in the small RNA tree.
     * @return true if the search should continue.
     */
    private boolean advance() {

        boolean haveGapped = false;
        for (int i = 0; i < U_SG_encoded.size(); i++) {
            if (U_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                haveGapped = true;
                break;
            }//end if.
        }//end for.
        if (!haveGapped) {
            for (int i = 0; i < U_MG_encoded.size(); i++) {
                if (U_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    haveGapped = true;
                    break;
                }//end if.
            }//end for.
        }//end if don't have gapped.
        if (haveGapped == false && U_NG_encoded[Data.RULES_BROKEN] == TRUE) {
            NOTHING_VALID = true;
            return true;
        }//end if.
        if (mGapNode.parent != null && (haveGapped || U_NG_encoded[Data.RULES_BROKEN] == FALSE)) {
            sGapIndex--;
            noGapIndex--;
            sNode = sNode.parent;
            mGapIndex--;
            mGapNode = mGapNode.parent;
            return true;
        } else {
            if (haveGapped == false && U_NG_encoded[Data.RULES_BROKEN] == FALSE) {
                NO_GAPPED_UP_ONLY = true;
            } else if (haveGapped == true && U_NG_encoded[Data.RULES_BROKEN] == TRUE) {
                GAPPED_UP_ONLY = true;
            } else if (haveGapped == true && U_NG_encoded[Data.RULES_BROKEN] == FALSE) {
                BOTH_UP = true;
            }
            return false;
        }//end else.
    }//end method.

    /**
     * Debugging - used for printing the contents of the data structures to the STD out.
     *
     */
    private void printStuff() {
        if (Data.VERY_VERBOSE) {
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println("No Gap: " + new String(this.noGapSRNA));
            System.out.println("No Gap: " + new String(this.noGapPositions) + "rules broken: " + this.U_NG_encoded[Data.RULES_BROKEN] + " score: " + this.noGapScore);
            System.out.println("No Gap: " + new String(this.noGapMRNA));
            System.out.println("GAPPED_UP_ONLY: " + this.GAPPED_UP_ONLY);
            System.out.println("NO_GAPPED_UP_ONLY: " + this.NO_GAPPED_UP_ONLY);
            System.out.println("BOTH_UP: " + this.BOTH_UP);
            for (int i = 0; i < mGapPositions.size(); i++) {
                System.out.println("mGap " + i + ": " + new String(mGapSRNA.get(i)));
                System.out.println("mGap " + i + ": " + new String(mGapPositions.get(i)) + "rules broken: " + U_MG_encoded.get(i)[Data.RULES_BROKEN] + " score: " + this.mGapScore.get(i));
                System.out.println("mGap " + i + ": " + new String(mGapMRNA.get(i)));
                System.out.println("GAPPED_UP_ONLY: " + this.GAPPED_UP_ONLY);
                System.out.println("NO_GAPPED_UP_ONLY: " + this.NO_GAPPED_UP_ONLY);
                System.out.println("BOTH_UP: " + this.BOTH_UP);
            }
            for (int i = 0; i < sGapPositions.size(); i++) {
                System.out.println("sGap " + i + ": " + new String(sGapSRNA.get(i)));
                System.out.println("sGap " + i + ": " + new String(sGapPositions.get(i)) + "rules broken: " + U_SG_encoded.get(i)[Data.RULES_BROKEN] + " score: " + this.sGapScore.get(i));
                System.out.println("sGap " + i + ": " + new String(sGapMRNA.get(i)));
                System.out.println("GAPPED_UP_ONLY: " + this.GAPPED_UP_ONLY);
                System.out.println("NO_GAPPED_UP_ONLY: " + this.NO_GAPPED_UP_ONLY);
                System.out.println("BOTH_UP: " + this.BOTH_UP);
            }
            System.out.println("--------------------------------------------------------------------------------------------");
        }
    }//end method.

    /**
     * Logic for a mismatch in a duplex containing a gap on the sRNA strand when
     * searching up the tree towards the root.
     */
    private void U_SG_mismatch() {
        if (sGapNode != null) {
            boolean haveTwoAjacentMismatches = false;
            if (!sGapPrevious && sNode.parent.edge != treeRootEdge) {
                haveTwoAjacentMismatches = true;
            }//end if.
            if (sNode.edge == G && sequence[sGapIndex] == A || sNode.edge == T && sequence[sGapIndex] == C) {
                for (int i = 0; i < U_SG_encoded.size(); i++) {
                    if (U_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                        if (Data.VERY_VERBOSE) {
                            sGapSRNA.get(i)[sGapIndex] = sNode.edge;
                            sGapPositions.get(i)[sGapIndex] = 'o';
                            sGapMRNA.get(i)[sGapIndex] = sequence[sGapIndex];
                        }//end verbose.
                        sGapScore.set(i, sGapScore.get(i) + 0.5f);
                        U_SG_encoded.get(i)[sGapIndex] = (byte) (Data.SG_MISMATCH_HALF + sNode.edge);
                        if (sGapScore.get(i) > 2.5f || haveTwoAjacentMismatches) {
                            U_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                        }//end if.
                    }//end if
                }//end for.
                sGapPrevious = false;
            } else {
                for (int i = 0; i < U_SG_encoded.size(); i++) {
                    if (U_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                        if (Data.VERY_VERBOSE) {
                            sGapSRNA.get(i)[sGapIndex] = sNode.edge;
                            sGapPositions.get(i)[sGapIndex] = ' ';
                            sGapMRNA.get(i)[sGapIndex] = sequence[sGapIndex];
                        }//end verbose.
                        sGapScore.set(i, sGapScore.get(i) + 1.0f);
                        U_SG_encoded.get(i)[sGapIndex] = (byte) (Data.SG_MISMATCH_FULL + sNode.edge);
                        if (sGapScore.get(i) > 2.5f || haveTwoAjacentMismatches) {
                            U_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                        }//end if
                    }//end if.
                }//end for.
                sGapPrevious = false;
            }//end else.
        }//end if not null.
    }//end method.

    /**
     * Logic for a mismatch in a duplex containing a gap on the mRNA strand when
     * searching up the tree towards the root.
     */
    private void U_MG_mismatch() {
        if (mGapNode.parent != null) {
            boolean flagged = false;
            if (!mGapPrevious && mGapNode.parent.edge != treeRootEdge) {
                flagged = true;
            }
            if (mGapNode.edge == G && sequence[mGapIndex] == A || mGapNode.edge == T && sequence[mGapIndex] == C) {
                for (int i = 0; i < U_MG_encoded.size(); i++) {
                    if (U_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                        if (Data.VERY_VERBOSE) {
                            mGapSRNA.get(i)[noGapIndex - 1] = mGapNode.edge;
                            mGapPositions.get(i)[noGapIndex - 1] = 'o';
                            mGapMRNA.get(i)[noGapIndex - 1] = sequence[mGapIndex];
                        }//end verbose.
                        mGapScore.set(i, mGapScore.get(i) + 0.5f);
                        U_MG_encoded.get(i)[noGapIndex - 1] = (byte) (Data.MG_MISMATCH_HALF + mGapNode.edge);
                        if (mGapScore.get(i) > 2.5f || flagged) {
                            U_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                        }//end if.
                    }//end if.
                }//end for.
                mGapPrevious = false;
            } else {
                for (int i = 0; i < U_MG_encoded.size(); i++) {
                    if (U_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                        if (Data.VERY_VERBOSE) {
                            mGapSRNA.get(i)[noGapIndex - 1] = mGapNode.edge;
                            mGapPositions.get(i)[noGapIndex - 1] = ' ';
                            mGapMRNA.get(i)[noGapIndex - 1] = sequence[mGapIndex];
                        }
                        mGapScore.set(i, mGapScore.get(i) + 1.0f);
                        U_MG_encoded.get(i)[noGapIndex - 1] = (byte) (Data.MG_MISMATCH_FULL + mGapNode.edge);
                        if (mGapScore.get(i) > 2.5f || flagged) {
                            U_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                        }//end if
                    }//end if.
                }//end for.
                mGapPrevious = false;
            }//end else.
        }//end not null.
    }//end method.

    /**
     * Logic for an exact match in a duplex which has a gap on the mRNA strand when
     * searching up the tree towards the root.
     * @return True if an exact match.
     */
    private boolean U_MG_exactMatch() {
        if (mGapNode.edge == sequence[mGapIndex]) {
            //System.out.println("hello");
            for (int i = 0; i < U_MG_encoded.size(); i++) {
                if (U_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (Data.VERY_VERBOSE) {
                        mGapSRNA.get(i)[noGapIndex - 1] = mGapNode.edge;
                        mGapPositions.get(i)[noGapIndex - 1] = '|';
                        mGapMRNA.get(i)[noGapIndex - 1] = sequence[mGapIndex];
                    }//end verbose.
                    U_MG_encoded.get(i)[noGapIndex - 1] = Data.MG_EXACT_MATCH;
                }//end if.
            }//end for.
            mGapPrevious = true;
            return true;
        } else {
            return false;
        }//end else.
    }//end method.

    /**
     * Logic for an exact match in a duplex which has a gap on the sRNA strand when
     * searching up the tree towards the root.
     * @return True if an exact match.
     */
    private boolean U_SG_ExactMatch() {
        if (sNode != null && sNode.edge == sequence[sGapIndex]) {
            for (int i = 0; i < U_SG_encoded.size(); i++) {
                if (U_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (Data.VERY_VERBOSE) {
                        sGapSRNA.get(i)[sGapIndex] = sNode.edge;
                        sGapPositions.get(i)[sGapIndex] = '|';
                        sGapMRNA.get(i)[sGapIndex] = sequence[sGapIndex];
                    }//end verbose.
                    U_SG_encoded.get(i)[sGapIndex] = Data.SG_EXACT_MATCH;
                }//end if.
            }//end for.
            sGapPrevious = true;
            return true;
        } else {
            return false;
        }//end else.
    }//end method.

    /**
     * Set the best gapped score fields for searching down the tree after a
     * base case is found.
     */
    private void setBestGappedDown() {
        bestGapScoreDown = 99.9f;//DEFAULT NONSENS VALUE - IS THIS APPEARS, YOU KNOW NOTHING WAS FOUND.
        bestGapDownStrand = 0;//DEFAULT NONSENS VALUE - IS THIS APPEARS, YOU KNOW NOTHING WAS FOUND.
        bestGapScoreDownIndex = -9;//DEFAULT NONSENS VALUE - IS THIS APPEARS, YOU KNOW NOTHING WAS FOUND.
        for (int i = 0; i < D_SG_encoded.size(); i++) {
            if (D_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                if (sGapScoreD.get(i) <= bestGapScoreDown) {
                    bestGapScoreDown = sGapScoreD.get(i);
                    bestGapDownStrand = SRNA_STRAND;
                    bestGapScoreDownIndex = i;
                }//end score test.
            }//end rules broken.
        }//end for.
        for (int i = 0; i < D_MG_encoded.size(); i++) {
            if (D_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                if (mGapScoreD.get(i) <= bestGapScoreDown) {
                    bestGapScoreDown = mGapScoreD.get(i);
                    bestGapDownStrand = MRNA_STRAND;
                    bestGapScoreDownIndex = i;
                }//end score test.
            }//end if not broken.
        }//end for.
    }//end method.

    /**
     * Set the best gapped score fields for searching up the tree.
     */
    private void setBestGappedUp() {
        usingNoGapForBOTH_UP = false;
        bestScoreUpForNoGap = 99.9f;
        BEST_SCORE_UP_STRAND = 0;
        if (NO_GAPPED_UP_ONLY) {
            bestScoreUpForNoGap = noGapScore;
        } else if (GAPPED_UP_ONLY || BOTH_UP) {
            for (int i = 0; i < U_SG_encoded.size(); i++) {
                if (U_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (sGapScore.get(i) <= bestScoreUpForNoGap) {
                        bestScoreUpForNoGap = sGapScore.get(i);
                        BEST_SCORE_UP_STRAND = this.SRNA_STRAND;
                        bestGapScoreUpIndex = i;
                    }
                }
            }
            for (int i = 0; i < U_MG_encoded.size(); i++) {
                if (U_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (mGapScore.get(i) <= bestScoreUpForNoGap) {
                        bestScoreUpForNoGap = mGapScore.get(i);
                        BEST_SCORE_UP_STRAND = this.MRNA_STRAND;
                        bestGapScoreUpIndex = i;
                    }
                }
            }
            if (BOTH_UP) {
                if (noGapScore <= bestScoreUpForNoGap) {
                    bestScoreUpForNoGap = noGapScore;
                    usingNoGapForBOTH_UP = true;
                }//else leave it as it is.
            }
        }
    }//end method.

    /**
     * Initialise the fields when starting a new search down the tree.
     */
    private void D_init() {

        mgDownBroken = new Stack<Byte>();
        mgDownScore = new Stack<Float>();
        mgOriginLevel = new Stack<Byte>();
        dmgOrigLevel = null;
        dmgBroken = null;
        dmgScore = null;
        dmgOriginLevel = new ArrayList<Byte>();
        //previousLevel = 99;
        D_NG_encoded = new byte[33];
        D_NG_encoded[Data.RULES_BROKEN] = FALSE;
        noGapScoreD = 0.0f;
        NGmismatchDown = new Stack<Float>();
        NGmismatchDown.push(noGapScoreD);
        rulesBroken = new Stack<Byte>();
        rulesBroken.push(D_NG_encoded[Data.RULES_BROKEN]);
        D_SG_encoded = new ArrayList<byte[]>();
        sGapScoreD = new ArrayList<Float>();
        D_MG_encoded = new ArrayList<byte[]>();
        mGapScoreD = new ArrayList<Float>();
        level = 11;
        mGapIndexD = level - 1;
        nodes = new Stack<Node>();
        nodes.push(startNode);
        treeLevel = new Stack<Byte>();
        treeLevel.push(level);
        this.setBestGappedUp();
        if (Data.VERY_VERBOSE) {
            noGapSRNAD = new byte[32];
            noGapPositionsD = new byte[32];
            noGapMRNAD = new byte[32];
            sGapSRNAD = new ArrayList<byte[]>();
            sGapPositionsD = new ArrayList<byte[]>();
            sGapMRNAD = new ArrayList<byte[]>();
            mGapSRNAD = new ArrayList<byte[]>();
            mGapPositionsD = new ArrayList<byte[]>();
            mGapMRNAD = new ArrayList<byte[]>();
        }//end verbose.
    }//end method.
    private Stack<Byte> mgDownBroken;
    private Stack<Float> mgDownScore;
    private Stack<Byte> mgOriginLevel;
    private Byte dmgOrigLevel;
    private Byte dmgBroken;
    private Float dmgScore;
    private ArrayList<Byte> dmgOriginLevel;

    /**
     * Pop the stacks and push the nodes when searching down the tree.
     */
    private void pop() {

        for (int i = 0; i < pushed; i++) {
            NGmismatchDown.push(noGapScoreD);
            rulesBroken.push(D_NG_encoded[Data.RULES_BROKEN]);

            if (!D_MG_encoded.isEmpty()) {
                mgDownBroken.push(D_MG_encoded.get(D_MG_encoded.size() - 1)[Data.RULES_BROKEN]);
                mgDownScore.push(mGapScoreD.get(D_MG_encoded.size() - 1));
                mgOriginLevel.push(dmgOriginLevel.get(dmgOriginLevel.size() - 1));
            }
        }

        if (!mgDownBroken.isEmpty()) {
            dmgOrigLevel = mgOriginLevel.pop();
            dmgBroken = mgDownBroken.pop();
            dmgScore = mgDownScore.pop();
            if (dmgBroken == FALSE && dmgOrigLevel < level) {
                makeNew_D_MG_Encoded(dmgScore);
            }
        }

        focusNode = nodes.pop();
        level = treeLevel.pop();
        noGapIndexD = level + 3;
        mGapIndexD = noGapIndexD - 1;
        sGapIndexD = noGapIndexD + 1;
        noGapScoreD = NGmismatchDown.pop();
        D_NG_encoded[Data.RULES_BROKEN] = rulesBroken.pop();
        pushed = 0;


        if (focusNode.edgeA != null) {
            nodes.push(focusNode.edgeA);
            treeLevel.push((byte) (level + 1));
            pushed++;

            if (Data.VERY_VERBOSE) {
                int tee = level + 1;
                System.out.println(tee + " A");
            }
        }
        if (focusNode.edgeC != null) {
            nodes.push(focusNode.edgeC);
            treeLevel.push((byte) (level + 1));
            pushed++;

            if (Data.VERY_VERBOSE) {
                int tee = level + 1;
                System.out.println(tee + "C");
            }
        }
        if (focusNode.edgeG != null) {
            nodes.push(focusNode.edgeG);
            treeLevel.push((byte) (level + 1));
            pushed++;

            if (Data.VERY_VERBOSE) {
                int tee = level + 1;
                System.out.println(tee + "G");
            }
        }
        if (focusNode.edgeT != null) {
            nodes.push(focusNode.edgeT);
            treeLevel.push((byte) (level + 1));
            pushed++;

            if (Data.VERY_VERBOSE) {
                int tee = level + 1;
                System.out.println(tee + "T");
            }
        }
    }//end method.

    /**
     * Logic for an exact match on an un-gapped duplex when searching down the tree.
     * @return True if an exact match.
     */
    private boolean D_NG_exactMatch() {
        //JUST GET OUTA HERE - AND DONT LET ANY FURTHER EXECUTIONS IN CONDITIONAL STATEMENTS DEPENDANT UPON THIS RESULT.
        if (D_NG_encoded[Data.RULES_BROKEN] == TRUE) {
            return true;
        }
        if (focusNode.edge == sequence[noGapIndexD]) {
            if (Data.VERY_VERBOSE) {
                noGapSRNAD[noGapIndexD] = focusNode.edge;
                noGapPositionsD[noGapIndexD] = '|';
                noGapMRNAD[noGapIndexD] = sequence[noGapIndexD];
            }//end verbose.
            D_NG_encoded[noGapIndexD] = Data.NG_MATCH;
            return true;
        } else {
            return false;
        }
    }//end method.

    /**
     * Logic and rules for a gap situation when searching down the tree.
     * @return True if a gap found.
     */
    private boolean D_gapFound() {
        if (!params.isAllowSingleNtGap()) {
            return false;
        }
        boolean gapFound = false;
        if (D_NG_encoded[Data.RULES_BROKEN] == FALSE) {
            Float score = noGapScore + noGapScoreD + 1.0f;
            boolean broken = false;
            if (level == 12 && score > 2.5f) {
                broken = true;
            }
            if (score <= params.getMaxMismatches() && !broken) {
                if (focusNode.parent != null && focusNode.parent.edge == sequence[noGapIndexD - 1] && noGapIndexD + 1 < sequence.length
                        && focusNode.edge == sequence[noGapIndexD + 1]) {
                    if (Data.VERY_VERBOSE) {
                        byte[] sRNA = new byte[32];
                        byte[] positions = new byte[32];
                        byte[] mRNA = new byte[32];
                        System.arraycopy(noGapSRNAD, 10, sRNA, 10, 22);
                        System.arraycopy(noGapPositionsD, 10, positions, 10, 22);
                        System.arraycopy(noGapMRNAD, 10, mRNA, 10, 22);
                        sRNA[noGapIndexD] = '-';
                        positions[noGapIndexD] = ' ';
                        mRNA[noGapIndexD] = sequence[noGapIndexD];
                        sGapSRNAD.add(sRNA);
                        sGapPositionsD.add(positions);
                        sGapMRNAD.add(mRNA);
                    }//end verbose.
                    //sGapBrokenRulesD.add(D_NG_encode[Data.RULES_BROKEN]);
                    sGapScoreD.add(score);
                    //sGapStartTreeLevel.add(level);
                    gapFound = true;
                    byte[] temp_D_SG_encoded = new byte[34];
                    System.arraycopy(D_NG_encoded, 0, temp_D_SG_encoded, 0, 33);
                    temp_D_SG_encoded[noGapIndexD] = Data.SG_GAP;
                    temp_D_SG_encoded[Data.TREE_LEVEL_INDEX] = level;
                    D_SG_encoded.add(temp_D_SG_encoded);
                }
                if (focusNode.parent != null && focusNode.parent.edge == sequence[noGapIndexD - 1] && !nodes.isEmpty()
                        && nodes.peek().edge == sequence[noGapIndexD]) {
                    if (Data.VERY_VERBOSE) {
                        byte[] sRNA = new byte[32];
                        byte[] positions = new byte[32];
                        byte[] mRNA = new byte[32];
                        System.arraycopy(noGapSRNAD, 10, sRNA, 10, 22);
                        System.arraycopy(noGapPositionsD, 10, positions, 10, 22);
                        System.arraycopy(noGapMRNAD, 10, mRNA, 10, 22);
                        sRNA[noGapIndexD] = focusNode.edge;
                        positions[noGapIndexD] = ' ';
                        mRNA[noGapIndexD] = '-';
                        mGapSRNAD.add(sRNA);
                        mGapPositionsD.add(positions);
                        mGapMRNAD.add(mRNA);
                    }
                    //mGapBrokenRulesD.add(D_NG_encode[Data.RULES_BROKEN]);
                    mGapScoreD.add(score);
                    gapFound = true;
                    mGapFound = true;
                    byte[] temp_D_MG_encoded = new byte[34];
                    System.arraycopy(D_NG_encoded, 0, temp_D_MG_encoded, 0, 33);
                    temp_D_MG_encoded[noGapIndexD] = (byte) (Data.MG_GAP + focusNode.edge);
                    temp_D_MG_encoded[Data.TREE_LEVEL_INDEX] = level;
                    D_MG_encoded.add(temp_D_MG_encoded);

                    mgDownBroken.push(FALSE);
                    mgDownScore.push(score);
                    mgOriginLevel.push(level);
                    dmgOriginLevel.add(level);

                }
            }
        }
        if (gapFound) {
            D_NG_mismatch();
        }
        return gapFound;
    }//end method.

    /**
     * Logic for a mismatch in an un-gapped duplex when searching down the tree.
     */
    private void D_NG_mismatch() {
        if (Data.VERY_VERBOSE) {
            noGapSRNAD[noGapIndexD] = focusNode.edge;
            noGapMRNAD[noGapIndexD] = sequence[noGapIndexD];
        }
        //IF G-U BASE FOR NO-GAP.
        if (focusNode.edge == G && sequence[noGapIndexD] == A || focusNode.edge == T && sequence[noGapIndexD] == C) {
            if (Data.VERY_VERBOSE) {
                noGapPositionsD[noGapIndexD] = 'o';
            }
            noGapScoreD += 0.5f;
            D_NG_encoded[noGapIndexD] = (byte) (Data.NG_MISMATCH_HALF + focusNode.edge);
        } else {
            if (Data.VERY_VERBOSE) {
                noGapPositionsD[noGapIndexD] = ' ';
            }
            noGapScoreD += 1.0f;
            D_NG_encoded[noGapIndexD] = (byte) (Data.NG_MISMATCH_FULL + focusNode.edge);
        }
        if (level == 12 && noGapScoreD + bestScoreUpForNoGap > 2.5f) {
            D_NG_encoded[Data.RULES_BROKEN] = TRUE;
        }
        if (noGapScoreD + bestScoreUpForNoGap > params.getMaxMismatches() ) {
            D_NG_encoded[Data.RULES_BROKEN] = TRUE;
        }
        if (!params.isAllowAdjacentMismatches() && doubleGapCheck(D_NG_encoded, Data.ENCODED_SOURCE_IS_NO_GAP)) {
            D_NG_encoded[Data.RULES_BROKEN] = TRUE;
        }
    }//end method.

    /**
     * Logic for an exact match in a duplex which has a gap on the mRNA strand.
     * @return True if an exact match.
     */
    private boolean D_MG_exactMatch() {

        //System.out.println("THE FOCUS NODE IS....: "+(char)focusNode.edge);
        int length;
        if (mGapFound) {
            length = D_MG_encoded.size() - 1;
        } else {
            length = D_MG_encoded.size();
        }
        if (focusNode.edge == sequence[mGapIndexD]) {
            for (int i = 0; i < length; i++) {
                if (D_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (Data.VERY_VERBOSE) {
                        mGapSRNAD.get(i)[noGapIndexD] = focusNode.edge;
                        mGapPositionsD.get(i)[noGapIndexD] = '|';
                        mGapMRNAD.get(i)[noGapIndexD] = sequence[mGapIndexD];
                    }
                    D_MG_encoded.get(i)[noGapIndexD] = Data.MG_EXACT_MATCH;
                }
            }
            return true;
        } else {
            return false;
        }
    }//end method.

    /**
     * Logic for an exact match in a duplex which has a gap on the sRNA strand.
     * @return True if an exact match.
     */
    private boolean D_SG_exactMatch() {
        if (focusNode.edge == sequence[sGapIndexD]) {
            for (int i = 0; i < D_SG_encoded.size(); i++) {
                if (D_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    if (Data.VERY_VERBOSE) {
                        sGapSRNAD.get(i)[sGapIndexD] = focusNode.edge;
                        sGapPositionsD.get(i)[sGapIndexD] = '|';
                        sGapMRNAD.get(i)[sGapIndexD] = sequence[sGapIndexD];
                    }
                    D_SG_encoded.get(i)[sGapIndexD] = Data.SG_EXACT_MATCH;
                }
            }
            return true;
        } else {
            return false;
        }
    }//end method.
    private float[] makeAnewOne = new float[3];

    /**
     * @deprecated
     * @param write
     * @param makeNew
     * @param score
     * @param i
     * @return
     */
    private synchronized float[] accessMakeNewOne(boolean write, float makeNew, float score, int i) {
        if (write) {
            makeAnewOne[0] = makeNew;
            makeAnewOne[1] = score;
            makeAnewOne[2] = i;
            return null;
        } else {
            return makeAnewOne;
        }
    }

    //private boolean logicA = false;
    //private boolean logicB = false;
    //private float aScore;
    /**
     * Logic for a mismatch in a duplex where there is a gap on the mRNA strand when
     * searching down the tree.
     */
    private void D_MG_mismatch() {
        //  logicA = false;
        //   logicB = false;

        int length;
        if (mGapFound) {
            length = D_MG_encoded.size() - 1;
        } else {
            length = D_MG_encoded.size();
        }
        for (int i = 0; i < length; i++) {

            if (D_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                //       logicA = true;
                if (Data.VERY_VERBOSE) {
                    mGapSRNAD.get(i)[noGapIndexD] = focusNode.edge;
                    mGapMRNAD.get(i)[noGapIndexD] = sequence[mGapIndexD];
                }
                //IF G-U BASE FOR NO-GAP.
                if (focusNode.edge == G && sequence[mGapIndexD] == A || focusNode.edge == T && sequence[mGapIndexD] == C) {
                    //System.out.println("if G-U base (G-A): "+focusNode.edge +" "+sequence[mGapIndexD]);
                    if (Data.VERY_VERBOSE) {
                        mGapPositionsD.get(i)[noGapIndexD] = 'o';

                    }
                    //            aScore = (float)0.5;
                    mGapScoreD.set(i, mGapScoreD.get(i) + 0.5f);
                    D_MG_encoded.get(i)[noGapIndexD] = (byte) (Data.MG_MISMATCH_HALF + focusNode.edge);

                } else {
                    if (Data.VERY_VERBOSE) {
                        mGapPositionsD.get(i)[noGapIndexD] = ' ';
                    }
                    //          aScore = 1;
                    mGapScoreD.set(i, mGapScoreD.get(i) + 1.0f);
                    D_MG_encoded.get(i)[noGapIndexD] = (byte) (Data.MG_MISMATCH_FULL + focusNode.edge);

                }
                if (level == 12 && mGapScoreD.get(i) > 2.5f) {
                    D_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                    //           logicB = true;

                }
                if (mGapScoreD.get(i) > params.getMaxMismatches() ) {
                    D_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                    //          logicB = true;
                }
                if (!params.isAllowAdjacentMismatches() && doubleGapCheck(D_MG_encoded.get(i), Data.ENCODED_SOURCE_IS_M_GAP)) {
                    D_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                    //            logicB = true;
                }
            }//end rules broken.





        }//end for.

    }//end method.

    //private int previousLevel = 99;
    private void makeNew_D_MG_Encoded(Float score) {

        int lastIndexOfMGD = D_MG_encoded.size() - 1;
//        if(previousLevel > level && lastIndexOfMGD >= 0 && level > D_MG_encoded.get(lastIndexOfMGD)[Data.TREE_LEVEL_INDEX] && D_MG_encoded.get(lastIndexOfMGD)[Data.RULES_BROKEN] == TRUE){
//            previousLevel = level;

        //if(lastIndexOfMGD < D_MG_encoded.size()){
        byte[] temp_D_MG_encoded = new byte[34];
        System.arraycopy(D_MG_encoded.get(lastIndexOfMGD), 0, temp_D_MG_encoded, 0, 33);
        temp_D_MG_encoded[Data.RULES_BROKEN] = FALSE;
        D_MG_encoded.add(temp_D_MG_encoded);
        mGapScoreD.add(score);

        if (Data.VERY_VERBOSE) {
            byte[] sRNA = new byte[32];
            byte[] positions = new byte[32];
            byte[] mRNA = new byte[32];
            System.arraycopy(mGapSRNAD.get(lastIndexOfMGD), 10, sRNA, 10, 22);
            System.arraycopy(mGapPositionsD.get(lastIndexOfMGD), 10, positions, 10, 22);
            System.arraycopy(mGapMRNAD.get(lastIndexOfMGD), 10, mRNA, 10, 22);
//                    sRNA[noGapIndexD] = focusNode.edge;
//                    if(aScore == 0.5){
//                        positions[noGapIndexD] = 'o';
//                    }else{
//                        positions[noGapIndexD] = ' ';
//                    }
//                    mRNA[noGapIndexD] = sequence[mGapIndexD];
            mGapSRNAD.add(sRNA);
            mGapPositionsD.add(positions);
            mGapMRNAD.add(mRNA);
        }
        //}
    }

    /**
     * Logic for a mismatch in a duplex which has a gap on the sRNA strand when
     * searching down the tree.
     */
    private void D_SG_mismatch() {
        for (int i = 0; i < D_SG_encoded.size(); i++) {
            if (D_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                if (Data.VERY_VERBOSE) {
                    sGapSRNAD.get(i)[sGapIndexD] = focusNode.edge;
                    sGapMRNAD.get(i)[sGapIndexD] = sequence[sGapIndexD];
                }
                //IF G-U BASE GOES HERE FOR NO-GAP.
                if (focusNode.edge == G && sequence[sGapIndexD] == A || focusNode.edge == T && sequence[sGapIndexD] == C) {
                    if (Data.VERY_VERBOSE) {
                        sGapPositionsD.get(i)[sGapIndexD] = 'o';
                    }
                    sGapScoreD.set(i, sGapScoreD.get(i) + 0.5f);
                    D_SG_encoded.get(i)[sGapIndexD] = (byte) (Data.SG_MISMATCH_HALF + focusNode.edge);
                } else {
                    if (Data.VERY_VERBOSE) {
                        sGapPositionsD.get(i)[sGapIndexD] = ' ';
                    }
                    sGapScoreD.set(i, sGapScoreD.get(i) + 1.0f);
                    D_SG_encoded.get(i)[sGapIndexD] = (byte) (Data.SG_MISMATCH_FULL + focusNode.edge);
                }
                if (level == 12 && sGapScoreD.get(i) > 2.5f) {
                    D_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                }
                if (sGapScoreD.get(i) > params.getMaxMismatches() ) {
                    D_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                }
                if (!params.isAllowAdjacentMismatches() && doubleGapCheck(D_SG_encoded.get(i), Data.ENCODED_SOURCE_IS_S_GAP)) {
                    D_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                }
            }//end rules broken.
        }//end for.
    }//end method.

    /**
     * Tests to see if there are two adjacent mismatches.
     * @param encoded The encoded duplex.
     * @param encodedSource The source of the encoded duplex.
     * @return True if there are two adjacent mismatches.
     */
    private boolean doubleGapCheck(byte[] encoded, int encodedSource) {
        switch (encodedSource) {
            case Data.ENCODED_SOURCE_IS_S_GAP: {
                boolean AA = false;
                if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_FULL_A) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_FULL_C) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_FULL_G) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_FULL_T) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_HALF_A) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_HALF_C) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_HALF_G) {
                    AA = true;
                } else if (encoded[sGapIndexD - 2] == Data.SG_MISMATCH_HALF_T) {
                    AA = true;
                }
                if (AA) {
                    boolean BB = false;
                    if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_FULL_A) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_FULL_C) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_FULL_G) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_FULL_T) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_HALF_A) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_HALF_C) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_HALF_G) {
                        BB = true;
                    } else if (encoded[sGapIndexD - 1] == Data.SG_MISMATCH_HALF_T) {
                        BB = true;
                    }
                    if (BB) {
                        return true;
                    }
                }
                break;
            }
            case Data.ENCODED_SOURCE_IS_M_GAP: {
                boolean AA = false;
                if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_FULL_A) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_FULL_C) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_FULL_G) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_FULL_T) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_HALF_A) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_HALF_C) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_HALF_G) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.MG_MISMATCH_HALF_T) {
                    AA = true;
                }
                if (AA) {
                    boolean BB = false;
                    if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_FULL_A) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_FULL_C) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_FULL_G) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_FULL_T) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_HALF_A) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_HALF_C) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_HALF_G) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.MG_MISMATCH_HALF_T) {
                        BB = true;
                    }
                    if (BB) {
                        return true;
                    }
                }
                break;
            }
            case Data.ENCODED_SOURCE_IS_NO_GAP: {
                boolean AA = false;
                if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_FULL_A) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_FULL_C) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_FULL_G) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_FULL_T) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_HALF_A) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_HALF_C) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_HALF_G) {
                    AA = true;
                } else if (encoded[noGapIndexD - 2] == Data.NG_MISMATCH_HALF_T) {
                    AA = true;
                }
                if (AA) {
                    boolean BB = false;
                    if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_FULL_A) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_FULL_C) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_FULL_G) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_FULL_T) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_HALF_A) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_HALF_C) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_HALF_G) {
                        BB = true;
                    } else if (encoded[noGapIndexD - 1] == Data.NG_MISMATCH_HALF_T) {
                        BB = true;
                    }
                    if (BB) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }//end method.

    /**
     * Verbose debugging function for printing the contents of the data structures.
     *
     */
    private void printStuffDown() {
        if (Data.VERY_VERBOSE) {
            System.out.println("No GapD: " + new String(this.noGapSRNAD));
            System.out.println("No GapD: " + new String(this.noGapPositionsD) + "br: " + D_NG_encoded[Data.RULES_BROKEN] + " score: " + (this.noGapScoreD + bestScoreUpForNoGap));
            System.out.println("No GapD: " + new String(this.noGapMRNAD));
            for (int i = 0; i < mGapPositionsD.size(); i++) {
                System.out.println("m GAPD:  " + new String(mGapSRNAD.get(i)));
                System.out.println("m GAPD:  " + new String(mGapPositionsD.get(i)) + "br: " + D_MG_encoded.get(i)[Data.RULES_BROKEN] + " score: " + mGapScoreD.get(i));
                System.out.println("m GAPD:  " + new String(mGapMRNAD.get(i)));
            }
            for (int i = 0; i < sGapPositionsD.size(); i++) {
                System.out.println("s GAPD:  " + new String(sGapSRNAD.get(i)));
                System.out.println("s GAPD:  " + new String(sGapPositionsD.get(i)) + "br: " + D_SG_encoded.get(i)[Data.RULES_BROKEN] + " score: " + sGapScoreD.get(i));
                System.out.println("s GAPD:  " + new String(sGapMRNAD.get(i)));
            }
            System.out.println("GAPPED_UP_ONLY: " + this.GAPPED_UP_ONLY);
            System.out.println("NO_GAPPED_UP_ONLY: " + this.NO_GAPPED_UP_ONLY);
            System.out.println("BOTH_UP: " + this.BOTH_UP);
        }
    }//end method.

    /**
     * Test to see if we have any valid alignment. IF we do not, we can save some time.
     * @return True if we can break the search.
     */
    private boolean saveSomeTime() {
        boolean saveTime = false;
        if (GAPPED_UP_ONLY) {
            if (D_NG_encoded[Data.RULES_BROKEN] == TRUE) {
                saveTime = true;
            } else {
                saveTime = false;
            }//end else.
        } else {
            boolean haveGapped = false;
            for (int i = 0; i < D_MG_encoded.size(); i++) {
                if (level < D_MG_encoded.get(i)[Data.TREE_LEVEL_INDEX]) {
                    D_MG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                }//end if.
                if (D_MG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    haveGapped = true;
                }//end if.
            }//end for.
            for (int i = 0; i < D_SG_encoded.size(); i++) {
                if (level < D_SG_encoded.get(i)[Data.TREE_LEVEL_INDEX]) {
                    D_SG_encoded.get(i)[Data.RULES_BROKEN] = TRUE;
                }//end if.
                if (D_SG_encoded.get(i)[Data.RULES_BROKEN] == FALSE) {
                    haveGapped = true;
                }//end if.
            }//end for.
            if (D_NG_encoded[Data.RULES_BROKEN] == TRUE && haveGapped == false) {
                saveTime = true;
            }//end if.

        }//end else.
        return saveTime;
    }//end method.

    /**
     * Get the best alignment when there is a gap and no gap alignment found.
     * @return Best state.
     */
    private int getBestBOTH_UP() {
        //(NGU & NGD) v (NGU & GD) v (GU & NGD)
        this.setBestGappedUp();
        this.setBestGappedDown();

        //A = NGU & NGD
        float As = noGapScore + noGapScoreD;
        boolean okA = D_NG_encoded[Data.RULES_BROKEN] == FALSE && As <= params.getMaxMismatches();

        //B = NGU & GD
        float Bs = bestGapScoreDown;
        boolean okB = bestGapDownStrand != 0 && Bs <= params.getMaxMismatches();

        //GU & NGD
        float Cs;
        boolean okC;
        if (usingNoGapForBOTH_UP) {
            Cs = As;
            okC = okA;
        } else {
            Cs = bestScoreUpForNoGap + noGapScoreD;
            okC = D_NG_encoded[Data.RULES_BROKEN] == FALSE && Cs <= params.getMaxMismatches();
        }

        if (Data.VERY_VERBOSE) {
            System.out.println("-----------------------------------");
            System.out.println("Tree.getBestBOTH_UP() = ");
            System.out.println("OKA: " + okA);
            System.out.println("As: " + As);
            System.out.println("OKB: " + okB);
            System.out.println("Bs: " + Bs);
            System.out.println("OKC: " + okC);
            System.out.println("Cs: " + Cs);
        }

        float value = 99.99f;//Default non-value 99.9
        int state = 0;//Default non-value 0
        if (Cs < value && okC) {
            value = Cs;
            state = 3;
        }
        if (Bs < value && okB) {
            value = Bs;
            state = 2;
        }
        if (As <= value && okA) {
            state = 1;
        }

        if (Data.VERY_VERBOSE) {
            System.out.println("STATE: " + state);
            System.out.println("-----------------------------------");
        }

        return state;
    }//end method.


    /**
     * Logic for selecting the best alignment found when traversing down a unique
     * path in the small RNA tree.
     * @return true if an acceptable alignment was found.
     */
    private boolean selectBestAlignmentsFound() {
        hasGap = false;
        features = new boolean[2];
        if (Data.VERY_VERBOSE) {
            outSrna = new byte[32];
            outPositions = new byte[32];
            outMrna = new byte[32];
        }
        outAlignmentScore = 0;
        if(GAPPED_UP_ONLY) {
            hasGap = true;
            if (D_NG_encoded[Data.RULES_BROKEN] == FALSE && BEST_SCORE_UP_STRAND == SRNA_STRAND) {
                U_decoded = Data.U_decode(U_SG_encoded.get(bestGapScoreUpIndex), sequence);
                D_decoded = Data.D_decode(D_NG_encoded, sequence, features);
                finalIndex = noGapIndexD + 1;

                if (Data.VERY_VERBOSE) {
                    System.arraycopy(sGapSRNA.get(bestGapScoreUpIndex), 0, outSrna, 0, 14);
                    System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                    System.arraycopy(sGapPositions.get(bestGapScoreUpIndex), 0, outPositions, 0, 14);
                    System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                    System.arraycopy(sGapMRNA.get(bestGapScoreUpIndex), 0, outMrna, 0, 14);
                    System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                }
                outAlignmentScore = (sGapScore.get(bestGapScoreUpIndex) - 1.0f) + Data.GAP_SCORE + noGapScoreD;
                return true;
            }else if (D_NG_encoded[Data.RULES_BROKEN] == FALSE && BEST_SCORE_UP_STRAND == MRNA_STRAND) {
                U_decoded = Data.U_decode(U_MG_encoded.get(bestGapScoreUpIndex), sequence);
                D_decoded = Data.D_decode(D_NG_encoded, sequence,features);
                finalIndex = noGapIndexD + 1;
                if (Data.VERY_VERBOSE) {
                    System.arraycopy(mGapSRNA.get(bestGapScoreUpIndex), 0, outSrna, 0, 14);
                    System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                    System.arraycopy(mGapPositions.get(bestGapScoreUpIndex), 0, outPositions, 0, 14);
                    System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                    System.arraycopy(mGapMRNA.get(bestGapScoreUpIndex), 0, outMrna, 0, 14);
                    System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                }
                outAlignmentScore = (mGapScore.get(bestGapScoreUpIndex) - 1.0f) + Data.GAP_SCORE + noGapScoreD;
                return true;
            }else {
                return false;
            }
        } else if (NO_GAPPED_UP_ONLY) {
            U_decoded = Data.U_decode(U_NG_encoded, sequence);
            if (Data.VERY_VERBOSE) {
                System.arraycopy(noGapSRNA, 0, outSrna, 0, 14);
                System.arraycopy(noGapPositions, 0, outPositions, 0, 14);
                System.arraycopy(noGapMRNA, 0, outMrna, 0, 14);
            }
            this.setBestGappedDown();
            if (D_NG_encoded[Data.RULES_BROKEN] == FALSE && (noGapScore + noGapScoreD) <= bestGapScoreDown) {
                D_decoded = Data.D_decode(D_NG_encoded, sequence,features);
                finalIndex = noGapIndexD + 1;
                if (Data.VERY_VERBOSE) {
                    System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                    System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                    System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                }
                outAlignmentScore = noGapScore + noGapScoreD;
                return true;
            } else {
                hasGap = true;
                if (bestGapDownStrand == SRNA_STRAND) {
                    D_decoded = Data.D_decode(D_SG_encoded.get(bestGapScoreDownIndex), sequence,features);
                    finalIndex = sGapIndexD + 1;
                    if (Data.VERY_VERBOSE) {
                        System.arraycopy(sGapSRNAD.get(bestGapScoreDownIndex), 14, outSrna, 14, 18);
                        System.arraycopy(sGapPositionsD.get(bestGapScoreDownIndex), 14, outPositions, 14, 18);
                        System.arraycopy(sGapMRNAD.get(bestGapScoreDownIndex), 14, outMrna, 14, 18);
                    }
                    outAlignmentScore = (sGapScoreD.get(bestGapScoreDownIndex));
                    return true;
                } else if (bestGapDownStrand == MRNA_STRAND) {
                    D_decoded = Data.D_decode(D_MG_encoded.get(bestGapScoreDownIndex), sequence,features);
                    finalIndex = noGapIndexD + 1;
                    if (Data.VERY_VERBOSE) {
                        System.arraycopy(mGapSRNAD.get(bestGapScoreDownIndex), 14, outSrna, 14, 18);
                        //System.out.println("DEBUG: "+new String(outSrna));
                        System.arraycopy(mGapPositionsD.get(bestGapScoreDownIndex), 14, outPositions, 14, 18);
                        System.arraycopy(mGapMRNAD.get(bestGapScoreDownIndex), 14, outMrna, 14, 18);
                    }
                    outAlignmentScore = (mGapScoreD.get(bestGapScoreDownIndex));
                    return true;
                } else {
                    return false;
                }
            }
        } else if (BOTH_UP) {
            //(NGU & NGD) v (NGU & GD) v (GU & NGD)
            switch (this.getBestBOTH_UP()) {
                case 1: {
                    U_decoded = Data.U_decode(U_NG_encoded, sequence);
                    D_decoded = Data.D_decode(D_NG_encoded, sequence,features);
                    finalIndex = noGapIndexD + 1;
                    if (Data.VERY_VERBOSE) {
                        System.arraycopy(noGapSRNA, 0, outSrna, 0, 14);
                        System.arraycopy(noGapPositions, 0, outPositions, 0, 14);
                        System.arraycopy(noGapMRNA, 0, outMrna, 0, 14);
                        System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                        System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                        System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                    }
                    outAlignmentScore = noGapScore + noGapScoreD;
                    if (outAlignmentScore <= params.getMaxMismatches() ) {
                        return true;
                    } else {
                        return false;
                    }
                }
                case 2: {
                    hasGap = true;
                    U_decoded = Data.U_decode(U_NG_encoded, sequence);
                    if (Data.VERY_VERBOSE) {
                        System.arraycopy(noGapSRNA, 0, outSrna, 0, 14);
                        System.arraycopy(noGapPositions, 0, outPositions, 0, 14);
                        System.arraycopy(noGapMRNA, 0, outMrna, 0, 14);
                    }
                    if (bestGapDownStrand == SRNA_STRAND) {
                        D_decoded = Data.D_decode(D_SG_encoded.get(bestGapScoreDownIndex), sequence,features);
                        finalIndex = sGapIndexD + 1;
                        if (Data.VERY_VERBOSE) {
                            System.arraycopy(sGapSRNAD.get(bestGapScoreDownIndex), 14, outSrna, 14, 18);
                            System.arraycopy(sGapPositionsD.get(bestGapScoreDownIndex), 14, outPositions, 14, 18);
                            System.arraycopy(sGapMRNAD.get(bestGapScoreDownIndex), 14, outMrna, 14, 18);
                        }
                        outAlignmentScore = (sGapScoreD.get(bestGapScoreDownIndex) - 1.0f + Data.GAP_SCORE);
                        return true;
                    } else if (bestGapDownStrand == MRNA_STRAND) {
                        D_decoded = Data.D_decode(D_MG_encoded.get(bestGapScoreDownIndex), sequence,features);
                        finalIndex = noGapIndexD + 1;
                        if (Data.VERY_VERBOSE) {
                            System.arraycopy(mGapSRNAD.get(bestGapScoreDownIndex), 14, outSrna, 14, 18);
                            System.arraycopy(mGapPositionsD.get(bestGapScoreDownIndex), 14, outSrna, 14, 18);
                            System.arraycopy(mGapMRNAD.get(bestGapScoreDownIndex), 14, outMrna, 14, 18);
                        }
                        outAlignmentScore = (mGapScoreD.get(bestGapScoreDownIndex) - 1.0f + Data.GAP_SCORE);
                        return true;
                    } else {
                        //System.out.println("1FFF");
                        return false;
                    }
                }
                case 3: {
                    hasGap = true;
                    if (BEST_SCORE_UP_STRAND == SRNA_STRAND) {
                        U_decoded = Data.U_decode(U_SG_encoded.get(bestGapScoreUpIndex), sequence);
                        D_decoded = Data.D_decode(D_NG_encoded, sequence,features);
                        finalIndex = noGapIndexD + 1;
                        if (Data.VERY_VERBOSE) {
                            System.arraycopy(sGapSRNA.get(bestGapScoreUpIndex), 0, outSrna, 0, 14);
                            System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                            System.arraycopy(sGapPositions.get(bestGapScoreUpIndex), 0, outPositions, 0, 14);
                            System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                            System.arraycopy(sGapMRNA.get(bestGapScoreUpIndex), 0, outMrna, 0, 14);
                            System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                        }
                        outAlignmentScore = bestScoreUpForNoGap + noGapScoreD;
                        return true;
                    } else if (BEST_SCORE_UP_STRAND == MRNA_STRAND) {
                        U_decoded = Data.U_decode(U_MG_encoded.get(bestGapScoreUpIndex), sequence);
                        D_decoded = Data.D_decode(D_NG_encoded, sequence,features);
                        finalIndex = noGapIndexD + 1;
                        if (Data.VERY_VERBOSE) {
                            System.arraycopy(mGapSRNA.get(bestGapScoreUpIndex), 0, outSrna, 0, 14);
                            System.arraycopy(noGapSRNAD, 14, outSrna, 14, 18);
                            System.arraycopy(mGapPositions.get(bestGapScoreUpIndex), 0, outPositions, 0, 14);
                            System.arraycopy(noGapPositionsD, 14, outPositions, 14, 18);
                            System.arraycopy(mGapMRNA.get(bestGapScoreUpIndex), 0, outMrna, 0, 14);
                            System.arraycopy(noGapMRNAD, 14, outMrna, 14, 18);
                        }
                        outAlignmentScore = bestScoreUpForNoGap + noGapScoreD;
                        return true;
                    } else {
                        //System.out.println("2F");
                        return false;
                    }
                }//end case
                default: {
                    //System.out.println("3F");
                    return false;
                }
            }//end switch
        }//end else if
        //System.out.println("4F");
        return false;
    }//end method.

    /**
     * Starts the rule based complementarity search.
     * @param sequence 32nt fragment of transcript identified from cleavage position.
     * @param c The chart from which the sequence (fragment) was taken.
     * @param element The index of the compactAbundance and hit position of the degradome fragment on the chart.
     * @param sequenceStartPos The index of the sequence which corresponds to the 10th nucleotide level in the sRNA tree.
     * @return A String representing a record of the search delimited into data fields.
     */
    public String searchTree(byte[] sequence, Chart c, int element, int sequenceStartPos, int totalFragments) throws InterruptedException {
        //Get some memory to store the information we gather when walking the sRNA tree - this is our record which will be delimited into fields.
        String outString = "";
        if (sequence == null) {
            return outString;
        }
        if (sequence.length < 19) {
            return outString;
        }
        ArrayList<Node> subTreeList = null;
        if (params.isAllowMismatchAtPositionEleven()) {
            subTreeList = getStartPositionListMM11(sequence[13], sequence[14]);
        } else {
            subTreeList = getStartPositionList(sequence[13], sequence[14]);
        }
        if (Data.VERY_VERBOSE) {
            System.out.println("-------------------------------------------------------------------");
            System.out.println("STARTING CHARS: " + (char) sequence[13] + " " + (char) sequence[14]);
            System.out.println("Number of nodes in subTreeList: " + subTreeList.size());
        }
        //It is possible that the nts which identify the starting position could contain unknown characters e.g. NN which would
        //cause the function getStartPositionList(N,N) to return null, therefore, trying to get an iterator on a null value would
        //throw a null pointer exception.  If there is no list to look through, then there is nothing to do, so return.
        if (subTreeList == null) {
            if (Data.VERY_VERBOSE) {
                System.out.println("Call to getStartPositionList(byte,byte) returned null.");
            }
            return outString;
        }
        Iterator<Node> subTreeIterator = subTreeList.iterator();
        this.sequence = sequence;
        NEXT:
        while(subTreeIterator.hasNext()) {
            if(Tree.interrupted()){throw new InterruptedException();}
            U_init(subTreeIterator.next());
            while (advance()) {
                if (NOTHING_VALID) {
                    continue NEXT;
                }
                //UNGAPPED.
                if (this.U_NG_exactMatch()) {
                } else if (this.U_gapFound()) {
                } else {
                    this.U_NG_mismatch();
                }
                if (params.isAllowSingleNtGap()) {
                    //GAPPED ON MRNA
                    if (U_MG_exactMatch()) {
                    } else {
                        U_MG_mismatch();
                    }//end else.
                    //GAPPED ON SRNA
                    if (U_SG_ExactMatch()) {
                    } else {
                        U_SG_mismatch();
                    }//end else.
                }// end if using gaps.
                if (Data.VERY_VERBOSE) {
                    this.printStuff();
                }
            }//end while.
            this.D_init();
            SAVETIME:
            while(!nodes.isEmpty()) {
                if(Tree.interrupted()){throw new InterruptedException();}
                mGapFound = false;
                pop();
                //System.out.println("Number of nodes in stack "+nodes.size());
                if (noGapIndexD >= sequence.length) {
                    //System.out.println("Continue because noGapIndeD >= sequence length.");
                    continue SAVETIME;
                }//end if.
                if (saveSomeTime()) {
                    //System.out.println("Continue because saveSomeTime() function return true.");
                    continue SAVETIME;
                }//end if.
                if (D_NG_exactMatch()) {
                } else if (D_gapFound()) {
                } else {
                    D_NG_mismatch();
                }//end else.
                if (params.isAllowSingleNtGap()) {
                    if (D_NG_encoded[Data.RULES_BROKEN] == FALSE || !GAPPED_UP_ONLY) {
                        if (D_MG_exactMatch()) {
                        } else {
                            D_MG_mismatch();
                        }
                        if (sGapIndexD < sequence.length) {
                            if (D_SG_exactMatch()) {
                            } else {
                                D_SG_mismatch();
                            }//end else.
                        }//end if.
                    }//end if.
                }//end if.
                if(Data.VERY_VERBOSE){this.printStuffDown();}
                //TEST FOR BASE CASE.
                if (focusNode.isTerminator && selectBestAlignmentsFound()) {

                    //Empty any clingers within the re-used memory...
                    for (int i = finalIndex; i < D_decoded[0].length; i++) {
                        D_decoded[0][i] = 32;
                        D_decoded[1][i] = 32;
                    }
                    //Turn the decoded srna into a string.
                    String shortRNAout = new String(Data.join(U_decoded[0], D_decoded[0]));
                    //Prepare the short read for searching against the category trees by removing any gaps & white space.
                    StringBuilder st = new StringBuilder(shortRNAout);
                    for (int i = 0; i < st.length(); i++) {
                        if (st.charAt(i) == '-') {
                            hasGap = true;
                            st.deleteCharAt(i);
                        }
                    }
                    String send = st.toString().trim();
                    //Calculate pValue
                    double pValue = -1.0;//Default value - if not using p-values.
                    if (params.isCalculatePvalues()) {
                        switch (c.compactCategory[element]) {
                            case 0: {
                                pValue = pValues.mainUShuffle(send.toCharArray(), c0, outAlignmentScore, params);
                                break;
                            }
                            case 1: {
                                pValue = pValues.mainUShuffle(send.toCharArray(), c1, outAlignmentScore, params);
                                break;
                            }
                            case 2: {
                                pValue = pValues.mainUShuffle(send.toCharArray(), c2, outAlignmentScore, params);
                                break;
                            }
                            case 3: {
                                pValue = pValues.mainUShuffle(send.toCharArray(), c3, outAlignmentScore, params);
                                break;
                            }
                            case 4: {
                                pValue = pValues.mainUShuffle(send.toCharArray(), c4, outAlignmentScore, params);
                                break;
                            }
                        }
                        //Test if the pValue exceeded the cut off.
                        if (params.isNotIncludePvalueGrtCutoff() && pValue == -1.0) {
                            //If it did and we are not reporting p-values above the cut-off, then don't go any further.
                            continue SAVETIME;
                        }
                    }//end if using p-values.
                    //Print the ID for the transcript.
                    outString += "" + c.ID + Data.END_OF_FIELD;
                    //Print the category.
                    outString += "" + c.compactCategory[element] + Data.END_OF_FIELD;
                    //Print the cleavage position.
                    outString += "" + c.compactPositions[element] + Data.END_OF_FIELD;
                    //Print the p-value.
                    outString += "" + pValue + Data.END_OF_FIELD;
                    //print the raw abundance.
                    outString += "" + c.compactAbundance[element] + Data.END_OF_FIELD;
                    //Print the weighted abundance.
                    outString += "" + c.compactWeightedAbundance[element] + Data.END_OF_FIELD;
                    //Calculate the normalised weighted abundance for the degradome hit.
                    double normWeightAbund = (c.compactWeightedAbundance[element] / totalFragments) * Data.NORMALISATION_MULTIPLIER;
                    //Print the normalised weighted abundance for the degradome hit.
                    outString += "" + normWeightAbund + Data.END_OF_FIELD;
                    //Print the sRNA string.
                    outString += "5' " + shortRNAout + " 3'" + LINE_SEPARATOR;
                    //Print out the position bars.
                    String posOut = new String(Data.join(U_decoded[1], D_decoded[1]));
                    outString += "   " + posOut + LINE_SEPARATOR;
                    //Make the mrna a string rather than a char array.
                    String mrnaOut = new String(Data.join(U_decoded[2], D_decoded[2]));
                    //Print out the mRNA.
                    outString += "3' " + mrnaOut + " 5'" + Data.END_OF_FIELD;
                    //Print the alignment score.
                    outString += "" + outAlignmentScore + Data.END_OF_FIELD;
                        if (Data.VERY_VERBOSE) {
                        System.out.println("###############################################################################################");
                        System.out.println("------------------------------------------------------------------------------------------------");
                        System.out.println(new String(outSrna));
                        System.out.println(new String(outPositions));
                        System.out.println(new String(outMrna));
                        System.out.println(new String(Data.join(U_decoded[0], D_decoded[0])));
                        System.out.println(new String(Data.join(U_decoded[1], D_decoded[1])));
                        System.out.println(new String(Data.join(U_decoded[2], D_decoded[2])));
                        System.out.println("P-VALUES --- Gap: "+hasGap+" mmEleven: " + features[Data.MM_ELEVEN] + " Adjacent Mismatches: " + features[Data.ADJACENT_MM] );
                        System.out.println("outAlignmentScore: " + outAlignmentScore);
                        System.out.println("noGapScoreD: " + noGapScoreD);
                        System.out.println("bestScoreUpForNoGap: " + bestScoreUpForNoGap);
                        System.out.println("GAPPED_UP_ONLY: " + GAPPED_UP_ONLY);
                        System.out.println("NO_GAPPED_UP_ONLY: " + NO_GAPPED_UP_ONLY);
                        System.out.println("BOTH_UP: " + BOTH_UP);
                        System.out.println("NOTHING VALID: " + NOTHING_VALID);
                        System.out.println("TREE LEVEL: " + level);
                        System.out.println("noGapBrokenRulesD: " + D_NG_encoded[Data.RULES_BROKEN]);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.println("#############################################################################################");
                    }//end very verbose.
                    //Print the short read id. (> if not given).
                    outString += "" + focusNode.terminator.id + Data.END_OF_FIELD;
                    //Print the abundance of the short read.
                    outString += "" + focusNode.terminator.abundance + Data.END_OF_FIELD;
                    //Calculate the normalised abundance for the short read.
                    double shortReadNormAbund = ((double) focusNode.terminator.abundance / (double) totalRawShortReads) * (double) Data.NORMALISATION_MULTIPLIER;
                    //Print the short read normalised abundance.
                    outString += "" + shortReadNormAbund + Data.END_OF_RECORD;
                    //System.out.println(outString);
                }//end BASE CASE.
            }//end while.
        }//end while.

        return outString;
    }//END METHOD.

    /**
     * @deprecated
     * Get the index into the p-value array which corresponds to the mismatch
     * score of the duplex of interest.
     * @param mm mismatch score.
     * @return an index into the p-value list associated with a mismatch score.
     */
    private int getPValueIndex(float mm) {
        if (mm == 0.0) {
            return 0;
        } else if (mm == 0.5) {
            return 1;
        } else if (mm == 1.0) {
            return 2;
        } else if (mm == 1.5) {
            return 3;
        } else if (mm == 2.0) {
            return 4;
        } else if (mm == 2.5) {
            return 5;
        } else if (mm == 3.0) {
            return 6;
        } else if (mm == 3.5) {
            return 7;
        } else if (mm == 4.0) {
            return 8;
        } else if (mm == 4.5) {
            return 9;
        } else if (mm == 5.0) {
            return 10;
        } else if (mm == 5.5) {
            return 11;
        } else if (mm == 6.0) {
            return 12;
        } else if (mm == 6.5) {
            return 13;
        } else if (mm == 7.0) {
            return 14;
        } else if (mm == 7.5) {
            return 15;
        } else if (mm == 8.0) {
            return 16;
        } else if (mm == 8.5) {
            return 17;
        } else if (mm == 9.0) {
            return 18;
        } else if (mm == 9.5) {
            return 19;
        } else if (mm == 10.0) {
            return 20;
        } else if (mm == 10.5) {
            return 21;
        } else if (mm == 11.0) {
            return 22;
        } else if (mm == 11.5) {
            return 23;
        } else if (mm == 12.0) {
            return 24;
        } else if (mm == 12.5) {
            return 25;
        } else if (mm == 13.0) {
            return 26;
        } else if (mm == 13.5) {
            return 27;
        } else if (mm == 14.0) {
            return 28;
        }
        return 0;
    }//end method.
}//END CLASS

