package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * Tool wrapper for an MAList
 * @author mka07yyu
 */
public class MATool extends RunnableTool {

    MAList malist;
    
    public MATool (String refSample, String obsSample, NormalisationType normType, int base, int offset)
    {
        super("MATool");
        malist = new MAList(refSample, obsSample, normType, base, offset);
    }
    
    @Override
    protected void process() throws Exception {
        buildList();
    }
    
    protected void buildList()
    {
        //malist = new MAList(refSample, obsSample, refLibSize, obsLibSize, normType, base, offset);
    }
    
    public MAList getMAList()
    {
        return malist;
    }
    
    protected void trimBy(double trim, MAparam p)
    {
        malist.trimBy(trim, p);
    }
    
    public void trimByM(double trim)
    {
        trimBy(trim, MAparam.M);
    }
    
    public void trimByA(double trim) {
        trimBy(trim, MAparam.A);
    }
    
    public void sortByA() {
        malist.sortByM();
    }

    public void sortByM() {
        malist.sortByA();
    }
    
}
