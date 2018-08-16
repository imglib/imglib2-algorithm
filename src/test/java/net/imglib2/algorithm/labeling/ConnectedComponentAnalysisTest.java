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

package net.imglib2.algorithm.labeling;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class ConnectedComponentAnalysisTest
{

	private final long[] dims2D = new long[] { 5, 4 };

	private final int numElements2D = ( int ) ( dims2D[ 0 ] * dims2D[ 1 ] );

	private final long[] maskData2D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 1, 1,
			0, 0, 1, 1, 1
	};

	private final long[] labelingDiamondShapeUnitRadius2D = new long[] {
			1, 0, 0, 0, 0,
			0, 2, 0, 0, 0,
			0, 0, 0, 3, 3,
			0, 0, 3, 3, 3
	};

	private final long[] labelingDiamondShapeDoubleRadius2D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 2, 2,
			0, 0, 2, 2, 2
	};

	private final long[] labelingRectangleShapeUnitRadius2D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 2, 2,
			0, 0, 2, 2, 2
	};

	private final long[] labelingRectangleShapeDoubleRadius2D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 1, 1,
			0, 0, 1, 1, 1
	};

	private final long[] dims3D = new long[] { 5, 4, 3 };

	private final int numElements3D = ( int ) ( dims2D[ 0 ] * dims2D[ 1 ] * dims3D[ 2 ] );

	private final long[] maskData3D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 1, 1,
			0, 0, 1, 1, 1,

			0, 1, 0, 1, 1,
			1, 0, 0, 0, 0,
			1, 1, 1, 0, 0,
			1, 1, 0, 0, 0,

			0, 1, 0, 1, 1,
			0, 0, 0, 0, 0,
			1, 1, 0, 0, 0,
			1, 0, 0, 0, 1
	};

	private final long[] labelingDiamondShapeUnitRadius3D = new long[] {
			1, 0, 0, 0, 0,
			0, 2, 0, 0, 0,
			0, 0, 0, 3, 3,
			0, 0, 3, 3, 3,

			0, 4, 0, 5, 5,
			6, 0, 0, 0, 0,
			6, 6, 6, 0, 0,
			6, 6, 0, 0, 0,

			0, 4, 0, 5, 5,
			0, 0, 0, 0, 0,
			6, 6, 0, 0, 0,
			6, 0, 0, 0, 7
	};

	private final long[] labelingRectangleShapeUnitRadius3D = new long[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 0, 1, 1,
			0, 0, 1, 1, 1,

			0, 1, 0, 2, 2,
			1, 0, 0, 0, 0,
			1, 1, 1, 0, 0,
			1, 1, 0, 0, 0,

			0, 1, 0, 2, 2,
			0, 0, 0, 0, 0,
			1, 1, 0, 0, 0,
			1, 0, 0, 0, 3
	};

	private final RandomAccessibleInterval< UnsignedLongType > maskStore2D = ArrayImgs.unsignedLongs( maskData2D, dims2D );

	private final RandomAccessibleInterval< BitType > mask2D = Converters.convert( maskStore2D, ( s, t ) -> t.set( s.get() == 1 ), new BitType() );

	private final RandomAccessibleInterval< UnsignedLongType > maskStore3D = ArrayImgs.unsignedLongs( maskData3D, dims3D );

	private final RandomAccessibleInterval< BitType > mask3D = Converters.convert( maskStore3D, ( s, t ) -> t.set( s.get() == 1 ), new BitType() );

	@Test
	public void test2D()
	{
		testDiamondShapeUnitRadius2D();
		testDiamondShapeDoubleRadius2D();

		testDiamondShapeUnitRadius2DWithOffset( 5, 10 );

		testRectangleShapeUnitRadius2D();
		testRectangleShapeDoubleRadius2D();

		testDefault2D();
	}

	@Test
	public void test3D()
	{
		testDiamondShapeUnitRadius3D();

		testRectangleShapeUnitRadius3D();

		testDefault3D();
	}

	private void testDiamondShapeUnitRadius2DWithOffset( final long offX, final long offY )
	{
		final long[] offset = { offX, offY };
		final long[] labelingStore = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims2D );
		ConnectedComponentAnalysis.connectedComponents(
				Views.translate( mask2D, offset ),
				Views.translate( labeling, offset ),
				new DiamondShape( 1 ) );
		Assert.assertArrayEquals( labelingDiamondShapeUnitRadius2D, labelingStore );
	}

	private void testDiamondShapeUnitRadius2D()
	{
		testDiamondShapeUnitRadius2DWithOffset( 0, 0 );
	}

	private void testDiamondShapeDoubleRadius2D()
	{
		final long[] labelingStore = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims2D );
		ConnectedComponentAnalysis.connectedComponents( mask2D, labeling, new DiamondShape( 2 ) );
		Assert.assertArrayEquals( labelingDiamondShapeDoubleRadius2D, labelingStore );
	}

	private void testRectangleShapeUnitRadius2D()
	{
		final long[] labelingStore = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims2D );
		ConnectedComponentAnalysis.connectedComponents( mask2D, labeling, new RectangleShape( 1, true ) );
		Assert.assertArrayEquals( labelingRectangleShapeUnitRadius2D, labelingStore );
	}

	private void testRectangleShapeDoubleRadius2D()
	{
		final long[] labelingStore = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims2D );
		ConnectedComponentAnalysis.connectedComponents( mask2D, labeling, new RectangleShape( 2, true ) );
		Assert.assertArrayEquals( labelingRectangleShapeDoubleRadius2D, labelingStore );
	}

	private void testDefault2D()
	{
		final long[] labelingStore1 = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling1 = ArrayImgs.unsignedLongs( labelingStore1, dims2D );
		ConnectedComponentAnalysis.connectedComponents( mask2D, labeling1, new DiamondShape( 1 ) );

		final long[] labelingStore2 = new long[ numElements2D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling2 = ArrayImgs.unsignedLongs( labelingStore2, dims2D );
		ConnectedComponentAnalysis.connectedComponents( mask2D, labeling2 );

		Assert.assertArrayEquals( labelingStore1, labelingStore2 );
	}

	private void testDiamondShapeUnitRadius3D()
	{
		final long[] labelingStore = new long[ numElements3D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims3D );
		ConnectedComponentAnalysis.connectedComponents( mask3D, labeling, new DiamondShape( 1 ) );
		Assert.assertArrayEquals( labelingDiamondShapeUnitRadius3D, labelingStore );
	}

	private void testRectangleShapeUnitRadius3D()
	{
		final long[] labelingStore = new long[ numElements3D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling = ArrayImgs.unsignedLongs( labelingStore, dims3D );
		ConnectedComponentAnalysis.connectedComponents( mask3D, labeling, new RectangleShape( 1, true ) );
		Assert.assertArrayEquals( labelingRectangleShapeUnitRadius3D, labelingStore );
	}

	private void testDefault3D()
	{
		final long[] labelingStore1 = new long[ numElements3D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling1 = ArrayImgs.unsignedLongs( labelingStore1, dims3D );
		ConnectedComponentAnalysis.connectedComponents( mask3D, labeling1, new DiamondShape( 1 ) );

		final long[] labelingStore2 = new long[ numElements3D ];
		final ArrayImg< UnsignedLongType, LongArray > labeling2 = ArrayImgs.unsignedLongs( labelingStore2, dims3D );
		ConnectedComponentAnalysis.connectedComponents( mask3D, labeling2 );

		Assert.assertArrayEquals( labelingStore1, labelingStore2 );
	}

}
