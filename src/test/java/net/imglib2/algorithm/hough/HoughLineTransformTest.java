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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

public class HoughLineTransformTest
{

	private BufferedImage groundTruth;

	private Img< UnsignedByteType > tpImg;

	public static int N_THREADS = Runtime.getRuntime().availableProcessors();

	public static ExecutorService ES = Executors.newFixedThreadPool( N_THREADS );

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
				randomAccess.setPosition( new int[] { i, j } );
				randomAccess.get().set( 255 );
			}
		}
	}

	@Test
	public < T extends RealType< T > & NativeType< T > > void testHoughLineTransformToTarget() throws IOException
	{
		final HoughLineTransform line = HoughLineTransform.integerHoughLine( tpImg, ES );

		if ( line.process() )
		{
			final Img< T > result = line.getResult();
			final long[] dims = new long[ result.numDimensions() ];
			result.dimensions( dims );
			final RandomAccess< T > ra = result.randomAccess();

			assertEquals( "Ground truth and result height do not match.", groundTruth.getHeight(), dims[ 1 ] );
			assertEquals( "Ground truth and result width do not match.", groundTruth.getWidth(), dims[ 0 ] );

			for ( long i = 0; i < groundTruth.getWidth(); ++i )
			{
				for ( long j = 0; j < groundTruth.getHeight(); ++j )
				{
					ra.setPosition( new long[] { i, j } );

					assertEquals( "Ground truth and result pixel do not match at " + Util.printCoordinates( ra ) + ".", groundTruth.getData().getSample( ( int ) i, ( int ) j, 0 ), ra.get().getRealDouble(), 0 );
				}
			}
		}
	}
}
