package util.geom;

/**
 * This class represents a 3D Plane.
 * The points of the plane are given by <code>n*x = d</code>, where <code>n</code>
 * is the unit normal vector of the plane and <code>d</code> the (positive) distance to
 * the origin.
 * 
 * @author Institute of Stochastics, Ulm University
 */
public class Plane3D {
	
	/** The distance from the origin, this is always positive. */
	private final double distance;
	
	/** The (direction of the) unit normal vector of the plane. */
	private final Point3D normal;
	
	/** 
	 * Constructs a 3D Plane. The location of the plane is determined by <code>distance</code>,
	 * the minimal distance of the plane from the origin.
	 * 
	 * @param distance	the distance of the plane from the origin
	 * @param normal	The (direction of the) unit normal vector	
	 */
	public Plane3D(double distance, Point3D normal) {
		this.distance = distance;
		this.normal	= normal.norm();
		
		if (distance < 0.0) {
			throw new IllegalArgumentException("Cant work with negative distance!");
		}
	}
	
	/** 
	 * Checks if a Plane is close or equal to another one.
	 *
	 * @param geom 	the Geometry3D which the object should be compared to
	 * @return <code>true</code> if the other plane is close or equal
	 */
	public boolean isSimilar(Plane3D geom) {
		Plane3D s = (Plane3D) geom;
		if (Utilities3D.isEqual(distance, s.getDistance())) {
			if (normal.isSimilar(s.getNormalVec())) {
				return true;
			} else if (Utilities3D.isEqual(distance, 0.0) && normal.isSimilar(s.getNormalVec().reflectOrigin())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Reflects the plane at the origin without changing the original plane.
	 * 
	 * @return the resulting reflected plane
	 */    
	public Plane3D reflectOrigin() {		
		return new Plane3D(distance, normal.reflectOrigin());
	}
	
	/**
	 * Translates the plane by a vector <code>translateVector</code> without changing the
	 * original plane.
	 * 
	 * @param translateVector the point or vector by which the object shall be translated
	 * @return the resulting translated plane
	 */
	public Plane3D translateBy(Point3D translateVector) {
		double additionalDist = normal.getScalarProduct(translateVector);
		double newDist = distance + additionalDist;
		Point3D newDir;
		if (newDist < 0) {
			newDir = normal.reflectOrigin();
			newDist = -newDist;
		} else {
			newDir = normal;
		}
		return new Plane3D(newDist, newDir);
	}
	
	/**
	 * Scales the plane by a factor.
	 * 
	 * @param scale  the scale for this operation
	 * @return the scaled object
	 */
	public Plane3D scaleBy(double scale) {
		double newDist = distance * scale;
		Point3D newDir;
		if (newDist < 0) {
			newDir = normal.reflectOrigin();
			newDist = -newDist;
		} else {
			newDir = normal;
		}
		return new Plane3D(newDist, newDir);
	}
	
	/**
	 * Returns the distance of the plane from the origin.   
	 *    
	 * @return <code>Distance</code>, the distance of the plane from the origin. 
	 */
	public double getDistance(){
		return distance;
	}
	
	/**
	 * Returns the normal unit vector of the plane.     
	 *  
	 * @return <code>normal</code>, the normal unit vector of the plane.
	 */
	public Point3D getNormalVec(){
		return normal;
	}
	
	/**
	 * Returns a string representation of this plane for debugging purposes.
	 * 
	 * @return a string representation of this plane
	 */
	@Override
	public String toString() {
		return "3D plane with distance from the origin : " + distance + " and unit normal vector : " + normal;
	}
	
}
