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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gnu.trove.set.hash.TDoubleHashSet;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class ParallelizeOverBlocksTest
{

	private final Random rng = new Random();

	private final long[] imgSize = { 10, 9, 8, 7 };

	private final int[] blockSize = { 4, 5, 4, 7 };

	private final int numBlocks = 3 * 2 * 2 * 1;

	private final ArrayImg< DoubleType, DoubleArray > data = ArrayImgs.doubles( imgSize );

	private final int numThreads = Math.max( Math.min( Runtime.getRuntime().availableProcessors() - 1, 3 ), 3 );

	private final ExecutorService es = Executors.newFixedThreadPool( numThreads );

	private final int numTasks = 3 * numThreads;

	@Before
	public void setup()
	{
		data.forEach( d -> d.set( rng.nextDouble() ) );
	}

	@After
	public void tearDown()
	{
		this.es.shutdown();
	}

	@Test
	public void ensureDataIsValid()
	{
		final TDoubleHashSet values = new TDoubleHashSet();
		data.forEach( v -> values.add( v.getRealDouble() ) );
		Assert.assertTrue( values.size() > 0 );
	}

	@Test
	public void testGaussian() throws InterruptedException, ExecutionException
	{
		final double sigma = 2;

		final List< Void > results = test( ofThrowingBiConsumer( ( ra, rai ) -> Gauss3.gauss( sigma, ra, rai ) ) );
		Assert.assertEquals( numBlocks, results.size() );
	}

	@Test
	public void testGradient() throws InterruptedException, ExecutionException
	{
		for ( int d = 0; d < data.numDimensions(); ++d )
		{
			final int finalD = d;
			final List< Void > results = test( ofBiConsumer( ( ra, rai ) -> PartialDerivative.gradientCentralDifference( ra, rai, finalD ) ) );
			Assert.assertEquals( numBlocks, results.size() );
		}
	}

	private < T > List< T > test(
			final BiFunction< RandomAccessible< DoubleType >, RandomAccessibleInterval< DoubleType >, T > func )
			throws InterruptedException, ExecutionException
	{

		final RandomAccessible< DoubleType > extended = Views.extendBorder( data );
		final ArrayImg< DoubleType, DoubleArray > comparison = ArrayImgs.doubles( imgSize );
		final ArrayImg< DoubleType, DoubleArray > reference = ArrayImgs.doubles( imgSize );

		func.apply( extended, reference );

		final List< T > results = ParallelizeOverBlocks.parallelizeAndWait(
				interval -> func.apply( extended, Views.interval( comparison, interval ) ),
				Intervals.minAsLongArray( data ),
				Intervals.maxAsLongArray( data ),
				blockSize,
				es,
				numTasks );

		for ( ArrayCursor< DoubleType > ref = reference.cursor(), comp = comparison.cursor(); ref.hasNext(); )
			Assert.assertEquals( ref.next().getRealDouble(), comp.next().getRealDouble(), 0.0 );

		return results;
	}

	@FunctionalInterface
	private static interface ThrowingBiConsumer< T, U >
	{
		public void accept( T t, U u ) throws Exception;

		public default BiConsumer< T, U > asBiConsumer()
		{
			return ( t, u ) -> {
				try
				{
					this.accept( t, u );
				}
				catch ( final Exception e )
				{
					throw new RuntimeException( e );
				}
			};
		}
	}

	private static < T, U > BiFunction< T, U, Void > ofThrowingBiConsumer(
			final ThrowingBiConsumer< T, U > consumer )
	{
		return ofBiConsumer( consumer.asBiConsumer() );
	}

	private static < T, U > BiFunction< T, U, Void > ofBiConsumer(
			final BiConsumer< T, U > consumer )
	{
		return ( t, u ) -> {
			consumer.accept( t, u );
			return null;
		};
	}

}
