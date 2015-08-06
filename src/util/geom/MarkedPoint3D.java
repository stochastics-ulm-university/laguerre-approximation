package util.geom;

/**
 * This class represents a marked 3D point. This class is immutable up to the mark,
 * that means that the point (i.e., its coordinates) and the mark object can't be
 * changed after its construction.
 * 
 * @author Institute of Stochastics, Ulm University
 * 
 * @param <T> the class of the mark 
 */
public class MarkedPoint3D<T> extends Point3D {
	
	/** The mark. */
	private final T mark;
	
	/**
	 * Constructs a new point with the coordinates given by <code>point</code> and
	 * with the given mark.
	 * 
	 * @param point     the point defining the position of the marked point
	 * @param mark      the mark for the marked point
	 */
	public MarkedPoint3D(Point3D point, T mark) {
		super(point);
		this.mark = mark;
	}
	
	/**
	 * Returns the mark of this marked point.
	 * 
	 * @return the mark of this marked point
	 */
	public T getMark() {
		return mark;
	}
	
	/**
	 * Checks if the marked point is very close or equal to <code>geom</code>.
	 * 
	 * @param geom  the Geometry3D which shall be compared to the point
	 * @return <code>true</code> if the Geometry3D <code>geom</code> is
	 *         very close or equal to the point and the mark is equal
	 */
	public boolean isSimilar(Point3D geom) {
		if (geom instanceof MarkedPoint3D) {
			MarkedPoint3D<?> mp = (MarkedPoint3D<?>)geom;
			return super.isSimilar(geom) && mark.equals(mp.getMark());
		} else
			return false;
	}
	
	/**
	 * Reflects the marked point at the origin without changing the
	 * original marked point.
	 * 
	 * @return the resulting reflected marked point
	 */
	@Override
	public MarkedPoint3D<T> reflectOrigin() {
		return new MarkedPoint3D<T>(super.reflectOrigin(), mark);
	}
	
	/**
	 * Applies a real scale to the marked point without changing the original marked point.
	 * 
	 * @param scale   the scale for this operation
	 * @return the scaled marked point
	 */
	@Override
	public MarkedPoint3D<T> scaleBy(double scale) {
		return new MarkedPoint3D<T>(super.scaleBy(scale), mark);
	}
	
	/**
	 * Scales the marked point a three-dimensional scale in each direction without
	 * changing the original marked point.
	 * 
	 * @param scale  the 3D scale for this operation
	 * @return the resulting scaled marked point
	 */
	@Override
	public MarkedPoint3D<T> scaleBy(double[] scale) {
		return new MarkedPoint3D<T>(super.scaleBy(scale), mark);
	}
	
	/**
	 * Translates the marked point by a point <code>translateVector</code> without
	 * changing the original marked point.
	 * 
	 * @param translateVector  the point or vector by which the object shall be
	 *                         translated
	 * @return the resulting translated marked point
	 */
	@Override
	public MarkedPoint3D<T> translateBy(Point3D translateVector) {
		return new MarkedPoint3D<T>(super.translateBy(translateVector), mark);
	} 
	
	/**
	 * Norms the marked point without changing the original marked point.
	 * 
	 * @return the normed marked point
	 */
	@Override
	public MarkedPoint3D<T> norm() {
		return new MarkedPoint3D<T>(super.norm(), mark);
	}
	
	/**
	 * Returns a string representation of this marked point for debugging purposes.
	 * 
	 * @return a string representation of this marked point
	 */
	@Override
	public String toString() {
		return super.toString() + " mark: " + mark.toString();
	}
	
}
