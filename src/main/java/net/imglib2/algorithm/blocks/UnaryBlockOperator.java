package net.imglib2.algorithm.blocks;

import net.imglib2.algorithm.blocks.convert.ClampType;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

import static net.imglib2.algorithm.blocks.convert.Convert.convert;

/**
 * Wraps {@code BlockProcessor<I,O>}, where {@code I} is the primitive array
 * type backing ImgLib2 {@code NativeType} {@code S} and {@code O} is the
 * primitive array type backing ImgLib2 {@code NativeType} {@code T}.
 * <p>
 * Typically, {@code UnaryBlockOperator} should be used rather than {@link
 * BlockProcessor} directly, to avoid mistakes with unchecked (primitive array)
 * type casts.
 *
 * @param <S>
 * 		source type
 * @param <T>
 * 		target type
 */
public interface UnaryBlockOperator< S extends NativeType< S >, T extends NativeType< T > >
{
	/**
	 * Get (an instance of) the wrapped {@code BlockProcessor}.
	 * <p>
	 * Note that this involves an unchecked cast, that is, the returned {@code
	 * BlockProcessor<I,O>} will be cast to the {@code I, O} types expected by
	 * the caller.
	 * <p>
	 * This is mostly intented for internal use, e.g., in {@link BlockAlgoUtils}.
	 *
	 * @param <I>
	 * 		input primitive array type, e.g., float[]. Must correspond to S.
	 * @param <O>
	 * 		output primitive array type, e.g., float[]. Must correspond to T.
	 *
	 * @return an instance of the wrapped {@code BlockProcessor}
	 */
	< I, O > BlockProcessor< I, O > blockProcessor();

	S getSourceType();

	T getTargetType();

	/**
	 * Get a thread-safe version of this {@code BlockProcessor}.
	 * (Implemented as a wrapper that makes {@link ThreadLocal} copies).
	 */
	UnaryBlockOperator< S, T > threadSafe();

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends NativeType< U > > UnaryBlockOperator< S, U > andThen( UnaryBlockOperator< T, U > op )
	{
		return new DefaultUnaryBlockOperator<>(
				getSourceType(),
				op.getTargetType(),
				blockProcessor().andThen( op.blockProcessor() ) );
	}

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to converting the
	 * input values to type {@code U} (possibly {@code clamp}ing to the range of
	 * {@code U}). and then applying {@code this} to the result.
	 */
	default < U extends NativeType< U > > UnaryBlockOperator< U, T > adaptSourceType( U newSourceType, ClampType clamp )
	{
		if ( newSourceType.getClass().isInstance( getSourceType() ) )
			return Cast.unchecked( this );
		else
			return convert( newSourceType, getSourceType(), clamp ).andThen( this );
	}

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, an then converting the output values to type {@code U}
	 * (possibly {@code clamp}ing to the range of {@code U}).
	 */
	default  < U extends NativeType< U > > UnaryBlockOperator< S, U > adaptTargetType( U newTargetType, ClampType clamp )
	{
		if ( newTargetType.getClass().isInstance( getTargetType() ) )
			return Cast.unchecked( this );
		else
			return this.andThen( convert( getTargetType(), newTargetType, clamp ) );
	}
}
