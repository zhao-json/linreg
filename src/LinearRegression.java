import java.lang.Math;
import java.util.Arrays;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Performs either simple, weighted, or robust linear regression on a dataset of
 * x, y values. If dataset has non-distinct x-values, then only simple linear
 * regression is valid!
 * 
 * Resources for learning more: SLR:
 * https://en.wikipedia.org/wiki/Simple_linear_regression WLR:
 * https://en.wikipedia.org/wiki/Least_squares#Weighted_least_squares RLR:
 * https://en.wikipedia.org/wiki/Theil%E2%80%93Sen_estimator
 * 
 * @author Isaac Rozen
 * @author Jason Zhao
 */
public class LinearRegression {

	// Sample statistics are stored in this array
	// Ordering goes like this: Beta, Alpha, r^2, avgx, avgy, stdx, stdy
	// The regression line is given by y = alpha + beta(x)
	int statSize = 7;
	int squared = 2; // squaring a value

	double[] SampleStats = new double[statSize];

	/**
	 * Weighted linear regression, edits SampleStats array with values of: beta,
	 * alpha, r^2, avgx, avgy, stdx, stdy.
	 * 
	 * Recall that the regression line is given by y = alpha + beta(x).
	 * 
	 * Sample usage for plotting SLR data:
	 * 
	 * regression.SLR(dataset); points = regression.plotLine(dataset);
	 * 
	 * @param dataset
	 *          Dataset of x, y.
	 * @param weights
	 *          Array of weights corresponding to each x,y.
	 */
	public void WLR(double[][] dataset, double[] weights) {

		int sampleSize = dataset[0].length;

		double xsum = 0, ysum = 0;
		double xresidsqrsum = 0;
		double yresidsqrsum = 0;
		double varx, vary;
		double xwsum = 0, ywsum = 0, wsum = 0;
		double xwsqrsum = 0, xywsum = 0;

		double alpha = 0; // intercept of least squares line
		double beta = 0; // slope of least squares line
		double r = 0; // sample correlation coeff of least squares line
		double xbar, ybar; // sample means of x, y
		double stdx, stdy; // sample standard deviations of x, y

		// The following section computes unweighted sample statistics.
		// These are used to calculate means, generate standard deviations.
		for (int i = 0; i < sampleSize; i++) {
			xsum += dataset[0][i];
			ysum += dataset[1][i];
		}

		xbar = xsum / (sampleSize);
		ybar = ysum / (sampleSize);

		for (int i = 0; i < sampleSize; i++) {
			double xresid = dataset[0][i] - xbar;
			double yresid = dataset[1][i] - ybar;

			xresidsqrsum += Math.pow(xresid, squared);
			yresidsqrsum += Math.pow(yresid, squared);
		}

		varx = xresidsqrsum / (sampleSize - 1);
		vary = yresidsqrsum / (sampleSize - 1);

		stdx = Math.sqrt(varx);
		stdy = Math.sqrt(vary);

		// The following section computes weighted sample statistics.
		// If all weights are unity, then these calculations are similar to above.
		for (int i = 0; i < sampleSize; i++) {
			xwsum += (dataset[0][i] * weights[i]);
			ywsum += (dataset[1][i] * weights[i]);
			wsum += weights[i];
			xwsqrsum += (Math.pow(dataset[0][i], squared) * weights[i]);
			xywsum += (dataset[0][i] * dataset[1][i] * weights[i]);
		}

		double D = (wsum * xwsqrsum) - Math.pow(xwsum, squared);

		// Calculating the intercept, slope, and correlation for the weighted
		// least squares line. If all weights are unity, then this is the same as
		// calculating the simple least squares line.
		alpha = ((xwsqrsum * ywsum) - (xwsum * xywsum)) / D;
		beta = ((wsum * xywsum) - (xwsum * ywsum)) / D;
		r = beta * (stdx / stdy);

		// Write out to SampleStats array
	
		// numeric representations of stats element
		int betan = 0;
		int alphan = 1;
		int rSquaren = 2;
		int avgxn = 3;
		int avgyn = 4;
		int stdxn = 5;
		int stdyn = 6;
		
		SampleStats[betan] = beta;
		SampleStats[alphan] = alpha;
		SampleStats[rSquaren] = Math.pow(r, squared);
		SampleStats[avgxn] = xbar;
		SampleStats[avgyn] = ybar;
		SampleStats[stdxn] = stdx;
		SampleStats[stdyn] = stdy;
	}

	/**
	 * Simple linear regression, edits SampleStats array with values of: beta,
	 * alpha, r^2, avgx, avgy, stdx, stdy. Essentially feeds into WLR, using unity
	 * for weights.
	 * 
	 * Recall that the regression line is given by y = alpha + beta(x)
	 * 
	 * @param dataset
	 *          Dataset of x, y.
	 */
	public void SLR(double[][] dataset) {
		double[] weights = new double[(dataset[0].length)];

		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1.0;
		}

