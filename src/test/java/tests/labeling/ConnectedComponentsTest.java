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

package tests.labeling;

import static net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement.EIGHT_CONNECTED;
import static net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement.FOUR_CONNECTED;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.Test;

/**
 * TODO
 *
 * @author Lee Kamentsky
 * @author Tobias Pietzsch
 */
public class ConnectedComponentsTest
{
	private void test2D( final boolean[][] input, final int[][] expected, final StructuringElement se, final int start, final int background )
	{
		final long[] dimensions = new long[] { input.length, input[ 0 ].length };
		final Img< BitType > image = ArrayImgs.bits( dimensions );
		final Img< UnsignedShortType > indexImg = ArrayImgs.unsignedShorts( dimensions );
		final ImgLabeling< Integer, UnsignedShortType > labeling = new ImgLabeling< Integer, UnsignedShortType >( indexImg );

		/*
		 * Fill the image.
		 */
		final Cursor< BitType > c = image.localizingCursor();
		final int[] position = new int[ 2 ];
		while ( c.hasNext() )
		{
			final BitType t = c.next();
			c.localize( position );
			t.set( input[ position[ 0 ] ][ position[ 1 ] ] );
		}
		/*
		 * Run the algorithm.
		 */
		final Iterator< Integer > names = new Iterator< Integer >()
		{
			private int i = start;

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
			{}
		};
		ConnectedComponents.labelAllConnectedComponents( image, labeling, names, se );

		/*
		 * Check the result
		 */
		final Cursor< LabelingType< Integer > > lc = labeling.localizingCursor();
		final HashMap< Integer, Integer > map = new HashMap< Integer, Integer >();
		while ( lc.hasNext() )
		{
			final LabelingType< Integer > labels = lc.next();
			lc.localize( position );
			final int expectedValue = expected[ ( position[ 0 ] ) ][ ( position[ 1 ] ) ];
			if ( expectedValue == background )
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
		}
	}

	@Test
	public void testEmpty()
	{
		final boolean[][] input = new boolean[ 3 ][ 3 ];
		final int[][] expected = new int[ 3 ][ 3 ];
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testOne()
	{
		final boolean[][] input = new boolean[][] { { false, false, false }, { false, true, false }, { false, false, false } };
		final int[][] expected = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testOutOfBounds()
	{
		/*
		 * Make sure that the labeler can handle out of bounds conditions
		 */
		final long[][] offsets = new long[][] { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, { 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };
		for ( final long[] offset : offsets )
		{
			final boolean[][] input = new boolean[ 3 ][ 3 ];
			final int[][] expected = new int[ 3 ][ 3 ];

			input[ ( int ) ( offset[ 0 ] ) + 1 ][ ( int ) ( offset[ 1 ] ) + 1 ] = true;
			expected[ ( int ) ( offset[ 0 ] ) + 1 ][ ( int ) ( offset[ 1 ] ) + 1 ] = 1;
			test2D( input, expected, FOUR_CONNECTED, 1, 0 );
			test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
		}
	}

	@Test
	public void testOneObject()
	{
		final boolean[][] input = new boolean[][] { { false, false, false, false, false }, { false, true, true, true, false }, { false, true, true, true, false }, { false, true, true, true, false }, { false, false, false, false, false } };
		final int[][] expected = new int[][] { { 0, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 }, { 0, 1, 1, 1, 0 }, { 0, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0 } };
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testTwoObjects()
	{
		final boolean[][] input = new boolean[][] { { false, false, false, false, false }, { false, true, true, true, false }, { false, false, false, false, false }, { false, true, true, true, false }, { false, false, false, false, false } };
		final int[][] expected = new int[][] { { 0, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0 }, { 0, 2, 2, 2, 0 }, { 0, 0, 0, 0, 0 } };
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testBigObject()
	{
		final boolean[][] input = new boolean[ 25 ][ 25 ];
		final int[][] expected = new int[ 25 ][ 25 ];
		for ( int i = 0; i < input.length; i++ )
		{
			Arrays.fill( input[ i ], true );
			Arrays.fill( expected[ i ], 1 );
		}
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testBigBigObject()
	{
		final boolean[][] input = new boolean[ 100 ][ 100 ];
		final int[][] expected = new int[ 100 ][ 100 ];
		for ( int i = 0; i < input.length; i++ )
		{
			Arrays.fill( input[ i ], true );
			Arrays.fill( expected[ i ], 1 );
		}
		test2D( input, expected, FOUR_CONNECTED, 1, 0 );
		test2D( input, expected, EIGHT_CONNECTED, 1, 0 );
	}

	@Test
	public void testFourConnected()
	{
		final boolean[][] input = new boolean[][] { { false, false, false, false, false }, { false, true, true, true, false }, { true, false, false, false, false }, { false, true, true, true, false }, { false, false, false, false, true } };
		final int[][] expected4 = new int[][] { { 0, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 }, { 3, 0, 0, 0, 0 }, { 0, 2, 2, 2, 0 }, { 0, 0, 0, 0, 4 } };
		test2D( input, expected4, FOUR_CONNECTED, 1, 0 );

	}

	@Test
	public void testEightConnected()
	{
		final boolean[][] input = new boolean[][] { { false, false, false, false, false }, { false, true, true, true, false }, { true, false, false, false, false }, { false, true, true, true, false }, { false, false, false, false, true } };
		final int[][] expected8 = new int[][] { { 0, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 }, { 1, 0, 0, 0, 0 }, { 0, 1, 1, 1, 0 }, { 0, 0, 0, 0, 1 } };
		test2D( input, expected8, EIGHT_CONNECTED, 1, 0 );
	}
}
