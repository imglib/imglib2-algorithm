/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class GridsTest
{

	private final Interval interval = new FinalInterval( 3, 4, 5 );

	private final int[] blockSize = new int[] { 2, 2, 5 };

	@Test
	public void test()
	{

		final Interval interval = this.interval;

		final long[][] blockMinima = {
				{ 0, 0, 0 },
				{ 2, 0, 0 },
				{ 0, 2, 0 },
				{ 2, 2, 0 }
		};

		final List< long[] > blocks = Grids.collectAllOffsets( Intervals.dimensionsAsLongArray( interval ), blockSize );
		Assert.assertEquals( blockMinima.length, blocks.size() );
		for ( int i = 0; i < blockMinima.length; ++i )
			Assert.assertArrayEquals( blockMinima[ i ], blocks.get( i ) );

		final long[][] gridPositions = {
				{ 0, 0, 0 },
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ 1, 1, 0 }
		};

		final Interval[] intervals = {
				new FinalInterval( new long[] { 0, 0, 0 }, new long[] { 1, 1, 4 } ),
				new FinalInterval( new long[] { 2, 0, 0 }, new long[] { 2, 1, 4 } ),
				new FinalInterval( new long[] { 0, 2, 0 }, new long[] { 1, 3, 4 } ),
				new FinalInterval( new long[] { 2, 2, 0 }, new long[] { 2, 3, 4 } )
		};

		final List< Pair< Interval, long[] > > blocksAndGridPositions = Grids.collectAllContainedIntervalsWithGridPositions( Intervals.dimensionsAsLongArray( interval ), blockSize );
		Assert.assertEquals( intervals.length, blocksAndGridPositions.size() );
		for ( int i = 0; i < intervals.length; ++i )
		{
			final Interval block = blocksAndGridPositions.get( i ).getA();
			// if both contain each other they are equal
			Assert.assertTrue( Intervals.contains( block, intervals[ i ] ) && Intervals.contains( intervals[ i ], block ) );

			final long[] position = blocksAndGridPositions.get( i ).getB();
			Assert.assertArrayEquals( gridPositions[ i ], position );
		}
	}

	@Test
	public void testWithOffset()
	{

		final long[] offset = { 101, 37, -13 };
		final Interval interval = Intervals.translate( Intervals.translate( Intervals.translate( this.interval, offset[ 0 ], 0 ), offset[ 1 ], 1 ), offset[ 2 ], 2 );

		final long[][] blockMinima = {
				{ offset[ 0 ] + 0, offset[ 1 ] + 0, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 2, offset[ 1 ] + 0, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 0, offset[ 1 ] + 2, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 2, offset[ 1 ] + 2, offset[ 2 ] + 0 },
		};

		final List< long[] > blocks = Grids.collectAllOffsets( Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize );
		Assert.assertEquals( blockMinima.length, blocks.size() );
		for ( int i = 0; i < blockMinima.length; ++i )
			Assert.assertArrayEquals( blockMinima[ i ], blocks.get( i ) );

		final long[][] gridPositions = {
				{ 0, 0, 0 },
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ 1, 1, 0 }
		};

		final Interval[] intervals = {
				new FinalInterval( blockMinima[ 0 ], add( blockMinima[ 0 ], new long[] { 1, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 1 ], add( blockMinima[ 1 ], new long[] { 0, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 2 ], add( blockMinima[ 2 ], new long[] { 1, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 3 ], add( blockMinima[ 3 ], new long[] { 0, 1, 4 } ) )
		};

		final List< Pair< Interval, long[] > > blocksAndGridPositions = Grids.collectAllContainedIntervalsWithGridPositions( Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize );
		Assert.assertEquals( intervals.length, blocksAndGridPositions.size() );
		for ( int i = 0; i < intervals.length; ++i )
		{
			final Interval block = blocksAndGridPositions.get( i ).getA();
			// if both contain each other they are equal
			Assert.assertTrue( Intervals.contains( block, intervals[ i ] ) && Intervals.contains( intervals[ i ], block ) );

			final long[] position = blocksAndGridPositions.get( i ).getB();
			Assert.assertArrayEquals( gridPositions[ i ], position );
		}
	}

	@Test
	public void testRandomAccess()
	{
		final ArrayImg< BitType, LongArray > data = ArrayImgs.bits( 1, 2, 3, 4, 5 );
		data.forEach( BitType::setZero );
		final IntervalView< BitType > translated = Views.translate( data, 5, 4, 3, 2, 1 );
		final RandomAccess< BitType > access = translated.randomAccess();
		final UnsignedIntType count = new UnsignedIntType();
		Grids.forEachOffset(
				Intervals.minAsLongArray( translated ),
				Intervals.maxAsLongArray( translated ),
				IntStream.generate( () -> 1 ).limit( data.numDimensions() ).toArray(),
				access,
				() -> {
					count.inc();
					access.get().setOne();
				} );

		Assert.assertEquals( Intervals.numElements( data ), count.get() );
		data.forEach( v -> Assert.assertTrue( v.get() ) );
	}

	private static long[] add( final long[] arr, final long[] add )
	{
		final long[] result = new long[ arr.length ];
		for ( int d = 0; d < result.length; ++d )
			result[ d ] = arr[ d ] + add[ d ];
		return result;
	}

	@Test( expected = AssertionError.class )
	public void testZeroBlockSizeAssertion()
	{
		Grids.forEachOffset( new long[] { 0, 0 }, new long[] { 1, 1 }, new int[] { 1, 0 }, arr -> {} );
	}

	@Test( expected = AssertionError.class )
	public void testMinDimensionalityMismatchAssertion()
	{
		Grids.forEachOffset( new long[] { 0, 0, 0 }, new long[] { 1, 1 }, new int[] { 1, 1 }, arr -> {} );
	}

	@Test( expected = AssertionError.class )
	public void testMaxDimensionalityMismatchAssertion()
	{
		Grids.forEachOffset( new long[] { 0, 0 }, new long[] { 1, 1, 1 }, new int[] { 1, 1 }, arr -> {} );
	}

	@Test( expected = AssertionError.class )
	public void testMinLargerThanMaxAssertion()
	{
		Grids.forEachOffset( new long[] { 0, 2 }, new long[] { 1, 1 }, new int[] { 1, 1 }, arr -> {} );
	}

}
