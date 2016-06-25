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

import java.util.ArrayList;

import net.imglib2.Interval;
import net.imglib2.algorithm.neighborhood.HorizontalLineNeighborhood;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import org.junit.Test;

public class HorizontalLineNeighborhoodTest
{

	@Test
	public void testBehavior()
	{
		/*
		 * Create.
		 */

		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 64, 64 );

		final long[][] positions = new long[][] { { 20, 20 }, { 40, 40 } };
		final long[] spans = new long[] { 3, 2 };
		final int[] dims = new int[] { 0, 1 };
		final boolean[] skipCenters = new boolean[] { true, false };
		final ArrayRandomAccess< UnsignedByteType > sourceRandomAccess = img.randomAccess();

		final ArrayList< Interval > intervals = new ArrayList< Interval >( 2 );

		for ( int i = 0; i < spans.length; i++ )
		{
			final HorizontalLineNeighborhood< UnsignedByteType > ln = new HorizontalLineNeighborhood< UnsignedByteType >( positions[ i ], spans[ i ], dims[ i ], skipCenters[ i ], sourceRandomAccess );

			for ( final UnsignedByteType t : ln )
			{
				t.setInteger( 255 );
			}

			intervals.add( ln.getStructuringElementBoundingBox() );
		}

		/*
		 * Test
		 */

		final ArrayCursor< UnsignedByteType > cursor = img.cursor();

		while ( cursor.hasNext() )
		{
			cursor.fwd();
			boolean inside = false;
			for ( int i = 0; i < spans.length; i++ )
			{
				if ( !Intervals.contains( intervals.get( i ), cursor ) )
				{
					continue;
				}
				if ( skipCenters[ i ] )
				{
					boolean samePos = true;
					for ( int j = 0; j < img.numDimensions(); j++ )
					{
						if ( positions[ i ][ j ] != cursor.getLongPosition( j ) )
						{
							samePos = false;
							break;
						}
					}
					if ( samePos )
					{
						continue;
					}
				}
				inside = true;
				break;
			}
			if ( inside )
			{
				assertEquals( "At coordinates " + Util.printCoordinates( cursor ), 255, cursor.get().get() );
			}
			else
			{
				assertEquals( "At coordinates " + Util.printCoordinates( cursor ), 0, cursor.get().get() );
			}
		}
	}
}
