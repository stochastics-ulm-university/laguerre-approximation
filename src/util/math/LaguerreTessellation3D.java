package util.math;

import util.geom.HalfSpace3D;
import util.geom.MarkedPoint3D;
import util.geom.Point3D;

/**
 * Helper methods to work with Laguerre tessellations in 3D.
 * The generators are given by marked points, where the mark stands for
 * the radius of the sphere around the generator point.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class LaguerreTessellation3D {
	
	/** The upper bound for the relative error of double values. */
	private static final double DOUBLE_EPS = 1.11E-16;
	
	/**
	 * Constructs a single half-space of a 3D Laguerre cell.
	 * 
	 * @param point       the point to compute the Laguerre cell for
	 * @param otherPoint  the other marked point
	 * @return the half-space that contains <code>point</code>
	 */
	public static HalfSpace3D constructHalfSpaceForLaguerreCell(MarkedPoint3D<Double> point, MarkedPoint3D<Double> otherPoint) {
		// reference for normal vector and offset of the half-space:
		// see page 22, Dissertation Claudia Redenbach
		// (adapted to "n*z + b <= 0" instead of "n*z >= b")
		Point3D normalVector = new Point3D(otherPoint.getVectorTo(point).scaleBy(2.0));
		double offset = point.getLength()*point.getLength() - otherPoint.getLength()*otherPoint.getLength()
		                + otherPoint.getMark()*otherPoint.getMark() - point.getMark()*point.getMark();
		
		// the constructor of HalfSpace3D uses a larger eps-value, therefore rescaling
		// the normal vector now...
		// (only throw an exception if the length is *really* small)
		double normalVectorLength = normalVector.getLength();
		if (normalVectorLength < DOUBLE_EPS) {
			throw new RuntimeException("Could not compute normal vector of the half-space / separating plane, the two points "+point+" and "+otherPoint+" are identical!");
		}
		normalVector = normalVector.scaleBy(1.0 / normalVectorLength);
		offset = offset / normalVectorLength;
		
		return new HalfSpace3D(normalVector, offset);
	}
	
}
