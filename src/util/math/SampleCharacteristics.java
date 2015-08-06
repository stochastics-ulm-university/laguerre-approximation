package util.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Sample mean and sample variance for 1D data sets given as a set of
 * double values.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class SampleCharacteristics {
	
	/** The samples. */
	protected final ArrayList<Double> samples;
	
	/**
	 * Constructs a new characteristics object for the given samples.
	 * 
	 * @param samples  the samples
	 */
	public SampleCharacteristics(Collection<Double> samples) {
		this.samples = new ArrayList<Double>(samples);
	}
	
	/**
	 * Returns the samples.
	 * 
	 * @return the samples
	 */
	public List<Double> getSamples() {
		return Collections.unmodifiableList(this.samples);
	}
	
	/**
	 * Returns the sample mean.
	 * 
	 * @return the sample mean
	 */
	public double getMean() {
		double sum = 0.0;
		int counter = 0;
		for (Double sample : this.samples) {
			sum += sample;
			counter++;
		}
		return sum / counter;
	}
	
	/**
	 * Returns the (corrected) sample variance.
	 * 
	 * @return the sample variance
	 */
	public double getVariance() {
		double mean = this.getMean();
		double sumSquaredDiff = 0.0;
		int counter = 0;
		for (Double sample : this.samples) {
			sumSquaredDiff += (sample - mean) * (sample - mean);
			counter++;
		}
		return sumSquaredDiff / (counter-1);
	}
	
	/**
	 * Returns the sample standard deviation, i.e., the root of the
	 * sample variance.
	 * 
	 * @return the sample standard deviation
	 */
	public double getStddev() {
		return Math.sqrt(this.getVariance());
	}
	
}