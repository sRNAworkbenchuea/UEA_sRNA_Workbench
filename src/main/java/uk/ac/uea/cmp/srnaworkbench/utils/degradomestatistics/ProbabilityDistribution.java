/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils.degradomestatistics;

/**
     * The ProbabilityDistribution superclass provides an object for
     * encapsulating probability distributions.
     *
     * @version 1.0
     * @author Jaco van Kooten
     */
    public abstract class ProbabilityDistribution extends Object {

        /**
         * Constructs a probability distribution.
         */
        public ProbabilityDistribution() {
        }

        /**
         * Probability density function.
         *
         * @return the probability that a stochastic variable x has the value X,
         * i.e. P(x=X).
         */
        public abstract double probability(double X);

        /**
         * Cumulative distribution function.
         *
         * @return the probability that a stochastic variable x is less than or
         * equal to X, i.e. P(x&lt;=X).
         */
        public abstract double cumulative(double X);

        /**
         * Inverse of the cumulative distribution function.
         *
         * @return the value X for which P(x&lt;=X).
         */
        public abstract double inverse(double probability);

        /**
         * Check if the range of the argument of the distribution method is
         * between <code>lo</code> and <code>hi</code>.
         *
         * @exception OutOfRangeException If the argument is out of range.
         */
        protected final void checkRange(double x, double lo, double hi) {
            if (x < lo || x > hi) {
                System.exit(0);
            }
        }

        /**
         * Check if the range of the argument of the distribution method is
         * between 0.0 and 1.0.
         *
         * @exception OutOfRangeException If the argument is out of range.
         */
        protected final void checkRange(double x) {
            if (x < 0.0 || x > 1.0) {
               System.exit(0);
            }
        }
        
        private static final double FINDROOT_ACCURACY = 1.0e-15;
        private static final int FINDROOT_MAX_ITERATIONS = 150;

        /**
         * This method approximates the value of X for which
         * P(x&lt;X)=<I>prob</I>. It applies a combination of a Newton-Raphson
         * procedure and bisection method with the value <I>guess</I> as a
         * starting point. Furthermore, to ensure convergency and stability, one
         * should supply an inverval [<I>xLo</I>,<I>xHi</I>] in which the
         * probalility distribution reaches the value <I>prob</I>. The method
         * does no checking, it will produce bad results if wrong values for the
         * parameters are supplied - use it with care.
         */
        protected final double findRoot(double prob, double guess, double xLo, double xHi) {
            double x = guess, xNew = guess;
            double error, pdf, dx = 1.0;
            int i = 0;
            while (Math.abs(dx) > FINDROOT_ACCURACY && i++ < FINDROOT_MAX_ITERATIONS) {
// Apply Newton-Raphson step
                error = cumulative(x) - prob;
                if (error < 0.0) {
                    xLo = x;
                } else {
                    xHi = x;
                }
                pdf = probability(x);
                if (pdf != 0.0) {          // Avoid division by zero
                    dx = error / pdf;
                    xNew = x - dx;
                }
// If the Newton-Raphson fails to converge (which for example may be the
// case if the initial guess is to rough) we apply a bisection
// step to determine a more narrow interval around the root.
                if (xNew < xLo || xNew > xHi || pdf == 0.0) {
                    xNew = (xLo + xHi) / 2.0;
                    dx = xNew - x;
                }
                x = xNew;
            }
            return x;
        }
    }
    
