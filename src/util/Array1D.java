package util;

/**
 * A collection of static methods making it easier to work
 * with one-dimensional arrays.
 *
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class Array1D {
	
	/**
	 * Finds the maximum value in the 1D integer array.
	 *
	 * @param array  the 1D integer array
	 * @return the maximum value
	 */
	public static int max(int[] array) {
		int result = Integer.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (result < array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the maximum value in the 1D short array.
	 *
	 * @param array  the 1D short array
	 * @return the maximum value
	 */
	public static short max(short[] array) {
		short result = Short.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (result < array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the maximum value in the 1D double array.
	 *
	 * @param array  the 1D double array
	 * @return the maximum value
	 */
	public static double max(double[] array) {
		double result = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < array.length; i++) {
			if (result < array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the maximum value in the 1D float array.
	 *
	 * @param array  the 1D float array
	 * @return the maximum value
	 */
	public static float max(float[] array) {
		float result = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < array.length; i++) {
			if (result < array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the minimum value in the 1D integer array.
	 *
	 * @param array  the 1D integer array
	 * @return the minimum value
	 */
	public static int min(int[] array) {
		int result = Integer.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (result > array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the minimum value in the 1D short array.
	 *
	 * @param array  the 1D short array
	 * @return the minimum value
	 */
	public static short min(short[] array) {
		short result = Short.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (result > array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the minimum value in the 1D double array.
	 *
	 * @param array  the 1D double array
	 * @return the minimum value
	 */
	public static double min(double[] array) {
		double result = Double.POSITIVE_INFINITY;
		for (int i = 0; i < array.length; i++) {
			if (result > array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
	/**
	 * Finds the minimum value in the 1D float array.
	 *
	 * @param array  the 1D float array
	 * @return the minimum value
	 */
	public static float min(float[] array) {
		float result = Float.POSITIVE_INFINITY;
		for (int i = 0; i < array.length; i++) {
			if (result > array[i]) {
				result = array[i];
			}
		}
		return result;
	}
	
}
