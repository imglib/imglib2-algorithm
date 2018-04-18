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

package net.imglib2.algorithm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Utility methods to parallelize image processing tasks in blocks
 *
 */
public class ParallelizeOverBlocks
{

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param interval
	 * @param blockSize
	 * @param es
	 * @param numTasks
	 * @return {@link List} of results of computation. Note that for
	 *         computations that return void, this should be a list of
	 *         {@link Void}.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T > List< T > parallelizeAndWait(
			final Function< Interval, T > func,
			final Interval interval,
			final int[] blockSize,
			final ExecutorService es,
			final int numTasks ) throws InterruptedException, ExecutionException
	{
		return parallelizeAndWait( func, Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize, es, numTasks );
	}

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param interval
	 * @param blockSize
	 * @param es
	 * @param numTasks
	 * @return List of futures of the submitted tasks. Each future contains a
	 *         list of results.
	 */
	public static < T > List< Future< List< T > > > parallelize(
			final Function< Interval, T > func,
			final Interval interval,
			final int[] blockSize,
			final ExecutorService es,
			final int numTasks )
	{
		return parallelize( func, Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize, es, numTasks );
	}

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param min
	 * @param max
	 * @param blockSize
	 * @param es
	 * @param numTasks
	 * @return {@link List} of results of computation. Note that for
	 *         computations that return void, this should be a list of
	 *         {@link Void}.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T > List< T > parallelizeAndWait(
			final Function< Interval, T > func,
			final long[] min,
			final long[] max,
			final int[] blockSize,
			final ExecutorService es,
			final int numTasks ) throws InterruptedException, ExecutionException
	{
		final List< Future< List< T > > > futures = parallelize( func, min, max, blockSize, es, numTasks );
		final List< T > results = new ArrayList<>();
		for ( final Future< List< T > > future : futures )
			results.addAll( future.get() );
		return results;
	}

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param min
	 * @param max
	 * @param blockSize
	 * @param es
	 * @param numTasks
	 * @return List of futures of the submitted tasks. Each future contains a
	 *         list of results.
	 */
	public static < T > List< Future< List< T > > > parallelize(
			final Function< Interval, T > func,
			final long[] min,
			final long[] max,
			final int[] blockSize,
			final ExecutorService es,
			final int numTasks )
	{
		final List< Interval > blocks = Grids.collectAllOffsets( min, max, blockSize, blockMin -> {
			final long[] blockMax = new long[ blockMin.length ];
			for ( int d = 0; d < blockMax.length; ++d )
				blockMax[ d ] = Math.min( blockMin[ d ] + blockSize[ d ] - 1, max[ d ] );
			return new FinalInterval( blockMin, blockMax );
		} );
		return parallelize( func, blocks, es, numTasks );
	}

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param blocks
	 * @param es
	 * @param numTasks
	 * @return {@link List} of results of computation. Note that for
	 *         computations that return void, this should be a list of
	 *         {@link Void}.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T > List< T > parallelizeAndWait(
			final Function< Interval, T > func,
			final List< Interval > blocks,
			final ExecutorService es,
			final int numTasks ) throws InterruptedException, ExecutionException
	{
		final List< Future< List< T > > > futures = parallelize( func, blocks, es, numTasks );
		final List< T > results = new ArrayList<>();
		for ( final Future< List< T > > future : futures )
			results.addAll( future.get() );
		return results;
	}

	/**
	 *
	 * Submit blocked tasks and wait for execution.
	 *
	 * @param func
	 *            {@link Function} to be applied to each block as specified by
	 *            first parameter {@link Interval}.
	 * @param blocks
	 * @param es
	 * @param numTasks
	 * @return List of futures of the submitted tasks. Each future contains a
	 *         list of results.
	 */
	public static < T > List< Future< List< T > > > parallelize(
			final Function< Interval, T > func,
			final List< Interval > blocks,
			final ExecutorService es,
			final int numTasks )
	{
		final ArrayList< Future< List< T > > > futures = new ArrayList<>();
		final int taskSize = Math.max( blocks.size() / numTasks, 1 );
		for ( int i = 0; i < blocks.size(); ++i )
		{
			final int finalI = i;
			futures.add( es.submit( () -> blocks.subList( finalI, finalI + taskSize ).stream().map( func ).collect( Collectors.toList() ) ) );
		}

		return futures;
	}

	/**
	 * Utility method to turn a {@link Consumer} into a {@link Function}.
	 *
	 * @param consumer
	 * @return
	 */
	public static < T > Function< T, Void > ofConsumer( final Consumer< T > consumer )
	{
		return t -> {
			consumer.accept( t );
			return null;
		};
	}

}
