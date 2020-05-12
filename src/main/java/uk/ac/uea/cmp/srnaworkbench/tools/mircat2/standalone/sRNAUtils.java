/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.Patman;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.sRNA;
import java.util.ArrayList;

/**
 *
 * @author keu13sgu
 */
public class sRNAUtils {
    
    public static boolean isWindows()
  {
    String os = System.getProperty( "os.name" ).toLowerCase();
    return ( os.indexOf( "win" ) >= 0 );
  }

  public static boolean isMac()
  {
    String os = System.getProperty( "os.name" ).toLowerCase();
    return ( os.indexOf( "mac" ) >= 0 );
  }

  public static boolean isUnix()
  {
    String os = System.getProperty( "os.name" ).toLowerCase();
    return ( os.indexOf( "nix" ) >= 0 || os.indexOf( "nux" ) >= 0 );
  }
    
//    public static boolean isInSameCluster(sRNA s1, Patman p) {
//        int beg = p.getBeg();
//        int end = p.getEnd();
//
//        if (s1.getBeg() >= beg && s1.getEnd() <= end) {
//            return true;
//        }
//        if (s1.getBeg() <= beg && s1.getEnd() >= end) {
//            return true;
//        }
//
//        if (beg - s1.getBeg() <= Params.cluster && beg - s1.getBeg() >= 0) {
//            return true;
//        }
//
//        if (s1.getEnd() - end <= Params.cluster && s1.getEnd() - end >= 0) {
//            return true;
//        }
//
//        int mid = (end - beg) / 2 + beg;
//        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();
//
//        if (!(Math.abs(mid - midS) <= Params.cluster)) {
//            for (sRNA s : p) {
//                if (isInSameCluster(s1, s.getBeg(), s.getEnd())) {
//                    return true;
//                }
//            }
//            return false;
//        }
//        return true;
//
//    }
    
    public static boolean isInSameCluster(Patman s1, int beg, int end) {

        if (s1.getBeg() >= beg && s1.getEnd() <= end) {
            return true;
        }
        if (s1.getBeg() <= beg && s1.getEnd() >= end) {
            return true;
        }
        
        int mid = (end - beg) / 2 + beg;
        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();
        
        
        if((Math.abs(mid - s1.getBeg()) <= Params.cluster) || Math.abs(mid - s1.getEnd()) <= Params.cluster){
            return false;
        }

        if (beg - s1.getBeg() <= Params.cluster && beg - s1.getBeg() >= 0) {
            return true;
        }

        if (s1.getEnd() - end <= Params.cluster && s1.getEnd() - end >= 0) {
            return true;
        }

//        int mid = (end - beg) / 2 + beg;
//        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();

        if (!(Math.abs(mid - midS) <= Params.cluster)) {

            for(sRNA s2: s1)
                if (isInSameCluster(s2, beg, end)) {
                    return true;
                }

            return false;
        }
        return true;

    }
    
    public static boolean isInSameCluster(sRNA s1, Patman p) {
        int beg = p.getBeg();
        int end = p.getEnd();

        if (s1.getBeg() >= beg && s1.getEnd() <= end) {
            return true;
        }
        if (s1.getBeg() <= beg && s1.getEnd() >= end) {
            return true;
        }
        
        int mid = (end - beg) / 2 + beg;
        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();
        
        
        if((Math.abs(mid - s1.getBeg()) <= Params.cluster) || Math.abs(mid - s1.getEnd()) <= Params.cluster){
            return false;
        }
        
        if (beg - s1.getBeg() <= Params.cluster && beg - s1.getBeg() >= 0) {
            return true;
        }

        if (s1.getEnd() - end <= Params.cluster && s1.getEnd() - end >= 0) {
            return true;
        }

        if (!(Math.abs(mid - midS) <= Params.cluster)) {
            for (sRNA s : p) {
                if (isInSameCluster(s1, s.getBeg(), s.getEnd())) {
                    return true;
                }
            }
            return false;
        }
        return true;

    }
    
    public static boolean isInSameCluster(sRNA s1, int beg, int end) {

        if (s1.getBeg() >= beg && s1.getEnd() <= end) {
            return true;
        }
        if (s1.getBeg() <= beg && s1.getEnd() >= end) {
            return true;
        }
        
        int mid = (end - beg) / 2 + beg;
        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();
        
        
        if((Math.abs(mid - s1.getBeg()) <= Params.cluster) || Math.abs(mid - s1.getEnd()) <= Params.cluster){
            return false;
        }

        if (beg - s1.getBeg() <= Params.cluster && beg - s1.getBeg() >= 0) {
            return true;
        }

        if (s1.getEnd() - end <= Params.cluster && s1.getEnd() - end >= 0) {
            return true;
        }

//        int mid = (end - beg) / 2 + beg;
//        int midS = (s1.getEnd() - s1.getBeg()) / 2 + s1.getBeg();

        return (Math.abs(mid - midS) <= Params.cluster);

    }
 