		WLR(dataset, weights);
	}

	/**
	 * Robust linear regression, edits SampleStats array with values of beta and
	 * alpha. Leaves r^2, avgx, avgy, stdx, stdy unchanged from SLR.
	 * 
	 * Recall that the robust regression line is given by y = alpha + beta(x)
	 * 
	 * Note, this algorithm ONLY works if the x-predictors are unique!
	 * 
	 * @param dataset
	 *          Dataset of x, y.
	 */
	public void RLR(double[][] dataset) {

		double alpha, beta;

		int sampleSize = dataset[0].length;
		double[] intercepts = new double[sampleSize];

		// The number of possible slopes from unique pairs of x, y is given by:
		// n = (sampleSize choose 2). This calculation can overflow for small n
		// if we only use factorials, so we directly calculate the binom. coeff.
		int n = binomial(sampleSize, squared);
		double[] slopes = new double[n];

		// Iterate through all pairs of (x1, y1) and (x2, y2).
		// Store the slope of the line between these points in the slopes array.
		// We use index to keep track of where we are in the large slope array.
		int index = 0;

		for (int i = 0; i < sampleSize; i++) {
			for (int j = i + 1; j < sampleSize; j++) {
				double yi = dataset[1][i];
				double yj = dataset[1][j];

				double xi = dataset[0][i];
				double xj = dataset[0][j];

				double slope = (yj - yi) / (xj - xi);
				slopes[index] = slope;
				index++;
			}
		}

		// Find median of slopes; set equal to beta.
		Arrays.sort(slopes); // Sort slopes array in ascending order.
		if (n % squared == 1) {
			beta = slopes[(n / squared)];
		} else {
			beta = (slopes[(n / squared) - 1] + slopes[(n / squared)]) / squared;
		}

		// Compute all possible intercepts of xi, yi values with beta
		for (int i = 0; i < sampleSize; i++) {
			intercepts[i] = dataset[1][i] - (beta * dataset[0][i]);
		}

		// Find median of intercepts; set equal to alpha
		Arrays.sort(intercepts);
		if (sampleSize % squared == 1) {
			alpha = intercepts[(sampleSize / squared)];
		} else {
			alpha = (intercepts[(sampleSize / squared) - 1] + intercepts[(sampleSize / squared)]) / squared;
		}

		// Call SLR to generate appropriate sample statistics, then we overwrite
		// these.
		SLR(dataset);

		SampleStats[0] = beta; // Slope of RLR
		SampleStats[1] = alpha; // Intercept of RLR
	}

	/**
	 * Computes binomial coefficient
	 * 
	 * @param n
	 *          - pop size
	 * @param k
	 *          - sample size
	 * @return n Choose k
	 */
	private int binomial(int n, int k) {
		if (k > n - k) {
			k = n - k;
		}

		int b = 1;

		for (int i = 1, m = n; i <= k; i++, m--) {
			b *= (m / i);
		}

		return b;
	}

	/**
	 * @return Gives regression slope
	 */
	public double getSlope() {
		return SampleStats[0];
	}

	/**
	 * @return Gives regression intercept
	 */
	public double getIntercept() {
		return SampleStats[1];
	}

	/**
	 * @param dataset
	 *          Dataset of x, y.
	 * @return 2x2 array of points corresponding to the first and last predicted
	 *         values, and their corresponding predictor x-values. The line
	 *         between these two points is the linear regression line.
	 */
	public double[][] plotLine(double[][] dataset) {

		int sampleSize = dataset[0].length;
		int dimension = 2; // 2 by 2 array
		double[][] points = new double[dimension][dimension];

		xsort(dataset);

		points[0][0] = dataset[0][0];
		points[0][1] = dataset[0][sampleSize - 1];

		points[1][0] = points[0][0] * getSlope() + getIntercept();
		points[1][1] = points[1][0] * getSlope() + getIntercept();

		return points;
	}

	/**
	 * Sorts an array in ascending order based on x-values
	 * 
	 * @param array
	 *          2D array to sort. Components must be equal length
	 */
	public void xsort(double[][] array) {

		int arraySize = array[0].length;

		for (int i = 0; i < arraySize; i++) {
			for (int j = i; j < arraySize; j++) {
				// Swap positions if lower
				if (array[0][j] < array[0][i]) {
					double tempx = array[0][i];
					double tempy = array[1][i];

					array[0][i] = array[0][j];
					array[1][i] = array[1][j];

					array[0][j] = tempx;
					array[1][j] = tempy;
				}
			}
		}
		for (int i = 0; i < arraySize - 1; i++) {
			if (array[0][i] == array[0][i + 1]) {
				// generates Alert for error detection
				final Alert error = new Alert(AlertType.ERROR);
				error.setTitle("Error");
				error.setHeaderText("Error Found!");

				// handles where not all entities are fulfilled
				error.setContentText("Duplicate X values! "
						+ "Cannot perform regression!");

				error.showAndWait();
				System.exit(0);
			}
		}
	}
}
