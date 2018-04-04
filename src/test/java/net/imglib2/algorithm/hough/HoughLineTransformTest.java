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

package net.imglib2.algorithm.hough;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Before;
import org.junit.Test;

public class HoughLineTransformTest
{

	private BufferedImage groundTruth;

	private Img< UnsignedByteType > tpImg;

	public static int N_THREADS = Runtime.getRuntime().availableProcessors();

	@Before
	public void setUp() throws IOException
	{
		groundTruth = ImageIO.read( getClass().getResource( "result8.png" ) );
		tpImg = ArrayImgs.unsignedBytes( 50l, 50l );
		final RandomAccess< UnsignedByteType > randomAccess = tpImg.randomAccess();
		for ( int i = 24; i < 25; ++i )
		{
			for ( int j = 14; j < 35; ++j )
			{
				randomAccess.setPosition( i, 0 );
				randomAccess.setPosition( j, 1 );
				randomAccess.get().set( 255 );
			}
		}
	}

	@Test
	public < T extends RealType< T > & NativeType< T > > void testHoughLineTransformToTarget() throws IOException
	{
		final HoughLineTransform line = HoughLineTransform.integerHoughLine( tpImg );

		boolean success = line.process();
		assertTrue( success );
		final Img< T > result = line.getResult();
		final long[] dims = new long[ result.numDimensions() ];
		result.dimensions( dims );
		final RandomAccess< T > ra = result.randomAccess();

		int height = groundTruth.getHeight();
		int width = groundTruth.getWidth();
		assertEquals( "Ground truth and result height do not match.", height, dims[ 1 ] );
		assertEquals( "Ground truth and result width do not match.", width, dims[ 0 ] );

		DataBufferByte db = ( DataBufferByte ) groundTruth.getData().getDataBuffer();
		byte[] expected = db.getBankData()[ 0 ];

		int index = 0;
		for ( int j = 0; j < height; ++j )
		{
			ra.setPosition( j, 1 );
			for ( int i = 0; i < width; ++i )
			{
				ra.setPosition( i, 0 );

				assertEquals( expected[ index++ ], ra.get().getRealDouble(), 0 );

			}
		}
	}
}
