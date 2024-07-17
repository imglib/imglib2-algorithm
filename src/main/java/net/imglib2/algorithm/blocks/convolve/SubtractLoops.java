package net.imglib2.algorithm.blocks.convolve;

// TODO auto-generate

import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_f32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.from_u8;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_f32;
import static net.imglib2.algorithm.blocks.convolve.ConvertScalars.to_u8;

import net.imglib2.algorithm.blocks.util.OperandType;
import net.imglib2.util.Cast;

public class SubtractLoops
{
	// TODO add clamping
	// TODO add min-only-clamping to
	static < I > SubtractLoop< I > get( OperandType type )
	{
		switch ( type) {
		case I8: break;
		case U8: return Cast.unchecked( Subtract_u8.INSTANCE );
		case I16: break;
		case U16: break;
		case I32: break;
		case U32: break;
		case I64: break;
		case U64: break;
		case F32: return Cast.unchecked( Subtract_f32.INSTANCE );
		case F64: break;
		}
		throw new UnsupportedOperationException( "not implemented" );
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
}
