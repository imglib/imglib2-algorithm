package net.imglib2.algorithm.gradient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
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
