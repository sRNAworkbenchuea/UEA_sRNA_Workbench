package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

/**
 *
 * @author Matthew
 */
public enum FoldChangeDirection {
    UP, DOWN, STRAIGHT;
    
    @Override
    public String toString()
    {
        switch(this){
            case UP:
                return "U";
            case DOWN:
                return "D";
            default:
                return "S";
        }
    }
}
