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
package net.imglib2.algorithm.blocks.convert;

import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.util.UnaryOperatorType;
import net.imglib2.util.Cast;

import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_i8;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i8;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i8_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i8_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i8_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_u8;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u8;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u8_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u8_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u8_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_i16;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i16;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i16_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i16_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i16_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_u16;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u16;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u16_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u16_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u16_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_i32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i32_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i32_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i32_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_u32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u32_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u32_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_u32_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_i64;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i64;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i64_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i64_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_i64_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_f32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f32;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f32_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f32_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f32_clamp_max;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.from_f64;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f64;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f64_clamp;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f64_clamp_min;
import static net.imglib2.algorithm.blocks.util.ConvertScalars.to_f64_clamp_max;

/*
 * This is autogenerated source code -- DO NOT EDIT. Instead, edit the
 * corresponding template in templates/ and rerun bin/generate.groovy.
 */

class ConvertLoops
{
	static < I, O > ConvertLoop< I, O > get( UnaryOperatorType type )
	{
		return get( type, ClampType.NONE );
	}

	static < I, O > ConvertLoop< I, O > get( UnaryOperatorType type, ClampType clampType )
	{
		switch ( clampType )
		{
		case NONE:
			switch( type )
			{
			case I8_TO_I8: return Cast.unchecked( Convert_i8_to_i8.INSTANCE );
			case I8_TO_U8: return Cast.unchecked( Convert_i8_to_u8.INSTANCE );
			case I8_TO_I16: return Cast.unchecked( Convert_i8_to_i16.INSTANCE );
			case I8_TO_U16: return Cast.unchecked( Convert_i8_to_u16.INSTANCE );
			case I8_TO_I32: return Cast.unchecked( Convert_i8_to_i32.INSTANCE );
			case I8_TO_U32: return Cast.unchecked( Convert_i8_to_u32.INSTANCE );
			case I8_TO_I64: return Cast.unchecked( Convert_i8_to_i64.INSTANCE );
			case I8_TO_F32: return Cast.unchecked( Convert_i8_to_f32.INSTANCE );
			case I8_TO_F64: return Cast.unchecked( Convert_i8_to_f64.INSTANCE );
			case U8_TO_I8: return Cast.unchecked( Convert_u8_to_i8.INSTANCE );
			case U8_TO_U8: return Cast.unchecked( Convert_u8_to_u8.INSTANCE );
			case U8_TO_I16: return Cast.unchecked( Convert_u8_to_i16.INSTANCE );
			case U8_TO_U16: return Cast.unchecked( Convert_u8_to_u16.INSTANCE );
			case U8_TO_I32: return Cast.unchecked( Convert_u8_to_i32.INSTANCE );
			case U8_TO_U32: return Cast.unchecked( Convert_u8_to_u32.INSTANCE );
			case U8_TO_I64: return Cast.unchecked( Convert_u8_to_i64.INSTANCE );
			case U8_TO_F32: return Cast.unchecked( Convert_u8_to_f32.INSTANCE );
			case U8_TO_F64: return Cast.unchecked( Convert_u8_to_f64.INSTANCE );
			case I16_TO_I8: return Cast.unchecked( Convert_i16_to_i8.INSTANCE );
			case I16_TO_U8: return Cast.unchecked( Convert_i16_to_u8.INSTANCE );
			case I16_TO_I16: return Cast.unchecked( Convert_i16_to_i16.INSTANCE );
			case I16_TO_U16: return Cast.unchecked( Convert_i16_to_u16.INSTANCE );
			case I16_TO_I32: return Cast.unchecked( Convert_i16_to_i32.INSTANCE );
			case I16_TO_U32: return Cast.unchecked( Convert_i16_to_u32.INSTANCE );
			case I16_TO_I64: return Cast.unchecked( Convert_i16_to_i64.INSTANCE );
			case I16_TO_F32: return Cast.unchecked( Convert_i16_to_f32.INSTANCE );
			case I16_TO_F64: return Cast.unchecked( Convert_i16_to_f64.INSTANCE );
			case U16_TO_I8: return Cast.unchecked( Convert_u16_to_i8.INSTANCE );
			case U16_TO_U8: return Cast.unchecked( Convert_u16_to_u8.INSTANCE );
			case U16_TO_I16: return Cast.unchecked( Convert_u16_to_i16.INSTANCE );
			case U16_TO_U16: return Cast.unchecked( Convert_u16_to_u16.INSTANCE );
			case U16_TO_I32: return Cast.unchecked( Convert_u16_to_i32.INSTANCE );
			case U16_TO_U32: return Cast.unchecked( Convert_u16_to_u32.INSTANCE );
			case U16_TO_I64: return Cast.unchecked( Convert_u16_to_i64.INSTANCE );
			case U16_TO_F32: return Cast.unchecked( Convert_u16_to_f32.INSTANCE );
			case U16_TO_F64: return Cast.unchecked( Convert_u16_to_f64.INSTANCE );
			case I32_TO_I8: return Cast.unchecked( Convert_i32_to_i8.INSTANCE );
			case I32_TO_U8: return Cast.unchecked( Convert_i32_to_u8.INSTANCE );
			case I32_TO_I16: return Cast.unchecked( Convert_i32_to_i16.INSTANCE );
			case I32_TO_U16: return Cast.unchecked( Convert_i32_to_u16.INSTANCE );
			case I32_TO_I32: return Cast.unchecked( Convert_i32_to_i32.INSTANCE );
			case I32_TO_U32: return Cast.unchecked( Convert_i32_to_u32.INSTANCE );
			case I32_TO_I64: return Cast.unchecked( Convert_i32_to_i64.INSTANCE );
			case I32_TO_F32: return Cast.unchecked( Convert_i32_to_f32.INSTANCE );
			case I32_TO_F64: return Cast.unchecked( Convert_i32_to_f64.INSTANCE );
			case U32_TO_I8: return Cast.unchecked( Convert_u32_to_i8.INSTANCE );
			case U32_TO_U8: return Cast.unchecked( Convert_u32_to_u8.INSTANCE );
			case U32_TO_I16: return Cast.unchecked( Convert_u32_to_i16.INSTANCE );
			case U32_TO_U16: return Cast.unchecked( Convert_u32_to_u16.INSTANCE );
			case U32_TO_I32: return Cast.unchecked( Convert_u32_to_i32.INSTANCE );
			case U32_TO_U32: return Cast.unchecked( Convert_u32_to_u32.INSTANCE );
			case U32_TO_I64: return Cast.unchecked( Convert_u32_to_i64.INSTANCE );
			case U32_TO_F32: return Cast.unchecked( Convert_u32_to_f32.INSTANCE );
			case U32_TO_F64: return Cast.unchecked( Convert_u32_to_f64.INSTANCE );
			case I64_TO_I8: return Cast.unchecked( Convert_i64_to_i8.INSTANCE );
			case I64_TO_U8: return Cast.unchecked( Convert_i64_to_u8.INSTANCE );
			case I64_TO_I16: return Cast.unchecked( Convert_i64_to_i16.INSTANCE );
			case I64_TO_U16: return Cast.unchecked( Convert_i64_to_u16.INSTANCE );
			case I64_TO_I32: return Cast.unchecked( Convert_i64_to_i32.INSTANCE );
			case I64_TO_U32: return Cast.unchecked( Convert_i64_to_u32.INSTANCE );
			case I64_TO_I64: return Cast.unchecked( Convert_i64_to_i64.INSTANCE );
			case I64_TO_F32: return Cast.unchecked( Convert_i64_to_f32.INSTANCE );
			case I64_TO_F64: return Cast.unchecked( Convert_i64_to_f64.INSTANCE );
			case F32_TO_I8: return Cast.unchecked( Convert_f32_to_i8.INSTANCE );
			case F32_TO_U8: return Cast.unchecked( Convert_f32_to_u8.INSTANCE );
			case F32_TO_I16: return Cast.unchecked( Convert_f32_to_i16.INSTANCE );
			case F32_TO_U16: return Cast.unchecked( Convert_f32_to_u16.INSTANCE );
			case F32_TO_I32: return Cast.unchecked( Convert_f32_to_i32.INSTANCE );
			case F32_TO_U32: return Cast.unchecked( Convert_f32_to_u32.INSTANCE );
			case F32_TO_I64: return Cast.unchecked( Convert_f32_to_i64.INSTANCE );
			case F32_TO_F32: return Cast.unchecked( Convert_f32_to_f32.INSTANCE );
			case F32_TO_F64: return Cast.unchecked( Convert_f32_to_f64.INSTANCE );
			case F64_TO_I8: return Cast.unchecked( Convert_f64_to_i8.INSTANCE );
			case F64_TO_U8: return Cast.unchecked( Convert_f64_to_u8.INSTANCE );
			case F64_TO_I16: return Cast.unchecked( Convert_f64_to_i16.INSTANCE );
			case F64_TO_U16: return Cast.unchecked( Convert_f64_to_u16.INSTANCE );
			case F64_TO_I32: return Cast.unchecked( Convert_f64_to_i32.INSTANCE );
			case F64_TO_U32: return Cast.unchecked( Convert_f64_to_u32.INSTANCE );
			case F64_TO_I64: return Cast.unchecked( Convert_f64_to_i64.INSTANCE );
			case F64_TO_F32: return Cast.unchecked( Convert_f64_to_f32.INSTANCE );
			case F64_TO_F64: return Cast.unchecked( Convert_f64_to_f64.INSTANCE );
			default:
				throw new IllegalArgumentException();
			}
		case CLAMP:
			switch( type )
			{
			case I8_TO_I8: return Cast.unchecked( Convert_i8_to_i8_clamp.INSTANCE );
			case I8_TO_U8: return Cast.unchecked( Convert_i8_to_u8_clamp.INSTANCE );
			case I8_TO_I16: return Cast.unchecked( Convert_i8_to_i16_clamp.INSTANCE );
			case I8_TO_U16: return Cast.unchecked( Convert_i8_to_u16_clamp.INSTANCE );
			case I8_TO_I32: return Cast.unchecked( Convert_i8_to_i32_clamp.INSTANCE );
			case I8_TO_U32: return Cast.unchecked( Convert_i8_to_u32_clamp.INSTANCE );
			case I8_TO_I64: return Cast.unchecked( Convert_i8_to_i64_clamp.INSTANCE );
			case I8_TO_F32: return Cast.unchecked( Convert_i8_to_f32_clamp.INSTANCE );
			case I8_TO_F64: return Cast.unchecked( Convert_i8_to_f64_clamp.INSTANCE );
			case U8_TO_I8: return Cast.unchecked( Convert_u8_to_i8_clamp.INSTANCE );
			case U8_TO_U8: return Cast.unchecked( Convert_u8_to_u8_clamp.INSTANCE );
			case U8_TO_I16: return Cast.unchecked( Convert_u8_to_i16_clamp.INSTANCE );
			case U8_TO_U16: return Cast.unchecked( Convert_u8_to_u16_clamp.INSTANCE );
			case U8_TO_I32: return Cast.unchecked( Convert_u8_to_i32_clamp.INSTANCE );
			case U8_TO_U32: return Cast.unchecked( Convert_u8_to_u32_clamp.INSTANCE );
			case U8_TO_I64: return Cast.unchecked( Convert_u8_to_i64_clamp.INSTANCE );
			case U8_TO_F32: return Cast.unchecked( Convert_u8_to_f32_clamp.INSTANCE );
			case U8_TO_F64: return Cast.unchecked( Convert_u8_to_f64_clamp.INSTANCE );
			case I16_TO_I8: return Cast.unchecked( Convert_i16_to_i8_clamp.INSTANCE );
			case I16_TO_U8: return Cast.unchecked( Convert_i16_to_u8_clamp.INSTANCE );
			case I16_TO_I16: return Cast.unchecked( Convert_i16_to_i16_clamp.INSTANCE );
			case I16_TO_U16: return Cast.unchecked( Convert_i16_to_u16_clamp.INSTANCE );
			case I16_TO_I32: return Cast.unchecked( Convert_i16_to_i32_clamp.INSTANCE );
			case I16_TO_U32: return Cast.unchecked( Convert_i16_to_u32_clamp.INSTANCE );
			case I16_TO_I64: return Cast.unchecked( Convert_i16_to_i64_clamp.INSTANCE );
			case I16_TO_F32: return Cast.unchecked( Convert_i16_to_f32_clamp.INSTANCE );
			case I16_TO_F64: return Cast.unchecked( Convert_i16_to_f64_clamp.INSTANCE );
			case U16_TO_I8: return Cast.unchecked( Convert_u16_to_i8_clamp.INSTANCE );
			case U16_TO_U8: return Cast.unchecked( Convert_u16_to_u8_clamp.INSTANCE );
			case U16_TO_I16: return Cast.unchecked( Convert_u16_to_i16_clamp.INSTANCE );
			case U16_TO_U16: return Cast.unchecked( Convert_u16_to_u16_clamp.INSTANCE );
			case U16_TO_I32: return Cast.unchecked( Convert_u16_to_i32_clamp.INSTANCE );
			case U16_TO_U32: return Cast.unchecked( Convert_u16_to_u32_clamp.INSTANCE );
			case U16_TO_I64: return Cast.unchecked( Convert_u16_to_i64_clamp.INSTANCE );
			case U16_TO_F32: return Cast.unchecked( Convert_u16_to_f32_clamp.INSTANCE );
			case U16_TO_F64: return Cast.unchecked( Convert_u16_to_f64_clamp.INSTANCE );
			case I32_TO_I8: return Cast.unchecked( Convert_i32_to_i8_clamp.INSTANCE );
			case I32_TO_U8: return Cast.unchecked( Convert_i32_to_u8_clamp.INSTANCE );
			case I32_TO_I16: return Cast.unchecked( Convert_i32_to_i16_clamp.INSTANCE );
			case I32_TO_U16: return Cast.unchecked( Convert_i32_to_u16_clamp.INSTANCE );
			case I32_TO_I32: return Cast.unchecked( Convert_i32_to_i32_clamp.INSTANCE );
			case I32_TO_U32: return Cast.unchecked( Convert_i32_to_u32_clamp.INSTANCE );
			case I32_TO_I64: return Cast.unchecked( Convert_i32_to_i64_clamp.INSTANCE );
			case I32_TO_F32: return Cast.unchecked( Convert_i32_to_f32_clamp.INSTANCE );
			case I32_TO_F64: return Cast.unchecked( Convert_i32_to_f64_clamp.INSTANCE );
			case U32_TO_I8: return Cast.unchecked( Convert_u32_to_i8_clamp.INSTANCE );
			case U32_TO_U8: return Cast.unchecked( Convert_u32_to_u8_clamp.INSTANCE );
			case U32_TO_I16: return Cast.unchecked( Convert_u32_to_i16_clamp.INSTANCE );
			case U32_TO_U16: return Cast.unchecked( Convert_u32_to_u16_clamp.INSTANCE );
			case U32_TO_I32: return Cast.unchecked( Convert_u32_to_i32_clamp.INSTANCE );
			case U32_TO_U32: return Cast.unchecked( Convert_u32_to_u32_clamp.INSTANCE );
			case U32_TO_I64: return Cast.unchecked( Convert_u32_to_i64_clamp.INSTANCE );
			case U32_TO_F32: return Cast.unchecked( Convert_u32_to_f32_clamp.INSTANCE );
			case U32_TO_F64: return Cast.unchecked( Convert_u32_to_f64_clamp.INSTANCE );
			case I64_TO_I8: return Cast.unchecked( Convert_i64_to_i8_clamp.INSTANCE );
			case I64_TO_U8: return Cast.unchecked( Convert_i64_to_u8_clamp.INSTANCE );
			case I64_TO_I16: return Cast.unchecked( Convert_i64_to_i16_clamp.INSTANCE );
			case I64_TO_U16: return Cast.unchecked( Convert_i64_to_u16_clamp.INSTANCE );
			case I64_TO_I32: return Cast.unchecked( Convert_i64_to_i32_clamp.INSTANCE );
			case I64_TO_U32: return Cast.unchecked( Convert_i64_to_u32_clamp.INSTANCE );
			case I64_TO_I64: return Cast.unchecked( Convert_i64_to_i64_clamp.INSTANCE );
			case I64_TO_F32: return Cast.unchecked( Convert_i64_to_f32_clamp.INSTANCE );
			case I64_TO_F64: return Cast.unchecked( Convert_i64_to_f64_clamp.INSTANCE );
			case F32_TO_I8: return Cast.unchecked( Convert_f32_to_i8_clamp.INSTANCE );
			case F32_TO_U8: return Cast.unchecked( Convert_f32_to_u8_clamp.INSTANCE );
			case F32_TO_I16: return Cast.unchecked( Convert_f32_to_i16_clamp.INSTANCE );
			case F32_TO_U16: return Cast.unchecked( Convert_f32_to_u16_clamp.INSTANCE );
			case F32_TO_I32: return Cast.unchecked( Convert_f32_to_i32_clamp.INSTANCE );
			case F32_TO_U32: return Cast.unchecked( Convert_f32_to_u32_clamp.INSTANCE );
			case F32_TO_I64: return Cast.unchecked( Convert_f32_to_i64_clamp.INSTANCE );
			case F32_TO_F32: return Cast.unchecked( Convert_f32_to_f32_clamp.INSTANCE );
			case F32_TO_F64: return Cast.unchecked( Convert_f32_to_f64_clamp.INSTANCE );
			case F64_TO_I8: return Cast.unchecked( Convert_f64_to_i8_clamp.INSTANCE );
			case F64_TO_U8: return Cast.unchecked( Convert_f64_to_u8_clamp.INSTANCE );
			case F64_TO_I16: return Cast.unchecked( Convert_f64_to_i16_clamp.INSTANCE );
			case F64_TO_U16: return Cast.unchecked( Convert_f64_to_u16_clamp.INSTANCE );
			case F64_TO_I32: return Cast.unchecked( Convert_f64_to_i32_clamp.INSTANCE );
			case F64_TO_U32: return Cast.unchecked( Convert_f64_to_u32_clamp.INSTANCE );
			case F64_TO_I64: return Cast.unchecked( Convert_f64_to_i64_clamp.INSTANCE );
			case F64_TO_F32: return Cast.unchecked( Convert_f64_to_f32_clamp.INSTANCE );
			case F64_TO_F64: return Cast.unchecked( Convert_f64_to_f64_clamp.INSTANCE );
			default:
				throw new IllegalArgumentException();
			}
		case CLAMP_MIN:
			switch( type )
			{
					case I8_TO_I8: return Cast.unchecked( Convert_i8_to_i8_clamp_min.INSTANCE );
					case I8_TO_U8: return Cast.unchecked( Convert_i8_to_u8_clamp_min.INSTANCE );
					case I8_TO_I16: return Cast.unchecked( Convert_i8_to_i16_clamp_min.INSTANCE );
					case I8_TO_U16: return Cast.unchecked( Convert_i8_to_u16_clamp_min.INSTANCE );
					case I8_TO_I32: return Cast.unchecked( Convert_i8_to_i32_clamp_min.INSTANCE );
					case I8_TO_U32: return Cast.unchecked( Convert_i8_to_u32_clamp_min.INSTANCE );
					case I8_TO_I64: return Cast.unchecked( Convert_i8_to_i64_clamp_min.INSTANCE );
					case I8_TO_F32: return Cast.unchecked( Convert_i8_to_f32_clamp_min.INSTANCE );
					case I8_TO_F64: return Cast.unchecked( Convert_i8_to_f64_clamp_min.INSTANCE );
					case U8_TO_I8: return Cast.unchecked( Convert_u8_to_i8_clamp_min.INSTANCE );
					case U8_TO_U8: return Cast.unchecked( Convert_u8_to_u8_clamp_min.INSTANCE );
					case U8_TO_I16: return Cast.unchecked( Convert_u8_to_i16_clamp_min.INSTANCE );
					case U8_TO_U16: return Cast.unchecked( Convert_u8_to_u16_clamp_min.INSTANCE );
					case U8_TO_I32: return Cast.unchecked( Convert_u8_to_i32_clamp_min.INSTANCE );
					case U8_TO_U32: return Cast.unchecked( Convert_u8_to_u32_clamp_min.INSTANCE );
					case U8_TO_I64: return Cast.unchecked( Convert_u8_to_i64_clamp_min.INSTANCE );
					case U8_TO_F32: return Cast.unchecked( Convert_u8_to_f32_clamp_min.INSTANCE );
					case U8_TO_F64: return Cast.unchecked( Convert_u8_to_f64_clamp_min.INSTANCE );
					case I16_TO_I8: return Cast.unchecked( Convert_i16_to_i8_clamp_min.INSTANCE );
					case I16_TO_U8: return Cast.unchecked( Convert_i16_to_u8_clamp_min.INSTANCE );
					case I16_TO_I16: return Cast.unchecked( Convert_i16_to_i16_clamp_min.INSTANCE );
					case I16_TO_U16: return Cast.unchecked( Convert_i16_to_u16_clamp_min.INSTANCE );
					case I16_TO_I32: return Cast.unchecked( Convert_i16_to_i32_clamp_min.INSTANCE );
					case I16_TO_U32: return Cast.unchecked( Convert_i16_to_u32_clamp_min.INSTANCE );
					case I16_TO_I64: return Cast.unchecked( Convert_i16_to_i64_clamp_min.INSTANCE );
					case I16_TO_F32: return Cast.unchecked( Convert_i16_to_f32_clamp_min.INSTANCE );
					case I16_TO_F64: return Cast.unchecked( Convert_i16_to_f64_clamp_min.INSTANCE );
					case U16_TO_I8: return Cast.unchecked( Convert_u16_to_i8_clamp_min.INSTANCE );
					case U16_TO_U8: return Cast.unchecked( Convert_u16_to_u8_clamp_min.INSTANCE );
					case U16_TO_I16: return Cast.unchecked( Convert_u16_to_i16_clamp_min.INSTANCE );
					case U16_TO_U16: return Cast.unchecked( Convert_u16_to_u16_clamp_min.INSTANCE );
					case U16_TO_I32: return Cast.unchecked( Convert_u16_to_i32_clamp_min.INSTANCE );
					case U16_TO_U32: return Cast.unchecked( Convert_u16_to_u32_clamp_min.INSTANCE );
					case U16_TO_I64: return Cast.unchecked( Convert_u16_to_i64_clamp_min.INSTANCE );
					case U16_TO_F32: return Cast.unchecked( Convert_u16_to_f32_clamp_min.INSTANCE );
					case U16_TO_F64: return Cast.unchecked( Convert_u16_to_f64_clamp_min.INSTANCE );
					case I32_TO_I8: return Cast.unchecked( Convert_i32_to_i8_clamp_min.INSTANCE );
					case I32_TO_U8: return Cast.unchecked( Convert_i32_to_u8_clamp_min.INSTANCE );
					case I32_TO_I16: return Cast.unchecked( Convert_i32_to_i16_clamp_min.INSTANCE );
					case I32_TO_U16: return Cast.unchecked( Convert_i32_to_u16_clamp_min.INSTANCE );
					case I32_TO_I32: return Cast.unchecked( Convert_i32_to_i32_clamp_min.INSTANCE );
					case I32_TO_U32: return Cast.unchecked( Convert_i32_to_u32_clamp_min.INSTANCE );
					case I32_TO_I64: return Cast.unchecked( Convert_i32_to_i64_clamp_min.INSTANCE );
					case I32_TO_F32: return Cast.unchecked( Convert_i32_to_f32_clamp_min.INSTANCE );
					case I32_TO_F64: return Cast.unchecked( Convert_i32_to_f64_clamp_min.INSTANCE );
					case U32_TO_I8: return Cast.unchecked( Convert_u32_to_i8_clamp_min.INSTANCE );
					case U32_TO_U8: return Cast.unchecked( Convert_u32_to_u8_clamp_min.INSTANCE );
					case U32_TO_I16: return Cast.unchecked( Convert_u32_to_i16_clamp_min.INSTANCE );
					case U32_TO_U16: return Cast.unchecked( Convert_u32_to_u16_clamp_min.INSTANCE );
					case U32_TO_I32: return Cast.unchecked( Convert_u32_to_i32_clamp_min.INSTANCE );
					case U32_TO_U32: return Cast.unchecked( Convert_u32_to_u32_clamp_min.INSTANCE );
					case U32_TO_I64: return Cast.unchecked( Convert_u32_to_i64_clamp_min.INSTANCE );
					case U32_TO_F32: return Cast.unchecked( Convert_u32_to_f32_clamp_min.INSTANCE );
					case U32_TO_F64: return Cast.unchecked( Convert_u32_to_f64_clamp_min.INSTANCE );
					case I64_TO_I8: return Cast.unchecked( Convert_i64_to_i8_clamp_min.INSTANCE );
					case I64_TO_U8: return Cast.unchecked( Convert_i64_to_u8_clamp_min.INSTANCE );
					case I64_TO_I16: return Cast.unchecked( Convert_i64_to_i16_clamp_min.INSTANCE );
					case I64_TO_U16: return Cast.unchecked( Convert_i64_to_u16_clamp_min.INSTANCE );
					case I64_TO_I32: return Cast.unchecked( Convert_i64_to_i32_clamp_min.INSTANCE );
					case I64_TO_U32: return Cast.unchecked( Convert_i64_to_u32_clamp_min.INSTANCE );
					case I64_TO_I64: return Cast.unchecked( Convert_i64_to_i64_clamp_min.INSTANCE );
					case I64_TO_F32: return Cast.unchecked( Convert_i64_to_f32_clamp_min.INSTANCE );
					case I64_TO_F64: return Cast.unchecked( Convert_i64_to_f64_clamp_min.INSTANCE );
					case F32_TO_I8: return Cast.unchecked( Convert_f32_to_i8_clamp_min.INSTANCE );
					case F32_TO_U8: return Cast.unchecked( Convert_f32_to_u8_clamp_min.INSTANCE );
					case F32_TO_I16: return Cast.unchecked( Convert_f32_to_i16_clamp_min.INSTANCE );
					case F32_TO_U16: return Cast.unchecked( Convert_f32_to_u16_clamp_min.INSTANCE );
					case F32_TO_I32: return Cast.unchecked( Convert_f32_to_i32_clamp_min.INSTANCE );
					case F32_TO_U32: return Cast.unchecked( Convert_f32_to_u32_clamp_min.INSTANCE );
					case F32_TO_I64: return Cast.unchecked( Convert_f32_to_i64_clamp_min.INSTANCE );
					case F32_TO_F32: return Cast.unchecked( Convert_f32_to_f32_clamp_min.INSTANCE );
					case F32_TO_F64: return Cast.unchecked( Convert_f32_to_f64_clamp_min.INSTANCE );
					case F64_TO_I8: return Cast.unchecked( Convert_f64_to_i8_clamp_min.INSTANCE );
					case F64_TO_U8: return Cast.unchecked( Convert_f64_to_u8_clamp_min.INSTANCE );
					case F64_TO_I16: return Cast.unchecked( Convert_f64_to_i16_clamp_min.INSTANCE );
					case F64_TO_U16: return Cast.unchecked( Convert_f64_to_u16_clamp_min.INSTANCE );
					case F64_TO_I32: return Cast.unchecked( Convert_f64_to_i32_clamp_min.INSTANCE );
					case F64_TO_U32: return Cast.unchecked( Convert_f64_to_u32_clamp_min.INSTANCE );
					case F64_TO_I64: return Cast.unchecked( Convert_f64_to_i64_clamp_min.INSTANCE );
					case F64_TO_F32: return Cast.unchecked( Convert_f64_to_f32_clamp_min.INSTANCE );
					case F64_TO_F64: return Cast.unchecked( Convert_f64_to_f64_clamp_min.INSTANCE );
			default:
				throw new IllegalArgumentException();
			}
		case CLAMP_MAX:
			switch( type )
			{
					case I8_TO_I8: return Cast.unchecked( Convert_i8_to_i8_clamp_max.INSTANCE );
					case I8_TO_U8: return Cast.unchecked( Convert_i8_to_u8_clamp_max.INSTANCE );
					case I8_TO_I16: return Cast.unchecked( Convert_i8_to_i16_clamp_max.INSTANCE );
					case I8_TO_U16: return Cast.unchecked( Convert_i8_to_u16_clamp_max.INSTANCE );
					case I8_TO_I32: return Cast.unchecked( Convert_i8_to_i32_clamp_max.INSTANCE );
					case I8_TO_U32: return Cast.unchecked( Convert_i8_to_u32_clamp_max.INSTANCE );
					case I8_TO_I64: return Cast.unchecked( Convert_i8_to_i64_clamp_max.INSTANCE );
					case I8_TO_F32: return Cast.unchecked( Convert_i8_to_f32_clamp_max.INSTANCE );
					case I8_TO_F64: return Cast.unchecked( Convert_i8_to_f64_clamp_max.INSTANCE );
					case U8_TO_I8: return Cast.unchecked( Convert_u8_to_i8_clamp_max.INSTANCE );
					case U8_TO_U8: return Cast.unchecked( Convert_u8_to_u8_clamp_max.INSTANCE );
					case U8_TO_I16: return Cast.unchecked( Convert_u8_to_i16_clamp_max.INSTANCE );
					case U8_TO_U16: return Cast.unchecked( Convert_u8_to_u16_clamp_max.INSTANCE );
					case U8_TO_I32: return Cast.unchecked( Convert_u8_to_i32_clamp_max.INSTANCE );
					case U8_TO_U32: return Cast.unchecked( Convert_u8_to_u32_clamp_max.INSTANCE );
					case U8_TO_I64: return Cast.unchecked( Convert_u8_to_i64_clamp_max.INSTANCE );
					case U8_TO_F32: return Cast.unchecked( Convert_u8_to_f32_clamp_max.INSTANCE );
					case U8_TO_F64: return Cast.unchecked( Convert_u8_to_f64_clamp_max.INSTANCE );
					case I16_TO_I8: return Cast.unchecked( Convert_i16_to_i8_clamp_max.INSTANCE );
					case I16_TO_U8: return Cast.unchecked( Convert_i16_to_u8_clamp_max.INSTANCE );
					case I16_TO_I16: return Cast.unchecked( Convert_i16_to_i16_clamp_max.INSTANCE );
					case I16_TO_U16: return Cast.unchecked( Convert_i16_to_u16_clamp_max.INSTANCE );
					case I16_TO_I32: return Cast.unchecked( Convert_i16_to_i32_clamp_max.INSTANCE );
					case I16_TO_U32: return Cast.unchecked( Convert_i16_to_u32_clamp_max.INSTANCE );
					case I16_TO_I64: return Cast.unchecked( Convert_i16_to_i64_clamp_max.INSTANCE );
					case I16_TO_F32: return Cast.unchecked( Convert_i16_to_f32_clamp_max.INSTANCE );
					case I16_TO_F64: return Cast.unchecked( Convert_i16_to_f64_clamp_max.INSTANCE );
					case U16_TO_I8: return Cast.unchecked( Convert_u16_to_i8_clamp_max.INSTANCE );
					case U16_TO_U8: return Cast.unchecked( Convert_u16_to_u8_clamp_max.INSTANCE );
					case U16_TO_I16: return Cast.unchecked( Convert_u16_to_i16_clamp_max.INSTANCE );
					case U16_TO_U16: return Cast.unchecked( Convert_u16_to_u16_clamp_max.INSTANCE );
					case U16_TO_I32: return Cast.unchecked( Convert_u16_to_i32_clamp_max.INSTANCE );
					case U16_TO_U32: return Cast.unchecked( Convert_u16_to_u32_clamp_max.INSTANCE );
					case U16_TO_I64: return Cast.unchecked( Convert_u16_to_i64_clamp_max.INSTANCE );
					case U16_TO_F32: return Cast.unchecked( Convert_u16_to_f32_clamp_max.INSTANCE );
					case U16_TO_F64: return Cast.unchecked( Convert_u16_to_f64_clamp_max.INSTANCE );
					case I32_TO_I8: return Cast.unchecked( Convert_i32_to_i8_clamp_max.INSTANCE );
					case I32_TO_U8: return Cast.unchecked( Convert_i32_to_u8_clamp_max.INSTANCE );
					case I32_TO_I16: return Cast.unchecked( Convert_i32_to_i16_clamp_max.INSTANCE );
					case I32_TO_U16: return Cast.unchecked( Convert_i32_to_u16_clamp_max.INSTANCE );
					case I32_TO_I32: return Cast.unchecked( Convert_i32_to_i32_clamp_max.INSTANCE );
					case I32_TO_U32: return Cast.unchecked( Convert_i32_to_u32_clamp_max.INSTANCE );
					case I32_TO_I64: return Cast.unchecked( Convert_i32_to_i64_clamp_max.INSTANCE );
					case I32_TO_F32: return Cast.unchecked( Convert_i32_to_f32_clamp_max.INSTANCE );
					case I32_TO_F64: return Cast.unchecked( Convert_i32_to_f64_clamp_max.INSTANCE );
					case U32_TO_I8: return Cast.unchecked( Convert_u32_to_i8_clamp_max.INSTANCE );
					case U32_TO_U8: return Cast.unchecked( Convert_u32_to_u8_clamp_max.INSTANCE );
					case U32_TO_I16: return Cast.unchecked( Convert_u32_to_i16_clamp_max.INSTANCE );
					case U32_TO_U16: return Cast.unchecked( Convert_u32_to_u16_clamp_max.INSTANCE );
					case U32_TO_I32: return Cast.unchecked( Convert_u32_to_i32_clamp_max.INSTANCE );
					case U32_TO_U32: return Cast.unchecked( Convert_u32_to_u32_clamp_max.INSTANCE );
					case U32_TO_I64: return Cast.unchecked( Convert_u32_to_i64_clamp_max.INSTANCE );
					case U32_TO_F32: return Cast.unchecked( Convert_u32_to_f32_clamp_max.INSTANCE );
					case U32_TO_F64: return Cast.unchecked( Convert_u32_to_f64_clamp_max.INSTANCE );
					case I64_TO_I8: return Cast.unchecked( Convert_i64_to_i8_clamp_max.INSTANCE );
					case I64_TO_U8: return Cast.unchecked( Convert_i64_to_u8_clamp_max.INSTANCE );
					case I64_TO_I16: return Cast.unchecked( Convert_i64_to_i16_clamp_max.INSTANCE );
					case I64_TO_U16: return Cast.unchecked( Convert_i64_to_u16_clamp_max.INSTANCE );
					case I64_TO_I32: return Cast.unchecked( Convert_i64_to_i32_clamp_max.INSTANCE );
					case I64_TO_U32: return Cast.unchecked( Convert_i64_to_u32_clamp_max.INSTANCE );
					case I64_TO_I64: return Cast.unchecked( Convert_i64_to_i64_clamp_max.INSTANCE );
					case I64_TO_F32: return Cast.unchecked( Convert_i64_to_f32_clamp_max.INSTANCE );
					case I64_TO_F64: return Cast.unchecked( Convert_i64_to_f64_clamp_max.INSTANCE );
					case F32_TO_I8: return Cast.unchecked( Convert_f32_to_i8_clamp_max.INSTANCE );
					case F32_TO_U8: return Cast.unchecked( Convert_f32_to_u8_clamp_max.INSTANCE );
					case F32_TO_I16: return Cast.unchecked( Convert_f32_to_i16_clamp_max.INSTANCE );
					case F32_TO_U16: return Cast.unchecked( Convert_f32_to_u16_clamp_max.INSTANCE );
					case F32_TO_I32: return Cast.unchecked( Convert_f32_to_i32_clamp_max.INSTANCE );
					case F32_TO_U32: return Cast.unchecked( Convert_f32_to_u32_clamp_max.INSTANCE );
					case F32_TO_I64: return Cast.unchecked( Convert_f32_to_i64_clamp_max.INSTANCE );
					case F32_TO_F32: return Cast.unchecked( Convert_f32_to_f32_clamp_max.INSTANCE );
					case F32_TO_F64: return Cast.unchecked( Convert_f32_to_f64_clamp_max.INSTANCE );
					case F64_TO_I8: return Cast.unchecked( Convert_f64_to_i8_clamp_max.INSTANCE );
					case F64_TO_U8: return Cast.unchecked( Convert_f64_to_u8_clamp_max.INSTANCE );
					case F64_TO_I16: return Cast.unchecked( Convert_f64_to_i16_clamp_max.INSTANCE );
					case F64_TO_U16: return Cast.unchecked( Convert_f64_to_u16_clamp_max.INSTANCE );
					case F64_TO_I32: return Cast.unchecked( Convert_f64_to_i32_clamp_max.INSTANCE );
					case F64_TO_U32: return Cast.unchecked( Convert_f64_to_u32_clamp_max.INSTANCE );
					case F64_TO_I64: return Cast.unchecked( Convert_f64_to_i64_clamp_max.INSTANCE );
					case F64_TO_F32: return Cast.unchecked( Convert_f64_to_f32_clamp_max.INSTANCE );
					case F64_TO_F64: return Cast.unchecked( Convert_f64_to_f64_clamp_max.INSTANCE );
			default:
				throw new IllegalArgumentException();
			}
		default:
			throw new IllegalArgumentException();
		}
	}


