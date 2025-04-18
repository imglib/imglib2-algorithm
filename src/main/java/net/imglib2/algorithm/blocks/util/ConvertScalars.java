/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.util;

/*
 * This is autogenerated source code -- DO NOT EDIT. Instead, edit the
 * corresponding template in templates/ and rerun bin/generate.groovy.
 */

public class ConvertScalars
{
	/*
	 * Methods to convert each pixel type (i8, u8, i16, u16, i32, u32, i64, f32, f64)
	 * to the corresponding primitive type used for computation.
	 *
	 * i8, u8, i16, u16, i32 are all converted to int, because java
	 * computes in int for byte, short, and int operands.
	 *
	 * u32, i64 are converted to long, because that is the primitive type that
	 * can represent all values of the pixel type.
	 *
	 * f32 is converted to float
	 *
	 * f64 is converted to double
	 */
	public static int from_i8( byte value ) { return value; }
	public static int from_u8( byte value ) { return value & 0xff; }
	public static int from_i16( short value ) { return value; }
	public static int from_u16( short value ) { return value & 0xffff; }
	public static int from_i32( int value ) { return value; }
	public static long from_u32( int value ) { return value & 0xffffffffL; }
	public static long from_i64( long value ) { return value; }
	public static float from_f32( float value ) { return value; }
	public static double from_f64( double value ) { return value; }

	/*
	 * Methods to convert int values to each pixel type (i8, u8, i16, u16, i32, u32, i64, f32, f64).
	 *
	 * The basic to_u8() etc methods don't do any bounds-checking or clamping.
	 * They only cast the argument to the output type.
	 *
	 * The to_u8_clamp() etc methods clamp the argument to the range of the
	 * output type.
	 *
	 * The to_u8_clamp_max() etc methods clamp only to the upper bound output
	 * range. This is useful for computations that could possibly overflow but
	 * can never underflow (like summing unsigned values).
	 *
	 * The to_u8_clamp_min() etc methods clamp only to the lower bound output
	 * range. This is useful for computations that could possibly underflow but
	 * can never overflow (like subtracting unsigned values).
	 */
	public static byte to_i8( int value ) { return ( byte ) value; }
	public static byte to_i8_clamp_max( int value ) { return to_i8( Math.min( 0x7f, value ) ); }
	public static byte to_i8_clamp_min( int value ) { return to_i8( Math.max( -0x80, value ) ); }
	public static byte to_i8_clamp( int value ) { return to_i8( Math.min( 0x7f, Math.max( -0x80, value ) ) ); }
	public static byte to_u8( int value ) { return ( byte ) value; }
	public static byte to_u8_clamp_max( int value ) { return to_u8( Math.min( 0xff, value ) ); }
	public static byte to_u8_clamp_min( int value ) { return to_u8( Math.max( 0, value ) ); }
	public static byte to_u8_clamp( int value ) { return to_u8( Math.min( 0xff, Math.max( 0, value ) ) ); }
	public static short to_i16( int value ) { return ( short ) value; }
	public static short to_i16_clamp_max( int value ) { return to_i16( Math.min( 0x7fff, value ) ); }
	public static short to_i16_clamp_min( int value ) { return to_i16( Math.max( -0x8000, value ) ); }
	public static short to_i16_clamp( int value ) { return to_i16( Math.min( 0x7fff, Math.max( -0x8000, value ) ) ); }
	public static short to_u16( int value ) { return ( short ) value; }
	public static short to_u16_clamp_max( int value ) { return to_u16( Math.min( 0xffff, value ) ); }
	public static short to_u16_clamp_min( int value ) { return to_u16( Math.max( 0, value ) ); }
	public static short to_u16_clamp( int value ) { return to_u16( Math.min( 0xffff, Math.max( 0, value ) ) ); }
	public static int to_i32( int value ) { return value; }
	public static int to_i32_clamp_max( int value ) { return to_i32( value ); }
	public static int to_i32_clamp_min( int value ) { return to_i32( value ); }
	public static int to_i32_clamp( int value ) { return to_i32( value ); }
	public static int to_u32( int value ) { return value; }
	public static int to_u32_clamp_max( int value ) { return to_u32( Math.min( 0xffff_ffffL, value ) ); }
	public static int to_u32_clamp_min( int value ) { return to_u32( Math.max( 0L, value ) ); }
	public static int to_u32_clamp( int value ) { return to_u32( Math.min( 0xffff_ffffL, Math.max( 0L, value ) ) ); }
	public static long to_i64( int value ) { return value; }
	public static long to_i64_clamp_max( int value ) { return to_i64( value ); }
	public static long to_i64_clamp_min( int value ) { return to_i64( value ); }
	public static long to_i64_clamp( int value ) { return to_i64( value ); }
	public static float to_f32( int value ) { return value; }
	public static float to_f32_clamp_max( int value ) { return to_f32( value ); }
	public static float to_f32_clamp_min( int value ) { return to_f32( value ); }
	public static float to_f32_clamp( int value ) { return to_f32( value ); }
	public static double to_f64( int value ) { return value; }
	public static double to_f64_clamp_max( int value ) { return to_f64( value ); }
	public static double to_f64_clamp_min( int value ) { return to_f64( value ); }
	public static double to_f64_clamp( int value ) { return to_f64( value ); }

