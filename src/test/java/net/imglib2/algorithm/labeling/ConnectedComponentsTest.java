/*
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

package net.imglib2.algorithm.labeling;

import net.imglib2.Interval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.algorithm.util.TestImages;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement.EIGHT_CONNECTED;
import static net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement.FOUR_CONNECTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests {@link ConnectedComponents}
 *
 * @author Lee Kamentsky
 * @author Tobias Pietzsch
 */
public class ConnectedComponentsTest
{

	private ExecutorService executorService;

	@Before
	public void before()
	{
		executorService = Executors.newFixedThreadPool( 4 );
	}

	@After
	public void after()
	{
		executorService.shutdown();
	}

	@Test
	public void testEmpty()
	{
		final Img<BitType> input = ArrayImgs.bits( 3, 3 );
		final Img<IntType> expected = ArrayImgs.ints( 3, 3 );
		test( input, expected, FOUR_CONNECTED );
		test( input, expected, EIGHT_CONNECTED );
	}

	@Test
	public void testOne()
	{
		final Img<BitType> input = createBitTypeImage( new boolean[][] { //
				{ false, false, false }, //
				{ false, true, false }, //
				{ false, false, false } } );
		final Img<IntType> expected = ArrayImgs.ints( new int[] { //
				0, 0, 0, //
				0, 1, 0, //
				0, 0, 0  //
		}, 3, 3 );
		test( input, expected, FOUR_CONNECTED );
		test( input, expected, EIGHT_CONNECTED );
	}

	@Test
	public void testOutOfBounds()
	{
		/*
		 * Make sure that the labeler can handle out of bounds conditions
		 */
		final long[][] offsets = new long[][] { { 0, 0 }, { 1, 0 }, { 2, 0 }, { 0, 1 }, { 2, 1 }, { 0, 2 }, { 1, 2 }, { 2, 2 } };
		for ( final long[] offset : offsets )
		{
			final Img<BitType> input = ArrayImgs.bits( 3, 3 );
			final Img<IntType> expected = ArrayImgs.ints( 3, 3 );
			input.getAt( offset[ 0 ], offset[ 1 ] ).set( true );
			expected.getAt( offset[ 0 ], offset[ 1 ] ).set( 1 );
			test( input, expected, FOUR_CONNECTED );
			test( input, expected, EIGHT_CONNECTED );
		}
	}

	@Test
	public void testOneObject()
	{
		final Img<BitType> input2 = createBitTypeImage( new boolean[][] { //
				{ false, false, false, false, false }, //
				{ false, true, true, true, false }, //
				{ false, true, true, true, false }, //
				{ false, true, true, true, false }, //
				{ false, false, false, false, false } } );
		final Img<IntType> expected2 = ArrayImgs.ints( new int[] { //
				0, 0, 0, 0, 0, //
				0, 1, 1, 1, 0, //
				0, 1, 1, 1, 0, //
				0, 1, 1, 1, 0, //
				0, 0, 0, 0, 0  //
		}, 5, 5 );
		test( input2, expected2, FOUR_CONNECTED );
		test( input2, expected2, EIGHT_CONNECTED );
	}

	@Test
	public void testTwoObjects()
	{
		final Img<BitType> input2 = createBitTypeImage( new boolean[][] { //
				{ false, false, false, false, false }, //
				{ false, true, true, true, false }, //
				{ false, false, false, false, false }, //
				{ false, true, true, true, false }, //
				{ false, false, false, false, false } } );
		final Img<IntType> expected2 = ArrayImgs.ints( new int[] { //
				0, 0, 0, 0, 0, //
				0, 1, 1, 1, 0, //
				0, 0, 0, 0, 0, //
				0, 2, 2, 2, 0, //
				0, 0, 0, 0, 0  //
		}, 5, 5 );
		test( input2, expected2, FOUR_CONNECTED );
		test( input2, expected2, EIGHT_CONNECTED );
	}

	@Test
	public void testBigObject()
	{
		final Img<BitType> input = TestImages.bits2d( 25, 25, ( x, y ) -> true );
		final Img<IntType> expected = TestImages.ints2d( 25, 25, ( x, y ) -> 1 );
		test( input, expected, FOUR_CONNECTED );
		test( input, expected, EIGHT_CONNECTED );
	}

	@Test
	public void testBigBigObject()
	{
		final Img<BitType> input = TestImages.bits2d( 100, 100, ( x, y ) -> true );
		final Img<IntType> expected = TestImages.ints2d( 100, 100, ( x, y ) -> 1 );
		test( input, expected, FOUR_CONNECTED );
		test( input, expected, EIGHT_CONNECTED );
	}

