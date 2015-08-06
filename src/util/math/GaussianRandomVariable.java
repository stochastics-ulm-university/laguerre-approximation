package util.math;

import java.util.Random;

/**
 * This class implements a Gaussian random variable.
 * 
 * @author Institute of Stochastics, Ulm University
 */
public class GaussianRandomVariable {
	
	/** The random number generator that is used. */
	private static Random g = new Random();
	
	/** The mean value and standard deviation of the random variable. */
	private double mean, sd;
	
	/**
	 * Constructs a standard Gaussian random variable, i.e., with mean value 0.0
	 * and standard deviatation 1.0.
	 */
	public GaussianRandomVariable() {
		mean = 0.0;
		sd = 1.0;
	}
	
	/**
	 * Constructs a Gaussian random variable with the given mean value and
	 * variance.
	 *
	 * @param mean      the mean value of the random variable
	 * @param variance  the variance of the random variable
	 * @throws IllegalArgumentException if the <code>variance</code> is less than zero
	 */
	public GaussianRandomVariable(double mean, double variance) {
		if (!(variance >= 0.0))
			throw new IllegalArgumentException("Normal: variance" + " must be at least zero");
		this.mean = mean;
		this.sd = Math.sqrt(variance);
	}
	
	/**
	 * Returns a new realisation of this random variable.
	 *
	 * @return a new realisation of this random variable
	 */
	public double realise() {
		return g.nextGaussian() * sd + mean;
	}
	
	@Override
	public String toString() {
		return "Gaussian random variable with mean=" + mean + ", stddev=" + sd;
	}
	
}