	/*
	 * Methods to convert long values to each pixel type (i8, u8, i16, u16, i32, u32, i64, f32, f64).
	 *
	 * The basic to_u8() etc methods don't do any bounds-checking or clamping.
	 * They only cast the argument to the output type.
	 *
	 * The to_u8_clamp() etc methods clamp the argument to the range of the
	 * output type.
	 *
	 * The to_u8_clamp_max() etc methods clamp only to the upper bound output
	 * range. This is useful for computations that could possibly overflow but
	 * can never underflow (like summing unsigned values).
	 *
	 * The to_u8_clamp_min() etc methods clamp only to the lower bound output
	 * range. This is useful for computations that could possibly underflow but
	 * can never overflow (like subtracting unsigned values).
	 */
	public static byte to_i8( long value ) { return ( byte ) value; }
	public static byte to_i8_clamp_max( long value ) { return to_i8( Math.min( 0x7f, value ) ); }
	public static byte to_i8_clamp_min( long value ) { return to_i8( Math.max( -0x80, value ) ); }
	public static byte to_i8_clamp( long value ) { return to_i8( Math.min( 0x7f, Math.max( -0x80, value ) ) ); }
	public static byte to_u8( long value ) { return ( byte ) value; }
	public static byte to_u8_clamp_max( long value ) { return to_u8( Math.min( 0xff, value ) ); }
	public static byte to_u8_clamp_min( long value ) { return to_u8( Math.max( 0, value ) ); }
	public static byte to_u8_clamp( long value ) { return to_u8( Math.min( 0xff, Math.max( 0, value ) ) ); }
	public static short to_i16( long value ) { return ( short ) value; }
	public static short to_i16_clamp_max( long value ) { return to_i16( Math.min( 0x7fff, value ) ); }
	public static short to_i16_clamp_min( long value ) { return to_i16( Math.max( -0x8000, value ) ); }
	public static short to_i16_clamp( long value ) { return to_i16( Math.min( 0x7fff, Math.max( -0x8000, value ) ) ); }
	public static short to_u16( long value ) { return ( short ) value; }
	public static short to_u16_clamp_max( long value ) { return to_u16( Math.min( 0xffff, value ) ); }
	public static short to_u16_clamp_min( long value ) { return to_u16( Math.max( 0, value ) ); }
	public static short to_u16_clamp( long value ) { return to_u16( Math.min( 0xffff, Math.max( 0, value ) ) ); }
	public static int to_i32( long value ) { return ( int ) value; }
	public static int to_i32_clamp_max( long value ) { return to_i32( Math.min( 0x7fff_ffff, value ) ); }
	public static int to_i32_clamp_min( long value ) { return to_i32( Math.max( -0x8000_0000, value ) ); }
	public static int to_i32_clamp( long value ) { return to_i32( Math.min( 0x7fff_ffff, Math.max( -0x8000_0000, value ) ) ); }
	public static int to_u32( long value ) { return ( int ) value; }
	public static int to_u32_clamp_max( long value ) { return to_u32( Math.min( 0xffff_ffffL, value ) ); }
	public static int to_u32_clamp_min( long value ) { return to_u32( Math.max( 0L, value ) ); }
	public static int to_u32_clamp( long value ) { return to_u32( Math.min( 0xffff_ffffL, Math.max( 0L, value ) ) ); }
	public static long to_i64( long value ) { return value; }
	public static long to_i64_clamp_max( long value ) { return to_i64( value ); }
	public static long to_i64_clamp_min( long value ) { return to_i64( value ); }
	public static long to_i64_clamp( long value ) { return to_i64( value ); }
	public static float to_f32( long value ) { return ( float ) value; }
	public static float to_f32_clamp_max( long value ) { return to_f32( value ); }
	public static float to_f32_clamp_min( long value ) { return to_f32( value ); }
	public static float to_f32_clamp( long value ) { return to_f32( value ); }
	public static double to_f64( long value ) { return value; }
	public static double to_f64_clamp_max( long value ) { return to_f64( value ); }
	public static double to_f64_clamp_min( long value ) { return to_f64( value ); }
	public static double to_f64_clamp( long value ) { return to_f64( value ); }

