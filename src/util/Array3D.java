package util;

/**
 * A collection of static methods making it easier to work
 * with three-dimensional arrays.
 * Note that this class assumes that every dimension has a
 * fixed size, e.g. <code>300x200x100</code>.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class Array3D {
	
	/**
	 * Finds the maximum value in the 3D integer array.
	 *
	 * @param array  the 3D integer array
	 * @return the maximum value
	 */
	public static int max(int[][][] array) {
		int result = Integer.MIN_VALUE;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result < array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the maximum value in the 3D short array.
	 *
	 * @param array  the 3D short array
	 * @return the maximum value
	 */
	public static short max(short[][][] array) {
		short result = Short.MIN_VALUE;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result < array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the maximum value in the 3D double array.
	 *
	 * @param array  the 3D double array
	 * @return the maximum value
	 */
	public static double max(double[][][] array) {
		double result = Double.NEGATIVE_INFINITY;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result < array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the maximum value in the 3D float array.
	 *
	 * @param array  the 3D float array
	 * @return the maximum value
	 */
	public static float max(float[][][] array) {
		float result = Float.NEGATIVE_INFINITY;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result < array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the minimum value in the 3D integer array.
	 *
	 * @param array  the 3D integer array
	 * @return the minimum value
	 */
	public static int min(int[][][] array) {
		int result = Integer.MAX_VALUE;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result > array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the minimum value in the 3D short array.
	 *
	 * @param array  the 3D short array
	 * @return the minimum value
	 */
	public static short min(short[][][] array) {
		short result = Short.MAX_VALUE;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result > array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the minimum value in the 3D double array.
	 *
	 * @param array  the 3D double array
	 * @return the minimum value
	 */
	public static double min(double[][][] array) {
		double result = Double.POSITIVE_INFINITY;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result > array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Finds the minimum value in the 3D float array.
	 *
	 * @param array  the 3D float array
	 * @return the minimum value
	 */
	public static float min(float[][][] array) {
		float result = Float.POSITIVE_INFINITY;
		
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				for (int z = 0; z < array[0][0].length; z++) {
					if (result > array[x][y][z]) {
						result = array[x][y][z];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if the given indices are valid for the given integer 3D array.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param array  the integer 3D array
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, int[][][] array) {
		return valid(x, y, z, array.length, array[0].length, array[0][0].length);
	}
	
	/**
	 * Checks if the given indices are valid for the given short 3D array.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param array  the short 3D array
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, short[][][] array) {
		return valid(x, y, z, array.length, array[0].length, array[0][0].length);
	}
	
	/**
	 * Checks if the given indices are valid for the given double 3D array.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param array  the double 3D array
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, double[][][] array) {
		return valid(x, y, z, array.length, array[0].length, array[0][0].length);
	}
	
	/**
	 * Checks if the given indices are valid for the given float 3D array.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param array  the float 3D array
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, float[][][] array) {
		return valid(x, y, z, array.length, array[0].length, array[0][0].length);
	}
	
	/**
	 * Checks if the given indices are valid for the given boolean 3D array.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param array  the boolean 3D array
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, boolean[][][] array) {
		return valid(x, y, z, array.length, array[0].length, array[0][0].length);
	}
	
	/**
	 * Checks if the given indices are valid for an array of the given size.
	 *
	 * @param x      the index for the first dimension, usually the x-coordinate
	 * @param y      the index for the second dimension, usually the y-coordinate
	 * @param z      the index for the third dimension, usually the z-coordinate
	 * @param sizeX  the size of the first dimension
	 * @param sizeY  the size of the second dimension
	 * @param sizeZ  the size of the third dimension
	 * @return true if the indices are valid
	 */
	public static boolean valid(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return ((x >= 0) && (y >= 0) && (z >= 0) && (x < sizeX) && (y < sizeY) && (z < sizeZ));
	}
	
}
