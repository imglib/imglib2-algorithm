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
package net.imglib2.algorithm.blocks;

import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

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
	 * Number of source dimensions.
	 * <p>
	 * Some operators can be applied to arbitrary dimensions, e.g., converters.
	 * In this case, they should return {@code numSourceDimensions() <= 0}. It
	 * is expected that these operators do not modify the number of dimensions,
	 * that is, when such an operator is applied to a 3D block, the result is
	 * also a 3D block.
	 *
	 * @return the number of source dimensions
	 */
	int numSourceDimensions();

	/**
	 * Number of target dimensions.
	 * <p>
	 * Some operators can be applied to arbitrary dimensions, e.g., converters.
	 * In this case, they should return {@code numTargetDimensions() <= 0}. It
	 * is expected that these operators do not modify the number of dimensions,
	 * that is, when such an operator is applied to a 3D block, the result is
	 * also a 3D block.
	 *
	 * @return the number of target dimensions
	 */
	int numTargetDimensions();

	/**
	 * Get a thread-safe version of this {@code UnaryBlockOperator}.
	 * (Implemented as a wrapper that makes {@link ThreadLocal} copies).
	 */
	UnaryBlockOperator< S, T > threadSafe();

	/**
	 * Returns an instance of this {@link UnaryBlockOperator} that can be used
	 * independently, e.g., in another thread or in another place in the same
	 * pipeline.
	 */
	UnaryBlockOperator< S, T > independentCopy();

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends NativeType< U > > UnaryBlockOperator< S, U > andThen( UnaryBlockOperator< T, U > op )
	{
		if ( op instanceof NoOpUnaryBlockOperator )
			return Cast.unchecked( this );

		final boolean thisHasDimensions = numSourceDimensions() > 0;
		final boolean opHasDimensions = op.numSourceDimensions() > 0;
		if ( opHasDimensions && thisHasDimensions && numTargetDimensions() != op.numSourceDimensions() ) {
			throw new IllegalArgumentException( "UnaryBlockOperator cannot be concatenated: number of dimensions mismatch." );
		}
		final int numSourceDimensions = thisHasDimensions ? numSourceDimensions() : op.numSourceDimensions();
		final int numTargetDimensions = opHasDimensions ? op.numTargetDimensions() : numTargetDimensions();
		return new DefaultUnaryBlockOperator<>(
				getSourceType(),
				op.getTargetType(),
				numSourceDimensions,
				numTargetDimensions,
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
			return Convert.createOperator( newSourceType, getSourceType(), clamp ).andThen( this );
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
			return this.andThen( Convert.createOperator( getTargetType(), newTargetType, clamp ) );
	}
}
