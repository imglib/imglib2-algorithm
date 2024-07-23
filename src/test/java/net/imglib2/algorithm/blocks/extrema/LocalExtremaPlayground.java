/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.extrema;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;

public class LocalExtremaPlayground
{
	public static void main( String[] args )
	{
		float[] values = {
				0, 0, 0, 0, 0, 0,
				0, 0, 1, 0, 0, 0,
				1, 1, 2, 1, 0, 0,
				0, 1, 2, 3, 2, 0,
				0, 0, 1, 2, 1, 1,
				0, 0, 0, 1, 0, 0,
		};

		final RandomAccessibleInterval< FloatType> img = ArrayImgs.floats( values, 6, 6 );
		print( img );
		System.out.println();


		final int n = 2;
		final LocalMaximaProcessor proc = new LocalMaximaProcessor( n );
		final UnaryBlockOperator< FloatType, UnsignedByteType > operator = new DefaultUnaryBlockOperator<>( new FloatType(), new UnsignedByteType(), n, n, proc );

		final BlockSupplier< UnsignedByteType > sup = BlockSupplier.of( img ).andThen( operator );
		final byte[] mvalues = new byte[ 6 * 6 ];
		sup.copy( new int[] { 0, 0 }, mvalues, new int[] { 6, 6 } );

		final RandomAccessibleInterval< UnsignedByteType > mask = ArrayImgs.unsignedBytes( mvalues, 6, 6 );
		printMask( mask );
		System.out.println();
	}

	static void print( RandomAccessibleInterval< FloatType> img )
	{
		for ( long y = img.min( 1 ); y <= img.max( 1 ); ++y )
		{
			for ( long x = img.min( 0 ); x <= img.max( 0 ); ++x )
			{
				final float v = img.getAt( x, y ).get();
				System.out.printf( "%.0f, ", v );
			}
			System.out.println();
		}
	}

	static void printMask( RandomAccessibleInterval< UnsignedByteType> img )
	{
		for ( long y = img.min( 1 ); y <= img.max( 1 ); ++y )
		{
			for ( long x = img.min( 0 ); x <= img.max( 0 ); ++x )
			{
				final int v = img.getAt( x, y ).get();
				System.out.printf( "%d, ", v );
			}
			System.out.println();
		}
	}
}
