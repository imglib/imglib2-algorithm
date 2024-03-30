/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

	@Deprecated
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
