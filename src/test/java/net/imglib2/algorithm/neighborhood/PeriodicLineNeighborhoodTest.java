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
package net.imglib2.algorithm.neighborhood;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import net.imglib2.algorithm.neighborhood.PeriodicLineNeighborhood;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Before;
import org.junit.Test;

public class PeriodicLineNeighborhoodTest
{

	private int[][][] expected;

	private ArrayImg< UnsignedByteType, ByteArray > img;

	private long[][] positions;

	private long[] spans;

	private int[][] increments;

	private PeriodicLineNeighborhood< UnsignedByteType >[] neighborhoods;

	@SuppressWarnings( "unchecked" )
	@Before
	public void setUp() throws Exception
	{
		img = ArrayImgs.unsignedBytes( 64, 64 );
		final ArrayRandomAccess< UnsignedByteType > sourceRandomAccess = img.randomAccess();
		positions = new long[][] { { 20, 20 }, { 40, 40 } };
		spans = new long[] { 3, 2 };
		increments = new int[][] { { 1, 2 }, { 3, -1 } };
		neighborhoods = new PeriodicLineNeighborhood[ spans.length ];

		for ( int i = 0; i < spans.length; i++ )
		{
			final PeriodicLineNeighborhood< UnsignedByteType > ln = new PeriodicLineNeighborhood< UnsignedByteType >( positions[ i ], spans[ i ], increments[ i ], sourceRandomAccess );
			for ( final UnsignedByteType t : ln )
			{
				t.setInteger( 255 );
			}
			neighborhoods[ i ] = ln;
		}

		expected = new int[][][] { { { 17, 14 }, { 18, 16 }, { 19, 18 }, { 20, 20 }, { 21, 22 }, { 22, 24 }, { 23, 26 } }, { { 34, 42 }, { 37, 41 }, { 40, 40 }, { 43, 39 }, { 46, 38 } } };
	}

	@Test
	public void testNIterated()
	{
		final ArrayCursor< UnsignedByteType > cursor = img.cursor();

		// How many pixels are on?
		int sum = 0;
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			if ( cursor.get().get() > 0 )
				sum++;
		}

		int expectedPix = 0;
		for ( final long span : spans )
		{
			expectedPix += 2 * span + 1;
		}
		assertEquals( expectedPix, sum );
	}

	@Test
	public void testIteration()
	{
		// One way: are the expected pixels on?
		final ArrayRandomAccess< UnsignedByteType > ra = img.randomAccess();
		for ( final int[][] group : expected )
		{
			for ( final int[] coord : group )
			{
				ra.setPosition( coord );
				assertTrue( ra.get().get() > 0 );
			}
		}

		// The other way: if a pixel is on, is it expected?
		final ArrayCursor< UnsignedByteType > cursor = img.cursor();
		final int[] pos = new int[ img.numDimensions() ];
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			if ( cursor.get().get() > 0 )
			{
				cursor.localize( pos );
				boolean found = false;
				for ( final int[][] group : expected )
				{
					for ( final int[] coord : group )
					{
						if ( Arrays.equals( pos, coord ) )
						{
							found = true;
							break;
						}
					}
				}
				assertTrue( found );
			}

		}
	}

	@Test
	public void testSize()
	{
		for ( int i = 0; i < neighborhoods.length; i++ )
		{
			final PeriodicLineNeighborhood< UnsignedByteType > neighborhood = neighborhoods[ i ];
			assertEquals( 2 * spans[ i ] + 1, neighborhood.size() );
		}
	}

	@Test
	public void testMinInt()
	{
		for ( int i = 0; i < neighborhoods.length; i++ )
		{
			final PeriodicLineNeighborhood< UnsignedByteType > neighborhood = neighborhoods[ i ];
			for ( int d = 0; d < img.numDimensions(); d++ )
			{
				assertEquals( positions[ i ][ d ] - spans[ i ] * Math.abs( increments[ i ][ d ] ), neighborhood.min( d ) );
			}
		}
	}

	@Test
	public void testMaxInt()
	{
		for ( int i = 0; i < neighborhoods.length; i++ )
		{
			final PeriodicLineNeighborhood< UnsignedByteType > neighborhood = neighborhoods[ i ];
			for ( int d = 0; d < img.numDimensions(); d++ )
			{
				assertEquals( positions[ i ][ d ] + spans[ i ] * Math.abs( increments[ i ][ d ] ), neighborhood.max( d ) );
			}
		}
	}
}
