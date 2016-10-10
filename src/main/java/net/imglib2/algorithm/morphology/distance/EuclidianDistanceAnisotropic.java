package net.imglib2.algorithm.morphology.distance;

import java.util.Arrays;

/**
 *
 * Implementation of weighted anisotropic Euclidian distance:
 *
 * D( p ) = min_q f(q) + sum_i w_i*(p_i - q_i)*(p_i - q_i).
 *
 * @author Philipp Hanslovsky
 *
 */
public class EuclidianDistanceAnisotropic implements Distance
{

	private final double[] weights;

	private final double[] oneOverTwoTimesWeights;

	/**
	 * When accounting for anisotropic image data, the ratios of the weights
	 * should be equal to the squared ratios of the voxel sizes, e.g. if voxel
	 * is twice as long along the y-axis, the weight for the y-axis should be
	 * four times as big as the weight for the x-axis.
	 *
	 */
	public EuclidianDistanceAnisotropic( final double... weights )
	{
		super();
		this.weights = weights;
		this.oneOverTwoTimesWeights = createOneOverTwoTimesWeights( weights );
	}

	public EuclidianDistanceAnisotropic( final int nDim, final double weight )
	{
		this( create( nDim, weight ) );
	}

	private static double[] createOneOverTwoTimesWeights( final double[] weights )
	{
		final double[] result = new double[ weights.length ];
		for ( int i = 0; i < result.length; ++i )
			result[ i ] = 0.5 / weights[ i ];
		return result;
	}

	private static final double[] create( final int n, final double val )
	{
		final double[] arr = new double[ n ];
		Arrays.fill( arr, val );
		return arr;
	}

	@Override
	public double evaluate( final double x, final double xShift, final double yShift, final int dim )
	{
		final double diff = x - xShift;
		return weights[ dim ] * diff * diff + yShift;
	}

	@Override
	public double intersect( final double xShift1, final double yShift1, final double xShift2, final double yShift2, final int dim )
	{
		final double a = weights[ dim ];
		return oneOverTwoTimesWeights[ dim ] * ( a * xShift2 * xShift2 + yShift2 - ( a * xShift1 * xShift1 + yShift1 ) ) / ( xShift2 - xShift1 );
	}

}