	final Img<IntType> input = ArrayImgs.ints( new int[] { //
			0, 0, 0, 0, 0, //
			0, 1, 1, 1, 0, //
			1, 0, 0, 0, 0, //
			0, 1, 1, 1, 0, //
			0, 0, 0, 0, 1  //
	}, 5, 5 );

	final Img<IntType> expectedFourConnected = ArrayImgs.ints( new int[] { //
			0, 0, 0, 0, 0, //
			0, 1, 1, 1, 0, //
			3, 0, 0, 0, 0, //
			0, 2, 2, 2, 0, //
			0, 0, 0, 0, 4  //
	}, 5, 5 );

	final Img<IntType> expectedEightConnected = ArrayImgs.ints( new int[] { //
			0, 0, 0, 0, 0, //
			0, 1, 1, 1, 0, //
			1, 0, 0, 0, 0, //
			0, 1, 1, 1, 0, //
			0, 0, 0, 0, 1  //
	}, 5, 5 );

	@Test
	public void testFourConnected()
	{
		test( input, expectedFourConnected, FOUR_CONNECTED );
	}

	@Test
	public void testEightConnected()
	{
		test( input, expectedEightConnected, EIGHT_CONNECTED );
	}

	private <T extends IntegerType<T>> void test( Img<T> input, Img<IntType> expected, StructuringElement se )
	{
		final ImgLabeling<Integer, ?> labeling = createLabeling( input );
		ConnectedComponents.labelAllConnectedComponents( input, labeling, new IntegerIterator(), se );
		checkResult( expected, labeling );
	}

	@Test
	public void testExecutorService()
	{
		final ImgLabeling<Integer, ?> labeling = createLabeling( input );
		ConnectedComponents.labelAllConnectedComponents( input, labeling, new IntegerIterator(), FOUR_CONNECTED, executorService );
		checkResult( expectedFourConnected, labeling );
	}

	@Test
	public void testOutputIntType()
	{
		Img<IntType> output = ArrayImgs.ints( Intervals.dimensionsAsLongArray( input ) );
		ConnectedComponents.labelAllConnectedComponents( input, output, FOUR_CONNECTED );
		checkResult( expectedFourConnected, output );
	}

	@Test
	public void testOutputIntTypeAndExecutorService()
	{
		Img<IntType> output = ArrayImgs.ints( Intervals.dimensionsAsLongArray( input ) );
		ConnectedComponents.labelAllConnectedComponents( input, output, FOUR_CONNECTED, executorService );
		checkResult( expectedFourConnected, output );
	}

	private <T extends IntegerType<T>> ImgLabeling<Integer, ?> createLabeling( Interval interval )
	{
		return new ImgLabeling<>( ArrayImgs.unsignedShorts( Intervals.dimensionsAsLongArray( interval ) ) );
	}

	private Img<BitType> createBitTypeImage( boolean[][] input )
	{
		int width = input.length;
		int height = input[ 0 ].length;
		return TestImages.bits2d( width, height, ( x, y ) -> input[ y ][ x ] );
	}

	private void checkResult( Img<IntType> expected, ImgLabeling<Integer, ?> labeling )
	{
		final HashMap<Integer, Integer> map = new HashMap<>();
		LoopBuilder.setImages( expected, labeling ).forEachPixel( ( expectedPixel, labels ) -> {
			final int expectedValue = expectedPixel.get();
			if ( expectedValue == 0 )
				assertEquals( labels.size(), 0 );
			else
			{
				assertEquals( labels.size(), 1 );
				final Integer value = labels.iterator().next();
				if ( map.containsKey( value ) )
					assertEquals( expectedValue, map.get( value ).intValue() );
				else
					map.put( value, expectedValue );
			}
		} );
	}

	private void checkResult( Img<IntType> expected, Img<IntType> labeling )
	{
		final HashMap<Integer, Integer> map = new HashMap<>();
		LoopBuilder.setImages( expected, labeling ).forEachPixel( ( expectedPixel, labels ) -> {
			final int expectedValue = expectedPixel.get();
			final int labelValue = labels.get();
			if ( expectedValue == 0 )
				assertEquals( 0, labelValue );
			else
			{
				assertNotEquals( 0, labelValue );
				if ( map.containsKey( labelValue ) )
					assertEquals( expectedValue, map.get( labelValue ).intValue() );
				else
					map.put( labelValue, expectedValue );
			}
		} );
	}

	private static class IntegerIterator implements Iterator<Integer>
	{
		private int i = 1;

		@Override
		public boolean hasNext()
		{
			return true;
		}

		@Override
		public Integer next()
		{
			return i++;
		}

		@Override
		public void remove()
		{
		}
	}
}
