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
import net.imglib2.Cursor;
import net.imglib2.algorithm.neighborhood.DiamondTipsNeighborhood;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class DiamondTipsNeighorhoodTest
{

	@Test
	public final void testBehavior()
	{
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 10, 10 );
		final DiamondTipsNeighborhood< UnsignedByteType > diamond = new DiamondTipsNeighborhood< UnsignedByteType >( new long[] { 4, 5 }, 3, img.randomAccess() );

		final Cursor< UnsignedByteType > cursor = diamond.cursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().set( 100 );
		}

		// Test
		final long[][] targetPos = new long[][] { { 1, 5 }, { 7, 5 }, { 4, 2 }, { 4, 8 } };
		final ArrayRandomAccess< UnsignedByteType > ra = img.randomAccess();
		for ( int i = 0; i < targetPos.length; i++ )
		{
			final long[] pos = targetPos[ i ];
			ra.setPosition( pos );
			assertEquals( 100, ra.get().get() );
		}

	}

	@Test
	public final void testSize()
	{
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 10, 10 );
		final DiamondTipsNeighborhood< UnsignedByteType > diamond = new DiamondTipsNeighborhood< UnsignedByteType >( new long[] { 4, 5 }, 3, img.randomAccess() );
		assertEquals( 4, diamond.size() );
	}

	@Test
	public final void testIntervals()
	{
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( 10, 10 );
		final DiamondTipsNeighborhood< UnsignedByteType > diamond = new DiamondTipsNeighborhood< UnsignedByteType >( new long[] { 4, 5 }, 3, img.randomAccess() );
		assertEquals( 1, diamond.min( 0 ) );
		assertEquals( 2, diamond.min( 1 ) );
		assertEquals( 7, diamond.max( 0 ) );
		assertEquals( 8, diamond.max( 1 ) );
	}

}
