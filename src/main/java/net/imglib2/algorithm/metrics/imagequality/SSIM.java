package net.imglib2.algorithm.metrics.imagequality;

import java.util.Arrays;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.fast_gauss.FastGauss;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public class SSIM
{
	private final static int Z_AXIS = 2;

	public final static double K1 = 0.01;

	public final static double K2 = 0.03;

	public enum Filter
	{
		GAUSS,
		FASTGAUSS
	}

	// TODO unit test for methods
	// TODO javadoc
	// TODO 2D case vs 3D?
	// TODO how to enforce positive images
	public static < T extends RealType< T > > double computeMetrics(
			final RandomAccessibleInterval< T > reference,
			final RandomAccessibleInterval< T > processed,
			final Filter filter,
			final double sigma )
	{
		if ( !Arrays.equals( reference.dimensionsAsLongArray(), processed.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// get image range
		final double range = reference.randomAccess().get().getMaxValue();

		// calculate filter half-size
		int edge;
		if ( Filter.FASTGAUSS.equals( filter ) )
			edge = Math.max( 2, ( int ) ( 3 * sigma + 1 ) );
		else
			edge = Math.max( 2, ( int ) ( 3 * sigma + 0.5 ) );

		// convert images to double
		final RandomAccessibleInterval< DoubleType > refIm = Converters.convert( reference, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );
		final RandomAccessibleInterval< DoubleType > procIm = Converters.convert( processed, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );

		// create (weighted) means, variances and covariance
		final RandomAccessibleInterval< DoubleType > ux = computeWeightedMean( filter, sigma, refIm );
		final RandomAccessibleInterval< DoubleType > uy = computeWeightedMean( filter, sigma, procIm );
		final RandomAccessibleInterval< DoubleType > vx = computeWeightedVariance( filter, sigma, refIm, ux );
		final RandomAccessibleInterval< DoubleType > vy = computeWeightedVariance( filter, sigma, procIm, uy );
		final RandomAccessibleInterval< DoubleType > vxy = computeWeightedCovariance( filter, sigma, refIm, procIm, ux, uy );

		// compute SSIM image
		final RandomAccessibleInterval< DoubleType > S = computeSSIM( ux, uy, vx, vy, vxy, range );

		return computeMeanSSIM( S, edge );
	}

	// TODO there should be a more elegant way to create the empty images
	private static RandomAccessibleInterval< DoubleType > createRAI(
			RandomAccessibleInterval< DoubleType > input )
	{
		return Util.getSuitableImgFactory( input, Util.getTypeFromInterval( input ) ).create( input );
	}

	private static void filter(
			Filter filter,
			double sigma,
			ExtendedRandomAccessibleInterval< DoubleType, RandomAccessibleInterval< DoubleType > > input,
			RandomAccessibleInterval< DoubleType > output )
	{
		if ( Filter.FASTGAUSS.equals( filter ) )
			FastGauss.convolve( sigma, input, output );
		else
			Gauss3.gauss( sigma, input, output );
	}

	private static RandomAccessibleInterval< DoubleType > computeWeightedMean(
			Filter filter,
			double sigma,
			RandomAccessibleInterval< DoubleType > input )
	{
		ExtendedRandomAccessibleInterval< DoubleType, RandomAccessibleInterval< DoubleType > > extInput = Views.extendMirrorDouble( input );
		RandomAccessibleInterval< DoubleType > output = createRAI( input );
		filter( filter, sigma, extInput, output );
		return output;
	}

	private static RandomAccessibleInterval< DoubleType > computeWeightedVariance(
			Filter filter,
			double sigma,
			RandomAccessibleInterval< DoubleType > img,
			RandomAccessibleInterval< DoubleType > weightedMean )
	{
		// compute the square image
		RandomAccessibleInterval< DoubleType > square = createRAI( img );
		LoopBuilder.setImages( img, square ).forEachPixel( ( i, o ) -> o.set( i.get() * i.get() ) );

		// calculate weighted mean
		ExtendedRandomAccessibleInterval< DoubleType, RandomAccessibleInterval< DoubleType > > extendedImg = Views.extendMirrorDouble( square );
		RandomAccessibleInterval< DoubleType > meanSquare = createRAI( img );
		filter( filter, sigma, extendedImg, meanSquare );

		// compute variance
		LoopBuilder.setImages( meanSquare, weightedMean ).forEachPixel( ( v, u ) -> v.set( ( v.get() - u.get() * u.get() ) ) );

		return meanSquare;
	}

	private static RandomAccessibleInterval< DoubleType > computeWeightedCovariance(
			Filter filter,
			double sigma,
			RandomAccessibleInterval< DoubleType > im1,
			RandomAccessibleInterval< DoubleType > im2,
			RandomAccessibleInterval< DoubleType > weightedMean1,
			RandomAccessibleInterval< DoubleType > weightedMean2 )
	{
		// compute the product images
		RandomAccessibleInterval< DoubleType > product = createRAI( im1 );
		LoopBuilder.setImages( im1, im2, product ).forEachPixel( ( i1, i2, o ) -> o.set( i1.get() * i2.get() ) );

		// calculate weighted mean
		ExtendedRandomAccessibleInterval< DoubleType, RandomAccessibleInterval< DoubleType > > extendedImg = Views.extendMirrorDouble( product );
		RandomAccessibleInterval< DoubleType > meanProduct = createRAI( im1 );
		filter( filter, sigma, extendedImg, meanProduct );

		// compute covariance
		LoopBuilder.setImages( meanProduct, weightedMean1, weightedMean2 ).forEachPixel( ( v, u1, u2 ) -> v.set( ( v.get() - u1.get() * u2.get() ) ) );

		return meanProduct;
	}

	private static RandomAccessibleInterval< DoubleType > computeSSIM(
			RandomAccessibleInterval< DoubleType > ux,
			RandomAccessibleInterval< DoubleType > uy,
			RandomAccessibleInterval< DoubleType > vx,
			RandomAccessibleInterval< DoubleType > vy,
			RandomAccessibleInterval< DoubleType > vxy,
			double range
	)
	{
		final double C1 = K1 * K1 * range * range;
		final double C2 = K2 * K2 * range * range;

		// compute intermediate images
		final RandomAccessibleInterval< DoubleType > A1 = createRAI( ux );
		LoopBuilder.setImages( ux, uy, A1 ).forEachPixel( ( u1, u2, o ) -> o.set( 2 * u1.get() * u2.get() + C1 ) );

		final RandomAccessibleInterval< DoubleType > A2 = createRAI( ux );
		LoopBuilder.setImages( vxy, A2 ).forEachPixel( ( v, o ) -> o.set( 2 * v.get() + C2 ) );

		final RandomAccessibleInterval< DoubleType > B1 = createRAI( ux );
		LoopBuilder.setImages( ux, uy, B1 ).forEachPixel( ( u1, u2, o ) -> o.set( u1.get() * u1.get() + u2.get() * u2.get() + C1 ) );

		final RandomAccessibleInterval< DoubleType > B2 = createRAI( ux );
		LoopBuilder.setImages( vx, vy, B2 ).forEachPixel( ( v1, v2, o ) -> o.set( v1.get() + v2.get() + C2 ) );

		// SSIM image
		final RandomAccessibleInterval< DoubleType > S = createRAI( ux );
		LoopBuilder.setImages( A1, A2, B1, B2, S ).forEachPixel( ( a1, a2, b1, b2, s ) -> s.set( a1.get() * a2.get() / b1.get() / b2.get() ) );

		return S;
	}

	// TODO refactor
	private static Double computeMeanSSIM(
			RandomAccessibleInterval< DoubleType > S,
			int filterSize )
	{
		long[] dims = S.dimensionsAsLongArray();

		RandomAccessibleInterval< DoubleType > finalS;
		if ( dims.length > Z_AXIS && dims[ Z_AXIS ] > 1 )
		{
			finalS = Views.interval( S, Intervals.createMinMax( filterSize, filterSize, filterSize,
					S.max( 0 ) - filterSize, S.max( 1 ) - filterSize, S.max( 2 ) - filterSize ) );
		}
		else
		{
			finalS = Views.interval( S, Intervals.createMinMax( filterSize, filterSize,
					S.max( 0 ) - filterSize, S.max( 1 ) - filterSize ) );
		}

		double mssim = 0.;
		int counter = 0;
		for ( DoubleType d : Views.iterable( finalS ) )
		{
			mssim += d.get();
			counter++;
		}

		return mssim / counter;
	}
}