	/*
	 * Methods to convert float values to each pixel type (i8, u8, i16, u16, i32, u32, i64, f32, f64).
	 *
	 * The basic to_u8() etc methods don't do any bounds-checking or clamping.
	 * They round the argument and then cast to the output type.
	 *
	 * The to_u8_clamp_max() etc methods clamp only to the upper bound output
	 * range. This is useful for computations that could possibly overflow but
	 * can never underflow (like summing unsigned values).
	 *
	 * The to_u8_clamp_min() etc methods clamp only to the lower bound output
	 * range. This is useful for computations that could possibly underflow but
	 * can never overflow (like subtracting unsigned values).
	 */
	public static byte to_i8( float value ) { return to_i8( Math.round( value ) ); }
	public static byte to_i8_clamp_max( float value ) { return to_i8_clamp_max( Math.round( value ) ); }
	public static byte to_i8_clamp_min( float value ) { return to_i8_clamp_min( Math.round( value ) ); }
	public static byte to_i8_clamp( float value ) { return to_i8_clamp( Math.round( value ) ); }
	public static byte to_u8( float value ) { return to_u8( Math.round( value ) ); }
	public static byte to_u8_clamp_max( float value ) { return to_u8_clamp_max( Math.round( value ) ); }
	public static byte to_u8_clamp_min( float value ) { return to_u8_clamp_min( Math.round( value ) ); }
	public static byte to_u8_clamp( float value ) { return to_u8_clamp( Math.round( value ) ); }
	public static short to_i16( float value ) { return to_i16( Math.round( value ) ); }
	public static short to_i16_clamp_max( float value ) { return to_i16_clamp_max( Math.round( value ) ); }
	public static short to_i16_clamp_min( float value ) { return to_i16_clamp_min( Math.round( value ) ); }
	public static short to_i16_clamp( float value ) { return to_i16_clamp( Math.round( value ) ); }
	public static short to_u16( float value ) { return to_u16( Math.round( value ) ); }
	public static short to_u16_clamp_max( float value ) { return to_u16_clamp_max( Math.round( value ) ); }
	public static short to_u16_clamp_min( float value ) { return to_u16_clamp_min( Math.round( value ) ); }
	public static short to_u16_clamp( float value ) { return to_u16_clamp( Math.round( value ) ); }
	public static int to_i32( float value ) { return to_i32( Math.round( value ) ); }
	public static int to_i32_clamp_max( float value ) { return to_i32_clamp_max( Math.round( value ) ); }
	public static int to_i32_clamp_min( float value ) { return to_i32_clamp_min( Math.round( value ) ); }
	public static int to_i32_clamp( float value ) { return to_i32_clamp( Math.round( value ) ); }
	public static int to_u32( float value ) { return to_u32( Math.round( ( double ) value ) ); }
	public static int to_u32_clamp_max( float value ) { return to_u32_clamp_max( Math.round( ( double ) value ) ); }
	public static int to_u32_clamp_min( float value ) { return to_u32_clamp_min( Math.round( ( double ) value ) ); }
	public static int to_u32_clamp( float value ) { return to_u32_clamp( Math.round( ( double ) value ) ); }
	public static long to_i64( float value ) { return to_i64( Math.round( ( double ) value ) ); }
	public static long to_i64_clamp_max( float value ) { return to_i64_clamp_max( Math.round( ( double ) value ) ); }
	public static long to_i64_clamp_min( float value ) { return to_i64_clamp_min( Math.round( ( double ) value ) ); }
	public static long to_i64_clamp( float value ) { return to_i64_clamp( Math.round( ( double ) value ) ); }
	public static float to_f32( float value ) { return  value; }
	public static float to_f32_clamp_max( float value ) { return to_f32( value ); }
	public static float to_f32_clamp_min( float value ) { return to_f32( value ); }
	public static float to_f32_clamp( float value ) { return to_f32( value ); }
	public static double to_f64( float value ) { return  value; }
	public static double to_f64_clamp_max( float value ) { return to_f64( value ); }
	public static double to_f64_clamp_min( float value ) { return to_f64( value ); }
	public static double to_f64_clamp( float value ) { return to_f64( value ); }

