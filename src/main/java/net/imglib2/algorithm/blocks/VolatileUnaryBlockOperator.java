/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import static net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid.UNKNOWN;

import net.imglib2.Interval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import net.imglib2.type.NativeType;

/**
 * A {@code VolatileUnaryBlockOperator} computes blocks of {@code Volatile}
 * {@code NativeType<T>} data from blocks of {@code Volatile} {@code
 * NativeType<S>} data, both stored in {@link VolatileArray}s.
 * <p>
 * This is the {@code Volatile} counterpart of {@link UnaryBlockOperator}.
 * Typically, {@code VolatileUnaryBlockOperator}s are chained to a particular
 * {@code VolatileBlockSupplier<S>} using {@link VolatileBlockSupplier#andThen}
 * (or {@link #applyTo}), instead of calling {@code compute()} explicitly.
 * <p>
 * Implementations are not thread-safe in general. Use {@link
 * #independentCopy()} to obtain independent instances.
 *
 * @param <S>
 * 		source pixel type
 * @param <T>
 * 		target pixel type
 */
public interface VolatileUnaryBlockOperator< S extends Volatile< ? > & NativeType< S >, T extends Volatile< ? > & NativeType< T > >
{
	/**
	 * Compute a block (using {@code src} to provide input data) and store into
	 * the given {@code VolatileArray}.
	 *
	 * @param src
	 * 		the {@code VolatileBlockSupplier} that provides input data
	 * @param interval
	 * 		the output interval to compute
	 * @param dest
	 * 		{@code VolatileArray} to store computation result (data and
	 * 		validity).
	 */
	void compute( VolatileBlockSupplier< S > src, Interval interval, VolatileArray< ? > dest );

	/**
	 * TODO: revise javadoc (CLAUDE generated).
	 * <p>
	 * Whether {@link #compute computing} the given block would read only valid
	 * source data, without copying or computing anything.
	 * <p>
	 * To answers, the operator has to know which source interval {@code
	 * interval} is computed from. An operator that only finds that out while
	 * computing, because the mapping depends on the data, cannot answer: the
	 * default implementation returns {@link AllValid#UNKNOWN}.
	 *
	 * @param src
	 * 		the {@code VolatileBlockSupplier} that provides input data
	 * @param interval
	 * 		the output interval to ask about
	 * @param failFast
	 * 		if {@code true}, return as soon as the answer is known to be {@code
	 * 		FALSE}. If {@code false}, the whole source block is inspected, which
	 * 		for a cached source has the side effect of requesting all the data
	 * 		that computing the block would read.
	 */
	default AllValid allValid( VolatileBlockSupplier< S > src, Interval interval, boolean failFast )
	{
		return UNKNOWN;
	}

	S getSourceType();

	T getTargetType();

	/**
	 * Number of source dimensions.
	 * See {@link UnaryBlockOperator#numSourceDimensions()}.
	 *
	 * @return the number of source dimensions
	 */
	int numSourceDimensions();

	/**
	 * Number of target dimensions.
	 * See {@link UnaryBlockOperator#numTargetDimensions()}.
	 *
	 * @return the number of target dimensions
	 */
	int numTargetDimensions();

	/**
	 * Returns an instance of this {@link VolatileUnaryBlockOperator} that can
	 * be used independently, e.g., in another thread or in another place in
	 * the same pipeline.
	 */
	VolatileUnaryBlockOperator< S, T > independentCopy();

	/**
	 * Returns a {@code VolatileBlockSupplier<T>} that provide blocks by {@link
	 * #compute computing} on input blocks from the given {@code
	 * VolatileBlockSupplier<S>}.
	 */
	default VolatileBlockSupplier< T > applyTo( VolatileBlockSupplier< S > blocks )
	{
		return new VolatileConcatenatedBlockSupplier<>( blocks, independentCopy() );
	}

	/**
	 * Returns a {@code VolatileUnaryBlockOperator} that is equivalent to
	 * applying {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends Volatile< ? > & NativeType< U > > VolatileUnaryBlockOperator< S, U > andThen( VolatileUnaryBlockOperator< T, U > op )
	{
		return new VolatileConcatenatedUnaryBlockOperator<>( this, op.independentCopy() );
	}

	/**
	 * Returns a {@code VolatileUnaryBlockOperator} that is equivalent to
	 * converting the input values to type {@code U} (possibly {@code
	 * clamp}ing to the range of {@code U}). and then applying {@code this} to
	 * the result.
	 */
	default < U extends Volatile< ? > & NativeType< U > > VolatileUnaryBlockOperator< U, T > adaptSourceType( U newSourceType, ClampType clamp )
	{
		return Convert.createVolatileOperator( newSourceType, getSourceType(), clamp ).andThen( this );
	}

	/**
	 * Returns a {@code VolatileUnaryBlockOperator} that is equivalent to
	 * applying {@code this}, an then converting the output values to type
	 * {@code U} (possibly {@code clamp}ing to the range of {@code U}).
	 */
	default < U extends Volatile< ? > & NativeType< U > > VolatileUnaryBlockOperator< S, U > adaptTargetType( U newTargetType, ClampType clamp )
	{
		return this.andThen( Convert.createVolatileOperator( getTargetType(), newTargetType, clamp ) );
	}
}
