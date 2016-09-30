package net.imglib2.algorithm.morphology.distance;

/**
 * Strictly convex, real valued function that is separable in the dimensions of
 * its domain.
 *
 * Two distances d = f(x) and d' = f(x - x0) + y0, must have exactly one
 * intersection point (for each dimension): |{ x : f(x) = f(x -x0) + y0 }| = 1
 *
 * @author Philipp Hanslovsky
 *
 *
 *
 */
public interface Distance
{

	/**
	 * Evaluate the distance at position x, centered at xShift and shifted by
	 * yShift, along dimension specified by dim.
	 */
	double evaluate( double x, double xShift, double yShift, int dim );

	/**
	 *
	 * Determine the intersection point in dimension specified by dim between to
	 * distances that are shifted and offset.
	 *
	 * xShift1 < xShift2 (true?)
	 */
	double intersect( double xShift1, double yShift1, double xShift2, double yShift2, int dim );

}
