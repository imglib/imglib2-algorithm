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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link HoughTransforms#voteLines}, {@link HoughTransforms#pickPeaks},
 * {@link HoughTransforms#getSlope}, and {@link HoughTransforms#getIntercept}.
 *
 * @author Yili Zhao
 * @author Gabe Selzer
 */
public class HoughLineTransformTest
{

	private BufferedImage groundTruth;

	private Img< UnsignedByteType > tpImg;

	public static int N_THREADS = Runtime.getRuntime().availableProcessors();

	@Before
	public void setUp() throws IOException
	{
		groundTruth = ImageIO.read( getClass().getResource( "result9.png" ) );
		tpImg = ArrayImgs.unsignedBytes( 11l, 13l );
		final RandomAccess< UnsignedByteType > randomAccess = tpImg.randomAccess();
		// create vertical line
		randomAccess.setPosition( 5, 0 );
		for ( int i = 0; i < 13; i++ )
		{
			randomAccess.setPosition( i, 1 );
			randomAccess.get().set( 255 );
		}

		// create horizontal line
		randomAccess.setPosition( 5, 1 );
		for ( int i = 0; i < 11; i++ )
		{
			randomAccess.setPosition( i, 0 );
			randomAccess.get().set( 255 );
		}

		// create diagonal line
		for ( int i = 0; i < 11; i++ )
		{
			randomAccess.setPosition( i, 0 );
			randomAccess.setPosition( i / 3, 1 );
			randomAccess.get().set( 255 );
		}
	}

	@Test
	public < T extends RealType< T > > void testHoughLineTransformToTarget() throws IOException
	{
		// create votespace
		final long[] dims = new long[ tpImg.numDimensions() ];
		tpImg.dimensions( dims );
		final long[] outputDims = HoughTransforms.getVotespaceSize( tpImg );
		final Img< UnsignedByteType > votespace = new ArrayImgFactory().create( outputDims, tpImg.firstElement() );

		// run transform
		HoughTransforms.voteLines( tpImg, votespace );

		// compare expected / actual dimensions
		int height = groundTruth.getHeight();
		int width = groundTruth.getWidth();
		assertEquals( "Ground truth and result height do not match.", height, outputDims[ 1 ] );
		assertEquals( "Ground truth and result width do not match.", width, outputDims[ 0 ] );

		DataBufferByte db = ( DataBufferByte ) groundTruth.getData().getDataBuffer();
		byte[] expected = db.getBankData()[ 0 ];

		final RandomAccess< UnsignedByteType > ra = votespace.randomAccess();

		// compare expected / actual results for regression.
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

	@Test
	public < T extends RealType< T > > void testPickPeaks()
	{
		// three expected peaks
		final long[] expected = { -5, -89, -5, -88, 0, -76, 0, -74, 0, -73, 5, -2, 5, -1, 5, 0, 5, 1, 5, 2, 5, 88 };
		int index = 0;

		// create votespace
		final long[] dims = new long[ tpImg.numDimensions() ];
		tpImg.dimensions( dims );
		final long[] outputDims = HoughTransforms.getVotespaceSize( tpImg );
		final Img< UnsignedByteType > votespace = new ArrayImgFactory().create( outputDims, tpImg.firstElement() );

		// run transform
		HoughTransforms.voteLines( tpImg, votespace );

		final UnsignedByteType minPeak = Util.getTypeFromInterval( votespace ).createVariable();
		minPeak.setReal( 10 );

		List< Point > peaks = HoughTransforms.pickLinePeaks( votespace, minPeak );
		for ( Point p : peaks )
		{
			// assert dimension 0
			assertEquals( expected[ index++ ], p.getLongPosition( 0 ), 0 );
			// assert dimension 1
			assertEquals( expected[ index++ ], p.getLongPosition( 1 ), 0 );
		}
	}

	@Test
	public void testEquationConversion()
	{
		final int rho = 2, theta = 45;
		final double slope = HoughTransforms.getSlope( theta );
		final double yint = HoughTransforms.getIntercept( rho, theta );

		assertEquals( -1.0, slope, 1e-8 );
		assertEquals( 2 * Math.sqrt( 2 ), yint, 1e-8 );

		// make sure undefined slope does not throw an error.
		assertEquals( HoughTransforms.getSlope( 0 ), Double.NEGATIVE_INFINITY, 0 );
	}
}
