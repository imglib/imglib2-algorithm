package net.imglib2.algorithm.convolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * This class can be used to implement a separable convolution. It applies a
 * {@link LineConvolverFactory} on the given images.
 *
 * @author Matthias Arzt
 */
public class LineConvolution< T > extends AbstractMultiThreadedConvolution< T >
{
	private final LineConvolverFactory< ? super T > factory;

	private final int direction;

	public LineConvolution( final LineConvolverFactory< ? super T > factory, final int direction )
	{
		this.factory = factory;
		this.direction = direction;
	}

	@Override
	public Interval requiredSourceInterval( final Interval targetInterval )
	{
		final long[] min = Intervals.minAsLongArray( targetInterval );
		final long[] max = Intervals.maxAsLongArray( targetInterval );
		min[ direction ] -= factory.getBorderBefore();
		max[ direction ] += factory.getBorderAfter();
		return new FinalInterval( min, max );
	}

	@Override
	public T preferredSourceType( final T targetType )
	{
		return (T) factory.preferredSourceType( targetType );
	}

	@Override
	protected void process( final RandomAccessible< ? extends T > source, final RandomAccessibleInterval< ? extends T > target, final ExecutorService executorService, final int numThreads )
	{
		final RandomAccessibleInterval< ? extends T > sourceInterval = Views.interval( source, requiredSourceInterval( target ) );
		final long[] sourceMin = Intervals.minAsLongArray( sourceInterval );
		final long[] targetMin = Intervals.minAsLongArray( target );

		final Supplier< Consumer< Localizable > > actionFactory = () -> {

			final RandomAccess< ? extends T > in = sourceInterval.randomAccess();
			final RandomAccess< ? extends T > out = target.randomAccess();
			final Runnable convolver = factory.getConvolver( in, out, direction, target.dimension( direction ) );

			return position -> {
				in.setPosition( sourceMin );
				out.setPosition( targetMin );
				in.move( position );
				out.move( position );
				convolver.run();
			};
		};

		final long[] dim = Intervals.dimensionsAsLongArray( target );
		dim[ direction ] = 1;

		final int numTasks = numThreads > 1 ? numThreads * 4 : 1;
		LineConvolution.forEachIntervalElementInParallel( executorService, numTasks, new FinalInterval( dim ), actionFactory );
	}

	/**
	 * {@link #forEachIntervalElementInParallel(ExecutorService, int, Interval, Supplier)}
	 * executes a given action for each position in a given interval. Therefor
	 * it starts the specified number of tasks. Each tasks calls the action
	 * factory once, to get an instance of the action that should be executed.
	 * The action is then called multiple times by the task.
	 *
	 * @param service
	 *            {@link ExecutorService} used to create the tasks.
	 * @param numTasks
	 *            number of tasks to use.
	 * @param interval
	 *            interval to iterate over.
	 * @param actionFactory
	 *            factory that returns the action to be executed.
	 */
	// TODO: move to a better place
	public static void forEachIntervalElementInParallel( final ExecutorService service, final int numTasks, final Interval interval,
			final Supplier< Consumer< Localizable > > actionFactory )
	{
		final long[] min = Intervals.minAsLongArray( interval );
		final long[] dim = Intervals.dimensionsAsLongArray( interval );
		final long size = Intervals.numElements( dim );
		final long endIndex = size;
		final long taskSize = ( size + numTasks - 1 ) / numTasks; // round up
		final ArrayList< Callable< Void > > callables = new ArrayList<>();

		for ( int taskNum = 0; taskNum < numTasks; ++taskNum )
		{
			final long myStartIndex = taskNum * taskSize;
			final long myEndIndex = Math.min( endIndex, myStartIndex + taskSize );
			final Callable< Void > r = () -> {
				final Consumer< Localizable > action = actionFactory.get();
				final long[] position = new long[ dim.length ];
				final Localizable localizable = Point.wrap( position );
				for ( long index = myStartIndex; index < myEndIndex; ++index )
				{
					IntervalIndexer.indexToPositionWithOffset( index, dim, min, position );
					action.accept( localizable );
				}
				return null;
			};
			callables.add( r );
		}
		execute( service, callables );
	}

	private static void execute( final ExecutorService service, final ArrayList< Callable< Void > > callables )
	{
		try
		{
			final List< Future< Void > > futures = service.invokeAll( callables );
			for ( final Future< Void > future : futures )
				future.get();
		}
		catch ( final InterruptedException | ExecutionException e )
		{
			final Throwable cause = e.getCause();
			if ( cause instanceof RuntimeException )
				throw ( RuntimeException ) cause;
			throw new RuntimeException( e );
		}
	}
}
