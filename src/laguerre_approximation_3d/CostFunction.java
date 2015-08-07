package laguerre_approximation_3d;

import java.util.ArrayList;

import util.geom.HalfSpace3D;
import util.geom.MarkedPoint3D;
import util.geom.Point3D;
import util.math.LaguerreTessellation3D;

/**
 * A cost function that computes the total approximate discrepancy (i.e.,
 * the mean squared distance of test points to the respective cell faces
 * in an approximated Laguerre tessellation).
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 * @see "A. Spettl, T. Brereton, Q. Duan, T. Werz, C.E. Krill III, D.P. Kroese
 *       and V. Schmidt, Fitting Laguerre tessellation approximations to tomographic
 *       image data. arXiv:1508.01341 [cond-mat.mtrl-sci]"
 */
public class CostFunction {
	
	/** Internal object used to compute the cost of an individual cell. */
	private static class CellCost {
		private double sum = 0.0;
		private int n = 0;
		
		private void add(double cost) {
			sum += cost*cost;
			n++;
		}
		
		public double getCost() {
			return sum / n;
		}
	}
	
	/** Internal object used to compute the total cost, i.e., the mean cell cost. */
	private static class TotalCost {
		private double sum = 0.0;
		private int n = 0;
		
		private void add(CellCost cost) {
			sum += cost.sum;
			n += cost.n;
		}
		
		public double getCost() {
			return sum / n;
		}
	}
	
	/** List of test points for every cell adjacency. */
	private ArrayList<ArrayList<ArrayList<Point3D>>> testPoints;
	
	/** The list of indices belonging to adjacent cells, for every cell, with symmetry exploited to evaluate every pair only once (if possible). */
	protected int[][] indicesReduced;
	
	/** The list of indices belonging to adjacent cells, for every cell. */
	protected int[][] indicesAll;
	
	/**
	 * Constructs a new object for evaluating the cost of a Laguerre tessellation
	 * in comparison to the given test points located on faces between cells.
	 * 
	 * @param testPoints  the test points for pairs of cells (i.e., adjacent cells)
	 */
	public CostFunction(ArrayList<ArrayList<ArrayList<Point3D>>> testPoints) {
		this.testPoints = testPoints;
		
		boolean symmetric = true;
		outerLoop: for (int i = 0; i < testPoints.size(); i++) {
			for (int j = i+1; j < testPoints.size(); j++) {
				if (testPoints.get(i).get(j) != testPoints.get(j).get(i)) {
					symmetric = false;
					break outerLoop;
				}
			}
		}
		
		// fetch all indices of adjacent cells for every cell (in order
		// to avoid an almost no-op loop in the getCost method)
		this.indicesAll = new int[testPoints.size()][];
		for (int i = 0; i < testPoints.size(); i++) {
			ArrayList<Integer> l = new ArrayList<Integer>();
			for (int j = 0; j < testPoints.size(); j++) {
				if (testPoints.get(i).get(j) != null) {
					l.add(j);
				}
			}
			this.indicesAll[i] = new int[l.size()];
			for (int k = 0; k < l.size(); k++) {
				this.indicesAll[i][k] = l.get(k);
			}
		}
		// if symmetric, use every pair in only one of its cells
		if (symmetric) {
			this.indicesReduced = new int[testPoints.size()][];
			for (int i = 0; i < testPoints.size(); i++) {
				ArrayList<Integer> l = new ArrayList<Integer>();
				for (int j = i /* here is the difference to "indicesAll" construction */; j < testPoints.size(); j++) {
					if (testPoints.get(i).get(j) != null) {
						l.add(j);
					}
				}
				this.indicesReduced[i] = new int[l.size()];
				for (int k = 0; k < l.size(); k++) {
					this.indicesReduced[i][k] = l.get(k);
				}
			}
		} else {
			this.indicesReduced = this.indicesAll;
		}
	}
	
	/**
	 * Computes the cost value for the given generators, i.e., returns
	 * the mean squared (absolute) distance of the test points to the
	 * faces of the Laguerre tessellation implied by the given generators.
	 * 
	 * @param generators  the generators
	 * @return the mean squared (relative) distance
	 */
	public double getCost(ArrayList<MarkedPoint3D<Double>> generators) {
		TotalCost result = new TotalCost();
		for (int i = 0; i < this.indicesReduced.length; i++) {
			result.add(this.getCostForCell(generators, i, false));
		}
		return result.getCost();
	}
	
	/**
	 * Computes the cost value of a single cell for the given generators.
	 * 
	 * @param generators  the generators
	 * @param i           the index of the cell whose faces should be evaluated
	 * @return the mean squared (relative) distance
	 */
	public double getCostForCell(ArrayList<MarkedPoint3D<Double>> generators, int i) {
		return this.getCostForCell(generators, i, true).getCost();
	}
	
	/**
	 * Returns all indices of cell that are adjacent to the given one.
	 * 
	 * @param i  the index of the cell
	 * @return the indices of the adjacent cells
	 */
	public int[] getAdjacentIndices(int i) {
		return this.indicesAll[i].clone();
	}
	
	/**
	 * Computes the cost value of a single cell for the given generators.
	 * 
	 * @param generators  the generators
	 * @param i           the index of the cell whose faces should be evaluated
	 * @param forceAll    determines whether the evaluation of all neighbors is
	 *                    enforced (otherwise, only the reduced set of neighbors is
	 *                    used, where symmetry has been exploited; only sensible
	 *                    if the total cost of all cells should be evaluated)
	 * @return the cost value (sum of squared distances) and the number of test points
	 */
	private CellCost getCostForCell(ArrayList<MarkedPoint3D<Double>> generators, int i, boolean forceAll) {
		CellCost result = new CellCost();
		
		MarkedPoint3D<Double> pi = generators.get(i);
		if (pi == null) {
			return result;
		}
		
		for (int j : (forceAll ? this.indicesAll[i] : this.indicesReduced[i])) {
			MarkedPoint3D<Double> pj = generators.get(j);
			if (pj == null) {
				continue;
			}
			
			try {
				HalfSpace3D halfSpace = LaguerreTessellation3D.constructHalfSpaceForLaguerreCell(pi, pj);
				
				for (Point3D testVoxel : testPoints.get(i).get(j)) {
					result.add(halfSpace.signedDistanceToSurface(testVoxel));
				}
			} catch (RuntimeException e) {
				// ignore numerical issues, points that are equal by chance, etc.
			}
		}
		
		return result;
	}
	
}