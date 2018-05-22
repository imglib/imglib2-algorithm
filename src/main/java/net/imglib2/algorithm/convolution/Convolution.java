package net.imglib2.algorithm.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Convolution
{
	public static <T> void convolve1d( ConvolverFactory< ? super T, ? super T > convolverFactory, RandomAccessible< ? extends T > in, RandomAccessibleInterval< ? extends T > out, int d ) {
		Interval interval = convolve1dSourceInterval( convolverFactory, out, d );
		internConvolve( convolverFactory, Views.interval(in, interval), out, d );
	}

	public static Interval convolve1dSourceInterval( ConvolverFactory< ?, ? > convolverFactory, Interval targetInterval, int d ) {
		long[] min = Intervals.minAsLongArray( targetInterval );
		long[] max = Intervals.maxAsLongArray( targetInterval );
		min[d] -= convolverFactory.getBorderBefore();
		max[d] += convolverFactory.getBorderAfter();
		return new FinalInterval( min, max );
	}

	public static <T> void convolve(List<Pair<Integer, ? extends ConvolverFactory<? super T, ? super T>> > convolverFactories, ImgFactory<? extends T> tmpImageFactory, RandomAccessible<? extends T> in, RandomAccessibleInterval<? extends T> out) {
		if(convolverFactories.isEmpty())
			throw new IllegalArgumentException();
		List<RandomAccessibleInterval<? extends T>> tmpDimensions = tmpImages( convolverFactories, tmpImageFactory, out );
		RandomAccessibleInterval< ? extends T > sourceInterval = Views.interval( in, convolveSourceInterval( convolverFactories, out ) );
		for ( int i = 0; i < convolverFactories.size(); i++ )
		{
			int d = convolverFactories.get( i ).getA();
			ConvolverFactory< ? super T, ? super T > factory = convolverFactories.get( i ).getB();
			RandomAccessibleInterval< ? extends T > source = i == 0 ? sourceInterval : tmpDimensions.get( i - 1 );
			RandomAccessibleInterval< ? extends T > target = i != convolverFactories.size() - 1 ? tmpDimensions.get( i ) : out;
			internConvolve(factory, source, target, d);
		}
	}

	private static < T > void internConvolve( ConvolverFactory< ? super T, ? super T > factory, RandomAccessibleInterval< ? extends T > source, RandomAccessibleInterval< ? extends T > target, int d )
	{
		final long[] sourceMin = Intervals.minAsLongArray( source );
		final long[] targetMin = Intervals.minAsLongArray( target );

		Supplier<Consumer<Localizable>> actionFactory = () -> {

			final RandomAccess< ? extends T > in = source.randomAccess();
			final RandomAccess< ? extends T > out = target.randomAccess();
			final Runnable convolver = factory.getConvolver(in, out, d, target.dimension( d ) );

			return position -> {
				in.setPosition( sourceMin );
				out.setPosition( targetMin );
				in.move(position);
				out.move(position);
				convolver.run();
			};
		};

		final long[] dim = Intervals.dimensionsAsLongArray(target);
		dim[ d ] = 1;

		// FIXME: is there a better way to determine the number of threads
		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numTasks = numThreads > 1 ? numThreads * 4 : 1;
		ExecutorService executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
		Convolution.forEachIntervalElementInParallel(executorService, numTasks, new FinalInterval(dim), actionFactory);
	}

	public static <T> Interval convolveSourceInterval(List< Pair< Integer, ? extends ConvolverFactory< ? super T, ? super T > > > convolverFactories, Interval targetInterval) {
		Interval sourceInterval = targetInterval;
		for ( int i = convolverFactories.size() - 1; i >= 0; i-- )
		{
			int d = convolverFactories.get( i ).getA();
			ConvolverFactory<?, ?> factory = convolverFactories.get( i ).getB();
			sourceInterval = convolve1dSourceInterval( factory, sourceInterval, d );
		}
		return sourceInterval;
	}

	private static <T> List<RandomAccessibleInterval<? extends T>> tmpImages( List< Pair< Integer, ? extends ConvolverFactory< ? super T, ? super T > > > convolverFactories, ImgFactory< ? extends T > tmpImageFactory, Interval targetInterval ) {
		Interval sourceInterval = targetInterval;
		List<Interval> result = new ArrayList<>(  );
		result.addAll( Collections.nCopies(convolverFactories.size() - 1, null) );
		for ( int i = convolverFactories.size() - 1; i > 0; i-- )
		{
			int d = convolverFactories.get( i ).getA();
			sourceInterval = convolve1dSourceInterval( convolverFactories.get( i ).getB(), sourceInterval, d );
			result.set( i-1, new FinalInterval(Intervals.dimensionsAsLongArray( sourceInterval )) );
		}
		return result.stream().map(tmpImageFactory::create).collect( Collectors.toList());
	}

	/**
	 * {@link #forEachIntervalElementInParallel(ExecutorService, int, Interval, Supplier)} executes a given action
	 * for each position in a given interval. Therefor it starts the specified number of tasks. Each tasks calls
	 * the action factory ones, to get an instance of the action that should be executed. The action is then called
	 * multiple times by the task.
	 * @param service {@link ExecutorService} used to create the tasks.
	 * @param numTasks number of tasks to use.
	 * @param interval interval to iterate over.
	 * @param actionFactory factory that returns the action to be executed.
	 */
	// TODO: move to a better place
	public static void forEachIntervalElementInParallel( ExecutorService service, int numTasks, Interval interval,
			Supplier< Consumer< Localizable > > actionFactory )
	{
		long[] min = Intervals.minAsLongArray(interval);
		long[] dim = Intervals.dimensionsAsLongArray(interval);
		long size = LongStream.of(dim).reduce(1, (a, b) -> a * b);
		final long endIndex = size;
		final long taskSize = (size + numTasks - 1) / numTasks; // round up
		final ArrayList< Callable< Void > > callables = new ArrayList<>();

		for ( int taskNum = 0; taskNum < numTasks; ++taskNum )
		{
			final long myStartIndex = taskNum * taskSize;
			final long myEndIndex = Math.min( endIndex, myStartIndex + taskSize );
			final Callable< Void > r = () -> {
				Consumer<Localizable> action = actionFactory.get();
				final long[] position = new long[ dim.length ];
				final Localizable localizable = Point.wrap(position);
				for ( long index = myStartIndex; index < myEndIndex; ++index )
				{
					IntervalIndexer.indexToPositionWithOffset( index, dim, min, position);
					action.accept(localizable);
				}
				return null;
			};
			callables.add( r );
		}
		execute(service, callables);
	}

	private static void execute(ExecutorService service, ArrayList<Callable<Void>> callables) {
		try
		{
			List<Future<Void>> futures = service.invokeAll(callables);
			for(Future<Void> future : futures)
				future.get();
		}
		catch ( final InterruptedException | ExecutionException e )
		{
			throw new RuntimeException( e );
		}
	}
}
