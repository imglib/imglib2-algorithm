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

package net.imglib2.algorithm.binary;

import static org.junit.Assert.assertEquals;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

import org.junit.Test;

/**
 * Tests {@link Thresholder}.
 *
 * @author Curtis Rueden
 */
public class ThresholderTest
{
	@Test
	public void testThreshold()
	{
		final Img< UnsignedShortType > source = createImg();
		final int cutoff = 100;
		final UnsignedShortType threshold = new UnsignedShortType( cutoff );
		final boolean above = true;
		final int numThreads = 1;
		final Img< BitType > mask = Thresholder.threshold( source, threshold, above, numThreads );
		final RandomAccess< BitType > maskRA = mask.randomAccess();
		final Cursor< UnsignedShortType > cursor = source.localizingCursor();
		while (cursor.hasNext()) {
			final UnsignedShortType sample = cursor.next();
			maskRA.setPosition( cursor );
			assertEquals( Util.printCoordinates( cursor ), maskRA.get().get(), sample.get() > cutoff );
		}
	}

	private Img< UnsignedShortType > createImg()
	{
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( 200, 200 );
		int count = 0;
		for ( final UnsignedShortType sample : img )
		{
			sample.setInteger( count++ );
		}
		return img;
	}
}
