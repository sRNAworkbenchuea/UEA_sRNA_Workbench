/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils.degradomestatistics;

/**
 * The extra math library.
 * Provides extra functions not in java.lang.Math class.
 * This class cannot be subclassed or instantiated because all methods are static.
 * @version 1.2
 * @author Mark Hale
 */
public final class ExtraMath extends AbstractMath {
    private ExtraMath() {}
    
    /**
     * Rounds a number to so many significant figures.
     * @param x a number to be rounded.
     * @param significant number of significant figures to round to.
     */
    public static double round(final double x, final int significant) {
        if(x == 0.0)
            return x;
        else if(significant == 0)
            return 0.0;
        final double signedExp = log10(Math.abs(x)) - significant;
        if(signedExp < 0.0) {
            // keep the exponent positive so factor is representable
            final double factor = Math.pow(10.0, Math.floor(-signedExp));
            return Math.round(x*factor)/factor;
        } else {
            final double factor = Math.pow(10.0, Math.ceil(signedExp));
            return Math.round(x/factor)*factor;
        }
    }
    /**
     * Returns a random number within a specified range.
     */
    public static double random(double min, double max) {
        return (max-min)*Math.random()+min;
    }
    /**
     * Returns the sign of a number.
     * @return 1 if x>0.0, -1 if x<0.0, else 0.
     */
    public static int sign(double x) {
        if(x > 0.0)
            return 1;
        else if(x < 0.0)
            return -1;
        else
            return 0;
    }
    /**
     * Returns sqrt(x<sup>2</sup>+y<sup>2</sup>).
     */
    public static double hypot(final double x,final double y) {
        final double xAbs=Math.abs(x);
        final double yAbs=Math.abs(y);
        if(xAbs==0.0 && yAbs==0.0)
            return 0.0;
        else if(xAbs<yAbs)
            return yAbs*Math.sqrt(1.0+(x/y)*(x/y));
        else
            return xAbs*Math.sqrt(1.0+(y/x)*(y/x));
    }
    /**
     * Returns a<sup>b</sup>.
     * @param a an integer.
     * @param b a positive integer.
     */
    public static int pow(int a, int b) {
        if(b < 0) {
            throw new IllegalArgumentException(b+" must be a positive integer.");
        } else if(b == 0) {
            return 1;
        } else {
            if(a == 0) {
                return 0;
            } else if(a == 1) {
                return 1;
            } else if(a == 2) {
                return 1<<b;
            } else {
                for(int i=1; i<b; i++)
                    a *= a;
                return a;
            }
        }
    }
    /**
     * Returns 2<sup>a</sup>.
     * @param a a positive integer.
     */
    public static int pow2(int a) {
        return 1<<a;
    }
    /**
     * Returns the factorial.
     * (Wrapper for the gamma function).
     * @see SpecialMath#gamma
     * @param x a double.
     */
    public static double factorial(double x) {
        return SpecialMath.gamma(x+1.0);
    }
    /**
     * Returns the natural logarithm of the factorial.
     * (Wrapper for the log gamma function).
     * @see SpecialMath#logGamma
     * @param x a double.
     */
    public static double logFactorial(double x) {
        return SpecialMath.logGamma(x+1.0);
    }
    /**
     * Returns the binomial coefficient (n k).
     * Uses Pascal's recursion formula.
     * @jsci.planetmath PascalsRule
     * @param n an integer.
     * @param k an integer.
     */
    public static int binomial(int n,int k) {
        if(k == n || k ==0)
            return 1;
        else if(n == 0)
            return 1;
        else
            return binomial(n-1, k-1)+binomial(n-1, k);
    }
    /**
     * Returns the binomial coefficient (n k).
     * Uses gamma functions.
     * @jsci.planetmath BinomialCoefficient
     * @param n a double.
     * @param k a double.
     */
    public static double binomial(double n,double k) {
        return Math.exp(SpecialMath.logGamma(n+1.0)-SpecialMath.logGamma(k+1.0)-SpecialMath.logGamma(n-k+1.0));
    }
    /**
     * Returns the base 10 logarithm of a double.
     * @param x a double.
     */
    public static double log10(double x) {
        return Math.log(x)/NumericalConstants.LOG10;
    }
    /**
     * Returns the hyperbolic sine of a double.
     * @param x a double.
     */
    public static double sinh(double x) {
        return (Math.exp(x)-Math.exp(-x))/2.0;
    }
    /**
     * Returns the hyperbolic cosine of a double.
     * @param x a double.
     */
    public static double cosh(double x) {
        return (Math.exp(x)+Math.exp(-x))/2.0;
    }
    /**
     * Returns the hyperbolic tangent of a double.
     * @param x a double.
     */
    public static double tanh(double x) {
        return sinh(x)/cosh(x);
    }
    
