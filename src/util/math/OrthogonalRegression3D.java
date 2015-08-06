package util.math;

import java.util.Collection;

import util.geom.Plane3D;
import util.geom.Point3D;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Fitting of a plane to a set of points in 3D, where the orthogonal
 * distance from the points to the plane is minimized.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 * @see "P.P.N. de Groen: An introduction to total least squares. Nieuw Archief
 *       voor Wiskunde, 4th Series 14, 237–253 (1996)"
 */
public class OrthogonalRegression3D {
	
	/** The centroid of the points. */
	protected Point3D c;
	
	/** The matrix consisting the coordinates of the points shifted by the (reflected) centroid. */
	protected Matrix M;
	
	/** The singular value decomposition of the matrix <code>M</code>. */
	protected SingularValueDecomposition svd;
	
	/**
	 * Constructs a new object for orthogonal regression in 3D.
	 * 
	 * @param points  the set of points
	 */
	public OrthogonalRegression3D(Collection<Point3D> points) {
		// compute centroid of points (and check input data)
		double[] cCoord = new double[] { 0.0, 0.0, 0.0 };
		int n = 0;
		for (Point3D obj : points) {
			double[] coord = ((Point3D)obj).getCoordinates();
			cCoord[0] += coord[0];
			cCoord[1] += coord[1];
			cCoord[2] += coord[2];
			n++;
		}
		cCoord[0] /= n;
		cCoord[1] /= n;
		cCoord[2] /= n;
		c = new Point3D(cCoord);
		
		// construct matrix with shifted point coordinates
		M = new Matrix(n, 3);
		int i = 0;
		for (Point3D obj : points) {
			double[] coord = ((Point3D)obj).getCoordinates();
			M.set(i, 0, coord[0]-cCoord[0]);
			M.set(i, 1, coord[1]-cCoord[1]);
			M.set(i, 2, coord[2]-cCoord[2]);
			i++;
		}
		
		svd = null;
	}
	
	/**
	 * Returns the centroid of the points.
	 * 
	 * @return the centroid of the points
	 */
	public Point3D getCentroid() {
		return c;
	}
	
	/**
	 * Returns the matrix consisting the coordinates of the points shifted by the (reflected) centroid.
	 * 
	 * @return the matrix consisting the coordinates of the points shifted by the (reflected) centroid
	 */
	public Matrix getShiftedCoordinates() {
		return M;
	}
	
	/**
	 * Returns the singular value decomposition of the matrix containing the shifted coordinates.
	 * 
	 * @return the singular value decomposition of the matrix containing the shifted coordinates
	 */
	public SingularValueDecomposition getSVD() {
		if (svd == null) {
			svd = M.svd();
		}
		return svd;
	}
	
	/**
	 * Estimates the parameters of a plane by minimizing the orthogonal distance
	 * of the given points to the plane.
	 * 
	 * 
	 * @return the plane
	 * @throws IllegalArgumentException if there is a problem with the points
	 *                                  (e.g., not enough points)
	 */
	public Plane3D fitPlane() {
		return fitPlane(1.0);
	}
	
	/**
	 * Estimates the parameters of a plane by minimizing the orthogonal distance
	 * of the given points to the plane.
	 * 
	 * @param singularValueRatio the maximum value allowed for the ratio of the
	 *                           smallest singular value to the second-smallest
	 *                           (this can be used to avoid detecting planes for
	 *                           vertex- or edge-like shapes), i.e., <code>1.0</code>
	 *                           is no restriction
	 * @return the plane or <code>null</code> if the <code>singularValueRatio</code>-condition
	 *         is not fulfilled
	 * @throws IllegalArgumentException if there is a problem with the points
	 *                                  (e.g., not enough points)
	 */
	public Plane3D fitPlane(double singularValueRatio) {
		// use singular value decomposition to get smallest singular value and its right-singular vector
		SingularValueDecomposition svd = getSVD();
		int r = svd.rank();
		if (r < 3) {
			throw new IllegalArgumentException("Not enough points (which are not collinear)!");
		}
		double[] diagS = svd.getSingularValues();
		if (diagS[1]*singularValueRatio < diagS[2]) {
			// points are too different from a plane (smallest singular value is not that much smaller than the second-smallest)
			return null;
		}
		Matrix V = svd.getV();
		
		// right-singular vector (of smallest singular value) is the normal vector of the plane
		Point3D normalVector = new Point3D(V.getMatrix(0, 2, r-1, r-1).getColumnPackedCopy());
		
		// centroid lies on the plane, use this to construct distance to origin
		double distance = normalVector.getScalarProduct(c);
		
		// parameterization of our plane requires d to be non-negative
		if (distance < 0.0) {
			normalVector = normalVector.reflectOrigin();
			distance = -distance;
		}
		
		return new Plane3D(distance, normalVector);
	}
	
}