	static class Convert_i8_to_i8 implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_i8 INSTANCE = new Convert_i8_to_i8();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i8_clamp implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_i8_clamp INSTANCE = new Convert_i8_to_i8_clamp();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i8_clamp_min implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_i8_clamp_min INSTANCE = new Convert_i8_to_i8_clamp_min();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i8_clamp_max implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_i8_clamp_max INSTANCE = new Convert_i8_to_i8_clamp_max();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u8 implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_u8 INSTANCE = new Convert_i8_to_u8();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u8_clamp implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_u8_clamp INSTANCE = new Convert_i8_to_u8_clamp();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u8_clamp_min implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_u8_clamp_min INSTANCE = new Convert_i8_to_u8_clamp_min();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u8_clamp_max implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_i8_to_u8_clamp_max INSTANCE = new Convert_i8_to_u8_clamp_max();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i16 implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_i16 INSTANCE = new Convert_i8_to_i16();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i16_clamp implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_i16_clamp INSTANCE = new Convert_i8_to_i16_clamp();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i16_clamp_min implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_i16_clamp_min INSTANCE = new Convert_i8_to_i16_clamp_min();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i16_clamp_max implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_i16_clamp_max INSTANCE = new Convert_i8_to_i16_clamp_max();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u16 implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_u16 INSTANCE = new Convert_i8_to_u16();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u16_clamp implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_u16_clamp INSTANCE = new Convert_i8_to_u16_clamp();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u16_clamp_min implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_u16_clamp_min INSTANCE = new Convert_i8_to_u16_clamp_min();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u16_clamp_max implements ConvertLoop< byte[], short[] >
	{
		static final Convert_i8_to_u16_clamp_max INSTANCE = new Convert_i8_to_u16_clamp_max();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i32 implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_i32 INSTANCE = new Convert_i8_to_i32();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i32_clamp implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_i32_clamp INSTANCE = new Convert_i8_to_i32_clamp();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i32_clamp_min implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_i32_clamp_min INSTANCE = new Convert_i8_to_i32_clamp_min();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i32_clamp_max implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_i32_clamp_max INSTANCE = new Convert_i8_to_i32_clamp_max();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u32 implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_u32 INSTANCE = new Convert_i8_to_u32();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u32_clamp implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_u32_clamp INSTANCE = new Convert_i8_to_u32_clamp();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u32_clamp_min implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_u32_clamp_min INSTANCE = new Convert_i8_to_u32_clamp_min();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_u32_clamp_max implements ConvertLoop< byte[], int[] >
	{
		static final Convert_i8_to_u32_clamp_max INSTANCE = new Convert_i8_to_u32_clamp_max();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i64 implements ConvertLoop< byte[], long[] >
	{
		static final Convert_i8_to_i64 INSTANCE = new Convert_i8_to_i64();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i64_clamp implements ConvertLoop< byte[], long[] >
	{
		static final Convert_i8_to_i64_clamp INSTANCE = new Convert_i8_to_i64_clamp();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i64_clamp_min implements ConvertLoop< byte[], long[] >
	{
		static final Convert_i8_to_i64_clamp_min INSTANCE = new Convert_i8_to_i64_clamp_min();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_i64_clamp_max implements ConvertLoop< byte[], long[] >
	{
		static final Convert_i8_to_i64_clamp_max INSTANCE = new Convert_i8_to_i64_clamp_max();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f32 implements ConvertLoop< byte[], float[] >
	{
		static final Convert_i8_to_f32 INSTANCE = new Convert_i8_to_f32();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f32_clamp implements ConvertLoop< byte[], float[] >
	{
		static final Convert_i8_to_f32_clamp INSTANCE = new Convert_i8_to_f32_clamp();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f32_clamp_min implements ConvertLoop< byte[], float[] >
	{
		static final Convert_i8_to_f32_clamp_min INSTANCE = new Convert_i8_to_f32_clamp_min();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f32_clamp_max implements ConvertLoop< byte[], float[] >
	{
		static final Convert_i8_to_f32_clamp_max INSTANCE = new Convert_i8_to_f32_clamp_max();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f64 implements ConvertLoop< byte[], double[] >
	{
		static final Convert_i8_to_f64 INSTANCE = new Convert_i8_to_f64();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f64_clamp implements ConvertLoop< byte[], double[] >
	{
		static final Convert_i8_to_f64_clamp INSTANCE = new Convert_i8_to_f64_clamp();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f64_clamp_min implements ConvertLoop< byte[], double[] >
	{
		static final Convert_i8_to_f64_clamp_min INSTANCE = new Convert_i8_to_f64_clamp_min();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_i8( src[ i ] ) );
		}
	}

	static class Convert_i8_to_f64_clamp_max implements ConvertLoop< byte[], double[] >
	{
		static final Convert_i8_to_f64_clamp_max INSTANCE = new Convert_i8_to_f64_clamp_max();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_i8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i8 implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_i8 INSTANCE = new Convert_u8_to_i8();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i8_clamp implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_i8_clamp INSTANCE = new Convert_u8_to_i8_clamp();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i8_clamp_min implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_i8_clamp_min INSTANCE = new Convert_u8_to_i8_clamp_min();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i8_clamp_max implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_i8_clamp_max INSTANCE = new Convert_u8_to_i8_clamp_max();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u8 implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_u8 INSTANCE = new Convert_u8_to_u8();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u8_clamp implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_u8_clamp INSTANCE = new Convert_u8_to_u8_clamp();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u8_clamp_min implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_u8_clamp_min INSTANCE = new Convert_u8_to_u8_clamp_min();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u8_clamp_max implements ConvertLoop< byte[], byte[] >
	{
		static final Convert_u8_to_u8_clamp_max INSTANCE = new Convert_u8_to_u8_clamp_max();

		@Override
		public void apply( final byte[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i16 implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_i16 INSTANCE = new Convert_u8_to_i16();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i16_clamp implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_i16_clamp INSTANCE = new Convert_u8_to_i16_clamp();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i16_clamp_min implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_i16_clamp_min INSTANCE = new Convert_u8_to_i16_clamp_min();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i16_clamp_max implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_i16_clamp_max INSTANCE = new Convert_u8_to_i16_clamp_max();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u16 implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_u16 INSTANCE = new Convert_u8_to_u16();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u16_clamp implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_u16_clamp INSTANCE = new Convert_u8_to_u16_clamp();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u16_clamp_min implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_u16_clamp_min INSTANCE = new Convert_u8_to_u16_clamp_min();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u16_clamp_max implements ConvertLoop< byte[], short[] >
	{
		static final Convert_u8_to_u16_clamp_max INSTANCE = new Convert_u8_to_u16_clamp_max();

		@Override
		public void apply( final byte[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i32 implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_i32 INSTANCE = new Convert_u8_to_i32();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i32_clamp implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_i32_clamp INSTANCE = new Convert_u8_to_i32_clamp();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i32_clamp_min implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_i32_clamp_min INSTANCE = new Convert_u8_to_i32_clamp_min();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i32_clamp_max implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_i32_clamp_max INSTANCE = new Convert_u8_to_i32_clamp_max();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u32 implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_u32 INSTANCE = new Convert_u8_to_u32();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u32_clamp implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_u32_clamp INSTANCE = new Convert_u8_to_u32_clamp();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u32_clamp_min implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_u32_clamp_min INSTANCE = new Convert_u8_to_u32_clamp_min();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_u32_clamp_max implements ConvertLoop< byte[], int[] >
	{
		static final Convert_u8_to_u32_clamp_max INSTANCE = new Convert_u8_to_u32_clamp_max();

		@Override
		public void apply( final byte[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i64 implements ConvertLoop< byte[], long[] >
	{
		static final Convert_u8_to_i64 INSTANCE = new Convert_u8_to_i64();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i64_clamp implements ConvertLoop< byte[], long[] >
	{
		static final Convert_u8_to_i64_clamp INSTANCE = new Convert_u8_to_i64_clamp();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i64_clamp_min implements ConvertLoop< byte[], long[] >
	{
		static final Convert_u8_to_i64_clamp_min INSTANCE = new Convert_u8_to_i64_clamp_min();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_i64_clamp_max implements ConvertLoop< byte[], long[] >
	{
		static final Convert_u8_to_i64_clamp_max INSTANCE = new Convert_u8_to_i64_clamp_max();

		@Override
		public void apply( final byte[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f32 implements ConvertLoop< byte[], float[] >
	{
		static final Convert_u8_to_f32 INSTANCE = new Convert_u8_to_f32();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f32_clamp implements ConvertLoop< byte[], float[] >
	{
		static final Convert_u8_to_f32_clamp INSTANCE = new Convert_u8_to_f32_clamp();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f32_clamp_min implements ConvertLoop< byte[], float[] >
	{
		static final Convert_u8_to_f32_clamp_min INSTANCE = new Convert_u8_to_f32_clamp_min();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f32_clamp_max implements ConvertLoop< byte[], float[] >
	{
		static final Convert_u8_to_f32_clamp_max INSTANCE = new Convert_u8_to_f32_clamp_max();

		@Override
		public void apply( final byte[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f64 implements ConvertLoop< byte[], double[] >
	{
		static final Convert_u8_to_f64 INSTANCE = new Convert_u8_to_f64();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f64_clamp implements ConvertLoop< byte[], double[] >
	{
		static final Convert_u8_to_f64_clamp INSTANCE = new Convert_u8_to_f64_clamp();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f64_clamp_min implements ConvertLoop< byte[], double[] >
	{
		static final Convert_u8_to_f64_clamp_min INSTANCE = new Convert_u8_to_f64_clamp_min();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_u8( src[ i ] ) );
		}
	}

	static class Convert_u8_to_f64_clamp_max implements ConvertLoop< byte[], double[] >
	{
		static final Convert_u8_to_f64_clamp_max INSTANCE = new Convert_u8_to_f64_clamp_max();

		@Override
		public void apply( final byte[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_u8( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i8 implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_i8 INSTANCE = new Convert_i16_to_i8();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i8_clamp implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_i8_clamp INSTANCE = new Convert_i16_to_i8_clamp();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i8_clamp_min implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_i8_clamp_min INSTANCE = new Convert_i16_to_i8_clamp_min();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i8_clamp_max implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_i8_clamp_max INSTANCE = new Convert_i16_to_i8_clamp_max();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u8 implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_u8 INSTANCE = new Convert_i16_to_u8();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u8_clamp implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_u8_clamp INSTANCE = new Convert_i16_to_u8_clamp();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u8_clamp_min implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_u8_clamp_min INSTANCE = new Convert_i16_to_u8_clamp_min();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u8_clamp_max implements ConvertLoop< short[], byte[] >
	{
		static final Convert_i16_to_u8_clamp_max INSTANCE = new Convert_i16_to_u8_clamp_max();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i16 implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_i16 INSTANCE = new Convert_i16_to_i16();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i16_clamp implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_i16_clamp INSTANCE = new Convert_i16_to_i16_clamp();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i16_clamp_min implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_i16_clamp_min INSTANCE = new Convert_i16_to_i16_clamp_min();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i16_clamp_max implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_i16_clamp_max INSTANCE = new Convert_i16_to_i16_clamp_max();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u16 implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_u16 INSTANCE = new Convert_i16_to_u16();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u16_clamp implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_u16_clamp INSTANCE = new Convert_i16_to_u16_clamp();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u16_clamp_min implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_u16_clamp_min INSTANCE = new Convert_i16_to_u16_clamp_min();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u16_clamp_max implements ConvertLoop< short[], short[] >
	{
		static final Convert_i16_to_u16_clamp_max INSTANCE = new Convert_i16_to_u16_clamp_max();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i32 implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_i32 INSTANCE = new Convert_i16_to_i32();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i32_clamp implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_i32_clamp INSTANCE = new Convert_i16_to_i32_clamp();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i32_clamp_min implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_i32_clamp_min INSTANCE = new Convert_i16_to_i32_clamp_min();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i32_clamp_max implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_i32_clamp_max INSTANCE = new Convert_i16_to_i32_clamp_max();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u32 implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_u32 INSTANCE = new Convert_i16_to_u32();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u32_clamp implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_u32_clamp INSTANCE = new Convert_i16_to_u32_clamp();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u32_clamp_min implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_u32_clamp_min INSTANCE = new Convert_i16_to_u32_clamp_min();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_u32_clamp_max implements ConvertLoop< short[], int[] >
	{
		static final Convert_i16_to_u32_clamp_max INSTANCE = new Convert_i16_to_u32_clamp_max();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i64 implements ConvertLoop< short[], long[] >
	{
		static final Convert_i16_to_i64 INSTANCE = new Convert_i16_to_i64();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i64_clamp implements ConvertLoop< short[], long[] >
	{
		static final Convert_i16_to_i64_clamp INSTANCE = new Convert_i16_to_i64_clamp();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i64_clamp_min implements ConvertLoop< short[], long[] >
	{
		static final Convert_i16_to_i64_clamp_min INSTANCE = new Convert_i16_to_i64_clamp_min();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_i64_clamp_max implements ConvertLoop< short[], long[] >
	{
		static final Convert_i16_to_i64_clamp_max INSTANCE = new Convert_i16_to_i64_clamp_max();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f32 implements ConvertLoop< short[], float[] >
	{
		static final Convert_i16_to_f32 INSTANCE = new Convert_i16_to_f32();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f32_clamp implements ConvertLoop< short[], float[] >
	{
		static final Convert_i16_to_f32_clamp INSTANCE = new Convert_i16_to_f32_clamp();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f32_clamp_min implements ConvertLoop< short[], float[] >
	{
		static final Convert_i16_to_f32_clamp_min INSTANCE = new Convert_i16_to_f32_clamp_min();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f32_clamp_max implements ConvertLoop< short[], float[] >
	{
		static final Convert_i16_to_f32_clamp_max INSTANCE = new Convert_i16_to_f32_clamp_max();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f64 implements ConvertLoop< short[], double[] >
	{
		static final Convert_i16_to_f64 INSTANCE = new Convert_i16_to_f64();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f64_clamp implements ConvertLoop< short[], double[] >
	{
		static final Convert_i16_to_f64_clamp INSTANCE = new Convert_i16_to_f64_clamp();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f64_clamp_min implements ConvertLoop< short[], double[] >
	{
		static final Convert_i16_to_f64_clamp_min INSTANCE = new Convert_i16_to_f64_clamp_min();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_i16( src[ i ] ) );
		}
	}

	static class Convert_i16_to_f64_clamp_max implements ConvertLoop< short[], double[] >
	{
		static final Convert_i16_to_f64_clamp_max INSTANCE = new Convert_i16_to_f64_clamp_max();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_i16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i8 implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_i8 INSTANCE = new Convert_u16_to_i8();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i8_clamp implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_i8_clamp INSTANCE = new Convert_u16_to_i8_clamp();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i8_clamp_min implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_i8_clamp_min INSTANCE = new Convert_u16_to_i8_clamp_min();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i8_clamp_max implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_i8_clamp_max INSTANCE = new Convert_u16_to_i8_clamp_max();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u8 implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_u8 INSTANCE = new Convert_u16_to_u8();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u8_clamp implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_u8_clamp INSTANCE = new Convert_u16_to_u8_clamp();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u8_clamp_min implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_u8_clamp_min INSTANCE = new Convert_u16_to_u8_clamp_min();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u8_clamp_max implements ConvertLoop< short[], byte[] >
	{
		static final Convert_u16_to_u8_clamp_max INSTANCE = new Convert_u16_to_u8_clamp_max();

		@Override
		public void apply( final short[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i16 implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_i16 INSTANCE = new Convert_u16_to_i16();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i16_clamp implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_i16_clamp INSTANCE = new Convert_u16_to_i16_clamp();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i16_clamp_min implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_i16_clamp_min INSTANCE = new Convert_u16_to_i16_clamp_min();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i16_clamp_max implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_i16_clamp_max INSTANCE = new Convert_u16_to_i16_clamp_max();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u16 implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_u16 INSTANCE = new Convert_u16_to_u16();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u16_clamp implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_u16_clamp INSTANCE = new Convert_u16_to_u16_clamp();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u16_clamp_min implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_u16_clamp_min INSTANCE = new Convert_u16_to_u16_clamp_min();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u16_clamp_max implements ConvertLoop< short[], short[] >
	{
		static final Convert_u16_to_u16_clamp_max INSTANCE = new Convert_u16_to_u16_clamp_max();

		@Override
		public void apply( final short[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i32 implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_i32 INSTANCE = new Convert_u16_to_i32();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i32_clamp implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_i32_clamp INSTANCE = new Convert_u16_to_i32_clamp();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i32_clamp_min implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_i32_clamp_min INSTANCE = new Convert_u16_to_i32_clamp_min();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i32_clamp_max implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_i32_clamp_max INSTANCE = new Convert_u16_to_i32_clamp_max();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u32 implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_u32 INSTANCE = new Convert_u16_to_u32();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u32_clamp implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_u32_clamp INSTANCE = new Convert_u16_to_u32_clamp();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u32_clamp_min implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_u32_clamp_min INSTANCE = new Convert_u16_to_u32_clamp_min();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_u32_clamp_max implements ConvertLoop< short[], int[] >
	{
		static final Convert_u16_to_u32_clamp_max INSTANCE = new Convert_u16_to_u32_clamp_max();

		@Override
		public void apply( final short[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i64 implements ConvertLoop< short[], long[] >
	{
		static final Convert_u16_to_i64 INSTANCE = new Convert_u16_to_i64();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i64_clamp implements ConvertLoop< short[], long[] >
	{
		static final Convert_u16_to_i64_clamp INSTANCE = new Convert_u16_to_i64_clamp();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i64_clamp_min implements ConvertLoop< short[], long[] >
	{
		static final Convert_u16_to_i64_clamp_min INSTANCE = new Convert_u16_to_i64_clamp_min();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_i64_clamp_max implements ConvertLoop< short[], long[] >
	{
		static final Convert_u16_to_i64_clamp_max INSTANCE = new Convert_u16_to_i64_clamp_max();

		@Override
		public void apply( final short[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f32 implements ConvertLoop< short[], float[] >
	{
		static final Convert_u16_to_f32 INSTANCE = new Convert_u16_to_f32();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f32_clamp implements ConvertLoop< short[], float[] >
	{
		static final Convert_u16_to_f32_clamp INSTANCE = new Convert_u16_to_f32_clamp();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f32_clamp_min implements ConvertLoop< short[], float[] >
	{
		static final Convert_u16_to_f32_clamp_min INSTANCE = new Convert_u16_to_f32_clamp_min();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f32_clamp_max implements ConvertLoop< short[], float[] >
	{
		static final Convert_u16_to_f32_clamp_max INSTANCE = new Convert_u16_to_f32_clamp_max();

		@Override
		public void apply( final short[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f64 implements ConvertLoop< short[], double[] >
	{
		static final Convert_u16_to_f64 INSTANCE = new Convert_u16_to_f64();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f64_clamp implements ConvertLoop< short[], double[] >
	{
		static final Convert_u16_to_f64_clamp INSTANCE = new Convert_u16_to_f64_clamp();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f64_clamp_min implements ConvertLoop< short[], double[] >
	{
		static final Convert_u16_to_f64_clamp_min INSTANCE = new Convert_u16_to_f64_clamp_min();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_u16( src[ i ] ) );
		}
	}

	static class Convert_u16_to_f64_clamp_max implements ConvertLoop< short[], double[] >
	{
		static final Convert_u16_to_f64_clamp_max INSTANCE = new Convert_u16_to_f64_clamp_max();

		@Override
		public void apply( final short[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_u16( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i8 implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_i8 INSTANCE = new Convert_i32_to_i8();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i8_clamp implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_i8_clamp INSTANCE = new Convert_i32_to_i8_clamp();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i8_clamp_min implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_i8_clamp_min INSTANCE = new Convert_i32_to_i8_clamp_min();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i8_clamp_max implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_i8_clamp_max INSTANCE = new Convert_i32_to_i8_clamp_max();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u8 implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_u8 INSTANCE = new Convert_i32_to_u8();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u8_clamp implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_u8_clamp INSTANCE = new Convert_i32_to_u8_clamp();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u8_clamp_min implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_u8_clamp_min INSTANCE = new Convert_i32_to_u8_clamp_min();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u8_clamp_max implements ConvertLoop< int[], byte[] >
	{
		static final Convert_i32_to_u8_clamp_max INSTANCE = new Convert_i32_to_u8_clamp_max();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i16 implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_i16 INSTANCE = new Convert_i32_to_i16();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i16_clamp implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_i16_clamp INSTANCE = new Convert_i32_to_i16_clamp();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i16_clamp_min implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_i16_clamp_min INSTANCE = new Convert_i32_to_i16_clamp_min();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i16_clamp_max implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_i16_clamp_max INSTANCE = new Convert_i32_to_i16_clamp_max();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u16 implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_u16 INSTANCE = new Convert_i32_to_u16();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u16_clamp implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_u16_clamp INSTANCE = new Convert_i32_to_u16_clamp();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u16_clamp_min implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_u16_clamp_min INSTANCE = new Convert_i32_to_u16_clamp_min();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u16_clamp_max implements ConvertLoop< int[], short[] >
	{
		static final Convert_i32_to_u16_clamp_max INSTANCE = new Convert_i32_to_u16_clamp_max();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i32 implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_i32 INSTANCE = new Convert_i32_to_i32();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i32_clamp implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_i32_clamp INSTANCE = new Convert_i32_to_i32_clamp();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i32_clamp_min implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_i32_clamp_min INSTANCE = new Convert_i32_to_i32_clamp_min();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i32_clamp_max implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_i32_clamp_max INSTANCE = new Convert_i32_to_i32_clamp_max();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u32 implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_u32 INSTANCE = new Convert_i32_to_u32();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u32_clamp implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_u32_clamp INSTANCE = new Convert_i32_to_u32_clamp();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u32_clamp_min implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_u32_clamp_min INSTANCE = new Convert_i32_to_u32_clamp_min();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_u32_clamp_max implements ConvertLoop< int[], int[] >
	{
		static final Convert_i32_to_u32_clamp_max INSTANCE = new Convert_i32_to_u32_clamp_max();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i64 implements ConvertLoop< int[], long[] >
	{
		static final Convert_i32_to_i64 INSTANCE = new Convert_i32_to_i64();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i64_clamp implements ConvertLoop< int[], long[] >
	{
		static final Convert_i32_to_i64_clamp INSTANCE = new Convert_i32_to_i64_clamp();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i64_clamp_min implements ConvertLoop< int[], long[] >
	{
		static final Convert_i32_to_i64_clamp_min INSTANCE = new Convert_i32_to_i64_clamp_min();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_i64_clamp_max implements ConvertLoop< int[], long[] >
	{
		static final Convert_i32_to_i64_clamp_max INSTANCE = new Convert_i32_to_i64_clamp_max();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f32 implements ConvertLoop< int[], float[] >
	{
		static final Convert_i32_to_f32 INSTANCE = new Convert_i32_to_f32();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f32_clamp implements ConvertLoop< int[], float[] >
	{
		static final Convert_i32_to_f32_clamp INSTANCE = new Convert_i32_to_f32_clamp();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f32_clamp_min implements ConvertLoop< int[], float[] >
	{
		static final Convert_i32_to_f32_clamp_min INSTANCE = new Convert_i32_to_f32_clamp_min();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f32_clamp_max implements ConvertLoop< int[], float[] >
	{
		static final Convert_i32_to_f32_clamp_max INSTANCE = new Convert_i32_to_f32_clamp_max();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f64 implements ConvertLoop< int[], double[] >
	{
		static final Convert_i32_to_f64 INSTANCE = new Convert_i32_to_f64();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f64_clamp implements ConvertLoop< int[], double[] >
	{
		static final Convert_i32_to_f64_clamp INSTANCE = new Convert_i32_to_f64_clamp();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f64_clamp_min implements ConvertLoop< int[], double[] >
	{
		static final Convert_i32_to_f64_clamp_min INSTANCE = new Convert_i32_to_f64_clamp_min();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_i32( src[ i ] ) );
		}
	}

	static class Convert_i32_to_f64_clamp_max implements ConvertLoop< int[], double[] >
	{
		static final Convert_i32_to_f64_clamp_max INSTANCE = new Convert_i32_to_f64_clamp_max();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_i32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i8 implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_i8 INSTANCE = new Convert_u32_to_i8();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i8_clamp implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_i8_clamp INSTANCE = new Convert_u32_to_i8_clamp();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i8_clamp_min implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_i8_clamp_min INSTANCE = new Convert_u32_to_i8_clamp_min();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i8_clamp_max implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_i8_clamp_max INSTANCE = new Convert_u32_to_i8_clamp_max();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u8 implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_u8 INSTANCE = new Convert_u32_to_u8();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u8_clamp implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_u8_clamp INSTANCE = new Convert_u32_to_u8_clamp();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u8_clamp_min implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_u8_clamp_min INSTANCE = new Convert_u32_to_u8_clamp_min();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u8_clamp_max implements ConvertLoop< int[], byte[] >
	{
		static final Convert_u32_to_u8_clamp_max INSTANCE = new Convert_u32_to_u8_clamp_max();

		@Override
		public void apply( final int[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i16 implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_i16 INSTANCE = new Convert_u32_to_i16();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i16_clamp implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_i16_clamp INSTANCE = new Convert_u32_to_i16_clamp();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i16_clamp_min implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_i16_clamp_min INSTANCE = new Convert_u32_to_i16_clamp_min();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i16_clamp_max implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_i16_clamp_max INSTANCE = new Convert_u32_to_i16_clamp_max();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u16 implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_u16 INSTANCE = new Convert_u32_to_u16();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u16_clamp implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_u16_clamp INSTANCE = new Convert_u32_to_u16_clamp();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u16_clamp_min implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_u16_clamp_min INSTANCE = new Convert_u32_to_u16_clamp_min();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u16_clamp_max implements ConvertLoop< int[], short[] >
	{
		static final Convert_u32_to_u16_clamp_max INSTANCE = new Convert_u32_to_u16_clamp_max();

		@Override
		public void apply( final int[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i32 implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_i32 INSTANCE = new Convert_u32_to_i32();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i32_clamp implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_i32_clamp INSTANCE = new Convert_u32_to_i32_clamp();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i32_clamp_min implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_i32_clamp_min INSTANCE = new Convert_u32_to_i32_clamp_min();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i32_clamp_max implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_i32_clamp_max INSTANCE = new Convert_u32_to_i32_clamp_max();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u32 implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_u32 INSTANCE = new Convert_u32_to_u32();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u32_clamp implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_u32_clamp INSTANCE = new Convert_u32_to_u32_clamp();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u32_clamp_min implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_u32_clamp_min INSTANCE = new Convert_u32_to_u32_clamp_min();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_u32_clamp_max implements ConvertLoop< int[], int[] >
	{
		static final Convert_u32_to_u32_clamp_max INSTANCE = new Convert_u32_to_u32_clamp_max();

		@Override
		public void apply( final int[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i64 implements ConvertLoop< int[], long[] >
	{
		static final Convert_u32_to_i64 INSTANCE = new Convert_u32_to_i64();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i64_clamp implements ConvertLoop< int[], long[] >
	{
		static final Convert_u32_to_i64_clamp INSTANCE = new Convert_u32_to_i64_clamp();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i64_clamp_min implements ConvertLoop< int[], long[] >
	{
		static final Convert_u32_to_i64_clamp_min INSTANCE = new Convert_u32_to_i64_clamp_min();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_i64_clamp_max implements ConvertLoop< int[], long[] >
	{
		static final Convert_u32_to_i64_clamp_max INSTANCE = new Convert_u32_to_i64_clamp_max();

		@Override
		public void apply( final int[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f32 implements ConvertLoop< int[], float[] >
	{
		static final Convert_u32_to_f32 INSTANCE = new Convert_u32_to_f32();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f32_clamp implements ConvertLoop< int[], float[] >
	{
		static final Convert_u32_to_f32_clamp INSTANCE = new Convert_u32_to_f32_clamp();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f32_clamp_min implements ConvertLoop< int[], float[] >
	{
		static final Convert_u32_to_f32_clamp_min INSTANCE = new Convert_u32_to_f32_clamp_min();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f32_clamp_max implements ConvertLoop< int[], float[] >
	{
		static final Convert_u32_to_f32_clamp_max INSTANCE = new Convert_u32_to_f32_clamp_max();

		@Override
		public void apply( final int[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f64 implements ConvertLoop< int[], double[] >
	{
		static final Convert_u32_to_f64 INSTANCE = new Convert_u32_to_f64();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f64_clamp implements ConvertLoop< int[], double[] >
	{
		static final Convert_u32_to_f64_clamp INSTANCE = new Convert_u32_to_f64_clamp();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f64_clamp_min implements ConvertLoop< int[], double[] >
	{
		static final Convert_u32_to_f64_clamp_min INSTANCE = new Convert_u32_to_f64_clamp_min();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_u32( src[ i ] ) );
		}
	}

	static class Convert_u32_to_f64_clamp_max implements ConvertLoop< int[], double[] >
	{
		static final Convert_u32_to_f64_clamp_max INSTANCE = new Convert_u32_to_f64_clamp_max();

		@Override
		public void apply( final int[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_u32( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i8 implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_i8 INSTANCE = new Convert_i64_to_i8();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i8_clamp implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_i8_clamp INSTANCE = new Convert_i64_to_i8_clamp();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i8_clamp_min implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_i8_clamp_min INSTANCE = new Convert_i64_to_i8_clamp_min();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i8_clamp_max implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_i8_clamp_max INSTANCE = new Convert_i64_to_i8_clamp_max();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u8 implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_u8 INSTANCE = new Convert_i64_to_u8();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u8_clamp implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_u8_clamp INSTANCE = new Convert_i64_to_u8_clamp();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u8_clamp_min implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_u8_clamp_min INSTANCE = new Convert_i64_to_u8_clamp_min();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u8_clamp_max implements ConvertLoop< long[], byte[] >
	{
		static final Convert_i64_to_u8_clamp_max INSTANCE = new Convert_i64_to_u8_clamp_max();

		@Override
		public void apply( final long[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i16 implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_i16 INSTANCE = new Convert_i64_to_i16();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i16_clamp implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_i16_clamp INSTANCE = new Convert_i64_to_i16_clamp();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i16_clamp_min implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_i16_clamp_min INSTANCE = new Convert_i64_to_i16_clamp_min();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i16_clamp_max implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_i16_clamp_max INSTANCE = new Convert_i64_to_i16_clamp_max();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u16 implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_u16 INSTANCE = new Convert_i64_to_u16();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u16_clamp implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_u16_clamp INSTANCE = new Convert_i64_to_u16_clamp();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u16_clamp_min implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_u16_clamp_min INSTANCE = new Convert_i64_to_u16_clamp_min();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u16_clamp_max implements ConvertLoop< long[], short[] >
	{
		static final Convert_i64_to_u16_clamp_max INSTANCE = new Convert_i64_to_u16_clamp_max();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i32 implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_i32 INSTANCE = new Convert_i64_to_i32();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i32_clamp implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_i32_clamp INSTANCE = new Convert_i64_to_i32_clamp();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i32_clamp_min implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_i32_clamp_min INSTANCE = new Convert_i64_to_i32_clamp_min();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i32_clamp_max implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_i32_clamp_max INSTANCE = new Convert_i64_to_i32_clamp_max();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u32 implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_u32 INSTANCE = new Convert_i64_to_u32();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u32_clamp implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_u32_clamp INSTANCE = new Convert_i64_to_u32_clamp();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u32_clamp_min implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_u32_clamp_min INSTANCE = new Convert_i64_to_u32_clamp_min();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_u32_clamp_max implements ConvertLoop< long[], int[] >
	{
		static final Convert_i64_to_u32_clamp_max INSTANCE = new Convert_i64_to_u32_clamp_max();

		@Override
		public void apply( final long[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i64 implements ConvertLoop< long[], long[] >
	{
		static final Convert_i64_to_i64 INSTANCE = new Convert_i64_to_i64();

		@Override
		public void apply( final long[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i64_clamp implements ConvertLoop< long[], long[] >
	{
		static final Convert_i64_to_i64_clamp INSTANCE = new Convert_i64_to_i64_clamp();

		@Override
		public void apply( final long[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i64_clamp_min implements ConvertLoop< long[], long[] >
	{
		static final Convert_i64_to_i64_clamp_min INSTANCE = new Convert_i64_to_i64_clamp_min();

		@Override
		public void apply( final long[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_i64_clamp_max implements ConvertLoop< long[], long[] >
	{
		static final Convert_i64_to_i64_clamp_max INSTANCE = new Convert_i64_to_i64_clamp_max();

		@Override
		public void apply( final long[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f32 implements ConvertLoop< long[], float[] >
	{
		static final Convert_i64_to_f32 INSTANCE = new Convert_i64_to_f32();

		@Override
		public void apply( final long[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f32_clamp implements ConvertLoop< long[], float[] >
	{
		static final Convert_i64_to_f32_clamp INSTANCE = new Convert_i64_to_f32_clamp();

		@Override
		public void apply( final long[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f32_clamp_min implements ConvertLoop< long[], float[] >
	{
		static final Convert_i64_to_f32_clamp_min INSTANCE = new Convert_i64_to_f32_clamp_min();

		@Override
		public void apply( final long[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f32_clamp_max implements ConvertLoop< long[], float[] >
	{
		static final Convert_i64_to_f32_clamp_max INSTANCE = new Convert_i64_to_f32_clamp_max();

		@Override
		public void apply( final long[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f64 implements ConvertLoop< long[], double[] >
	{
		static final Convert_i64_to_f64 INSTANCE = new Convert_i64_to_f64();

		@Override
		public void apply( final long[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f64_clamp implements ConvertLoop< long[], double[] >
	{
		static final Convert_i64_to_f64_clamp INSTANCE = new Convert_i64_to_f64_clamp();

		@Override
		public void apply( final long[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f64_clamp_min implements ConvertLoop< long[], double[] >
	{
		static final Convert_i64_to_f64_clamp_min INSTANCE = new Convert_i64_to_f64_clamp_min();

		@Override
		public void apply( final long[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_i64( src[ i ] ) );
		}
	}

	static class Convert_i64_to_f64_clamp_max implements ConvertLoop< long[], double[] >
	{
		static final Convert_i64_to_f64_clamp_max INSTANCE = new Convert_i64_to_f64_clamp_max();

		@Override
		public void apply( final long[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_i64( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i8 implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_i8 INSTANCE = new Convert_f32_to_i8();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i8_clamp implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_i8_clamp INSTANCE = new Convert_f32_to_i8_clamp();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i8_clamp_min implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_i8_clamp_min INSTANCE = new Convert_f32_to_i8_clamp_min();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i8_clamp_max implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_i8_clamp_max INSTANCE = new Convert_f32_to_i8_clamp_max();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u8 implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_u8 INSTANCE = new Convert_f32_to_u8();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u8_clamp implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_u8_clamp INSTANCE = new Convert_f32_to_u8_clamp();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u8_clamp_min implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_u8_clamp_min INSTANCE = new Convert_f32_to_u8_clamp_min();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u8_clamp_max implements ConvertLoop< float[], byte[] >
	{
		static final Convert_f32_to_u8_clamp_max INSTANCE = new Convert_f32_to_u8_clamp_max();

		@Override
		public void apply( final float[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i16 implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_i16 INSTANCE = new Convert_f32_to_i16();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i16_clamp implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_i16_clamp INSTANCE = new Convert_f32_to_i16_clamp();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i16_clamp_min implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_i16_clamp_min INSTANCE = new Convert_f32_to_i16_clamp_min();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i16_clamp_max implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_i16_clamp_max INSTANCE = new Convert_f32_to_i16_clamp_max();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u16 implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_u16 INSTANCE = new Convert_f32_to_u16();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u16_clamp implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_u16_clamp INSTANCE = new Convert_f32_to_u16_clamp();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u16_clamp_min implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_u16_clamp_min INSTANCE = new Convert_f32_to_u16_clamp_min();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u16_clamp_max implements ConvertLoop< float[], short[] >
	{
		static final Convert_f32_to_u16_clamp_max INSTANCE = new Convert_f32_to_u16_clamp_max();

		@Override
		public void apply( final float[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i32 implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_i32 INSTANCE = new Convert_f32_to_i32();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i32_clamp implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_i32_clamp INSTANCE = new Convert_f32_to_i32_clamp();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i32_clamp_min implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_i32_clamp_min INSTANCE = new Convert_f32_to_i32_clamp_min();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i32_clamp_max implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_i32_clamp_max INSTANCE = new Convert_f32_to_i32_clamp_max();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u32 implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_u32 INSTANCE = new Convert_f32_to_u32();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u32_clamp implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_u32_clamp INSTANCE = new Convert_f32_to_u32_clamp();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u32_clamp_min implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_u32_clamp_min INSTANCE = new Convert_f32_to_u32_clamp_min();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_u32_clamp_max implements ConvertLoop< float[], int[] >
	{
		static final Convert_f32_to_u32_clamp_max INSTANCE = new Convert_f32_to_u32_clamp_max();

		@Override
		public void apply( final float[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i64 implements ConvertLoop< float[], long[] >
	{
		static final Convert_f32_to_i64 INSTANCE = new Convert_f32_to_i64();

		@Override
		public void apply( final float[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i64_clamp implements ConvertLoop< float[], long[] >
	{
		static final Convert_f32_to_i64_clamp INSTANCE = new Convert_f32_to_i64_clamp();

		@Override
		public void apply( final float[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i64_clamp_min implements ConvertLoop< float[], long[] >
	{
		static final Convert_f32_to_i64_clamp_min INSTANCE = new Convert_f32_to_i64_clamp_min();

		@Override
		public void apply( final float[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_i64_clamp_max implements ConvertLoop< float[], long[] >
	{
		static final Convert_f32_to_i64_clamp_max INSTANCE = new Convert_f32_to_i64_clamp_max();

		@Override
		public void apply( final float[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f32 implements ConvertLoop< float[], float[] >
	{
		static final Convert_f32_to_f32 INSTANCE = new Convert_f32_to_f32();

		@Override
		public void apply( final float[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f32_clamp implements ConvertLoop< float[], float[] >
	{
		static final Convert_f32_to_f32_clamp INSTANCE = new Convert_f32_to_f32_clamp();

		@Override
		public void apply( final float[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f32_clamp_min implements ConvertLoop< float[], float[] >
	{
		static final Convert_f32_to_f32_clamp_min INSTANCE = new Convert_f32_to_f32_clamp_min();

		@Override
		public void apply( final float[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f32_clamp_max implements ConvertLoop< float[], float[] >
	{
		static final Convert_f32_to_f32_clamp_max INSTANCE = new Convert_f32_to_f32_clamp_max();

		@Override
		public void apply( final float[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f64 implements ConvertLoop< float[], double[] >
	{
		static final Convert_f32_to_f64 INSTANCE = new Convert_f32_to_f64();

		@Override
		public void apply( final float[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f64_clamp implements ConvertLoop< float[], double[] >
	{
		static final Convert_f32_to_f64_clamp INSTANCE = new Convert_f32_to_f64_clamp();

		@Override
		public void apply( final float[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f64_clamp_min implements ConvertLoop< float[], double[] >
	{
		static final Convert_f32_to_f64_clamp_min INSTANCE = new Convert_f32_to_f64_clamp_min();

		@Override
		public void apply( final float[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f32_to_f64_clamp_max implements ConvertLoop< float[], double[] >
	{
		static final Convert_f32_to_f64_clamp_max INSTANCE = new Convert_f32_to_f64_clamp_max();

		@Override
		public void apply( final float[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_f32( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i8 implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_i8 INSTANCE = new Convert_f64_to_i8();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i8_clamp implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_i8_clamp INSTANCE = new Convert_f64_to_i8_clamp();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i8_clamp_min implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_i8_clamp_min INSTANCE = new Convert_f64_to_i8_clamp_min();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i8_clamp_max implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_i8_clamp_max INSTANCE = new Convert_f64_to_i8_clamp_max();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i8_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u8 implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_u8 INSTANCE = new Convert_f64_to_u8();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u8_clamp implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_u8_clamp INSTANCE = new Convert_f64_to_u8_clamp();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u8_clamp_min implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_u8_clamp_min INSTANCE = new Convert_f64_to_u8_clamp_min();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u8_clamp_max implements ConvertLoop< double[], byte[] >
	{
		static final Convert_f64_to_u8_clamp_max INSTANCE = new Convert_f64_to_u8_clamp_max();

		@Override
		public void apply( final double[] src, final byte[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u8_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i16 implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_i16 INSTANCE = new Convert_f64_to_i16();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i16_clamp implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_i16_clamp INSTANCE = new Convert_f64_to_i16_clamp();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i16_clamp_min implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_i16_clamp_min INSTANCE = new Convert_f64_to_i16_clamp_min();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i16_clamp_max implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_i16_clamp_max INSTANCE = new Convert_f64_to_i16_clamp_max();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i16_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u16 implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_u16 INSTANCE = new Convert_f64_to_u16();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u16_clamp implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_u16_clamp INSTANCE = new Convert_f64_to_u16_clamp();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u16_clamp_min implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_u16_clamp_min INSTANCE = new Convert_f64_to_u16_clamp_min();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u16_clamp_max implements ConvertLoop< double[], short[] >
	{
		static final Convert_f64_to_u16_clamp_max INSTANCE = new Convert_f64_to_u16_clamp_max();

		@Override
		public void apply( final double[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u16_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i32 implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_i32 INSTANCE = new Convert_f64_to_i32();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i32_clamp implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_i32_clamp INSTANCE = new Convert_f64_to_i32_clamp();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i32_clamp_min implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_i32_clamp_min INSTANCE = new Convert_f64_to_i32_clamp_min();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i32_clamp_max implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_i32_clamp_max INSTANCE = new Convert_f64_to_i32_clamp_max();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i32_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u32 implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_u32 INSTANCE = new Convert_f64_to_u32();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u32_clamp implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_u32_clamp INSTANCE = new Convert_f64_to_u32_clamp();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u32_clamp_min implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_u32_clamp_min INSTANCE = new Convert_f64_to_u32_clamp_min();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_u32_clamp_max implements ConvertLoop< double[], int[] >
	{
		static final Convert_f64_to_u32_clamp_max INSTANCE = new Convert_f64_to_u32_clamp_max();

		@Override
		public void apply( final double[] src, final int[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_u32_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i64 implements ConvertLoop< double[], long[] >
	{
		static final Convert_f64_to_i64 INSTANCE = new Convert_f64_to_i64();

		@Override
		public void apply( final double[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i64_clamp implements ConvertLoop< double[], long[] >
	{
		static final Convert_f64_to_i64_clamp INSTANCE = new Convert_f64_to_i64_clamp();

		@Override
		public void apply( final double[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i64_clamp_min implements ConvertLoop< double[], long[] >
	{
		static final Convert_f64_to_i64_clamp_min INSTANCE = new Convert_f64_to_i64_clamp_min();

		@Override
		public void apply( final double[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_i64_clamp_max implements ConvertLoop< double[], long[] >
	{
		static final Convert_f64_to_i64_clamp_max INSTANCE = new Convert_f64_to_i64_clamp_max();

		@Override
		public void apply( final double[] src, final long[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_i64_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f32 implements ConvertLoop< double[], float[] >
	{
		static final Convert_f64_to_f32 INSTANCE = new Convert_f64_to_f32();

		@Override
		public void apply( final double[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f32_clamp implements ConvertLoop< double[], float[] >
	{
		static final Convert_f64_to_f32_clamp INSTANCE = new Convert_f64_to_f32_clamp();

		@Override
		public void apply( final double[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f32_clamp_min implements ConvertLoop< double[], float[] >
	{
		static final Convert_f64_to_f32_clamp_min INSTANCE = new Convert_f64_to_f32_clamp_min();

		@Override
		public void apply( final double[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f32_clamp_max implements ConvertLoop< double[], float[] >
	{
		static final Convert_f64_to_f32_clamp_max INSTANCE = new Convert_f64_to_f32_clamp_max();

		@Override
		public void apply( final double[] src, final float[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f32_clamp_max( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f64 implements ConvertLoop< double[], double[] >
	{
		static final Convert_f64_to_f64 INSTANCE = new Convert_f64_to_f64();

		@Override
		public void apply( final double[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f64_clamp implements ConvertLoop< double[], double[] >
	{
		static final Convert_f64_to_f64_clamp INSTANCE = new Convert_f64_to_f64_clamp();

		@Override
		public void apply( final double[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f64_clamp_min implements ConvertLoop< double[], double[] >
	{
		static final Convert_f64_to_f64_clamp_min INSTANCE = new Convert_f64_to_f64_clamp_min();

		@Override
		public void apply( final double[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_min( from_f64( src[ i ] ) );
		}
	}

	static class Convert_f64_to_f64_clamp_max implements ConvertLoop< double[], double[] >
	{
		static final Convert_f64_to_f64_clamp_max INSTANCE = new Convert_f64_to_f64_clamp_max();

		@Override
		public void apply( final double[] src, final double[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = to_f64_clamp_max( from_f64( src[ i ] ) );
		}
	}
}
