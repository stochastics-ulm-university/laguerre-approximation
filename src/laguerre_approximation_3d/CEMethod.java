package laguerre_approximation_3d;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import util.ThreadedTodoWorker;
import util.geom.MarkedPoint3D;
import util.geom.Point3D;
import util.geom.Utilities3D;
import util.math.GaussianRandomVariable;
import util.math.SampleCharacteristics;
import util.math.TruncatedGaussianRandomVariable;

/**
 * Approximation of Laguerre tessellations using the cross-entropy method.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 * @see "A. Spettl, T. Brereton, Q. Duan, T. Werz, C.E. Krill III, D.P. Kroese
 *       and V. Schmidt, Fitting Laguerre tessellation approximations to tomographic
 *       image data. arXiv:1508.01341 [cond-mat.mtrl-sci]"
 */
public class CEMethod {
	
	/** Object used to format double/float-values for printing to the console. */
	private static DecimalFormat floatFormat = new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.ENGLISH));
	
	/** Internal class for the maximal standard deviations currently in the CE method. */
	private static class MaxSigma {
		public double maxSigmaCoord  = 0.0;
		public double maxSigmaRadius = 0.0;
		
		public double max() {
			return Math.max(maxSigmaCoord, maxSigmaRadius);
		}
		
		@Override
		public String toString() {
			return "maxSigmaCoord="+floatFormat.format(maxSigmaCoord)+", maxSigmaRadius="+floatFormat.format(maxSigmaRadius);
		}
	}
	
	/** Internal class for the best generators detected so far in the CE method. */
	private static class BestGenerators {
		private ArrayList<MarkedPoint3D<Double>> generators;
		private double costValue;
		
		public BestGenerators(ArrayList<MarkedPoint3D<Double>> generators) {
			this.generators = generators;
			this.nextIteration();
		}
		
		public synchronized void nextIteration() {
			this.costValue = Double.POSITIVE_INFINITY;
		}
		
		public synchronized void register(ArrayList<MarkedPoint3D<Double>> generators, double costValue) {
			if (this.costValue > costValue) {
				this.generators = generators;
				this.costValue = costValue;
			}
		}
		
		public synchronized double getCostValue() {
			return this.costValue;
		}
		
		public synchronized ArrayList<MarkedPoint3D<Double>> getGenerators() {
			return this.generators;
		}
	}
	
	/**
	 * Runs the cross-entropy (CE) method to find a 'good' set of generators that
	 * define a Laguerre tessellation approximating a given data set.
	 * 
	 * @param cellCentroids  the centroids of all cells
	 * @param cellVolumes    the volumes of all cells, in the same order as defined
	 *                       by the <code>cellCentroids</code> array
	 * @param ceParameters   all parameters for the CE method itself
	 * @param costFunction   the cost function which determines the quality of the
	 *                       given generators, it must also use the same order as
	 *                       the <code>cellCentroids</code> array
	 * @return the list of optimal generators, in the same order as defined
	 *         by <code>cellCentroids</code>
	 */
	public static ArrayList<MarkedPoint3D<Double>> findOptimalGenerators(final Point3D[] cellCentroids, final double[] cellVolumes, final CEParameters ceParameters, final CostFunction costFunction) {
		if (cellCentroids.length != cellVolumes.length) {
			throw new IllegalArgumentException("Wrong length of cellCentroids or cellVolumes array!");
		}
		
		int eliteSetSize = (int)Math.ceil(ceParameters.rho*ceParameters.M);
		
		// CE method, initial sampling parameters
		// (mean values and standard deviations for coordinates and radii are stored as two sets of marked points)
		
		final ArrayList<MarkedPoint3D<Double>> mu = new ArrayList<MarkedPoint3D<Double>>(cellCentroids.length);
		for (int i = 0; i < cellCentroids.length; i++) {
			if (cellCentroids[i] != null) {
				double r = computeRadiusByVolume(cellVolumes[i]);
				mu.add(new MarkedPoint3D<Double>(cellCentroids[i], r));
			} else {
				mu.add(null);
			}
		}
		
		// evaluate cell-based cost values
		double[] initialCostsInCells = new double[mu.size()];
		for (int j = 0; j < mu.size(); j++) {
			if (mu.get(j) != null) {
				initialCostsInCells[j] = costFunction.getCostForCell(mu, j);
			} else {
				initialCostsInCells[j] = Double.NaN;
			}
		}
		
		// choose sigma based on per-cell cost values in initial configuration
		final ArrayList<MarkedPoint3D<Double>> sigma = new ArrayList<MarkedPoint3D<Double>>(cellCentroids.length);
		for (int j = 0; j < mu.size(); j++) {
			if (mu.get(j) != null) {
				double r = computeRadiusByVolume(cellVolumes[j]);
				double s = Math.sqrt(initialCostsInCells[j]);
				if (Double.isNaN(s) || s < r/20.0) {
					// cost too small, e.g., already zero? This is unlikely, but we enforce a very small minimum value.
					s = r/20.0;
				}
				MarkedPoint3D<Double> newSigma = new MarkedPoint3D<Double>(new Point3D(s, s, s), s);
				sigma.add(newSigma);
			} else {
				sigma.add(null);
			}
		}
		
		double costForMu = costFunction.getCost(mu);
		
		// CE method iterations, with M samples each
		
		LinkedList<Double> lastBestEliteCosts = new LinkedList<Double>();
		final BestGenerators bestGenerators = new BestGenerators(mu);
		MaxSigma currentMaxSigma;
		int injectCounter = 0;
		double prevInjectBestEliteSetCost = Double.POSITIVE_INFINITY;
		boolean injectAllowed = true;
		int t = 0;
		while ((currentMaxSigma = maxSigma(sigma)).max() > Utilities3D.eps) {
			t++;
			System.out.print("CE iteration "+t+" ("+currentMaxSigma+", costForMu="+floatFormat.format(costForMu)+")");
			
			bestGenerators.nextIteration();
			
			// construct all required Gaussian random variables
			
			final ArrayList<GaussianRandomVariable> gaussianX = new ArrayList<GaussianRandomVariable>(mu.size());
			final ArrayList<GaussianRandomVariable> gaussianY = new ArrayList<GaussianRandomVariable>(mu.size());
			final ArrayList<GaussianRandomVariable> gaussianZ = new ArrayList<GaussianRandomVariable>(mu.size());
			final ArrayList<TruncatedGaussianRandomVariable> gaussianR = new ArrayList<TruncatedGaussianRandomVariable>(mu.size());
			for (int i = 0; i < mu.size(); i++) {
				MarkedPoint3D<Double> generatorMu = mu.get(i);
				MarkedPoint3D<Double> generatorSigma = sigma.get(i);
				if (generatorMu != null) {
					gaussianX.add(new GaussianRandomVariable(generatorMu.getCoordinates()[0], generatorSigma.getCoordinates()[0]*generatorSigma.getCoordinates()[0]));
					gaussianY.add(new GaussianRandomVariable(generatorMu.getCoordinates()[1], generatorSigma.getCoordinates()[1]*generatorSigma.getCoordinates()[1]));
					gaussianZ.add(new GaussianRandomVariable(generatorMu.getCoordinates()[2], generatorSigma.getCoordinates()[2]*generatorSigma.getCoordinates()[2]));
					gaussianR.add(new TruncatedGaussianRandomVariable(generatorMu.getMark(), generatorSigma.getMark()*generatorSigma.getMark(), 0, Double.POSITIVE_INFINITY));
				} else {
					gaussianX.add(null);
					gaussianY.add(null);
					gaussianZ.add(null);
					gaussianR.add(null);
				}
			}
			
			// prepare result objects for M realisations
			
			final float[][][] allGenerators = new float[ceParameters.M][mu.size()][4]; // note: we use float-values to reduce memory consumption
			final double[] allCosts = new double[ceParameters.M];
			
			// generate M realisations and evaluate costs
			
			ThreadedTodoWorker.workOnIndices(0, ceParameters.M-1, 1, new ThreadedTodoWorker.SimpleTodoWorker<Integer>() {
				public void processTodoItem(Integer todo) {
					int n = todo.intValue();
					
					// sample generators
					ArrayList<MarkedPoint3D<Double>> generators = new ArrayList<MarkedPoint3D<Double>>(mu.size());
					for (int i = 0; i < mu.size(); i++) {
						if (mu.get(i) != null) {
							generators.add(new MarkedPoint3D<Double>(new Point3D(gaussianX.get(i).realise(),
							                                                     gaussianY.get(i).realise(),
							                                                     gaussianZ.get(i).realise()),
							                                         gaussianR.get(i).realise()));
						} else {
							generators.add(null);
						}
					}
					
					// compute cost
					double cost = costFunction.getCost(generators);
					
					// store generators
					for (int i = 0; i < generators.size(); i++) {
						MarkedPoint3D<Double> generator = generators.get(i);
						if (generator == null) {
							allGenerators[n][i] = null; // make sure all accesses to coordinates and radius of this (non-existing) generator fail
						} else {
							double[] generatorCoord = generator.getCoordinates();
							allGenerators[n][i][0] = (float)generatorCoord[0];
							allGenerators[n][i][1] = (float)generatorCoord[1];
							allGenerators[n][i][2] = (float)generatorCoord[2];
							allGenerators[n][i][3] = generator.getMark().floatValue();
						}
					}
					
					// store cost value
					allCosts[n] = cost;
					bestGenerators.register(generators, cost);
				}
			});
			
			// determine elite set threshold
			
			double[] costsSorted = allCosts.clone();
			Arrays.sort(costsSorted);
			double threshold = costsSorted[eliteSetSize-1];
			
			System.out.println(" [elite set cost values: min="+floatFormat.format(costsSorted[0])+", max="+floatFormat.format(threshold)+"]");
			
			if (Double.isInfinite(threshold)) {
				throw new RuntimeException("Elite set contains configurations with infinite cost! Aborting.");
			}
			
			// collect data of elite set and update mu/sigma values
			
			for (int j = 0; j < mu.size(); j++) {
				if (mu.get(j) == null) {
					continue;
				}
				ArrayList<Double> samplesX = new ArrayList<Double>(eliteSetSize);
				ArrayList<Double> samplesY = new ArrayList<Double>(eliteSetSize);
				ArrayList<Double> samplesZ = new ArrayList<Double>(eliteSetSize);
				ArrayList<Double> samplesR = new ArrayList<Double>(eliteSetSize);
				for (int i = 0; i < ceParameters.M; i++) {
					if (allCosts[i] <= threshold) {
						float[] p = allGenerators[i][j];
						samplesX.add(new Double(p[0]));
						samplesY.add(new Double(p[1]));
						samplesZ.add(new Double(p[2]));
						samplesR.add(new Double(p[3]));
					}
				}
				SampleCharacteristics edfEliteSetX = new SampleCharacteristics(samplesX);
				SampleCharacteristics edfEliteSetY = new SampleCharacteristics(samplesY);
				SampleCharacteristics edfEliteSetZ = new SampleCharacteristics(samplesZ);
				SampleCharacteristics edfEliteSetR = new SampleCharacteristics(samplesR);
				
				MarkedPoint3D<Double> newMu = new MarkedPoint3D<Double>(new Point3D(edfEliteSetX.getMean(), edfEliteSetY.getMean(), edfEliteSetZ.getMean()), edfEliteSetR.getMean());
				MarkedPoint3D<Double> newSigma = new MarkedPoint3D<Double>(new Point3D(edfEliteSetX.getStddev(), edfEliteSetY.getStddev(), edfEliteSetZ.getStddev()), edfEliteSetR.getStddev());
				
				mu.set(j, newMu);
				sigma.set(j, newSigma);
			}
			
			// keep track of the best cost values of the last few iterations
			lastBestEliteCosts.addLast(bestGenerators.getCostValue());
			if (lastBestEliteCosts.size() > Math.max(ceParameters.tauInject, ceParameters.tauTerminate)) {
				lastBestEliteCosts.removeFirst();
			}
			
			// make sure that we have the current (new) mean values in our "bestGenerators"
			// storage, if they are better (which they are, almost always)
			costForMu = costFunction.getCost(mu);
			bestGenerators.register(mu, costForMu);
			
			// if the cost value is really small, then terminate (does not occur in practice...)
			if (costForMu < Utilities3D.eps) {
				break;
			}
			
			// criterion for termination: no significant change in cost for 10 iterations
			if (lastBestEliteCosts.size() >= ceParameters.tauTerminate) {
				Iterator<Double> it = lastBestEliteCosts.descendingIterator();
				double last = it.next();
				boolean allInRange = true;
				for (int i = 1; i < ceParameters.tauTerminate; i++) {
					double value = it.next();
					if (Math.abs(last-value) > ceParameters.deltaTerminate*last) {
						allInRange = false;
						break;
					}
				}
				
				if (allInRange) {
					// abort CE method, no relevant cost change in 10 iterations
					break;
				}
			}
			
			// inject variance now, if necessary
			if (injectAllowed && ceParameters.deltaInject > 0.0 && (ceParameters.injectMax < 0 || injectCounter < ceParameters.injectMax) && lastBestEliteCosts.size() >= ceParameters.tauInject) {
				Iterator<Double> it = lastBestEliteCosts.descendingIterator();
				double last = it.next();
				boolean allInRange = true;
				for (int i = 1; i < ceParameters.tauInject; i++) {
					double value = it.next();
					if (Math.abs(last-value) > ceParameters.deltaInject*last) {
						allInRange = false;
						break;
					}
				}
				
				if (allInRange && ceParameters.gamma < last/prevInjectBestEliteSetCost) {
					System.out.println("Variance injection ("+(injectCounter+1)+"/"+(ceParameters.injectMax < 0 ? "?" : ceParameters.injectMax)+") scheduled but not performed, best elite set cost value decreased only to "+(int)Math.round(100.0*last/prevInjectBestEliteSetCost)+"%...");
					injectAllowed = false;
				} else if (allInRange) {
					prevInjectBestEliteSetCost = last;
					injectCounter++;
					System.out.println("Injecting variance ("+injectCounter+"/"+(ceParameters.injectMax < 0 ? "?" : ceParameters.injectMax)+")...");
					
					// determine costs per cell
					double[] costsInCells = new double[mu.size()];
					for (int j = 0; j < mu.size(); j++) {
						if (mu.get(j) != null) {
							costsInCells[j] = costFunction.getCostForCell(mu, j);
						} else {
							costsInCells[j] = Double.NaN;
						}
					}
					// use maximum of cost values in neighborhood
					double[] localCostsInCells = costsInCells.clone();
					for (int j = 0; j < mu.size(); j++) {
						if (mu.get(j) != null) {
							int[] adjacentIndices = costFunction.getAdjacentIndices(j);
							for (int k = 0; k < adjacentIndices.length; k++) {
								double cost = costsInCells[adjacentIndices[k]];
								if (localCostsInCells[j] < cost) {
									localCostsInCells[j] = cost;
								}
							}
						}
					}
					
					// inject variance now
					for (int j = 0; j < sigma.size(); j++) {
						MarkedPoint3D<Double> oldSigma = sigma.get(j);
						if (oldSigma != null) {
							double inject = ceParameters.kappa * Math.sqrt(localCostsInCells[j]);
							if (Double.isNaN(inject)) {
								continue;
							}
							MarkedPoint3D<Double> newSigma = new MarkedPoint3D<Double>(new Point3D(oldSigma.getCoordinates()[0] + inject,
							                                                                       oldSigma.getCoordinates()[1] + inject,
							                                                                       oldSigma.getCoordinates()[2] + inject),
							                                                           oldSigma.getMark() + inject);
							sigma.set(j, newSigma);
						}
					}
					
					// make sure termination or next injection need at least some more iterations
					lastBestEliteCosts.clear();
				}
			}
		}
		System.out.println("CE done after "+t+" iterations ("+currentMaxSigma+", costForMu="+floatFormat.format(costForMu)+")");
		
		return bestGenerators.getGenerators();
	}
	
	/**
	 * Determines the maximum value of all coordinates and marks for the given
	 * list of marked points (which are the standard deviations).
	 * 
	 * @param sigma  the list of marked points
	 * @return the maximum values for the standard deviations of the coordinates
	 *         and the radii
	 */
	private static MaxSigma maxSigma(ArrayList<MarkedPoint3D<Double>> sigma) {
		MaxSigma result = new MaxSigma();
		for (MarkedPoint3D<Double> obj : sigma) {
			if (obj == null) {
				continue;
			}
			double[] coord = obj.getCoordinates();
			double val1 = Math.max(coord[0], Math.max(coord[1], coord[2]));
			double val2 = obj.getMark();
			if (result.maxSigmaCoord  < val1) result.maxSigmaCoord  = val1;
			if (result.maxSigmaRadius < val2) result.maxSigmaRadius = val2;
		}
		return result;
	}
	
	/**
	 * Computes the radius of the volume-equivalent sphere.
	 * 
	 * @param volume  the volume of the 3D object
	 * @return the radius of the volume-equivalent sphere
	 */
	private static double computeRadiusByVolume(double volume) {
		return Math.pow(volume * 3.0 / 4.0 / Math.PI, 1.0/3.0);
	}
	
}
