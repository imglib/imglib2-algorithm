package net.imglib2.algorithm.blocks.util;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Enumerates operand types corresponding to the standard {@code RealType}s.
 */
public enum OperandType
{
	I8,
	U8,
	I16,
	U16,
	I32,
	U32,
	I64,
	U64,
	F32,
	F64;

	public static OperandType of( NativeType< ? > type )
	{
		if ( type instanceof ByteType )
			return I8;
		else if ( type instanceof UnsignedByteType )
			return U8;
		else if ( type instanceof ShortType )
			return I16;
		else if ( type instanceof UnsignedShortType )
			return U16;
		else if ( type instanceof IntType )
			return I32;
		else if ( type instanceof UnsignedIntType )
			return U32;
		else if ( type instanceof LongType )
			return I64;
		else if ( type instanceof UnsignedLongType )
			return U64;
		else if ( type instanceof FloatType )
			return F32;
		else if ( type instanceof DoubleType )
			return F64;
		else
			throw new IllegalArgumentException( "Unsupported Type: " + type.getClass().getSimpleName() );
	}
}
