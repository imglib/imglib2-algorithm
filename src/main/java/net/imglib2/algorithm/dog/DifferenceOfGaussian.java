/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.dog;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.concurrent.ExecutorService;

/**
 * Compute Difference-of-Gaussian of a {@link RandomAccessible}.
 * 
 * @author Tobias Pietzsch
 */
public class DifferenceOfGaussian
{
	/**
	 * @deprecated Please use:
	 * Parallelization.withExecutor( service ).run( () -> DoG( sigmaSmaller, sigmaLarger, input, dog ) );
	 */
	public static < I extends NumericType< I >, T extends NumericType< T > & NativeType< T > > void DoG(
			final double[] sigmaSmaller,
			final double[] sigmaLarger,
			final RandomAccessible< I > input,
			final RandomAccessibleInterval< T > dog,
			final ExecutorService service )
	{
		Parallelization.runWithExecutor( service,
				() -> DoG( sigmaSmaller, sigmaLarger, input, dog )
		);
	}

	/**
	 * Compute the difference of Gaussian for the input. Input convolved with
	 * Gaussian of sigmaSmaller is subtracted from input convolved with Gaussian
	 * of sigmaLarger (where {@code sigmaLarger > sigmaSmaller}).
	 * <p>
	 * Creates an appropriate temporary image and calls
	 * {@link #DoG(double[], double[], RandomAccessible, RandomAccessible, RandomAccessibleInterval)}
	 * .
	 * </p>
	 *
	 * @param sigmaSmaller
	 *            stddev (in every dimension) of smaller Gaussian.
	 * @param sigmaLarger
	 *            stddev (in every dimension) of larger Gaussian.
	 * @param input
	 *            the input image extended to infinity (or at least covering the
	 *            same interval as the dog result image, plus borders for
	 *            convolution).
	 * @param dog
	 *            the Difference-of-Gaussian result image.
	 */
	public static < I extends NumericType< I >, T extends NumericType< T > & NativeType< T > > void DoG(
			final double[] sigmaSmaller,
			final double[] sigmaLarger,
			final RandomAccessible< I > input,
			final RandomAccessibleInterval< T > dog )
	{
		final T type = Util.getTypeFromInterval( dog );
		final Img< T > g1 = Util.getArrayOrCellImgFactory( dog, type ).create( dog );
		final long[] translation = new long[ dog.numDimensions() ];
		dog.min( translation );
		DoG( sigmaSmaller, sigmaLarger, input, Views.translate( g1, translation ), dog );
	}

	/**
	 * @deprecated  Please use instead
	 * {@code Parallelization.withExecutor( service ). run( () -> DoG( sigmaSmaller, sigmaLarger, input, tmp, dog ) ); }
	 */
	public static < I extends NumericType< I >, T extends NumericType< T > & NativeType< T > > void DoG(
			final double[] sigmaSmaller,
			final double[] sigmaLarger,
			final RandomAccessible< I > input,
			final RandomAccessible< T > tmp,
			final RandomAccessibleInterval< T > dog,
			final ExecutorService service )
	{
		Parallelization.runWithExecutor( service,
				() -> DoG( sigmaSmaller, sigmaLarger, input, tmp, dog )
		);
	}

	/**
	 * Compute the difference of Gaussian for the input. Input convolved with
	 * Gaussian of sigmaSmaller is subtracted from input convolved with Gaussian
	 * of sigmaLarger (where sigmaLarger &gt; sigmaSmaller).
	 *
	 * @param sigmaSmaller
	 *            stddev (in every dimension) of smaller Gaussian.
	 * @param sigmaLarger
	 *            stddev (in every dimension) of larger Gaussian.
	 * @param input
	 *            the input image extended to infinity (or at least covering the
	 *            same interval as the dog result image, plus borders for
	 *            convolution).
	 * @param tmp
	 *            temporary image, must at least cover the same interval as the
	 *            dog result image.
	 * @param dog
	 *            the Difference-of-Gaussian result image.
	 */
	public static < I extends NumericType< I >, T extends NumericType< T > & NativeType< T > > void DoG(
			final double[] sigmaSmaller,
			final double[] sigmaLarger,
			final RandomAccessible< I > input,
			final RandomAccessible< T > tmp,
			final RandomAccessibleInterval< T > dog )
	{
		final IntervalView< T > tmpInterval = Views.interval( tmp, dog );
		Gauss3.gauss( sigmaSmaller, input, tmpInterval );
		Gauss3.gauss( sigmaLarger, input, dog );
		LoopBuilder.setImages( dog, tmpInterval ).multiThreaded().forEachPixel( ( d, t ) -> d.sub( t ) );
	}

	/**
	 * Helper function to compute per-dimension sigmas in pixel coordinates. The
	 * parameters {@code sigma1} and {@code sigma2} specify desired
	 * sigmas (scale) in image coordinates. Taking into account the sigma of the
	 * input image as well as the image calibration, the resulting sigma arrays
	 * specifiy the smoothing that has to be applied to achieve the desired
	 * sigmas.
	 * 
	 * @param imageSigma
	 *            estimated sigma of the input image, in pixel coordinates.
	 * @param minf
	 *            multiple of the {@code imageSigma} that smoothing with
	 *            the resulting sigma must at least achieve.
	 * @param pixelSize
	 *            calibration. Dimensions of a pixel in image units.
	 * @param sigma1
	 *            desired sigma in image coordinates.
	 * @param sigma2
	 *            desired sigma in image coordinates.
	 * @return {@code double[2][numDimensions]}, array of two arrays
	 *         contains resulting sigmas for sigma1, sigma2.
	 */
	public static double[][] computeSigmas( final double imageSigma, final double minf, final double[] pixelSize, final double sigma1, final double sigma2 )
	{
		final int n = pixelSize.length;
		final double k = sigma2 / sigma1;
		final double[] sigmas1 = new double[ n ];
		final double[] sigmas2 = new double[ n ];
		for ( int d = 0; d < n; ++d )
		{
			final double s1 = Math.max( minf * imageSigma, sigma1 / pixelSize[ d ] );
			final double s2 = k * s1;
			sigmas1[ d ] = Math.sqrt( s1 * s1 - imageSigma * imageSigma );
			sigmas2[ d ] = Math.sqrt( s2 * s2 - imageSigma * imageSigma );
		}
		return new double[][] { sigmas1, sigmas2 };
	}

	/**
	 * Helper function to compute the minimum sigma that can be given to
	 * {@link #computeSigmas(double, double, double[], double, double)} while
	 * still achieving isotropic smoothed images.
	 */
	public static double computeMinIsotropicSigma( final double imageSigma, final double minf, final double[] pixelSize )
	{
		final int n = pixelSize.length;
		double s = pixelSize[ 0 ];
		for ( int d = 1; d < n; ++d )
			s = Math.max( s, pixelSize[ d ] );
		return minf * imageSigma * s;
	}
}
