package net.imglib2.algorithm.metrics.imagequality;

import java.util.Arrays;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.fast_gauss.FastGauss;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.RealSum;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Compute the structural similarity index measure (SSIM) between a reference and a processed image.
 * The SSIM is implemented here by using Gaussian weighting when computing the mean, variance and
 * covariance of small regions surrounding each pixel. The Gaussian filter standard deviation is
 * set as a parameter. The original publication used sigma = 1.5. The k1 and k2 SSIM parameters are
 * fixed, with K1 = 0.01 and K2 = 0.03.
 * <p>
 * The SSIM is run on 2D or 3D images. Because padding is applied to avoid edge effects in all
 * dimensions, the minimum dimension size must be big enough to prevent negative dimensions.
 * Depending on sigma, different algorithms are called to perform the Gaussian convolutions
 * (Gauss3 or FastGauss). Which yields the following constraints:
 * <p>
 * sigma > 2:  d > 2 * floor( 3.7210 * sigma + 0.20157 + 0.5 )
 * 0.5 <= sigma <= 2: d > 2 * floor( 3 * sigma + 0.5)
 * sigma < 0.5: d > 4
 * <p>
 * For sigma = 1.5, the minimum dimension depth is then 11.
 * <p>
 * Note that the SSIM metrics throws an exception if the images have singular dimensions for
 * the aforementioned reason.
 *
 * @author Joran Deschamps
 * @see <a href="https://ieeexplore.ieee.org/document/1284395">Original publication</a>
 * @see <a href="https://en.wikipedia.org/wiki/Structural_similarity">Wikipedia page</a>
 */
// TODO javadoc, expects at max 3 dimensions, no singular dimensions
public class SSIM
{
	public final static double K1 = 0.01;

	public final static double K2 = 0.03;

	private enum FilteringAlgorithm
	{
		GAUSS,
		FASTGAUSS
	}

	private static class Filter
	{
		private final FilteringAlgorithm algorithm;

		private final double sigma;

		private final int padding;

		public Filter( double sigma )
		{
			this.sigma = sigma;

			if ( sigma > 2 )
			{
				// FastGauss provides a faster implementation for large sigmas
				algorithm = FilteringAlgorithm.FASTGAUSS;

				// From FastGaussConvolverRealType
				padding = Math.max( 2, ( int ) ( 3.7210 * sigma + 0.20157 + 0.5 ) );
			}
			else
			{
				algorithm = FilteringAlgorithm.GAUSS;
				padding = Math.max( 2, ( int ) ( 3 * sigma + 0.5 ) );
			}
		}
	}

