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

package net.imglib2.algorithm.gradient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.View;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

/**
 *
 * Compute entries of n-dimensional Hessian matrix.
 *
 * @author Philipp Hanslovsky
 *
 */
public class HessianMatrix
{

	/**
	 * @param source
	 *            n-dimensional input {@link RandomAccessibleInterval}
	 * @param gaussian
	 *            n-dimensional output parameter
	 *            {@link RandomAccessibleInterval}
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            the gradients along all axes of the smoothed source (size of
	 *            last dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 * @param sigma
	 *            Scale for Gaussian smoothing.
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 * @throws IncompatibleTypeException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateMatrix(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > gaussian,
			final RandomAccessibleInterval< U > gradient,
			final RandomAccessibleInterval< U > hessianMatrix,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
			final double... sigma ) throws IncompatibleTypeException
	{

		if ( sigma.length == 1 )
			Gauss3.gauss( sigma[ 0 ], source, gaussian );
		else
			Gauss3.gauss( sigma, source, gaussian );
		return calculateMatrix( Views.extend( gaussian, outOfBounds ), gradient, hessianMatrix, outOfBounds );
	}

	/**
	 * @param source
	 *            n-dimensional pre-smoothed {@link RandomAccessible}. It is the
	 *            callers responsibility to smooth the input at the desired
	 *            scales.
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            the gradients along all axes of the smoothed source (size of
	 *            last dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 *
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > calculateMatrix(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< T > gradient,
			final RandomAccessibleInterval< T > hessianMatrix,
			final OutOfBoundsFactory< T, ? super RandomAccessibleInterval< T > > outOfBounds )
	{

		final int nDim = gradient.numDimensions() - 1;

		for ( long d = 0; d < nDim; ++d )
			PartialDerivative.gradientCentralDifference( source, Views.hyperSlice( gradient, nDim, d ), ( int ) d );

		return calculateMatrix( Views.extend( gradient, outOfBounds ), hessianMatrix );
	}

	/**
	 *
	 * @param gradients
	 *            n+1-dimensional {@link RandomAccessible} containing the
	 *            gradients along all axes of the smoothed source (size of last
	 *            dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > calculateMatrix(
			final RandomAccessible< T > gradients,
			final RandomAccessibleInterval< T > hessianMatrix )
	{

		final int nDim = gradients.numDimensions() - 1;

		long count = 0;
		for ( long d1 = 0; d1 < nDim; ++d1 )
		{
			final MixedTransformView< T > hs1 = Views.hyperSlice( gradients, nDim, d1 );
			for ( long d2 = d1; d2 < nDim; ++d2 )
			{
				final IntervalView< T > hs2 = Views.hyperSlice( hessianMatrix, nDim, count );
				PartialDerivative.gradientCentralDifference( hs1, hs2, ( int ) d2 );
				++count;
			}
		}
		return hessianMatrix;
	}

	// parallel

	/**
	 * @param source
	 *            n-dimensional input {@link RandomAccessibleInterval}
	 * @param gaussian
	 *            n-dimensional output parameter
	 *            {@link RandomAccessibleInterval}
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            the gradients along all axes of the smoothed source (size of
	 *            last dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 * @param nTasks
	 *            Number of tasks used for parallel computation of eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 * @param sigma
	 *            Scale for Gaussian smoothing.
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 * @throws IncompatibleTypeException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateMatrix(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > gaussian,
			final RandomAccessibleInterval< U > gradient,
			final RandomAccessibleInterval< U > hessianMatrix,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
			final int nTasks,
			final ExecutorService es,
			final double... sigma ) throws IncompatibleTypeException, InterruptedException, ExecutionException
	{

		if ( sigma.length == 1 )
			Gauss3.gauss( IntStream.range( 0, source.numDimensions() ).mapToDouble( i -> sigma[ 0 ] ).toArray(), source, gaussian, es );
		else
			Gauss3.gauss( sigma, source, gaussian, es );
		return calculateMatrix( Views.extend( gaussian, outOfBounds ), gradient, hessianMatrix, outOfBounds, nTasks, es );
	}

	/**
	 * @param source
	 *            n-dimensional pre-smoothed {@link RandomAccessible}. It is the
	 *            callers responsibility to smooth the input at the desired
	 *            scales.
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            the gradients along all axes of the smoothed source (size of
	 *            last dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 * @param nTasks
	 *            Number of tasks used for parallel computation of eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 * @throws IncompatibleTypeException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > calculateMatrix(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< T > gradient,
			final RandomAccessibleInterval< T > hessianMatrix,
			final OutOfBoundsFactory< T, ? super RandomAccessibleInterval< T > > outOfBounds,
			final int nTasks,
			final ExecutorService es ) throws IncompatibleTypeException, InterruptedException, ExecutionException
	{

		final int nDim = gradient.numDimensions() - 1;

		for ( long d = 0; d < nDim; ++d )
			PartialDerivative.gradientCentralDifferenceParallel( source, Views.hyperSlice( gradient, nDim, d ), ( int ) d, nTasks, es );

		return calculateMatrix( Views.extend( gradient, outOfBounds ), hessianMatrix, nTasks, es );
	}

	/**
	 *
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessible} containing the
	 *            gradients along all axes of the smoothed source (size of last
	 *            dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives as a linear representation
	 *            (size of last dimension is n * ( n + 1 ) / 2) of upper
	 *            triangular Hessian matrix: For n-dimensional input,
	 *            <code>hessianMatrix</code> (m) will be populated along the nth
	 *            dimension like this: [m11, m12, ... , m1n, m22, m23, ... ,
	 *            mnn]
	 * @param nTasks
	 *            Number of tasks used for parallel computation of eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 *
	 * @return Hessian matrix that was passed as output parameter.
	 * @throws IncompatibleTypeException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > calculateMatrix(
			final RandomAccessible< T > gradient,
			final RandomAccessibleInterval< T > hessianMatrix,
			final int nTasks,
			final ExecutorService es ) throws IncompatibleTypeException, InterruptedException, ExecutionException
	{

		final int nDim = gradient.numDimensions() - 1;

		long count = 0;
		for ( long d1 = 0; d1 < nDim; ++d1 )
		{
			final MixedTransformView< T > hs1 = Views.hyperSlice( gradient, nDim, d1 );
			for ( long d2 = d1; d2 < nDim; ++d2 )
			{
				final IntervalView< T > hs2 = Views.hyperSlice( hessianMatrix, nDim, count );
				PartialDerivative.gradientCentralDifferenceParallel( hs1, hs2, ( int ) d2, nTasks, es );
				++count;
			}
		}
		return hessianMatrix;
	}



	/**
	 *
	 * The {@link HessianMatrix#calculateMatrix} methods assume that the voxels
	 * of the input data are isotropic. This is not always the case and the
	 * finite differences would need to be normalized accordingly. As the
	 * derivative is a linear transformation re-normalization (scaling) of the
	 * Hessian matrix produces the desired result. In general, the normalization
	 * is arbitrary and we choose to normalize such that the second derivative
	 * remains unchanged along the dimension for which the extent of a voxel is
	 * the smallest.
	 *
	 * Note that the returned object is a {@link View} on the Hessian matrix and
	 * values are re-normalized as they are queried. For repeated use of the
	 * re-normalized Hessian matrix, it might be beneficial to persist the
	 * re-normalized values by copying into a {@link RandomAccessibleInterval}.
	 *
	 * @param hessian
	 *            Hessian matrix to be re-normalized (scaled).
	 *            <code>hessian</code> is an n+1 dimensional
	 *            {@link RandomAccessibleInterval} that stores a linear
	 *            representation of the upper triangular n-dimensional Hessian
	 *            matrix along the last dimension as specified in
	 *            {@link HessianMatrix#calculateMatrix(RandomAccessible, RandomAccessibleInterval)}.
	 * @param sigma
	 *            Specify the voxel size for each dimension.
	 * @return Scaled {@link View} of the hessian matrix stored in
	 *         <code>hessian</code>
	 */
	public static < T extends RealType< T > > IntervalView< T > scaleHessianMatrix( final RandomAccessibleInterval< T > hessian, final double[] sigma )
	{

		assert sigma.length == hessian.numDimensions() - 1;
		assert sigma.length * ( sigma.length + 1 ) / 2 == hessian.dimension( sigma.length );
		final int maxD = sigma.length;

		final double minSigma = Util.min(  sigma );
		final double minSigmaSq = minSigma * minSigma;
		final double[] sigmaSquared = new double[ sigma.length * ( sigma.length + 1 ) / 2 ];
		for ( int i1 = 0, k = 0; i1 < sigma.length; ++i1 )
			for ( int i2 = i1; i2 < sigma.length; ++i2, ++k )
				sigmaSquared[ k ] = sigma[ i1 ] * sigma[ i2 ] / minSigmaSq;

		final ScaleAsFunctionOfPosition< T > scaledMatrix = new ScaleAsFunctionOfPosition<>( hessian, l -> {
			return sigmaSquared[ l.getIntPosition( maxD ) ];
		} );

		return Views.interval( scaledMatrix, hessian );
	}


}
