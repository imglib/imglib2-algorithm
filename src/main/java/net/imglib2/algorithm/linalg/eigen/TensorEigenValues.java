package net.imglib2.algorithm.linalg.eigen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Compute eigenvalues of rank 2 tensors.
 *
 */
public class TensorEigenValues
{

	// static methods

	// symmetric

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of upper triangular
	 *            tensor in last dimension, i.e. if tensor (t) has n+1
	 *            dimensions, the last dimension must be of size n * ( n + 1 ) /
	 *            2, and the entries in the last dimension are arranged like
	 *            this: [t11, t12, ... , t1n, t22, t23, ... , tnn]
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *            the last dimension must be n.
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValuesSymmetric(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues )
	{

		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * ( eigenvalues.dimension( nDim - 1 ) + 1 ) / 2 == tensor.dimension( nDim - 1 );

		final EigenValues< T, U > ev;
		if ( nDim == 2 )
			ev = EigenValues.oneDimensional();
		else if ( nDim == 3 )
			ev = EigenValues.symmetric2D();
		else if ( nDim > 3 )
			ev = EigenValues.symmetric( nDim );
		else
			ev = EigenValues.invalid();

		return calculateEigenValuesImpl( tensor, eigenvalues, ev );
	}

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of upper triangular
	 *            tensor in last dimension, i.e. if tensor (t) has n+1
	 *            dimensions, the last dimension must be of size n * ( n + 1 ) /
	 *            2, and the entries in the last dimension are arranged like
	 *            this: [t11, t12, ... , t1n, t22, t23, ... , tnn]
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *            the last dimension must be n.
	 * @param nTasks
	 *            Number of tasks used for parallel computation of eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValuesSymmetric(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final int nTasks,
			final ExecutorService es )
	{

		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * ( eigenvalues.dimension( nDim - 1 ) + 1 ) / 2 == tensor.dimension( nDim - 1 );

		final EigenValues< T, U > ev;
		if ( nDim == 2 )
			ev = EigenValues.oneDimensional();
		else if ( nDim == 3 )
			ev = EigenValues.symmetric2D();
		else if ( nDim > 3 )
			ev = EigenValues.symmetric( nDim );
		else
			ev = EigenValues.invalid();

		return calculateEigenValues( tensor, eigenvalues, ev, nTasks, es );
	}

	// square

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of tensor in last
	 *            dimension, i.e. if tensor (t) has n+1 dimensions, the last
	 *            dimension must be of size n * n, and the entries in the last
	 *            dimension are arranged like this: [t11, t12, ... , t1n, t21,
	 *            t22, t23, ... , tn1, ... , tnn]
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValuesSquare(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues )
	{

		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * ( eigenvalues.dimension( nDim - 1 ) + 1 ) / 2 == tensor.dimension( nDim - 1 );

		final EigenValues< T, U > ev;
		if ( nDim == 2 )
			ev = EigenValues.oneDimensional();
		else if ( nDim == 3 )
			ev = EigenValues.square2D();
		else if ( nDim > 3 )
			ev = EigenValues.square( nDim );
		else
			ev = EigenValues.invalid();

		return calculateEigenValuesImpl( tensor, eigenvalues, ev );
	}

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of tensor in last
	 *            dimension, i.e. if tensor (t) has n+1 dimensions, the last
	 *            dimension must be of size n * n, and the entries in the last
	 *            dimension are arranged like this: [t11, t12, ... , t1n, t21,
	 *            t22, t23, ... , tn1, ... , tnn]
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *            the last dimension must be n.
	 * @param nTasks
	 *            Number of threads/workers used for parallel computation of
	 *            eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValuesSquare(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final int nTasks,
			final ExecutorService es )
	{
		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * eigenvalues.dimension( nDim - 1 ) == tensor.dimension( nDim - 1 );

		final EigenValues< T, U > ev;
		if ( nDim == 2 )
			ev = EigenValues.oneDimensional();
		else if ( nDim == 3 )
			ev = EigenValues.square2D();
		else if ( nDim > 3 )
			ev = EigenValues.square( nDim );
		else
			ev = EigenValues.invalid();

		return calculateEigenValues( tensor, eigenvalues, ev, nTasks, es );
	}

	// general

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of tensor in last
	 *            dimension. Parameter ev specifies representation.
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *            the last dimension must be n.
	 * @param ev
	 *            Implementation that specifies how to calculate eigenvalues
	 *            from last dimension of input.
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValues(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final EigenValues< T, U > ev )
	{
		return calculateEigenValuesImpl( tensor, eigenvalues, ev );
	}

