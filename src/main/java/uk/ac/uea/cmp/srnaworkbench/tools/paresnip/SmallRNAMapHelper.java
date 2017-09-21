
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

/**
 *
 * @author Leighton
 */
public final class SmallRNAMapHelper {

    private static final String LOOKS_LIKE_mIR = "miR";


    private int abundance;
    private String ID;

    public SmallRNAMapHelper(String id){
        abundance = 1;
        this.ID = id;
    }

    public void incrementAbundance(){
        abundance++;
    }

    public int getAbundance(){
        return abundance;
    }

    public String getID(){
        if(ID.length() > 30){
            return ID.substring(0, 30);
        }else{
            return ID;
        }
    }

    public void addAdditionalID(String additionalID){
        if(ID.length() > 40){return;}
        if(additionalID.contains(LOOKS_LIKE_mIR)){
            ID = additionalID+ID;
        }else{
            ID += additionalID;
        }
    }
}
