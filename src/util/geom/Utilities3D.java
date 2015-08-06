package util.geom;

/**
 * In this class some utilities for the use with the other 3D classes are
 * implemented.
 * 
 * @author Institute of Stochastics, Ulm University
 */
public final class Utilities3D {
	
	/** The precision for the numerical operations. Can be changed for practical reasons. */
	public static double eps = 1e-10;
	
	/** The precision for determining whether the two vectors are parallel in
	 * <code>fromToRotation</code>. Can be changed for practical reasons. */
	public static double epsRot = 1e-5;
	
	/**
	 * Returns the maximum absolute value of three real numbers.
	 *
	 * @param	x	the first number.
	 * @param	y	the second number.
	 * @param	z	the third number.
	 * @return	the maximum of the absolute values.
	 */
	private static double maxabs(double x, double y, double z) {
		x = (x < 0) ? -x : x;
		y = (y < 0) ? -y : y;
		z = (z < 0) ? -z : z;
		return (x > y) ? (x > z) ? x : z : (y > z) ? y : z;
	}
	
	/**
	 * Tests two double values for equality and deals with numerical errors
	 * using a specified epsilon value.
	 *
	 * @param x         one double value to be compared.
	 * @param y         the other double value to be compared.
	 * @param eps       the epsilon value to use for the comparison
	 * @param adjustEps determines whether the epsilon value should be
	 *                  automatically adjusted for values greater one
	 * @return <code>true</code> if and only if <code>x</code> and
	 *         <code>y</code> are equal.
	 */
	public static boolean isEqual(double x, double y, double eps, boolean adjustEps) {
		if (adjustEps) {
			// size of eps is adapted for values greater than 1
			eps = eps * maxabs(1, x, y);
		}
		return (x == y) || (Math.abs(x - y) < eps);
	}
	
	/**
	 * Tests two double values for equality and deals with numerical errors
	 * using a specified epsilon value.
	 *
	 * @param x   one double value to be compared.
	 * @param y   the other double value to be compared.
	 * @param eps the epsilon value to use for the comparison
	 *            (automatically adjusted for values greater one)
	 * @return <code>true</code> if and only if <code>x</code> and
	 *         <code>y</code> are equal.
	 */
	public static boolean isEqual(double x, double y, double eps) {
		return (x == y)
				// size of eps is adapted for values greater than 1
				|| (Math.abs(x - y) < eps * maxabs(1, x, y));
	}
	
	/**
	 * Tests two double values for equality and deals with numerical errors.
	 *
	 * @param x one double value to be compared.
	 * @param y the other double value to be compared.
	 * @return <code>true</code> if and only if <code>x</code> and
	 *         <code>y</code> are equal.
	 */
	public static boolean isEqual(double x, double y) {
		return isEqual(x, y, eps);
	}
	
	/**
	 * Computes a rotation matrix that rotates a vector called
	 * <code>from</code> into another vector called <code>to</code>.
	 * 
	 * @param from  normalized non-zero vector
	 * @param to    normalized non-zero vector
	 * @return the rotation object
	 * @see "Tomas Moeller and John Hughes, Efficiently Building a Matrix to Rotate One Vector to Another,
	 *      Journal of Graphics Tools, 4(4):1-4, 1999"
	 * @see <a href="http://jgt.akpeters.com/papers/MollerHughes99/">Abstract and source code</a>
	 */
	public static Rotation3D fromToRotation(Point3D from, Point3D to) {
		double[][] rotMatrix = new double[3][3];
		
		double e = from.getScalarProduct(to);
		double f = Math.abs(e);
		
		if (Utilities3D.isEqual(1.0, f, epsRot)) {
			// "from" and "to"-vector almost parallel
			
			// find the vector most nearly orthogonal to "from"
			Point3D x;
			if (Math.abs(from.getCoordinates()[0]) < Math.abs(from.getCoordinates()[1])) {
				if (Math.abs(from.getCoordinates()[0]) < Math.abs(from.getCoordinates()[2])) {
					x = new Point3D(new double[] { 1.0, 0.0, 0.0 });
				} else {
					x = new Point3D(new double[] { 0.0, 0.0, 1.0 });
				}
			} else {
				if (Math.abs(from.getCoordinates()[1]) < Math.abs(from.getCoordinates()[2])) {
					x = new Point3D(new double[] { 0.0, 1.0, 0.0 });
				} else {
					x = new Point3D(new double[] { 0.0, 0.0, 1.0 });
				}
			}
			
			Point3D u = x.getVectorTo(from);
			Point3D v = x.getVectorTo(to);
			
			double c1 = 2.0 / u.getScalarProduct(u);
			double c2 = 2.0 / v.getScalarProduct(v);
			double c3 = c1 * c2 * u.getScalarProduct(v);
			
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					rotMatrix[i][j] = - c1 * u.getCoordinates()[i] * u.getCoordinates()[j]
							- c2 * v.getCoordinates()[i] * v.getCoordinates()[j]
									+ c3 * v.getCoordinates()[i] * u.getCoordinates()[j];
				}
				rotMatrix[i][i] += 1.0;
			}
		} else {
			// the most common case, unless "from"="to", or "from"=-"to"
			// (hand optimized version, see http://jgt.akpeters.com/papers/MollerHughes99/code.html)
			
			Point3D v   = from.getVectorProduct(to);
			double[] vc = v.getCoordinates();
			double h    = 1.0/(1.0 + e);
			
			double hvx = h * vc[0];
			double hvz = h * vc[2];
			double hvxy = hvx * vc[1];
			double hvxz = hvx * vc[2];
			double hvyz = hvz * vc[1];
			
			rotMatrix[0][0] = e + hvx * vc[0];
			rotMatrix[0][1] = hvxy - vc[2];
			rotMatrix[0][2] = hvxz + vc[1];
			
			rotMatrix[1][0] = hvxy + vc[2];
			rotMatrix[1][1] = e + h * vc[1] * vc[1];
			rotMatrix[1][2] = hvyz - vc[0];
			
			rotMatrix[2][0] = hvxz - vc[1];
			rotMatrix[2][1] = hvyz + vc[0];
			rotMatrix[2][2] = e + hvz * vc[2];
		}
		
		return new Rotation3D(rotMatrix);
	}
	
}