	/**
	 * Compute the SSIM metrics. The original publications used sigma = 1.5. The method expects
	 * 2D or 3D images, no singular dimensions, and a minimum depth for each dimension that
	 * allows padding on both sides.
	 *
	 * @param reference
	 * 		Reference image
	 * @param processed
	 * 		Processed image
	 * @param sigma
	 * 		Standard deviation of the Gaussian filter
	 * @param <T>
	 * 		Pixel type of the images
	 *
	 * @return SSIM metrics score
	 */
	public static < T extends RealType< T > > double computeMetrics(
			final RandomAccessibleInterval< T > reference,
			final RandomAccessibleInterval< T > processed,
			final double sigma )
	{
		if ( !Arrays.equals( reference.dimensionsAsLongArray(), processed.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		if ( reference.numDimensions() > 3 )
			throw new IllegalArgumentException( "2D or 3D images expected." );

		// get image range from the type
		final double range = reference.randomAccess().get().getMaxValue() - reference.randomAccess().get().getMinValue();

		// create filter
		Filter filter = new Filter( sigma );

		// sanity check on the dimensions: padding over all dimensions can cause negative dimensions
		for ( long d : reference.dimensionsAsLongArray() )
		{
			if ( d <= 2 * filter.padding )
			{
				throw new IllegalArgumentException( "Sigma = " + filter.sigma + " is not compatible with dimension of depth " + d +
						", minimum size: " + ( 2 * filter.padding + 1 ) + "." );
			}
		}

		// convert images to double
		final RandomAccessibleInterval< DoubleType > refIm = Converters.convert( reference, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );
		final RandomAccessibleInterval< DoubleType > procIm = Converters.convert( processed, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );

		// create (weighted) means, variances and covariance of the sliding windows
		final RandomAccessibleInterval< DoubleType > ux = computeWeightedMean( filter, refIm );
		final RandomAccessibleInterval< DoubleType > uy = computeWeightedMean( filter, procIm );
		final RandomAccessibleInterval< DoubleType > vx = computeWeightedVariance( filter, refIm, ux );
		final RandomAccessibleInterval< DoubleType > vy = computeWeightedVariance( filter, procIm, uy );
		final RandomAccessibleInterval< DoubleType > vxy = computeWeightedCovariance( filter, refIm, procIm, ux, uy );

		// compute SSIM image
		final RandomAccessibleInterval< DoubleType > S = computeSSIM( ux, uy, vx, vy, vxy, range );

		return computeMean( S );
	}

	/*
	 * Create a RandomAccessibleInterval using a suitable ImgFactory given the input, with
	 * a (negative) padding applied to all dimensions. The padding must be given as a
	 * positive value.
	 */
	private static RandomAccessibleInterval< DoubleType > createRAI(
			RandomAccessibleInterval< DoubleType > input,
			int padding )
	{
		final Interval cropped = Intervals.expand( input, -padding );
		final RandomAccessibleInterval< DoubleType > output = Util.getSuitableImgFactory( input, new DoubleType() ).create( cropped );

		// shift the coordinates of output to (padding, padding)
		final long[] translation = new long[ input.numDimensions() ];
		Arrays.fill( translation, padding );

		return Views.translate( output, translation );
	}

	/*
	 * Filter the input with a Gaussian filter, based on the {@code filter} parameter.
	 */
	private static void filter(
			Filter filter,
			ExtendedRandomAccessibleInterval< DoubleType, RandomAccessibleInterval< DoubleType > > input,
			RandomAccessibleInterval< DoubleType > output )
	{
		if ( FilteringAlgorithm.FASTGAUSS.equals( filter.algorithm ) )
			FastGauss.convolve( filter.sigma, input, output );
		else
			Gauss3.gauss( filter.sigma, input, output );
	}

	/*
	 * Compute a weighted mean image. The returned image is padded to crop out pixels
	 * influenced by edge effects.
	 */
	private static RandomAccessibleInterval< DoubleType > computeWeightedMean(
			Filter filter,
			RandomAccessibleInterval< DoubleType > input )
	{
		// The weighted mean is padded to exclude the edges
		RandomAccessibleInterval< DoubleType > output = createRAI( input, filter.padding );
		filter( filter, Views.extendMirrorDouble( input ), output );
		return output;
	}

	/*
	 * Compute a weighted variance image. The returned image is padded to crop out pixels
	 * influenced by edge effects.
	 */
	private static RandomAccessibleInterval< DoubleType > computeWeightedVariance(
			Filter filter,
			RandomAccessibleInterval< DoubleType > img,
			RandomAccessibleInterval< DoubleType > weightedMean )
	{
		// compute the square image
		RandomAccessibleInterval< DoubleType > square = createRAI( img, 0 );
		LoopBuilder.setImages( img, square ).forEachPixel( ( i, o ) -> o.set( i.get() * i.get() ) );

		// calculate (weighted) mean of the sliding window in the squared image, pad the output
		RandomAccessibleInterval< DoubleType > meanSquare = createRAI( img, filter.padding );
		filter( filter, Views.extendMirrorDouble( square ), meanSquare );

		// compute variance
		LoopBuilder.setImages( meanSquare, weightedMean ).forEachPixel( ( v, u ) -> v.set( ( v.get() - u.get() * u.get() ) ) );

		return meanSquare;
	}

	/*
	 * Compute a weighted covariance image. The returned image is padded to crop out pixels
	 * influenced by edge effects.
	 */
	private static RandomAccessibleInterval< DoubleType > computeWeightedCovariance(
			Filter filter,
			RandomAccessibleInterval< DoubleType > im1,
			RandomAccessibleInterval< DoubleType > im2,
			RandomAccessibleInterval< DoubleType > weightedMean1,
			RandomAccessibleInterval< DoubleType > weightedMean2 )
	{
		// compute the product images
		RandomAccessibleInterval< DoubleType > product = createRAI( im1, 0 );
		LoopBuilder.setImages( im1, im2, product ).forEachPixel( ( i1, i2, o ) -> o.set( i1.get() * i2.get() ) );

		// calculate (weighted) mean of the sliding window in the product image, pad the output
		RandomAccessibleInterval< DoubleType > meanProduct = createRAI( im1, filter.padding );
		filter( filter, Views.extendMirrorDouble( product ), meanProduct );

		// compute covariance
		LoopBuilder.setImages( meanProduct, weightedMean1, weightedMean2 ).forEachPixel( ( v, u1, u2 ) -> v.set( ( v.get() - u1.get() * u2.get() ) ) );

		return meanProduct;
	}

	/*
	 * Compute the SSIM image from the mean, variance and covariance images.
	 */
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
		final RandomAccessibleInterval< DoubleType > A1 = createRAI( ux, 0 );
		LoopBuilder.setImages( ux, uy, A1 ).forEachPixel( ( u1, u2, o ) -> o.set( 2 * u1.get() * u2.get() + C1 ) );

		final RandomAccessibleInterval< DoubleType > A2 = createRAI( ux, 0 );
		LoopBuilder.setImages( vxy, A2 ).forEachPixel( ( v, o ) -> o.set( 2 * v.get() + C2 ) );

		final RandomAccessibleInterval< DoubleType > B1 = createRAI( ux, 0 );
		LoopBuilder.setImages( ux, uy, B1 ).forEachPixel( ( u1, u2, o ) -> o.set( u1.get() * u1.get() + u2.get() * u2.get() + C1 ) );

		final RandomAccessibleInterval< DoubleType > B2 = createRAI( ux, 0 );
		LoopBuilder.setImages( vx, vy, B2 ).forEachPixel( ( v1, v2, o ) -> o.set( v1.get() + v2.get() + C2 ) );

		// SSIM image
		final RandomAccessibleInterval< DoubleType > S = createRAI( ux, 0 );
		LoopBuilder.setImages( A1, A2, B1, B2, S ).forEachPixel( ( a1, a2, b1, b2, s ) -> s.set( a1.get() * a2.get() / b1.get() / b2.get() ) );

		return S;
	}

	private static Double computeMean(
			RandomAccessibleInterval< DoubleType > img )
	{
		int counter = 0;
		RealSum sum = new RealSum();
		for ( DoubleType d : Views.iterable( img ) )
		{
			sum.add( d.get() );
			counter++;
		}

		return sum.getSum() / counter;
	}
}