	/**
	 *
	 * @param tensor
	 *            Input that holds linear representation of tensor in last
	 *            dimension. Parameter ev specifies representation.
	 *
	 * @param eigenvalues
	 *            Target {@link RandomAccessibleInterval} for storing the
	 *            resulting tensor eigenvalues. Number of dimensions must be the
	 *            same as for input. For an n+1 dimensional input, the size of
	 *            the last dimension must be n.
	 * @param ev
	 *            Implementation that specifies how to calculate eigenvalues
	 *            from last dimension of input.
	 * @param nTasks
	 *            Number of threads/workers used for parallel computation of
	 *            eigenvalues.
	 * @param es
	 *            {@link ExecutorService} providing workers for parallel
	 *            computation. Service is managed (created, shutdown) by caller.
	 *
	 */
	public static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValues(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final EigenValues< T, U > ev,
			final int nTasks,
			final ExecutorService es )
	{

		assert nTasks > 0: "Passed nTasks < 1";

		final int nDim = tensor.numDimensions();

		long dimensionMax = Long.MIN_VALUE;
		int dimensionArgMax = -1;

		for ( int d = 0; d < nDim - 1; ++d )
		{
			final long size = tensor.dimension( d );
			if ( size > dimensionMax )
			{
				dimensionMax = size;
				dimensionArgMax = d;
			}
		}

		final long stepSize = Math.max( dimensionMax / nTasks, 1 );
		final long stepSizeMinusOne = stepSize - 1;
		final long max = dimensionMax - 1;

		final ArrayList< Callable< RandomAccessibleInterval< U > > > tasks = new ArrayList<>();
		for ( long currentMin = 0; currentMin < dimensionMax; currentMin += stepSize )
		{
			final long currentMax = Math.min( currentMin + stepSizeMinusOne, max );
			final long[] minT = new long[ nDim ];
			final long[] maxT = new long[ nDim ];
			final long[] minE = new long[ nDim ];
			final long[] maxE = new long[ nDim ];
			tensor.min( minT );
			tensor.max( maxT );
			eigenvalues.min( minE );
			eigenvalues.max( maxE );
			minE[ dimensionArgMax ] = minT[ dimensionArgMax ] = currentMin;
			maxE[ dimensionArgMax ] = maxT[ dimensionArgMax ] = currentMax;
			final IntervalView< T > currentTensor = Views.interval( tensor, new FinalInterval( minT, maxT ) );
			final IntervalView< U > currentEigenvalues = Views.interval( eigenvalues, new FinalInterval( minE, maxE ) );
			tasks.add( () -> calculateEigenValuesImpl( currentTensor, currentEigenvalues, ev ) );
		}


		try
		{
			final List< Future< RandomAccessibleInterval< U > > > futures = es.invokeAll( tasks );
			for ( final Future< RandomAccessibleInterval< U > > f : futures )
				try
			{
					f.get();
			}
			catch ( final ExecutionException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch ( final InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return eigenvalues;



	}

	private static < T extends RealType< T >, U extends RealType< U > > RandomAccessibleInterval< U > calculateEigenValuesImpl(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final EigenValues< T, U > ev )
	{
		final Cursor< RealComposite< T > > m = Views.iterable( Views.collapseReal( tensor ) ).cursor();
		final Cursor< RealComposite< U > > e = Views.iterable( Views.collapseReal( eigenvalues ) ).cursor();
		while ( m.hasNext() )
			ev.compute( m.next(), e.next() );
		return eigenvalues;
	}

	/**
	 *
	 * Create appropriately sized image for tensor input.
	 *
	 * @param tensor
	 *            n+1 dimensional {@link RandomAccessibleInterval}.
	 * @param factory
	 *            {@link ImgFactory} used for creating the result image.
	 * @param u
	 *            Variable necessary for creating the result image.
	 * @return n+1 dimensional {@link Img} with size n in the last dimension.
	 */
	public static < T extends RealType< T >, U extends RealType< U > > Img< U > createAppropriateResultImg(
			final RandomAccessibleInterval< T > tensor,
			final ImgFactory< U > factory,
			final U u )
	{
		final int nDim = tensor.numDimensions();
		final long[] dimensions = new long[ nDim ];
		tensor.dimensions( dimensions );
		dimensions[ nDim - 1 ] = nDim - 1;
		return factory.create( dimensions, u );
	}


}