    /**
     * Returns the hyperbolic cotangent of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>coth(x)&nbsp;=&nbsp;(e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup>)/(e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup>)</i>,
    
     * in other words, {@linkplain Math#cosh cosh(<i>x</i>)}/{@linkplain Math#sinh sinh(<i>x</i>)}.
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is an infinity with the same sign as the argument.
     * <li>If the argument is positive infinity, then the result is <code>+1.0</code>.
     * <li>If the argument is negative infinity, then the result is <code>-1.0</code>.
     * </ul>
     * @param x The number whose hyperbolic cotangent is sought
     * @return The hyperbolic cotangent of <code>x</code>
     */
    public static double
            coth(double x) {
        return 1.0D/tanh(x);
    } //coth
    
    /**
     * Returns the hyperbolic cosecant of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>csch(x)&nbsp;=&nbsp;(2/(e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup>)</i>,
     * in other words, 1/{@linkplain Math#sinh sinh(<i>x</i>)}.
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is an infinity with the same sign as the argument.
     * <li>If the argument is positive infinity, then the result is <code>+0.0</code>.
     * <li>If the argument is negative infinity, then the result is <code>-0.0</code>.
     * </ul>
     * @param x The number whose hyperbolic cosecant is sought
     * @return The hyperbolic cosecant of <code>x</code>
     */
    public static double
            csch(double x) {
        return 1.0D/sinh(x);
    } //csch
    
    /**
     * Returns the hyperbolic secant of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>sech(x)&nbsp;=&nbsp;(2/(e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup>)</i>,
     * in other words, 1/{@linkplain Math#cosh cosh(<i>x</i>)}.
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is an infinity (positive or negative), then the result is <code>+0.0</code>.
     * </ul>
     * @param x The number whose hyperbolic secant is sought
     * @return The hyperbolic secant of <code>x</code>
     */
    public static double
            sech(double x) {
        return 1.0D/cosh(x);
    } //sech
    
    /**
     * Returns the inverse hyperbolic sine of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>asinh(x)&nbsp;=&nbsp;ln(x&nbsp;+&nbsp;sqrt(x<sup>2</sup>&nbsp;+&nbsp;1))</i>
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is infinite, then the result is an infinity with the same sign as the argument.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param x The number whose inverse hyperbolic sine is sought
     * @return The inverse hyperbolic sine of <code>x</code>
     */
    public static double
            asinh(double x) {
        //Math.hypot(Double.NEGATIVE_INFINITY, 1.0D) is Double.POSITIVE_INFINITY
        //return Double.isInfinite(x) ? x : (x == 0.0) ? x : Math.log(x + Math.hypot(x, 1.0D));
        return Double.isInfinite(x) ? x : (x == 0.0) ? x : Math.log(x+Math.sqrt(x*x+1.0));
    } //asinh
    
    /**
     * Returns the inverse hyperbolic cosine of a <code>double</code> value.
     * Note that <i>cosh(±acosh(x))&nbsp;=&nbsp;x</i>; this function arbitrarily returns the positive branch.
     * <p>The identity is:
     * <p><i>acosh(x)&nbsp;=&nbsp;ln(x&nbsp;±&nbsp;sqrt(x<sup>2</sup>&nbsp;-&nbsp;1))</i>
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN or less than one, then the result is NaN.
     * <li>If the argument is a positive infinity, then the result is (positive) infinity.
     * <li>If the argument is one, then the result is (positive) zero.
     * </ul>
     * @param x The number whose inverse hyperbolic cosine is sought
     * @return The inverse hyperbolic cosine of <code>x</code>
     */
    public static double
            acosh(double x) {
        return Math.log(x + Math.sqrt(x*x - 1.0D));
    } //acosh
    
