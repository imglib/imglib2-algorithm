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

import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class DiamondStrelTest
{

	@Test
	public final void test2Doptimization()
	{
		// We test that we get the same results whether we optimize or not
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 39, 39 );
		final ArrayRandomAccess< UnsignedByteType > ra = img.randomAccess();
		ra.setPosition( new long[] { 19, 19 } );
		ra.get().set( 255 );

		final int[] radiuses = new int[] { 2, 5, 7 };
		for ( final int radius : radiuses )
		{
			final List< Shape > strelStd = StructuringElements.diamond( radius, img.numDimensions(), false );
			final Img< UnsignedByteType > std = Dilation.dilate( img, strelStd, 1 );
			final List< Shape > strelOpt = StructuringElements.diamond( radius, img.numDimensions(), true );
			final Img< UnsignedByteType > opt = Dilation.dilate( img, strelOpt, 1 );

			final Cursor< UnsignedByteType > cStd = std.cursor();
			final RandomAccess< UnsignedByteType > raOpt = opt.randomAccess( opt );
			while ( cStd.hasNext() )
			{
				cStd.fwd();
				raOpt.setPosition( cStd );
				assertEquals( cStd.get().get(), raOpt.get().get() );
			}
		}

	}

	@Test
	public final void test3Doptimization()
	{
		// We test that we get the same results whether we optimize or not
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 19, 19, 19 );
		final ArrayRandomAccess< UnsignedByteType > ra = img.randomAccess();
		ra.setPosition( new long[] { 9, 9, 9 } );
		ra.get().set( 255 );

		final int[] radiuses = new int[] { 2, 3, 5 };
		for ( final int radius : radiuses )
		{
			final List< Shape > strelStd = StructuringElements.diamond( radius, img.numDimensions(), false );
			final Img< UnsignedByteType > std = Dilation.dilate( img, strelStd, 1 );
			final List< Shape > strelOpt = StructuringElements.diamond( radius, img.numDimensions(), true );
			final Img< UnsignedByteType > opt = Dilation.dilate( img, strelOpt, 1 );

			final Cursor< UnsignedByteType > cStd = std.cursor();
			final RandomAccess< UnsignedByteType > raOpt = opt.randomAccess( opt );
			while ( cStd.hasNext() )
			{
				cStd.fwd();
				raOpt.setPosition( cStd );
				assertEquals( cStd.get().get(), raOpt.get().get() );
			}
		}

	}

}
