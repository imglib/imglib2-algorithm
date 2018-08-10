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

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import org.junit.Test;

public class RandomAccessiblePairWithNeighborhoodTest
{

	@Test
	public void testRandomAccessOnPair()
	{
		// Extend input with Views.extend()
		Img< ByteType > in = new ArrayImgFactory<>( new ByteType() ).create( 3, 3 );
		RandomAccessibleInterval< ByteType > extendedIn = Views.interval( Views.extendBorder( in ), in );

		// Create RandomAccessiblePair
		final RandomAccessible< Neighborhood< ByteType > > safe = new RectangleShape( //
				1,
				false ).neighborhoodsRandomAccessibleSafe( extendedIn );
		RandomAccessible< Pair< Neighborhood< ByteType >, ByteType > > pair = Views.pair( safe, extendedIn );

		// Set position out of bounds
		RandomAccess< Pair< Neighborhood< ByteType >, ByteType > > randomAccess = pair.randomAccess();
		randomAccess.setPosition( new int[] { 0, 0 } );

		// Get value from Neighborhood via RandomAccessiblePair
		Pair< Neighborhood< ByteType >, ByteType > pair2 = randomAccess.get();
		Neighborhood< ByteType > neighborhood = pair2.getA();
		Cursor< ByteType > cursor = neighborhood.cursor();

		// Tries to access (-1, -1) of the source
		cursor.next().getRealDouble();
	}

}
