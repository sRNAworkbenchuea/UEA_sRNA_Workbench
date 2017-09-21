package uk.ac.uea.cmp.srnaworkbench.utils.math;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows safe specification of a log parameter
 * and static methods that calculate logs based on this parameter.
 * 
 * Calculation of logs using specific bases is also made more convenient.
 * Using Math.Log() for specific bases required dividing by Math.log(base). This
 * is implemented in the calculate() method here.
 */
public enum Logarithm {
    // Other bases may be added here at one's convenience. Otherwise use the
    // custom base and then setBase if neeeded.
    NONE(null), BASE_2(2), BASE_10(10), BASE_CUSTOM(null);
    private static final String NONE_STRING = "NO_LOG";
    Integer base;

    Logarithm(Integer base) {
        this.base = base;
    }

    /**
     * Sets the base of this log but only if the current Logarithm is
     * BASE_CUSTOM. Otherwise, throws an IllegalArgumentException.
     * @param base
     * @return 
     */
    public Logarithm setBase(int base) {
        if(this.equals(Logarithm.BASE_CUSTOM))
        {
            this.base = base;
            return this;
        }
        else
        {
            throw new IllegalArgumentException("Attempted to set a different base for Logarithm using setBase when the Logarithm enum was not BASE_CUSTOM.");
        }
    }

    public Integer getBase() {
        if (this.equals(NONE)) {
            return null;
        }
        if (this.base == null) {
            throw new UnsetCustomLogBaseException();
        }
        return this.base;
    }

    /**
     * Calculates the result of taking a logarithm of the specified value
     * using this base.
     * @param value
     * @return the value's logarithm
     */
    public double calculate(double value) {
        return calculate(value, 0.0);
    }
    
    /**
     * Calculates the result of taking this log of the specified value after adding
     * the specified pseudoCount.
     * The advantage of using this is in the case of NONE, no pseudoCount is even applied
     * @param value the value to log
     * @param pseudoCount the pseudo count to add to the value 
     * @return 
     */
    public double calculate(double value, double pseudoCount) {
        if (this.equals(NONE)) {
            return value;
        }
        return Math.log(value + pseudoCount) / Math.log(this.getBase());
    }
    
    /**
     * Reverse the effect of calculating using this Logarithm
     * @param logarithm calculated using this object's calculate() function
     * @return The initial value that was put through the calculate() function
     */
    public double exp(double logarithm) {
        if (this.equals(NONE)) {
            return logarithm;
        }
        return Math.pow(this.getBase(), logarithm);
    }

    /**
     * Prints a string indicating the type of logarithm this enum
     * represents.
     * @return
     */
    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "NO_LOG";
            default:
                return "Log" + this.getBase();
        }
    }

    /**
     * Retrieve a logarithm enum from a String that
     * would have been returned by toString()
     * @param logString
     * @return
     */
    public static Logarithm fromString(String logString) {
        if (logString.equals(Logarithm.NONE_STRING)) {
            return Logarithm.NONE;
        }
        Pattern p = Pattern.compile("Log(\\d+)");
        Matcher match = p.matcher(logString);
        if (match.matches()) {
            int thisBase;
            try {
                thisBase = Integer.valueOf(match.group(1));
            } catch (NumberFormatException e) {
                throw new IllegalLogarithmStringRepresentationException(logString);
            }
            switch (thisBase) {
                case 2:
                    return Logarithm.BASE_2;
                case 10:
                    return Logarithm.BASE_10;
                default:
                    Logarithm custom = Logarithm.BASE_CUSTOM;
                    custom.setBase(thisBase);
                    return custom;
            }
        } else {
            throw new IllegalLogarithmStringRepresentationException(logString);
        }
    }

    private static class IllegalLogarithmStringRepresentationException extends IllegalArgumentException {

        public IllegalLogarithmStringRepresentationException(String badLogString) {
            super("The string \"" + badLogString + "\" is not representative of a correct logarithm");
        }
    }

    private static class UnsetCustomLogBaseException extends IllegalArgumentException {

        public UnsetCustomLogBaseException() {
            super("The BASE_CUSTOM enum was used without setting a base");
        }
    }
    
    //TODO: Port to a Unit Test
    public static void main(String[] args) {
        Logarithm nolog = Logarithm.NONE;
        String nolog2 = "NO_LOG";
        String good = "Log2";
        String good2 = "Log10";
        String good3 = "Log12";
        String bad = "Logo";
        System.out.println(nolog);
        System.out.println(Logarithm.fromString(nolog2));
        System.out.println(Logarithm.fromString(good));
        System.out.println(Logarithm.fromString(good2));
        System.out.println(Logarithm.fromString(good3));
        System.out.println(Logarithm.BASE_10);
        Logarithm log100 = Logarithm.BASE_CUSTOM.setBase(100);
        System.out.println("Log100 ( 20 ) = " + log100.calculate(20));
        System.out.println("20^Log100 ( 20 ) = " + log100.exp(log100.calculate(20)));
        System.out.println(Logarithm.fromString(log100.toString()));
        //System.out.println(Logarithm.fromString(bad));

    }
    
}