	/*
	 * Methods to convert double values to each pixel type (i8, u8, i16, u16, i32, u32, i64, f32, f64).
	 *
	 * The basic to_u8() etc methods don't do any bounds-checking or clamping.
	 * They round the argument and then cast to the output type.
	 *
	 * The to_u8_clamp_max() etc methods clamp only to the upper bound output
	 * range. This is useful for computations that could possibly overflow but
	 * can never underflow (like summing unsigned values).
	 *
	 * The to_u8_clamp_min() etc methods clamp only to the lower bound output
	 * range. This is useful for computations that could possibly underflow but
	 * can never overflow (like subtracting unsigned values).
	 */
	public static byte to_i8( double value ) { return to_i8( Math.round( value ) ); }
	public static byte to_i8_clamp_max( double value ) { return to_i8_clamp_max( Math.round( value ) ); }
	public static byte to_i8_clamp_min( double value ) { return to_i8_clamp_min( Math.round( value ) ); }
	public static byte to_i8_clamp( double value ) { return to_i8_clamp( Math.round( value ) ); }
	public static byte to_u8( double value ) { return to_u8( Math.round( value ) ); }
	public static byte to_u8_clamp_max( double value ) { return to_u8_clamp_max( Math.round( value ) ); }
	public static byte to_u8_clamp_min( double value ) { return to_u8_clamp_min( Math.round( value ) ); }
	public static byte to_u8_clamp( double value ) { return to_u8_clamp( Math.round( value ) ); }
	public static short to_i16( double value ) { return to_i16( Math.round( value ) ); }
	public static short to_i16_clamp_max( double value ) { return to_i16_clamp_max( Math.round( value ) ); }
	public static short to_i16_clamp_min( double value ) { return to_i16_clamp_min( Math.round( value ) ); }
	public static short to_i16_clamp( double value ) { return to_i16_clamp( Math.round( value ) ); }
	public static short to_u16( double value ) { return to_u16( Math.round( value ) ); }
	public static short to_u16_clamp_max( double value ) { return to_u16_clamp_max( Math.round( value ) ); }
	public static short to_u16_clamp_min( double value ) { return to_u16_clamp_min( Math.round( value ) ); }
	public static short to_u16_clamp( double value ) { return to_u16_clamp( Math.round( value ) ); }
	public static int to_i32( double value ) { return to_i32( Math.round( value ) ); }
	public static int to_i32_clamp_max( double value ) { return to_i32_clamp_max( Math.round( value ) ); }
	public static int to_i32_clamp_min( double value ) { return to_i32_clamp_min( Math.round( value ) ); }
	public static int to_i32_clamp( double value ) { return to_i32_clamp( Math.round( value ) ); }
	public static int to_u32( double value ) { return to_u32( Math.round( value ) ); }
	public static int to_u32_clamp_max( double value ) { return to_u32_clamp_max( Math.round( value ) ); }
	public static int to_u32_clamp_min( double value ) { return to_u32_clamp_min( Math.round( value ) ); }
	public static int to_u32_clamp( double value ) { return to_u32_clamp( Math.round( value ) ); }
	public static long to_i64( double value ) { return to_i64( Math.round( value ) ); }
	public static long to_i64_clamp_max( double value ) { return to_i64_clamp_max( Math.round( value ) ); }
	public static long to_i64_clamp_min( double value ) { return to_i64_clamp_min( Math.round( value ) ); }
	public static long to_i64_clamp( double value ) { return to_i64_clamp( Math.round( value ) ); }
	public static float to_f32( double value ) { return ( float )  value; }
	public static float to_f32_clamp_max( double value ) { return to_f32( value ); }
	public static float to_f32_clamp_min( double value ) { return to_f32( value ); }
	public static float to_f32_clamp( double value ) { return to_f32( value ); }
	public static double to_f64( double value ) { return  value; }
	public static double to_f64_clamp_max( double value ) { return to_f64( value ); }
	public static double to_f64_clamp_min( double value ) { return to_f64( value ); }
	public static double to_f64_clamp( double value ) { return to_f64( value ); }
}
