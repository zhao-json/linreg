import java.lang.Math;
import java.util.Arrays;

/**
 * Performs local regression using the the LOESS method
 * See: http://www.itl.nist.gov/div898/handbook/pmd/section1/pmd144.htm
 * Also see: https://en.wikipedia.org/wiki/Local_regression
 * 
 * @author Isaac Rozen
 */
public class LocalRegression extends LinearRegression {
	
	int weight = 2; // third column, second index

	/**
	 * LOESS fits linear least squares lines to local subsets of the data.
	 * This approach is useful for non-parametric regression, as it requires
	 * no assumptions about the actual function.
	 * 
	 * Sample usage for plotting LOESS data:
	 * 
	 * intSlope = regression.LOESS(dataset, q);
	 * points = regression.plotLOESS(dataset, intSlope);
	 * 
	 * @param dataset Dataset of x,y. Assumed unique x per y.
	 * @param q Fraction of dataset to use per subset.
	 * eg if we have 20 points, and q = 0.25, then each subset contains 5 points.
	 * If the subset size is less than 4, it get set to 4.
	 * This prevents some misbehavior for small subsets of three or fewer points.
	 * 
	 * @return Array of slope/intercept pairs from the sorted dataset.
	 */
	public double[][] LOESS(double[][] dataset, double q) {
		
		// generate sample statistics, the slope, int, r^2 are irrelevant
		// but the others are useful
		super.SLR(dataset);
		
		int loopnum = 1;
		
		// Calculate subset size
		int sampleSize = dataset[0].length;
		int subsetSize = (int)Math.ceil(q * sampleSize);
		
		int minPoint = 4; //must be greater than 4 
		if (subsetSize < minPoint) {
			subsetSize = minPoint;
		}
		
		// Sort the dataset according to the x-values.
		xsort(dataset);
		
		//Initialize arrays of subset weights and data values.
		double[] subsetWeights = new double[subsetSize];
		double[][] subsetDataset = new double[weight][subsetSize];
		
		// Store subset statistics in here as a pair of slopes and intercepts.
		double[][] slopeInterceptSet = new double[weight][sampleSize];
		
		// Form the initial subset to sample from
		// Each inner loop iteration selects consecutive x-values to set as center
		// This center value is the point around which the subset weights are made
		// Then, weighted lin regression is performed on this set
		//
		// EXAMPLE: If we have a subset of 4 points ABCD, from dataset ABCDE:
		// Then sample 3x using A, B, and C as consecutive centers of that subset
		for (int i = 0; i < subsetSize - 1; i++) {
			for (int j = 0; j < subsetSize; j++) {
				subsetDataset[0][j] = dataset[0][j];
				subsetDataset[1][j] = dataset[1][j];
			}
			
			calcWeight(subsetDataset[0], subsetWeights, i);
			
			WLR(subsetDataset, subsetWeights);
			slopeInterceptSet[0][i] = getSlope();
			slopeInterceptSet[1][i] = getIntercept();
		}
		
		// After the initial sampling, we increment the sampling indices
		// Here, the center is always the second-to-last index
		// This may lead to slight skewing, but other approaches are arbitrary.
		//
		// EXAMPLE: after using A B C as centers, we have to sample D, E.
		// So subset here is chosen as BCDE, using D as center.
		// Then, we use BCDE again but with E as the center.
		for (int i = subsetSize - 1; i < sampleSize; ++i) {
			
			// Loopnum is just a running index from 1 to check for end of dataset.
			// Kind of a kludge, it just works!
			if ((loopnum + subsetSize) <= sampleSize) {
				for (int j = 0; j < subsetSize; j++) {
					subsetDataset[0][j] = dataset[0][j + loopnum];
					subsetDataset[1][j] = dataset[1][j + loopnum];
				}

				calcWeight(subsetDataset[0], subsetWeights, subsetSize - weight);
				
				WLR(subsetDataset, subsetWeights);
				slopeInterceptSet[0][i] = getSlope();
				slopeInterceptSet[1][i] = getIntercept();
				loopnum++;
			}
			
			// This else block runs only when we reach the end of the dataset.
			// It runs similarly to the initial sampling period, except using the
			// fixed subset of the final couple points.
			else {
				for (int j = sampleSize - 1, k = subsetSize - 1; j >= sampleSize-subsetSize; 
					j--, k--) {
					subsetDataset[0][k] = dataset[0][j];
					subsetDataset[1][k] = dataset[1][j];
				}
				
				if (i == sampleSize - 1) {
					calcWeight(subsetDataset[0], subsetWeights, subsetSize - 1);
				}
				else {
					calcWeight(subsetDataset[0], subsetWeights, subsetSize - weight);
				}
				
				WLR(subsetDataset, subsetWeights);
				slopeInterceptSet[0][i] = getSlope();
				slopeInterceptSet[1][i] = getIntercept();
			}
		}
		
		// All pairs of slopes and intercepts are stored in this array of doubles!
		return slopeInterceptSet;
	}
	
