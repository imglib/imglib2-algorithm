package net.imglib2.algorithm.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;

import java.util.concurrent.ExecutorService;

/**
 * This class can be used to implement a separable convolution. It applies a
 * {@link LineConvolverFactory} on the given images.
 *
 * @author Matthias Arzt
 */
public class LineConvolution< T > implements Convolution<T>
{
	private final LineConvolverFactory< ? super T > factory;

	private final int direction;

	private ExecutorService executor;

	public LineConvolution( final LineConvolverFactory< ? super T > factory, final int direction )
	{
		this.factory = factory;
		this.direction = direction;
	}

	@Deprecated
	@Override
	public void setExecutor( ExecutorService executor )
	{
		this.executor = executor;
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
	public void process( RandomAccessible< ? extends T > source, RandomAccessibleInterval< ? extends T > target )
	{
		final RandomAccessibleInterval< ? extends T > sourceInterval = Views.interval( source, requiredSourceInterval( target ) );
		final long[] sourceMin = Intervals.minAsLongArray( sourceInterval );
		final long[] targetMin = Intervals.minAsLongArray( target );

		final long[] dim = Intervals.dimensionsAsLongArray( target );
		dim[ direction ] = 1;

		RandomAccessibleInterval< Localizable > positions = Localizables.randomAccessibleInterval( new FinalInterval( dim ) );
		TaskExecutor taskExecutor = executor == null ? Parallelization.getTaskExecutor() : TaskExecutors.forExecutorService( executor );
		LoopBuilder.setImages( positions ).multiThreaded(taskExecutor).forEachChunk(
				chunk -> {

					final RandomAccess< ? extends T > in = sourceInterval.randomAccess();
					final RandomAccess< ? extends T > out = target.randomAccess();
					final Runnable convolver = factory.getConvolver( in, out, direction, target.dimension( direction ) );

					chunk.forEachPixel( position -> {
						in.setPosition( sourceMin );
						out.setPosition( targetMin );
						in.move( position );
						out.move( position );
						convolver.run();
					} );

					return null;
				}
		);
	}
}
