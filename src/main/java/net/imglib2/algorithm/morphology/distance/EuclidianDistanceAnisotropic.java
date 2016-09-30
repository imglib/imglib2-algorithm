package net.imglib2.algorithm.morphology.distance;

import java.util.Arrays;

public class EuclidianDistanceAnisotropic implements Distance
{

	private final double[] weights;

	private final double[] oneOverTwoTimesWeights;

	/**
	 * Weights equals weight * square of pixel size (TODO formulate more
	 * nicely!)
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
