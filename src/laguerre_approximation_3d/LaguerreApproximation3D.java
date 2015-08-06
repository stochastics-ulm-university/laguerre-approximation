package laguerre_approximation_3d;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import util.Array3D;
import util.MultiPageImageReader3D;
import util.ThreadedTodoWorker;
import util.geom.MarkedPoint3D;
import util.geom.Plane3D;
import util.geom.Point3D;
import util.geom.Rotation3D;
import util.geom.Utilities3D;
import util.math.OrthogonalRegression3D;

/**
 * Main class for detecting the optimal generators of a Laguerre tessellation
 * that should approximate a given labeled image.
 * The method is based on the paper by A. Spettl et al. (2015), where an
 * interface-based cost function is introduced, namely the total approximate
 * discrepancy in formula (5). The corresponding minimization problem
 * (formula (6)) is solved with the cross-entropy method.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 * @see "A. Spettl, T. Brereton, Q. Duan, T. Werz, C.E. Krill III, D.P. Kroese
 *       and V. Schmidt, Fitting Laguerre tessellation approximations to tomographic
 *       image data. Preprint."
 */
public class LaguerreApproximation3D {
	
	/** The number of test points to use for every face between two cells. */
	public static int defaultNumberOfTestPoints = 10;
	
	/**
	 * Main method for performing the Laguerre approximation as described in
	 * the paper by A. Spettl et al. (2015).
	 * <p>
	 * Input and output files are specified as parameters on the command line.
	 * As input, a labeled 3D TIF image file is expected (cf. Section 3.1 of
	 * the paper), which can be generated with e.g. the open source software
	 * package ImageJ / Fiji.
	 * Output is a simple text file containing the detected generators. One
	 * line corresponds to one generator. The first value is the label, the
	 * remaining four values are x-, y-, z-coordinates and the radius.
	 * <p>
	 * Note: To process the example image data sets with size 700x700x700
	 * (artificial PCM and PLT data), a Java heap size of about 3.5 GB is
	 * required. (Internally, every voxel is stored as a 16-bit integer value.
	 * Therefore, memory requirements are larger than strictly necessary.)
	 * You can define the maximal Java heap by passing e.g. <code>-Xmx3500m</code>
	 * to the Java virtual machine. The experimental data set is larger and
	 * requires about 4.5 GB Java heap.
	 * 
	 * @param args  the command line arguments, i.e., an array where the first
	 *              parameter defines the input image file (a multi-page TIF file),
	 *              and the second parameter defines the output text file
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		// multi-threading: by default, we use as many CPU cores as present in the system
		// note: you can set the NUMBER_OF_THREADS environment variable to define the number of threads that should be used
		ThreadedTodoWorker.ENABLED = true;
		
		OptionParser parser = new OptionParser();
		OptionSpec<Integer> optionM             = parser.accepts("M").withRequiredArg().ofType(Integer.class);
		OptionSpec<Double> optionRho            = parser.accepts("rho").withRequiredArg().ofType(Double.class);
		OptionSpec<Integer> optionTauInject     = parser.accepts("tauInject").withRequiredArg().ofType(Integer.class);
		OptionSpec<Double> optionDeltaInject    = parser.accepts("deltaInject").withRequiredArg().ofType(Double.class);
		OptionSpec<Integer> optionInjections    = parser.accepts("injections").withRequiredArg().ofType(Integer.class);
		OptionSpec<Double> optionGamma          = parser.accepts("gamma").withRequiredArg().ofType(Double.class);
		OptionSpec<Double> optionKappa          = parser.accepts("kappa").withRequiredArg().ofType(Double.class);
		OptionSpec<Integer> optionTauTerminate  = parser.accepts("tauTerminate").withRequiredArg().ofType(Integer.class);
		OptionSpec<Double> optionDeltaTerminate = parser.accepts("deltaTerminate").withRequiredArg().ofType(Double.class);
		
		System.out.println("---------------------------------------------------");
		System.out.println(" Laguerre tessellation approximation of 3D images  ");
		System.out.println("---------------------------------------------------");
		System.out.println("Copyright: Institute of Stochastics, Ulm University");
		System.out.println("License:   GPL v2.0 or higher");
		System.out.println("");
		
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			printUsage();
			System.exit(1);
		}
		
		if (options.nonOptionArguments().size() != 2) {
			printUsage();
			System.exit(1);
		}
		
		String imageFilePath = (String)options.nonOptionArguments().get(0);
		String generatorsFileName = (String)options.nonOptionArguments().get(1);
		
		// the parameters for the CE minimization (by default, the parameters as suggested
		// in the paper are used)
		CEParameters ceParameters = new CEParameters();
		// e.g., use fixed number of variance injections:
		if (options.has(optionM))              ceParameters.M = options.valueOf(optionM);
		if (options.has(optionRho))            ceParameters.rho = options.valueOf(optionRho);
		if (options.has(optionTauInject))      ceParameters.tauInject = options.valueOf(optionTauInject);
		if (options.has(optionDeltaInject))    ceParameters.deltaInject = options.valueOf(optionDeltaInject);
		if (options.has(optionInjections))     ceParameters.injectMax = options.valueOf(optionInjections);
		if (ceParameters.injectMax >= 0)       ceParameters.gamma = Double.POSITIVE_INFINITY;
		if (options.has(optionGamma))          ceParameters.gamma = options.valueOf(optionGamma);
		if (options.has(optionKappa))          ceParameters.kappa = options.valueOf(optionKappa);
		if (options.has(optionTauTerminate))   ceParameters.tauTerminate = options.valueOf(optionTauTerminate);
		if (options.has(optionDeltaTerminate)) ceParameters.deltaTerminate = options.valueOf(optionDeltaTerminate);
		
		System.out.println("Parameters:");
		System.out.println("Input image file:     "+imageFilePath);
		System.out.println("Output text file:     "+generatorsFileName);
		System.out.println("Test points per face: "+defaultNumberOfTestPoints);
		System.out.println("");
		System.out.println("Parameters of CE method:");
		System.out.println("M:                    "+ceParameters.M);
		System.out.println("rho:                  "+ceParameters.rho);
		System.out.println("tau_inject:           "+ceParameters.tauInject);
		System.out.println("delta_inject:         "+ceParameters.deltaInject);
		System.out.println("inject_max:           "+ceParameters.injectMax+((ceParameters.injectMax < 0) ? " (auto)" : ""));
		System.out.println("gamma:                "+ceParameters.gamma);
		System.out.println("kappa:                "+ceParameters.kappa);
		System.out.println("tau_terminate:        "+ceParameters.tauTerminate);
		System.out.println("delta_terminate:      "+ceParameters.deltaTerminate);
		System.out.println("");
		
		// read image data
		System.out.print("Reading labeled image...");
		short[][][] labeledImage = MultiPageImageReader3D.read(imageFilePath);
		System.out.println(" done.");
		System.out.println("Using labels: [1, "+Array3D.max(labeledImage)+"]");
		
		// perform CE optimization
		ArrayList<MarkedPoint3D<Double>> generators = findOptimalGenerators(labeledImage, defaultNumberOfTestPoints, ceParameters);
		
		// write results to disk
		System.out.print("Writing generators to text file...");
		writeGeneratorsToFile(generators, generatorsFileName);
		System.out.println(" done.");
	}
	
	/**
	 * Prints the usage comments to the standard output.
	 */
	public static void printUsage() {
		System.out.println("Usage:");
		System.out.println("First parameter:  file name of e.g. a multi-page TIF file");
		System.out.println("                  (a labeled image is required)");
		System.out.println("Second parameter: file name of the output text file");
		System.out.println("                  (list of generators, one generator per line in the format 'label x y z r')");
		System.out.println("");
		
		CEParameters defaults = new CEParameters();
		System.out.println("Optional arguments (and their default values):");
		System.out.println("--M="+defaults.M+" "+
		                   "--rho="+defaults.rho+" "+
		                   "--tauInject="+defaults.tauInject+" "+
		                   "--deltaInject="+defaults.deltaInject+" "+
		                   "--injections="+defaults.injectMax+" "+
		                   "--gamma="+defaults.gamma+" "+
		                   "--kappa="+defaults.kappa+" "+
		                   "--tauTerminate="+defaults.tauTerminate+" "+
		                   "--deltaTerminate="+defaults.deltaTerminate);
	}
	
