/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.gauss3;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * Gaussian convolution.
 * 
 * @author Tobias Pietzsch
 */
public final class Gauss3
{
	/**
	 * Apply Gaussian convolution to source and write the result to target.
	 * In-place operation (source==target) is supported.
	 * 
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 * 
	 * <p>
	 * Computation may be multi-threaded, according to the current
	 * {@link Parallelization} context. (By default, it will use the
	 * {@link ForkJoinPool#commonPool() common ForkJoinPool})
	 *
	 * @param sigma
	 *            standard deviation of isotropic Gaussian.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target ) throws IncompatibleTypeException
	{
		final int n = source.numDimensions();
		final double[] s = new double[ n ];
		for ( int d = 0; d < n; ++d )
			s[ d ] = sigma;
		gauss( s, source, target );
	}

	/**
	 * Apply Gaussian convolution to source and write the result to target.
	 * In-place operation (source==target) is supported.
	 * 
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 * 
	 * <p>
	 * Computation may be multi-threaded, according to the current
	 * {@link Parallelization} context. (By default, it will use the
	 * {@link ForkJoinPool#commonPool() common ForkJoinPool})
	 *
	 * @param sigma
	 *            standard deviation in every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double[] sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target ) throws IncompatibleTypeException
	{
		final double[][] halfkernels = halfkernels( sigma );
		final Convolution< NumericType< ? > > convolution = SeparableKernelConvolution.convolution( Kernel1D.symmetric( halfkernels ) );
		convolution.process( source, target );
	}

	/**
	 * @deprecated
	 * Deprecated. Please use
	 * {@link Gauss3#gauss(double, RandomAccessible, RandomAccessibleInterval)
	 * gauss(sigma, source, target)} instead. The number of threads used to
	 * calculate the Gaussion convolution may by set with the
	 * {@link Parallelization} context, as show in this example:
	 * <pre>
	 * {@code
	 * Parallelization.runWithNumThreads( numThreads,
	 *    () -> gauss( sigma, source, target )
	 * );
	 * }
	 * </pre>
	 *
	 * <p>
	 * Apply Gaussian convolution to source and write the result to output.
	 * In-place operation (source==target) is supported.
	 * 
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 * 
	 * @param sigma
	 *            standard deviation in every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param numThreads
	 *            how many threads to use for the computation.
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	@Deprecated
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double[] sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target, final int numThreads ) throws IncompatibleTypeException
	{
		Parallelization.runWithNumThreads( numThreads,
				() -> gauss( sigma, source, target )
		);
	}

	/**
	 * @deprecated
	 * Deprecated. Please use
	 * {@link Gauss3#gauss(double, RandomAccessible, RandomAccessibleInterval)
	 * gauss(sigma, source, target)} instead. The ExecutorService used to
	 * calculate the Gaussion convolution may by set with the
	 * {@link Parallelization} context, as show in this example:
	 * <pre>
	 * {@code
	 * Parallelization.runWithExecutor( executorService,
	 *    () -> gauss( sigma, source, target )
	 * );
	 * }
	 * </pre>
	 *
	 * <p>
	 * Apply Gaussian convolution to source and write the result to output.
	 * In-place operation (source==target) is supported.
	 * 
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 * 
	 * @param sigma
	 *            standard deviation in every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param service
	 *            service providing threads for multi-threading
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	@Deprecated
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double[] sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target, final ExecutorService service ) throws IncompatibleTypeException
	{
		Parallelization.runWithExecutor( service,
				() -> gauss( sigma, source, target )
		);
	}

	public static double[][] halfkernels( final double[] sigma )
	{
		final int n = sigma.length;
		final double[][] halfkernels = new double[ n ][];
		final int[] size = halfkernelsizes( sigma );
		for ( int i = 0; i < n; ++i )
			halfkernels[ i ] = halfkernel( sigma[ i ], size[ i ], true );
		return halfkernels;
	}

	public static int[] halfkernelsizes( final double[] sigma )
	{
		final int n = sigma.length;
		final int[] size = new int[ n ];
		for ( int i = 0; i < n; ++i )
			size[ i ] = Math.max( 2, ( int ) ( 3 * sigma[ i ] + 0.5 ) + 1 );
		return size;
	}

	/**
	 * Returns a gaussian half kernel with the given sigma and size.
	 * <p>
	 * The edges are smoothed by a second degree polynomial.
	 * This improves the first derivative and the fourier spectrum
	 * of the gaussian kernel.
	 */
	public static double[] halfkernel( final double sigma, final int size, final boolean normalize )
	{
		final double two_sq_sigma = 2 * square( sigma );
		final double[] kernel = new double[ size ];

		kernel[ 0 ] = 1;
		for ( int x = 1; x < size; ++x )
			kernel[ x ] = Math.exp( -square( x ) / two_sq_sigma );

		smoothEdge( kernel );

		if ( normalize )
			normalizeHalfkernel( kernel );

		return kernel;
	}

	/**
	 * This method smooths the truncated end of the gaussian kernel.
	 * The code is taken from ImageJ1 "Gaussian Blur ...".
	 * <p>
	 * Detailed explanation:
	 * <p>
	 * The values kernel[x] for r < x < L are replaced by the values
	 * of a polynomial p(x) = slope * (x - L) ^ 2.
	 * Where L equals kernel.length. And the "slope" and "r" are chosen
	 * such that there is a smooth transition between the kernel and
	 * the polynomial. Thus their value and first derivative
	 * match for x = r: p(r) = kernel[r] and p'(r) = kernel'[r].
	 */
	private static void smoothEdge( double[] kernel )
	{
		int L = kernel.length;
		double slope = Double.MAX_VALUE;
		int r = L;
		while ( r > L / 2 )
		{
			r--;
			double a = kernel[ r ] / square( L - r );
			if ( a < slope )
				slope = a;
			else
			{
				r++;
				break;
			}
		}
		for ( int x = r + 1; x < L; x++ )
			kernel[ x ] = slope * square( L - x );
	}

	/**
	 * Normalizes a half kernel such that the values sum up to 1.
	 */
	private static void normalizeHalfkernel( double[] kernel )
	{
		double sum = 0.5 * kernel[ 0 ];
		for ( int x = 1; x < kernel.length; ++x )
			sum += kernel[ x ];
		sum *= 2;
		multiply( kernel, 1 / sum );
	}

	// -- Helper methods --

	private static double square( double x )
	{
		return x * x;
	}

	private static void multiply( double[] values, double factor )
	{
		for ( int x = 0; x < values.length; ++x )
			values[ x ] *= factor;
	}
}
