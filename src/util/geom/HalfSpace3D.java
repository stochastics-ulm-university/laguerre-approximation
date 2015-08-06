package util.geom;

/**
 * Closed half-space in 3D.
 * <p>
 * All points of the half-space are given by the inequality <code>n*x + b &lt;= 0</code>,
 * where <code>n</code> is the (outwards pointing) normal vector and <code>b</code> is
 * the offset.
 * <p>
 * If we have a unit normal vector, then the offset is the signed distance to the origin,
 * i.e., the offset is positive if the origin is not contained in the half-space, and
 * negative if the origin is contained. 
 * 
 * @author Institute of Stochastics, Ulm University
 */
public class HalfSpace3D {
	
	/** Normal vector pointing outwards from the half-space, always a unit vector. */
	private Point3D normalVector;
	
	/** Signed distance from the origin. */
	private double offset;
	
	/**
	 * Constructs a new half-space by a given normal vector and offset.
	 * 
	 * @param normalVector  a normal vector of the half-space 
	 * @param offset        the offset, i.e., the signed distance from the origin, given the normal vector has length one
	 */
	public HalfSpace3D(Point3D normalVector, double offset) {
		double normalVectorLength = normalVector.getLength();
		if (Utilities3D.isEqual(normalVectorLength, 0.0)) {
			throw new IllegalArgumentException("Normal vector must have a length greater than zero!");
		} else if (Utilities3D.isEqual(normalVectorLength, 1.0)) {
			this.normalVector = normalVector;
			this.offset = offset;
		} else {
			this.normalVector = normalVector.scaleBy(1.0 / normalVectorLength);
			this.offset = offset / normalVectorLength;
		}
	}
	
	/**
	 * Returns the normal vector pointing outwards from the half-space, always a unit vector.
	 * 
	 * @return the unit normal vector
	 */
	public Point3D getNormalVector() {
		return this.normalVector;
	}
	
	/**
	 * Returns the offset, i.e., the signed distance from the origin.
	 * 
	 * @return the offset
	 */
	public double getOffset() {
		return this.offset;
	}
	
	/**
	 * Translates the half-space by a point <code>translateVector</code> without
	 * changing the original half-space.
	 * 
	 * @param translateVector  the point or vector by which the object shall be
	 *                         translated
	 * @return the resulting translated half-space
	 */
	public HalfSpace3D translateBy(Point3D translateVector) {
		double offsetShift = this.getNormalVector().getScalarProduct(translateVector);
		return new HalfSpace3D(this.getNormalVector(), this.offset - offsetShift);
	}
	
	/**
	 * Scales the half-space by a factor.
	 * 
	 * @param scale  the scale for this operation
	 * @return the resulting scaled half-space
	 */
	public HalfSpace3D scaleBy(double scale) {
		Point3D newNormalVector = this.getNormalVector();
		double newOffset = this.offset*scale;
		if (scale < 0.0) {
			newNormalVector = newNormalVector.reflectOrigin();
			newOffset = -newOffset;
		}
		return new HalfSpace3D(newNormalVector, newOffset);
	}
	
	/**
	 * Checks if this half-space is very close or equal to <code>geom</code>.
	 *
	 * @param geom the Geometry3D which shall be compared to this half-space
	 * @return <code>true</code> if the Geometry3D <code>geom</code> is
	 *         very close or equal to this half-space
	 */
	public boolean isSimilar(HalfSpace3D geom) {
		HalfSpace3D other = (HalfSpace3D)geom;
		
		if (!this.getNormalVector().isSimilar(other.getNormalVector())) {
			return false;
		}
		
		if ((this.getOffset() != other.getOffset()) && !Utilities3D.isEqual(this.getOffset(), other.getOffset())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Computes <code>n*x + b</code>, where <code>n</code> is the normal vector,
	 * <code>b</code> is the offset and <code>x</code> the given point.
	 * 
	 * @param point  the point
	 * @return zero if the point lies on the boundary, a positive value if
	 *         the point is outside this half-space, a negative value if
	 *         the point is inside this half-space
	 */
	protected double leftSideOfInequality(Point3D point) {
		return this.normalVector.getScalarProduct(point) + this.offset;
	}
	
	/**
	 * Computes the (signed) distance from the given point to the
	 * surface of this half-space.
	 * 
	 * @param point  the point
	 * @return the distance to the surface, positive if the point is
	 *         outside the object, negative otherwise
	 */
	public double signedDistanceToSurface(Point3D point) {
		return this.leftSideOfInequality(point);
	}
	
	/**
	 * Returns a string representation of this half-space for debugging purposes.
	 *
	 * @return a string representation of this half-space
	 */
	@Override
	public String toString() {
		return "Half-space with normal vector (" + this.getNormalVector().toString() + ") and offset " + this.getOffset();
	}
	
}