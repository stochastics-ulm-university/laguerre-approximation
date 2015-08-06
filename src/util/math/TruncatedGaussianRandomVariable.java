package util.math;

/**
 * This class implements a truncated Gaussian random variable. For the
 * simulation of this random variable, a Gaussian random variable is
 * simulated until its result is within the given range.
 * 
 * @author Institute of Stochastics, Ulm University
 */
public class TruncatedGaussianRandomVariable {
	
	/** The underlying Gaussian random variable used for the simulation. */
	private GaussianRandomVariable g;
	
	/** The range with which the realisation must be contained. */
	private double min, max;
	
	/**
	 * Constructs a new truncated Gaussian random variable, i.e. a Gaussian
	 * random variable whose realisations must be contained in an interval
	 * <code>[min,max]</code>. The mean value and variance of the underlying
	 * Gaussian random variable are given.
	 * 
	 * @param mean      the mean value of the underlying Gaussian random variable
	 * @param variance  the variance of the underlying Gaussian random variable
	 * @param min       the lower bound of the interval
	 * @param max       the upper bound of the interval
	 */
	public TruncatedGaussianRandomVariable(double mean, double variance, double min, double max) {
		this(new GaussianRandomVariable(mean, variance), min, max);
	}
	
	/**
	 * Constructs a new truncated version of a Gaussian random variable, i.e.
	 * a Gaussian random variable whose realisations must be contained in an
	 * interval <code>[min,max]</code>.
	 * 
	 * @param gaussian  the underlying Gaussian random variable
	 * @param min       the lower bound of the interval
	 * @param max       the upper bound of the interval
	 */
	public TruncatedGaussianRandomVariable(GaussianRandomVariable gaussian, double min, double max) {
		if (min >= max)
			throw new IllegalArgumentException("TruncatedGaussian: min must be smaller than max!");
		this.g = gaussian;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Returns the next realisation of this random variable.
	 * 
	 * @return the next random value according to the truncated Gaussian
	 *         distribution
	 */
	public double realise() {
		double value;
		do {
			value = g.realise();
		} while (value < min || value > max);
		return value;
	}
	
	@Override
	public String toString() {
		return "Truncated Gaussian random variable with min="+min+", max="+max+", underlying: "+g;
	}
	
}