    public static ArrayList<Patman> makeClusters(Patman list) {

        ArrayList<Patman> cls = new ArrayList<>();

        Patman cl = new Patman();
        cl.add(list.get(0));
        cls.add(cl);

        for (int i = 1; i < list.size(); i++) {
            boolean found = false;
            for (int j = cls.size() - 1; j >= 0; j--) {
                if (isInSameCluster(list.get(i), cl)) {
                    cl.add(list.get(i));
                    found = true;
                    break;
                }
            }
            if (!found) {
                cl = new Patman();
                cl.add(list.get(i));
                cls.add(cl);
            }
        }

        return cls;
    }
    
    public static sRNA bestSRNA(Patman list, int b, int e) {
        int max = 0;
        int index = 0;
        
        if(list.isEmpty())
            return null;

        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            sRNA s = list.get(i);
            
            if(!(Math.abs(s.getBeg()- b) < (Params.clearCut + Params.threePrimeOverhang) || 
                    Math.abs(s.getEnd()- e) < (Params.clearCut + Params.threePrimeOverhang))){
                continue;
            }
            
            if (s.getAb() > max) {
                max = s.getAb();
                index = i;
                count = 1;
            }
            else if(s.getAb() == max){
                count ++;
            }
        }

        int max2 = 0;
        if(count > 1)
            for (int i = 0; i < list.size(); i++) {
                sRNA s = list.get(i);

                if (s.getAb() == list.get(index).getAb()) {
                    int beg = s.getBeg();
                    int end = s.getEnd();
                    int sumMAS = 0;
                    for (sRNA sp : list) {
                        if (Math.abs(sp.getBeg() - beg) <= Params.clearCut && Math.abs(sp.getEnd() - end) <= Params.clearCut) {
                            sumMAS += sp.getAb();
                        }
                    }
                    if (sumMAS > max2) {
                        max2 = sumMAS;
                        index = i;
                    }
                }
            }

        return list.get(index);
    }
    
    public static sRNA bestSRNA(Patman list) {
        int max = 0;
        int index = 0;
        
        if(list.isEmpty())
            return null;

        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            sRNA s = list.get(i);
            if (s.getAb() > max) {
                max = s.getAb();
                index = i;
                count = 1;
            }
            else if(s.getAb() == max){
                count ++;
            }
        }

        int max2 = 0;
        if(count > 1)
            for (int i = 0; i < list.size(); i++) {
                sRNA s = list.get(i);

                if (s.getAb() == list.get(index).getAb()) {
                    int beg = s.getBeg();
                    int end = s.getEnd();
                    int sumMAS = 0;
                    for (sRNA sp : list) {
                        if (Math.abs(sp.getBeg() - beg) <= Params.clearCut && Math.abs(sp.getEnd() - end) <= Params.clearCut) {
                            sumMAS += sp.getAb();
                        }
                    }
                    if (sumMAS > max2) {
                        max2 = sumMAS;
                        index = i;
                    }
                }
            }

        return list.get(index);
    }
    
    
    //assumes both patmans are ordered 
    public static void addToPatman2( Patman fromMap,Patman toMap) {
        
        int jInit = toMap.size() - 1;
        
        int i;
        for( i = 0; i < fromMap.size(); i++){
            sRNA sf = fromMap.get(i);
            sRNA st = toMap.get(jInit);

            //the following sequences are downstream, cannot be double
            if(sf.getBeg() + Params.SUBWINDOW > st.getEnd()){
                break;
            }
            
            boolean isDuplicate = false;
            
            for(int j = jInit; j >=0; j--){
                st = toMap.get(j);           
                if(st.getEnd() + Params.SUBWINDOW < sf.getBeg()){
                    break;
                }
                if(st.equals(sf))
                {
                    isDuplicate = true;
                    break;
                }
            }
            if(!isDuplicate){
                toMap.add(sf);
            }

        }
        
        i--;
        for(int newi = i; newi < fromMap.size(); newi++){
            toMap.add(fromMap.get(newi));
        }

    }
    
    public static void addToPatman( Patman fromMap,Patman toMap) {
        for(sRNA s: fromMap){
            if(!toMap.contains(s))
                toMap.add(s);
        }
    }
    
    
    
        
    public static StringBuilder complement(StringBuilder toComplement){
        
        StringBuilder str = new StringBuilder();
        for(int i = 0; i< toComplement.length(); i++){
            char c = toComplement.charAt(i);
            switch(c){
                case 'A':
                case 'a': 
                    str.append("T");
                    break;
                    
                case 'T':
                case 't': 
                    str.append("A");
                    break;
                    
                case 'C':
                case 'c': 
                    str.append("G");
                    break;
                    
                    
                case 'G':
                case 'g': 
                    str.append("C");
                    break;
                    
                default: str.append(c);
            }
        }
        
        return str;
    }

}
