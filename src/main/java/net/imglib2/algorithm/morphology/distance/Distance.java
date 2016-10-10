package net.imglib2.algorithm.morphology.distance;

/**
 * Family of strictly convex, real valued functions that are separable in all
 * dimension. The interface thus specifies just a one-dimensional function that
 * is parameterized by an offset along both the x- and the y-axis. These
 * parameters are passed at evaluation along with the dimension.
 *
 * Two distinct members of the same family, d = f(x) and d' = f(x - x0) + y0,
 * must have exactly one intersection point (for each dimension): |{ x : f(x) =
 * f(x -x0) + y0 }| = 1
 *
 * This interface is used in {@link DistanceTransform}:
 *
 * D( p ) = min_q f(q) + d(p,q) where p,q are points on a grid/image.
 *
 * @author Philipp Hanslovsky
 *
 */
public interface Distance
{

	/**
	 * Evaluate function with family parametrs xShift and yShift at position x
	 * in dimension dim.
	 */
	double evaluate( double x, double xShift, double yShift, int dim );

	/**
	 *
	 * Determine the intersection point in dimension dim of two members of the
	 * function family. The members are parameterized by xShift1, yShift1,
	 * xShift2, yShift2.
	 *
	 * xShift1 < xShift2
	 */
	double intersect( double xShift1, double yShift1, double xShift2, double yShift2, int dim );

}
