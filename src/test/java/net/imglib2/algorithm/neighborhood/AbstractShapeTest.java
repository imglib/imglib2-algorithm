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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayLocalizingCursor;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractShapeTest
{

	protected final Shape shape;

	protected ArrayImg< UnsignedShortType, ShortArray > img;

	public AbstractShapeTest()
	{
		this.shape = createShape();
	}

	@Before
	public void setUp() throws Exception
	{
		final long[] dims = new long[] { 10, 10, 10 };
		this.img = ArrayImgs.unsignedShorts( dims );
	}

	/**
	 * Instantiates the shape under test.
	 * 
	 * @return a new {@link Shape}.
	 */
	protected abstract Shape createShape();

	/**
	 * Returns {@code true} iff the specified {@code pos} coordinates
	 * are within a neighborhood generated from the Shape under test, and
	 * positioned on {@code center}.
	 * 
	 * @param pos
	 *            the position to test.
	 * @param center
	 *            the neighborhood center.
	 * @return {@code true} if pos is inside the nieghborhood.
	 */
	protected abstract boolean isInside( final long[] pos, final long[] center );

	@Test
	public void testNeighborhoods()
	{
		final IterableInterval< Neighborhood< UnsignedShortType >> neighborhoods = shape.neighborhoods( img );
		for ( final Neighborhood< UnsignedShortType > neighborhood : neighborhoods )
		{
			final Cursor< UnsignedShortType > cursor = neighborhood.localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				if ( !Intervals.contains( img, cursor ) )
				{
					continue;
				}
				cursor.get().inc();
			}
		}

		/*
		 * Dummy test: we just make sure that the central point of the image has
		 * been walked over exactly the shape size times. Because the source
		 * image is larger than the Shape it is the case.
		 */

		final long[] center = new long[ img.numDimensions() ];
		for ( int d = 0; d < center.length; d++ )
		{
			center[ d ] = img.dimension( d ) / 2;
		}

		final ArrayRandomAccess< UnsignedShortType > ra = img.randomAccess();
		ra.setPosition( center );
		final long size = neighborhoods.iterator().next().size();
		assertEquals( "Bad value at image center.", size, ra.get().get() );
	}

	@Test
	public void testNeighborhoodsRandomAccessible()
	{
		final RandomAccessible< Neighborhood< UnsignedShortType >> neighborhoods = shape.neighborhoodsRandomAccessible( img );
		final long[] center = new long[ img.numDimensions() ];
		for ( int d = 0; d < center.length; d++ )
		{
			center[ d ] = img.dimension( d ) / 2;
		}

		final RandomAccess< Neighborhood< UnsignedShortType >> ra = neighborhoods.randomAccess();
		ra.setPosition( center );
		final Neighborhood< UnsignedShortType > neighborhood = ra.get();
		for ( final UnsignedShortType pixel : neighborhood )
		{
			pixel.inc();
		}

		/*
		 * Test we iterated solely over the neighborhood, exactly once.
		 */

		final ArrayLocalizingCursor< UnsignedShortType > cursor = img.localizingCursor();
		final long[] pos = new long[ cursor.numDimensions() ];
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.localize( pos );
			final int expected = isInside( pos, center ) ? 1 : 0;
			assertEquals( "Wrong value at position " + Util.printCoordinates( pos ), expected, cursor.get().get() );
		}

	}


	@Test
	public void testNeighborhoodsSafeBehavior()
	{
		final IterableInterval< Neighborhood< UnsignedShortType >> safe = shape.neighborhoodsSafe( img );
		final Neighborhood< UnsignedShortType > neighborhoodSafe = safe.firstElement();
		final Cursor< UnsignedShortType > csafe1 = neighborhoodSafe.cursor();
		final Cursor< UnsignedShortType > csafe2 = neighborhoodSafe.cursor();
		assertNotEquals( "The two cursors from the safe iterator are the same object.", csafe1, csafe2 );

		final IterableInterval< Neighborhood< UnsignedShortType >> unsafe = shape.neighborhoods( img );
		final Neighborhood< UnsignedShortType > neighborhoodUnsafe = unsafe.firstElement();
		final Cursor< UnsignedShortType > cunsafe1 = neighborhoodUnsafe.cursor();
		final Cursor< UnsignedShortType > cunsafe2 = neighborhoodUnsafe.cursor();
		assertEquals( "The two cursors from the unsafe iterator are not the same object.", cunsafe1, cunsafe2 );

		final RandomAccessible< Neighborhood< UnsignedShortType >> safeRA = shape.neighborhoodsRandomAccessibleSafe( img );
		final Neighborhood< UnsignedShortType > neighborhoodSafeRA = safeRA.randomAccess().get();
		final Cursor< UnsignedShortType > crasafe1 = neighborhoodSafeRA.cursor();
		final Cursor< UnsignedShortType > crasafe2 = neighborhoodSafeRA.cursor();
		assertNotEquals( "The two cursors from the safe iterator are the same object.", crasafe1, crasafe2 );

		final RandomAccessible< Neighborhood< UnsignedShortType >> unsafeRA = shape.neighborhoodsRandomAccessible( img );
		final Neighborhood< UnsignedShortType > neighborhoodUnsafeRA = unsafeRA.randomAccess().get();
		final Cursor< UnsignedShortType > craunsafe1 = neighborhoodUnsafeRA.cursor();
		final Cursor< UnsignedShortType > craunsafe2 = neighborhoodUnsafeRA.cursor();
		assertEquals( "The two cursors from the unsafe iterator are not the same object.", craunsafe1, craunsafe2 );
	}
	
	@Test
	public void testJumpFwd()
	{
		// cursor which will be moved via .jumpFwd()
		final Cursor< Neighborhood< UnsignedShortType >> cNeigh1 =
				shape.neighborhoodsSafe( img ).localizingCursor();
		// cursor which will be moved via .fwd() for reference
		final Cursor< Neighborhood< UnsignedShortType >> cNeigh2 =
				shape.neighborhoodsSafe( img ).localizingCursor();

		final long[] dims1 = new long[ cNeigh1.numDimensions() ];
		final long[] dims2 = new long[ cNeigh2.numDimensions() ];

		while ( cNeigh1.hasNext() )
		{
			cNeigh1.jumpFwd( 1 );
			cNeigh2.fwd();

			cNeigh1.localize( dims1 );
			cNeigh2.localize( dims2 );
			assertArrayEquals( "Incorrect position for jumpFwd()", dims2, dims1 );
		}
	}

	@Test
	public void testNegativeIndices()
	{
		// cursor which will be moved via .jumpFwd()
		final Cursor< Neighborhood< UnsignedShortType > > c = shape.neighborhoods( img ).cursor();
		final long[] pos = new long[ img.numDimensions() ];

		c.jumpFwd( -1237 );
		for ( int i = 0; i < 1238; ++i )
			c.jumpFwd( 1 );
		c.localize( pos );
		assertArrayEquals( "Incorrect position for jumpFwd() with negative indices", new long[] {0, 0, 0}, pos );

		c.jumpFwd( -241 );
		for ( int i = 0; i < 241; ++i )
			c.fwd();
		c.localize( pos );
		assertArrayEquals( "Incorrect position for jumpFwd() with negative indices", new long[] {0, 0, 0}, pos );
	}
}
