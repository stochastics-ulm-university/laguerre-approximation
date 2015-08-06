package util.geom;

/**
 * This class represents a 3D rotation. This class is immutable, that means that it
 * can't be changed after its construction.
 * 
 * @author Institute of Stochastics, Ulm University
 */
public class Rotation3D {
	
	/**
	 * The rotation that doesn't change anything.
	 */
	public static final Rotation3D IDENTITY = new Rotation3D(new double[][] { { 1.0, 0.0, 0.0 },
	                                                                          { 0.0, 1.0, 0.0 },
	                                                                          { 0.0, 0.0, 1.0 } });
	
	/** The rotation matrix. */
	private final double[][] rotation;
	
	/**
	 * Constructs a rotation via a 3x3 matrix.
	 * 
	 * @param rotation  the 3x3 matrix defining the rotation
	 */
	public Rotation3D(double[][] rotation) {
		this.rotation = new double[3][3];
		for (int i = 0; i < 3; i++) {
			this.rotation[i] = rotation[i].clone();
		}
	}
	
	/**
	 * Applies the rotation represented by this class to a
	 * <code>Point3D</code> without changing the original point.
	 * 
	 * @param point the point to which the rotation is applied
	 * @return the resulting point after applying the rotation
	 */
	public Point3D applyTo(Point3D point) {
		double[] newCoords = new double[3];
		for (int i = 0; i < 3; i++) {
			newCoords[i] = 0;
			for (int j = 0; j < 3; j++) {
				newCoords[i] += rotation[i][j] * point.getCoordinates()[j];
			}
		}
		return new Point3D(newCoords);
	}
	
}