	/**
	 * Minimizes the interface-based distance measure given in A. Spettl et al. (2015)
	 * using the CE method.
	 * 
	 * @param labeledImage       the labeled image where each label denotes a cell
	 *                           with a unique number
	 * @param numberOfTestPoints the number of test points per face
	 * @param ceParameters       all parameters for the CE method
	 * @return the list of detected generators, the first entry corresponds to the
	 *         image region with label 1, and so on
	 * @throws IOException if an I/O error occurs
	 */
	public static ArrayList<MarkedPoint3D<Double>> findOptimalGenerators(short[][][] labeledImage, int numberOfTestPoints, CEParameters ceParameters) throws IOException {
		if (numberOfTestPoints < 1) {
			throw new IllegalArgumentException("Parameter numberOfTestPoints must be positive!");
		}
		
		int labelCount = Array3D.max(labeledImage);
		
		System.out.print("Determining centroids and volumes of regions...");
		Point3D[] cellCentroids = new Point3D[labelCount];
		double[] cellVolumes = new double[labelCount];
		for (int x = 0; x < labeledImage.length; x++) {
			for (int y = 0; y < labeledImage[x].length; y++) {
				for (int z = 0; z < labeledImage[x][y].length; z++) {
					int label = labeledImage[x][y][z];
					if (label > 0) {
						if (cellCentroids[label-1] == null) {
							cellCentroids[label-1] = Point3D.ORIGIN;
						}
						cellCentroids[label-1] = cellCentroids[label-1].translateBy(new Point3D(x, y, z));
						cellVolumes[label-1]++;
					} else if (label < 0) {
						throw new IllegalArgumentException("Labeled image may only use non-negative grayscale values!");
					}
				}
			}
		}
		for (int i = 0; i < labelCount; i++) {
			if (cellVolumes[i] > 0) {
				cellCentroids[i] = cellCentroids[i].scaleBy(1.0/cellVolumes[i]);
			}
		}
		System.out.println(" done.");
		
		// prepare data structure for voxels touching two cells
		HashMap<Integer,HashMap<Integer,ArrayList<Point3D>>> adjacentVoxels = new HashMap<Integer,HashMap<Integer,ArrayList<Point3D>>>();
		for (int i = 0; i < labelCount; i++) {
			adjacentVoxels.put(i, new HashMap<Integer,ArrayList<Point3D>>());
		}
		for (int i = 0; i < labelCount; i++) {
			for (int j = i+1; j < labelCount; j++) {
				if (cellVolumes[i] > 0 && cellVolumes[j] > 0) {
					ArrayList<Point3D> voxels = new ArrayList<Point3D>();
					adjacentVoxels.get(i).put(j, voxels);
					adjacentVoxels.get(j).put(i, voxels);
				}
			}
		}
		
		// determine voxels between regions
		System.out.print("Determining voxels touching two regions...");
		for (int x = 0; x < labeledImage.length; x++) {
			for (int y = 0; y < labeledImage[x].length; y++) {
				for (int z = 0; z < labeledImage[x][y].length; z++) {
					ArrayList<Integer> labels = new ArrayList<Integer>();
					
					for (int dx = -1; dx <= 1; dx++) {
						for (int dy = -1; dy <= 1; dy++) {
							for (int dz = -1; dz <= 1; dz++) {
								if (!Array3D.valid(x+dx, y+dy, z+dz, labeledImage)) {
									continue;
								}
								int neighborLabel = labeledImage[x+dx][y+dy][z+dz];
								if (neighborLabel > 0 && !labels.contains(neighborLabel)) {
									labels.add(neighborLabel);
								}
							}
						}
					}
					
					if (labels.size() == 2) {
						adjacentVoxels.get(labels.get(0)-1).get(labels.get(1)-1).add(new Point3D(x, y, z));
					}
				}
			}
		}
		System.out.println(" done.");
		
		// prepare test points data structure for pairs of cells (i.e., adjacent cells)
		ArrayList<ArrayList<ArrayList<Point3D>>> testPoints = new ArrayList<ArrayList<ArrayList<Point3D>>>(labelCount);
		for (int i = 0; i < labelCount; i++) {
			ArrayList<ArrayList<Point3D>> l = new ArrayList<ArrayList<Point3D>>(labelCount);
			for (int j = 0; j < labelCount; j++) {
				l.add(null);
			}
			testPoints.add(l);
		}
		int testPointsCount = 0;
		
		// fill test points data structure
		System.out.print("Determining test points...");
		for (int i = 0; i < labelCount; i++) {
			for (int j = i+1; j < labelCount; j++) {
				ArrayList<Point3D> voxelCoordinates = adjacentVoxels.get(i).get(j);
				
				if (voxelCoordinates != null && !voxelCoordinates.isEmpty()) {
					ArrayList<Point3D> l = testPoints.get(i).get(j);
					if (l == null) {
						l = new ArrayList<Point3D>();
						testPoints.get(i).set(j, l);
						testPoints.get(j).set(i, l);
					}
					
					// select test points based on an orthogonal regression of a plane, on which a circle is placed
					OrthogonalRegression3D regression = new OrthogonalRegression3D(voxelCoordinates);
					Point3D centroidPoint = regression.getCentroid();
					if (numberOfTestPoints > 1) {
						try {
							Plane3D plane = regression.fitPlane(0.5);
							if (plane == null) {
								// ignore, use only centroid...
							} else {
								Rotation3D rotation = Utilities3D.fromToRotation(new Point3D(0.0, 0.0, 1.0), plane.getNormalVec());
								
								double radius = Math.sqrt(voxelCoordinates.size())/4.0;
								for (int k = 0; k < numberOfTestPoints; k++) {
									double phi = 2.0*Math.PI/numberOfTestPoints*k;
									double x = radius*Math.cos(phi);
									double y = radius*Math.sin(phi);
									Point3D point = rotation.applyTo(new Point3D(x, y, 0.0)).translateBy(centroidPoint);
									l.add(point);
									testPointsCount++;
								}
							}
						} catch (IllegalArgumentException e) {
							// ignore, use only centroid...
						}
						
						// fallback: use centroid
						while (l.size() < numberOfTestPoints) {
							l.add(centroidPoint);
							testPointsCount++;
						}
					} else {
						// only one test point: always use centroid
						l.add(centroidPoint);
						testPointsCount++;
					}
				}
			}
		}
		System.out.println(" done.");
		System.out.println("Total number of test points: "+testPointsCount);
		
		// check for cells without any test points
		for (int i = 0; i < labelCount; i++) {
			if (cellVolumes[i] > 0) {
				int testPointsForCell = 0;
				for (int j = 0; j < labelCount; j++) {
					ArrayList<Point3D> testPointsForAdjacency = testPoints.get(i).get(j);
					if (testPointsForAdjacency != null) {
						testPointsForCell += testPointsForAdjacency.size();
					}
				}
				
				if (testPointsForCell == 0) {
					System.out.println("Warning: region with label "+(i+1)+" is not directly adjacent to other cells, it is ignored.");
					cellCentroids[i] = null;
					cellVolumes[i] = 0;
				}
			}
		}
		
		if (testPointsCount <= 0) {
			throw new RuntimeException("No test points detected, aborting.");
		}
		
		System.out.println("");
		System.out.println("Date & time (start): "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		System.out.println("");
		
		ArrayList<MarkedPoint3D<Double>> result = CEMethod.findOptimalGenerators(cellCentroids, cellVolumes, ceParameters, new CostFunction(testPoints));
		
		System.out.println("");
		System.out.println("Date & time (end): "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		System.out.println("");
		
		return result;
	}
	
	/**
	 * Writes all generators to a simple text file.
	 * 
	 * @param generators  the generators, where the first entry of the array
	 *                    corresponds to the cell with label one, and so on
	 * @param fileName    the file name of the text file
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeGeneratorsToFile(ArrayList<MarkedPoint3D<Double>> generators, String fileName) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
			for (int i = 0; i < generators.size(); i++) {
				MarkedPoint3D<Double> markedPoint = generators.get(i);
				out.write((i+1)+" ");
				if (markedPoint != null) {
					out.write(markedPoint.getCoordinates()[0]+" "+markedPoint.getCoordinates()[1]+" "+markedPoint.getCoordinates()[2]+" "+markedPoint.getMark().doubleValue());
				} else {
					out.write("null");
				}
				out.newLine();
			}
		}
	}
	
}