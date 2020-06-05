package net.imglib2.algorithm.convolution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;

/**
 * Abstract class to help implementing a Convolution, that is multi threaded
 * using an {@link ExecutorService}. This implements the method
 * {@link Convolution#setExecutor(ExecutorService)}.
 * <p>
 * Classes that derive from {@link AbstractMultiThreadedConvolution} must
 * override
 * {@link AbstractMultiThreadedConvolution#process(RandomAccessible, RandomAccessibleInterval, ExecutorService, int)}
 *
 * @author Matthias Arzt
 */
@Deprecated
public abstract class AbstractMultiThreadedConvolution< T > implements Convolution< T >
{

	private ExecutorService executor;

	abstract protected void process( RandomAccessible< ? extends T > source,
			RandomAccessibleInterval< ? extends T > target,
			ExecutorService executorService,
			int numThreads );

	@Override
	public void setExecutor( final ExecutorService executor )
	{
		this.executor = executor;
	}

	@Override
	final public void process( final RandomAccessible< ? extends T > source, final RandomAccessibleInterval< ? extends T > target )
	{
		if ( executor == null )
		{
			final int numThreads = Runtime.getRuntime().availableProcessors();
			final ExecutorService executor = Executors.newFixedThreadPool( numThreads );
			try
			{
				process( source, target, executor, numThreads );
			}
			finally
			{
				executor.shutdown();
			}
		}
		else
		{
			process( source, target, executor, getNumThreads( executor ) );
		}
	}

	static int getNumThreads( final ExecutorService executor )
	{
		int maxPoolSize = ( executor instanceof ThreadPoolExecutor ) ?
				( ( ThreadPoolExecutor ) executor ).getMaximumPoolSize() :
				Integer.MAX_VALUE;
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		return Math.max(1, Math.min(availableProcessors, maxPoolSize));
	}

}
