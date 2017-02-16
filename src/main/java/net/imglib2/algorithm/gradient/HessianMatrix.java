package net.imglib2.algorithm.gradient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
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
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
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
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
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
	 * @param gradient
	 *            n+1-dimensional {@link RandomAccessible} containing the
	 *            gradients along all axes of the smoothed source (size of last
	 *            dimension is n)
	 * @param hessianMatrix
	 *            n+1-dimensional {@link RandomAccessibleInterval} for storing
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
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
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 * @param nTasks
	 *            Number of threads/workers used for parallel computation of
	 *            eigenvalues.
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
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
	 * @param outOfBounds
	 *            {@link OutOfBoundsFactory} that specifies how out of bound
	 *            pixels of intermediate results should be handled (necessary
	 *            for gradient computation).
	 * @param nTasks
	 *            Number of threads/workers used for parallel computation of
	 *            eigenvalues.
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
	 *            all second partial derivatives (size of last dimension is n *
	 *            ( n + 1 ) / 2)
	 * @param nTasks
	 *            Number of threads/workers used for parallel computation of
	 *            eigenvalues.
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

}
