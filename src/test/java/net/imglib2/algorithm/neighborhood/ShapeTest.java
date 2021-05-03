/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import static org.junit.Assert.assertTrue;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test default implementations of {@link Shape}.
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ShapeTest
{

	protected Shape shape;

	protected ArrayImg< UnsignedShortType, ShortArray > img;

	@Before
	public void setUp() throws Exception
	{
		final long[] dims = new long[] { 10, 10, 10 };
		this.img = ArrayImgs.unsignedShorts( dims );
		this.shape = new Shape()
		{

			@Override
			public < T > IterableInterval< Neighborhood< T > > neighborhoods( RandomAccessibleInterval< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

			@Override
			public < T > RandomAccessible< Neighborhood< T > > neighborhoodsRandomAccessible( RandomAccessible< T > source )
			{
				final RectangleNeighborhoodFactory< T > f = RectangleNeighborhoodUnsafe.< T >factory();
				final Interval spanInterval = new FinalInterval( new long[] { -3, -3, -3 }, new long[] { 3, 3, 3 } );
				return new NeighborhoodsAccessible<>( source, spanInterval, f );
			}

			@Override
			public < T > IterableInterval< Neighborhood< T > > neighborhoodsSafe( RandomAccessibleInterval< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

			@Override
			public < T > RandomAccessible< Neighborhood< T > > neighborhoodsRandomAccessibleSafe( RandomAccessible< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

		};
	}

	@Test
	public void testStructuringElementBoundingBox()
	{
		Interval boundingBox = shape.getStructuringElementBoundingBox(
				img.numDimensions() );
		assertTrue( Intervals.equals( boundingBox, new FinalInterval(
				new long[] { -3, -3, -3 }, new long[] { 3, 3, 3 } ) ) );
	}
}
