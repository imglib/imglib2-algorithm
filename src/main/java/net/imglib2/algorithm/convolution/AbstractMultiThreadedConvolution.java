package net.imglib2.algorithm.convolution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract class to help implementing a Convolution, that is multi threaded
 * using an {@link ExecutorService}. This implements the method
 * {@link Convolution#setExecutor(ExecutorService)} and has useful protected
 * methods {@link #getExecutor()} and {@link #getNumThreads()}.
 */
public abstract class AbstractMultiThreadedConvolution< T > implements Convolution< T >
{
	private ExecutorService executor = null;

	@Override
	public void setExecutor( ExecutorService executor )
	{
		this.executor = executor;
	}

	protected ExecutorService getExecutor()
	{
		if ( executor == null )
			executor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
		return executor;
	}

	protected int getNumThreads()
	{
		ExecutorService executor = getExecutor();
		if ( executor instanceof ThreadPoolExecutor )
			return ( ( ThreadPoolExecutor ) executor ).getMaximumPoolSize();
		return Runtime.getRuntime().availableProcessors();
	}
}
