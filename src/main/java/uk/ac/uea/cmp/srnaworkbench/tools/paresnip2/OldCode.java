/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author rew13hpu
 */
public class OldCode {
    
//        private void clusterAtSeedRegion() {
//        String str;
//        String ref;
//
//        for (int i = 0; i < potentialGroups7to13.size(); i++) {
//            str = SmallRNA.longToString(i, 7);
//            for (int j = i; j < potentialGroups7to13.size(); j++) {
//                if (i == j) {
//                    potentialGroups7to13.get(i).add(j);
//                    potentialGroups2to8.get(i).add(j);
//                } else {
//                    ref = SmallRNA.longToString(j, 7);
//                    boolean potential = false;
//
//                    potential = alignmentSeed(str, ref) <= CUTOFF;
//
//                    if (potential) {
//                        potentialGroups7to13.get(i).add(j);
//                        potentialGroups7to13.get(j).add(i);
//                        potentialGroups2to8.get(i).add(j);
//                        potentialGroups2to8.get(j).add(i);
//                    }
//                }
//
//            }
//        }
//    }
    
    
//    private void clusterAtTail() {
//        int size = Engine.MIN_SRNA_SIZE;
//        int groupSize;
//        double numIndexes;
//
//        if (size >= 19) {
//            groupSize = 7;
//        } else {
//            groupSize = size - 7 - 5;
//        }
//
//        potentialGroupsTail = new ArrayList();
//        numIndexes = Math.pow(4, groupSize);
//
//        for (int i = 0; i < numIndexes; i++) {
//            potentialGroupsTail.add(i, new ResizableIntArray());
//        }
//
//        String str;
//        String ref;
//
//        for (int i = 0; i < potentialGroupsTail.size(); i++) {
//            //To use proper rules!!
//            str = TranscriptFragment.longToString(i, groupSize);
//            for (int j = i; j < potentialGroupsTail.size(); j++) {
//                // System.out.println(str);
//                if (i == j) {
//                    potentialGroupsTail.get(i).add(j);
//                } else {
//                    ref = SmallRNA.longToString(j, groupSize);
//
//                    boolean potential;
//                    //calculate if potential
//                    potential = alignmentTail(str, ref) <= CUTOFF;
//
//                    if (potential) {
//                        potentialGroupsTail.get(i).add(j);
//                        potentialGroupsTail.get(j).add(i);
//                    }
//                }
//            }
//        }
//
//    }
    
//        public static double alignmentTail(String a, String b) {
//
//        //CHANGE ALL THIS FROM DYNAMIC PROGRAMMING TO SOMETHING ELSE?
//        int[][] costs = new int[a.length() + 1][b.length() + 1];
//        for (int j = 0; j <= b.length(); j++) {
//            costs[0][j] = j;
//        }
//        for (int i = 1; i <= a.length(); i++) {
//            costs[i][0] = i;
//            for (int j = 1; j <= b.length(); j++) {
//                costs[i][j] = Math.min(1 + Math.min(costs[i - 1][j], costs[i][j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? costs[i - 1][j - 1] : costs[i - 1][j - 1] + 1);
//            }
//        }
//
//        // walk back through matrix to figure out path
//        StringBuilder aPathRev = new StringBuilder();
//        StringBuilder bPathRev = new StringBuilder();
//        int j = b.length();
//        int i = a.length();
//
//        while (i != 0 && j != 0) {
//            if (costs[i][j] == (a.charAt(i - 1) == b.charAt(j - 1) ? costs[i - 1][j - 1] : costs[i - 1][j - 1] + 1)) {
//                aPathRev.append(a.charAt(--i));
//                bPathRev.append(b.charAt(--j));
//
//            } else if (costs[i][j] == 1 + costs[i][j - 1]) {
//                aPathRev.append('-');
//                bPathRev.append(b.charAt(--j));
//            } else if (costs[i][j] == 1 + costs[i - 1][j]) {
//
//                aPathRev.append(a.charAt(--i));
//                bPathRev.append('-');
//            }
//        }
//        while (j > 0) {
//            aPathRev.append('-');
//            bPathRev.append(b.charAt(j));
//            j--;
//        }
//
//        while (i > 0) {
//            bPathRev.append('-');
//            aPathRev.append(a.charAt(i - 1));
//            i--;
//        }
//
//        // return new String[]{aPathRev.reverse().toString(), bPathRev.reverse().toString()};
//        String[] result = {aPathRev.reverse().toString(), bPathRev.reverse().toString()};
//
//        double score = 0.0;
//
//        for (i = 0; i < result[0].length(); i++) {
//            if (result[0].charAt(i) != result[1].charAt(i)) {
//                if ((result[0].charAt(i) == 'T' && result[1].charAt(i) == 'C') || (result[0].charAt(i) == 'G' && result[1].charAt(i) == 'A')) {
//                    score += 0.5;
//                } else {
//                    score += 1;
//                }
//            }
//        }
//        return score;
//    }
//
//    public static double alignmentSeed(String a, String b) {
//
//        //CHANGE ALL THIS FROM DYNAMIC PROGRAMMING TO SOMETHING ELSE?
//        int[][] costs = new int[a.length() + 1][b.length() + 1];
//        for (int j = 0; j <= b.length(); j++) {
//            costs[0][j] = j;
//        }
//        for (int i = 1; i <= a.length(); i++) {
//            costs[i][0] = i;
//            for (int j = 1; j <= b.length(); j++) {
//                costs[i][j] = Math.min(1 + Math.min(costs[i - 1][j], costs[i][j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? costs[i - 1][j - 1] : costs[i - 1][j - 1] + 1);
//            }
//        }
//
//        // walk back through matrix to figure out path
//        StringBuilder aPathRev = new StringBuilder();
//        StringBuilder bPathRev = new StringBuilder();
//        int j = b.length();
//        int i = a.length();
//
//        while (i != 0 && j != 0) {
//            if (costs[i][j] == (a.charAt(i - 1) == b.charAt(j - 1) ? costs[i - 1][j - 1] : costs[i - 1][j - 1] + 1)) {
//                aPathRev.append(a.charAt(--i));
//                bPathRev.append(b.charAt(--j));
//
//            } else if (costs[i][j] == 1 + costs[i][j - 1]) {
//                aPathRev.append('-');
//                bPathRev.append(b.charAt(--j));
//            } else if (costs[i][j] == 1 + costs[i - 1][j]) {
//
//                aPathRev.append(a.charAt(--i));
//                bPathRev.append('-');
//            }
//        }
//        while (j > 0) {
//            aPathRev.append('-');
//            bPathRev.append(b.charAt(j));
//            j--;
//        }
//
//        while (i > 0) {
//            bPathRev.append('-');
//            aPathRev.append(a.charAt(i - 1));
//            i--;
//        }
//
//        // return new String[]{aPathRev.reverse().toString(), bPathRev.reverse().toString()};
//        String[] result = {aPathRev.reverse().toString(), bPathRev.reverse().toString()};
//
//        double score = 0.0;
//
//        for (i = 0; i < result[0].length(); i++) {
//            if (result[0].charAt(i) != result[1].charAt(i)) {
//                if ((result[0].charAt(i) == 'T' && result[1].charAt(i) == 'C') || (result[0].charAt(i) == 'G' && result[1].charAt(i) == 'A')) {
//                    score += 1;
//                } else {
//                    score += 2;
//                }
//            }
//        }
//        return score;
//    }
    
//    public int getTailRegion() throws SequenceContainsInvalidBaseException {
//        if (stringSeq.length() < 19) {
//            
//            return (int) getWord(tailRegionLength, stringSeq.length() - tailRegionLength + 1);
//        }
//
//        return (int) getWord(7, 13);
//        
//        return (int) super.toBits(stringSeq.substring(14, stringSeq.length()));
//    }
    
    
//     @Override
//    public String toString() {
////        char[] s = new char[arr[6]];
////
////        s[0] = getChar(extract(0, 2));
////
////        int index = 1;
////        for (int i = 2; i < arr[6] * 2; i += 2) {
////            s[index] = getChar(extract(i, i + 2));
////            index++;
////        }
////
////        return new String(s);
//
//        return stringSeq;
//    }
    
//    
//    public int getTargetTailRegion() {
//
//        int size = Engine.MIN_SRNA_SIZE;
//
//        if (size >= 19) {
//            size = 7;
//        } else {
//            size = size - 7 - 5;
//        }
//
//        if (cleavagePosition < 9) {
//            return (int) getWord(size - missingNuleotides, cleavagePosition + 4);
//        }
//        return (int) getWord(size, cleavagePosition + 4);
//    }
}
