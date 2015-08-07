package laguerre_approximation_3d;

/**
 * All parameters required for the CE method to solve the Laguerre approximation
 * problem as described in the paper.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 * @see "A. Spettl, T. Brereton, Q. Duan, T. Werz, C.E. Krill III, D.P. Kroese
 *       and V. Schmidt, Fitting Laguerre tessellation approximations to tomographic
 *       image data. arXiv:1508.01341 [cond-mat.mtrl-sci]"
 */
public class CEParameters {
	
	/** Samples per iteration of the CE algorithm, e.g. <code>4000</code>. */
	public int M = 4000;
	
	/** Fraction of best samples to use for the elite set, e.g. <code>0.05</code>. */
	public double rho = 0.05;
	
	/** The number of iterations where the best cost values have to lie in the (small) interval; for variance injection. */
	public int tauInject = 10;
	
	/** The relative threshold of differences in best (elite set) cost values that is used for deciding when to inject variance (inactive if zero). */
	public double deltaInject = 0.05;
	
	/** The maximum number of variance injections that are performed (inactive if zero, unlimited if negative). */
	public int injectMax = -1;
	
	/** If we want to inject: the percentage of the best (elite set) cost value before the previous injection. If the current current best (elite set) cost value is larger, then we do not perform injections any more. */
	public double gamma = 0.9;
	
	/** Parameter of variance injection: the factor that is used w.r.t. the mean squared cost for the current cell. */
	public double kappa = 0.25;
	
	/** The number of iterations where the best cost values have to lie in the (small) interval; for termination criterion. */
	public int tauTerminate = 10;
	
	/** The relative threshold of differences in best (elite set) cost values that is used for deciding when to abort the CE method. */
	public double deltaTerminate = 0.01;
	
}