    /**
     * Returns the inverse hyperbolic tangent of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>atanh(x)&nbsp;=&nbsp;(1/2)*ln((1&nbsp;+&nbsp;x)/(1&nbsp;-&nbsp;x))</i>
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, an infinity, or has a modulus of greater than one, then the result is NaN.
     * <li>If the argument is plus or minus one, then the result is infinity with the same sign as the argument.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param x A double specifying the value whose inverse hyperbolic tangent is sought
     * @return A double specifying the inverse hyperbolic tangent of x
     */
    public static double
            atanh(double x) {
        //return (Math.log1p(x) - Math.log1p(-x))/2.0D;
        return (x != 0.0) ? (Math.log(1.0D + x)-Math.log(1.0D - x))/2.0D : x;
    } //atanh
    
    /**
     * Returns the inverse hyperbolic cotangent of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>acoth(x)&nbsp;=&nbsp;(1/2)*ln((x&nbsp;+&nbsp;1)/(x&nbsp;-&nbsp;1))</i>
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN or a modulus of less than one, then the result is NaN.
     * <li>If the argument is an infinity, then the result is zero with the same sign as the argument.
     * <li>If the argument is plus or minus one, then the result is infinity with the same sign as the argument.
     * </ul>
     * @param x The number whose inverse hyperbolic cotangent is sought
     * @return  The inverse hyperbolic cotangent of <code>x</code>
     */
    public static double
            acoth(double x) {
//    return (Math.log1p(x) - Math.log(x - 1.0D))/2.0D; // Difference of two same-sign infinities is NaN
        if (Double.isInfinite(x)) return (x < 0.0) ? -0.0D : +0.0D;
        //return (x == -1.0D) ? Double.NEGATIVE_INFINITY : (Math.log1p(x) - Math.log(x - 1.0D))/2.0D;
        return (x == -1.0D) ? Double.NEGATIVE_INFINITY : (Math.log(x+1.0) - Math.log(x - 1.0D))/2.0D;
    } //acoth
    
    /**
     * Returns the inverse hyperbolic cosecant of a <code>double</code> value.
     * <p>The identity is:
     * <p><i>acsch(x)&nbsp;=&nbsp;ln((1&nbsp;-&nbsp;sqrt(1&nbsp;+&nbsp;x<sup>2</sup>))/x)</i> for x &lt; 0;
     * <p><i>acsch(x)&nbsp;=&nbsp;ln((1&nbsp;+&nbsp;sqrt(1&nbsp;+&nbsp;x<sup>2</sup>))/x)</i> for x &gt; 0.
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is an infinity, then the result is zero with the same sign as the argument.
     * <li>If the argument is zero, then the result is infinity with the same sign as the argument.
     * </ul>
     * @param x The number whose inverse hyperbolic cosecant is sought
     * @return The inverse hyperbolic cosecant of <code>x</code>
     */
    public static double
            acsch(double x) {
//    return (x < 0) ? Math.log((1.0D - Math.sqrt(Math.hypot(1.0, x)))/x) : Math.log((1.0D + Math.sqrt(1.0, x))/x);
        
        if (Double.isInfinite(x)) return (x < 0.0) ? -0.0D : +0.0D;
        //log(+infinity) is +infinity, but log(-infinity) is NaN
        return (x == 0.0D) ? 1.0/x : Math.log((1.0D + sign(x)*Math.sqrt(x*x+1.0))/x);
    } //acsch
    
    /**
     * Returns the inverse hyperbolic secant of a <code>double</code> value.
     * Note that <i>sech(±asech(x))&nbsp;=&nbsp;x</i>; this function arbitrarily returns the positive branch.
     * <p>The identity is:
     * <p><i>asech(x)&nbsp;=&nbsp;ln((1&nbsp;+&nbsp;sqrt(1&nbsp;-&nbsp;x<sup>2</sup>))/x)</i>.
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, less than zero, or greater than one, then the result is NaN.
     * <li>If the argument is zero, then the result is infinity with the same sign as the argument.
     * </ul>
     * @param x The number whose hyperbolic secant is sought
     * @return The hyperbolic secant of <code>x</code>
     */
    public static double
            asech(double x) {
        //log(+infinity) is +infinity, but log(-infinity) is NaN
        return (x == 0.0D) ? 1.0/x : Math.log((1.0D + Math.sqrt(1.0D - x*x))/x);
    } //asech
}