	/**
	 * Plot the LOESS line. Requires slope/intercept pair from LOESS method.
	 * 
	 * @param dataset Dataset of x, y. Not necessarily sorted.
	 * @param slopeInterceptSet Set of slope/intercept pairs, use the LOESS
	 * method to obtain these!
	 * 
	 * @return Array of points corresponding to predictor x-values and their
	 * corresponding predicted y-values from the LOESS slope-intercept pairs.
	 * Plotting the lines between consecutive points yields the LOESS line.
	 */
	public double[][] plotLOESS(double[][] dataset, 
		double[][] slopeInterceptSet) {

		int sampleSize = dataset[0].length;
		double[][] points = new double[weight][sampleSize];
		
		xsort(dataset);
		
		for (int i = 0; i < sampleSize; i++) {
			double slope = slopeInterceptSet[0][i];
			double intercept = slopeInterceptSet[1][i];
			
			// Each pair is the predictor x and the predicted y according to
			// the intercept/slope pair.
			points[0][i] = dataset[0][i];
			points[1][i] = points[0][i] * slope + intercept;
		}
				
		return points;
	}
	
	/**
	 * Modifies an array of weights according to a weight function based around
	 * a center index from a subset of x-values.
	 * 
	 * @param subsetx Subset of x-values
	 * @param weights Set of corresponding weights for the x-values
	 * @param centerIndex Index to center weights around
	 */
	private void calcWeight(double[] subsetx, double[] weights, 
		int centerIndex) {
		
		int arraySize = weights.length;
		
		double[] distances = new double[arraySize];
		double[] scaledDistances = new double[arraySize];
		
		// Calculate absolute distance of each x-value from the central x-val
		for (int i = 0; i < arraySize; i++) {
			double distance = Math.abs(subsetx[i] - subsetx[centerIndex]);
			distances[i] = distance;
		}
		
		scaledDistances = Arrays.copyOf(distances, arraySize);

		// Sort the distances, find max value.
		Arrays.sort(distances);
		double maxdist = distances[arraySize - 1];
		
		// Scale distances from the maximum value.
		// Then calculate the weights according to these scaled distances.
		for (int i = 0; i < arraySize; i++) {
			scaledDistances[i] = scaledDistances[i] / maxdist;
			weights[i] = weightFn(scaledDistances[i]);
		}
	}
	
	/**
	 * @param x Scaled distance (assumed non-negative)
	 * @return Weight given by tricube weight function. If dist >=1, return 0.
	 */
	private double weightFn(double x) {
		double cubed = 3.0;
		int cube = 3;
		double b = 0;
		
		if (x >= 1) {
		}
		else {
			b = Math.pow(1 - Math.pow(x, cubed), cube); // Tricube weight function
		}
		
		return b;
	}
	
}
