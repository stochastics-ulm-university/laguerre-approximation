package util;

/**
 * A collection of static methods making it easier to work
 * with two-dimensional arrays.
 * Note that this class assumes that every dimension has a
 * fixed size, e.g. <code>300x200</code>.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class Array2D {
	
	/**
	 * Finds the maximum value in the 2D integer array.
	 *
	 * @param array  the 2D integer array
	 * @return the maximum value
	 */
	public static int max(int[][] array) {
		int result = Integer.MIN_VALUE;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result < array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the maximum value in the 2D short array.
	 *
	 * @param array  the 2D short array
	 * @return the maximum value
	 */
	public static short max(short[][] array) {
		short result = Short.MIN_VALUE;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result < array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the maximum value in the 2D double array.
	 *
	 * @param array  the 2D double array
	 * @return the maximum value
	 */
	public static double max(double[][] array) {
		double result = Double.NEGATIVE_INFINITY;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result < array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the maximum value in the 2D float array.
	 *
	 * @param array  the 2D float array
	 * @return the maximum value
	 */
	public static float max(float[][] array) {
		float result = Float.NEGATIVE_INFINITY;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result < array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the minimum value in the 2D integer array.
	 *
	 * @param array  the 2D integer array
	 * @return the minimum value
	 */
	public static int min(int[][] array) {
		int result = Integer.MAX_VALUE;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result > array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the minimum value in the 2D short array.
	 *
	 * @param array  the 2D short array
	 * @return the minimum value
	 */
	public static short min(short[][] array) {
		short result = Short.MAX_VALUE;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result > array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the minimum value in the 2D double array.
	 *
	 * @param array  the 2D double array
	 * @return the minimum value
	 */
	public static double min(double[][] array) {
		double result = Double.POSITIVE_INFINITY;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result > array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
	/**
	 * Finds the minimum value in the 2D float array.
	 *
	 * @param array  the 2D float array
	 * @return the minimum value
	 */
	public static float min(float[][] array) {
		float result = Float.POSITIVE_INFINITY;

		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				if (result > array[x][y]) {
					result = array[x][y];
				}
			}
		}

		return result;
	}
	
}
