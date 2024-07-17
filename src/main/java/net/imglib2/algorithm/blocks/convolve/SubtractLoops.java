/*
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
package net.imglib2.algorithm.blocks.convolve;

import net.imglib2.algorithm.blocks.util.OperandType;
import net.imglib2.util.Cast;

import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_i8;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_i8;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_u8;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_u8;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_i16;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_i16;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_u16;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_u16;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_i32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_i32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_u32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_u32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_i64;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_i64;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_f32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_f32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_f64;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_f64;

/*
 * This is autogenerated source code -- DO NOT EDIT. Instead, edit the
 * corresponding template in templates/ and rerun bin/generate.groovy.
 */

class SubtractLoops
{
    static < I > SubtractLoop< I > get( OperandType type )
    {
        switch( type )
        {
        case I8: return Cast.unchecked( Subtract_i8.INSTANCE );
        case U8: return Cast.unchecked( Subtract_u8.INSTANCE );
        case I16: return Cast.unchecked( Subtract_i16.INSTANCE );
        case U16: return Cast.unchecked( Subtract_u16.INSTANCE );
        case I32: return Cast.unchecked( Subtract_i32.INSTANCE );
        case U32: return Cast.unchecked( Subtract_u32.INSTANCE );
        case I64: return Cast.unchecked( Subtract_i64.INSTANCE );
        case F32: return Cast.unchecked( Subtract_f32.INSTANCE );
        case F64: return Cast.unchecked( Subtract_f64.INSTANCE );
        default:
            throw new IllegalArgumentException();
        }
    }

	static class Subtract_i8 implements SubtractLoop< byte[] >
	{
		static final Subtract_i8 INSTANCE = new Subtract_i8();

		@Override
		public void apply( final byte[] src0, final byte[] src1, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_i8( src0[ i ] ) - from_i8( src1[ i ] ) );
		}
	}

	static class Subtract_u8 implements SubtractLoop< byte[] >
	{
		static final Subtract_u8 INSTANCE = new Subtract_u8();

		@Override
		public void apply( final byte[] src0, final byte[] src1, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_u8( src0[ i ] ) - from_u8( src1[ i ] ) );
		}
	}

	static class Subtract_i16 implements SubtractLoop< short[] >
	{
		static final Subtract_i16 INSTANCE = new Subtract_i16();

		@Override
		public void apply( final short[] src0, final short[] src1, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_i16( src0[ i ] ) - from_i16( src1[ i ] ) );
		}
	}

	static class Subtract_u16 implements SubtractLoop< short[] >
	{
		static final Subtract_u16 INSTANCE = new Subtract_u16();

		@Override
		public void apply( final short[] src0, final short[] src1, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_u16( src0[ i ] ) - from_u16( src1[ i ] ) );
		}
	}

	static class Subtract_i32 implements SubtractLoop< int[] >
	{
		static final Subtract_i32 INSTANCE = new Subtract_i32();

		@Override
		public void apply( final int[] src0, final int[] src1, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_i32( src0[ i ] ) - from_i32( src1[ i ] ) );
		}
	}

	static class Subtract_u32 implements SubtractLoop< int[] >
	{
		static final Subtract_u32 INSTANCE = new Subtract_u32();

		@Override
		public void apply( final int[] src0, final int[] src1, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_u32( src0[ i ] ) - from_u32( src1[ i ] ) );
		}
	}

	static class Subtract_i64 implements SubtractLoop< long[] >
	{
		static final Subtract_i64 INSTANCE = new Subtract_i64();

		@Override
		public void apply( final long[] src0, final long[] src1, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_i64( src0[ i ] ) - from_i64( src1[ i ] ) );
		}
	}

	static class Subtract_f32 implements SubtractLoop< float[] >
	{
		static final Subtract_f32 INSTANCE = new Subtract_f32();

		@Override
		public void apply( final float[] src0, final float[] src1, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_f32( src0[ i ] ) - from_f32( src1[ i ] ) );
		}
	}

	static class Subtract_f64 implements SubtractLoop< double[] >
	{
		static final Subtract_f64 INSTANCE = new Subtract_f64();

		@Override
		public void apply( final double[] src0, final double[] src1, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_f64( src0[ i ] ) - from_f64( src1[ i ] ) );
		}
	}

}
