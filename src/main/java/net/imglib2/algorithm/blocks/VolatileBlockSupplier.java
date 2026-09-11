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

import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.Typed;
import net.imglib2.Volatile;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.blocks.PrimitiveBlocks.OnFallback;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks;
import net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import net.imglib2.type.NativeType;

import java.util.function.Function;

import static net.imglib2.blocks.PrimitiveBlocks.OnFallback.WARN;

/**
 * Provides blocks of data from a {@code Volatile & NativeType<T>} source.
 * Use the {@link VolatileBlockSupplier#copy} method to copy a block out of the
 * source into a {@link VolatileArray} (flat primitive array and corresponding
 * per-pixel validity) of the appropriate type.
 * <p>
 * Use the static method {@link VolatileBlockSupplier#of(RandomAccessible)
 * VolatileBlockSupplier.of} to create a {@code VolatileBlockSupplier} accessor
 * from a {@code RandomAccessible} source. (This is just a thin wrapper around
 * {@link VolatilePrimitiveBlocks}).
 * <p>
 * Currently, only pixel types {@code T} are supported that map one-to-one to a
 * primitive type. (For example, {@code ComplexDoubleType} or {@code
 * Unsigned4BitType} are not supported.)
 * <p>
 * If a source {@code RandomAccessible} view construction cannot be understood,
 * {@link VolatileBlockSupplier#of(RandomAccessible) BlockSupplier.of} will
 * return a fall-back implementation. Fallback can be configured with the
 * optional {@link PrimitiveBlocks.OnFallback OnFallback} argument to {@link
 * VolatileBlockSupplier#of(RandomAccessible, PrimitiveBlocks.OnFallback)
 * VolatileBlockSupplier.of}.
 * <p>
 * Use {@link VolatileBlockSupplier#andThen} to decorate a {@code VolatileBlockSupplier}
 * with a sequence of {@code VolatileUnaryBlockOperator}s.
 * <p>
 * NB: This is the {@code Volatile} counterpart of {@link BlockSupplier}. The two
 * are unrelated types, so a {@code VolatileBlockSupplier} can only be chained
 * with {@code VolatileUnaryBlockOperator}s, and a {@code BlockSupplier} only
 * with {@code UnaryBlockOperator}s.
 * <p>
 * Implementations are not thread-safe in general. Use {@link #threadSafe()} to
 * obtain a thread-safe instance (implemented using {@link ThreadLocal} copies).
 * E.g.,
 * <pre>{@code
 * 		VolatileBlockSupplier<VolatileFloatType> blocks = VolatileBlockSupplier.of(view).threadSafe();
 * }</pre>
 *
 * @param <T>
 * 		(volatile) pixel type
 */
public interface VolatileBlockSupplier< T extends Volatile< ? > & NativeType< T > > extends Typed< T >, EuclideanSpace
{
	/**
	 * Copy a block from the ({@code T}-typed) source into a {@code VolatileArray}
	 * (of the appropriate type). This copies both, pixel values and per-pixel
	 * validity.
	 *
	 * @param interval
	 * 		the block to copy
	 * @param dest
	 * 		{@code VolatileArray<?>} to copy into. Must correspond to {@code T},
	 * 		for example, if {@code T} is {@code VolatileUnsignedByteType} then
	 * 		it must be {@code VolatileArray<byte[]>}.
	 */
	void copy( Interval interval, VolatileArray< ? > dest );

	/**
	 * TODO: revise javadoc (CLAUDE generated).
	 * <p>
	 * Whether every pixel of the given block is valid, without copying or
	 * computing any data.
	 * <p>
	 * A {@code VolatileBlockSupplier} built with {@link #andThen} answers for
	 * the source data that computing the block reads, which requires the
	 * operators in the chain to map an output interval to the input interval
	 * it is computed from. An operator that cannot do that, because the mapping
	 * depends on the data, makes the whole chain answer {@code UNKNOWN}. That
	 * is <em>not</em> a statement about the data: A caller that uses {@code
	 * AllValid} to decide whether to attempt work should go ahead on {@code
	 * UNKNOWN}, as it would on {@code TRUE}. Only {@code FALSE} says that the
	 * data is not all there.
	 *
	 * @param interval
	 * 		the block to ask about
	 * @param failFast
	 * 		if {@code true}, return as soon as the answer is known to be {@code
	 * 		FALSE}. If {@code false}, the whole block is inspected, which for a
	 * 		cached source has the side effect of requesting all the data that
	 * 		the block reads from.
	 */
	AllValid allValid( Interval interval, boolean failFast );

	/**
	 * Get a thread-safe version of this {@code VolatileBlockSupplier}.
	 */
	VolatileBlockSupplier< T > threadSafe();

	/**
	 * Returns an instance of this {@link VolatileBlockSupplier} that can be
	 * used independently, e.g., in another thread or in another place in the
	 * same pipeline.
	 */
	VolatileBlockSupplier< T > independentCopy();

	/**
	 * Returns a {@code VolatileBlockSupplier} that provide blocks using the
	 * given {@code operator} with this {@code VolatileBlockSupplier} as input.
	 */
	default < U extends Volatile< ? > & NativeType< U > > VolatileBlockSupplier< U > andThen( VolatileUnaryBlockOperator< T, U > operator )
	{
		return operator.applyTo( this );
	}

	default < U extends Volatile< ? > & NativeType< U > > VolatileBlockSupplier< U > andThen( Function< VolatileBlockSupplier< T >, VolatileUnaryBlockOperator< T, U > > function )
	{
		return andThen( function.apply( this ) );
	}

	/**
	 * Create a {@code VolatileBlockSupplier} accessor for an arbitrary {@code
	 * RandomAccessible} source. Many View constructions (that ultimately end in
	 * {@code CellImg}, {@code ArrayImg}, etc.) are understood and will be
	 * handled by an optimized copier.
	 * <p>
	 * If the source {@code RandomAccessible} cannot be understood, a warning is
	 * printed, and a fall-back implementation (based on {@code LoopBuilder}) is
	 * returned.
	 * <p>
	 * The returned {@code VolatileBlockSupplier} is not thread-safe in general.
	 * Use {@link #threadSafe()} to obtain a thread-safe instance, e.g., {@code
	 * VolatileBlockSupplier.of(view).threadSafe()}.
	 *
	 * @param ra the source
	 * @return a {@code VolatileBlockSupplier} accessor for {@code ra}.
	 * @param <T> pixel type
	 */
	static < T extends Volatile< ? > & NativeType< T > > VolatileBlockSupplier< T > of(
			RandomAccessible< T > ra )
	{
		return of( ra, WARN );
	}

	/**
	 * Create a {@code VolatileBlockSupplier} accessor for an arbitrary {@code
	 * RandomAccessible} source. Many View constructions (that ultimately end in
	 * {@code CellImg}, {@code ArrayImg}, etc.) are understood and will be
	 * handled by an optimized copier.
	 * <p>
	 * If the source {@code RandomAccessible} cannot be understood, a fall-back
	 * implementation (based on {@code LoopBuilder}) has to be used. The {@code
	 * onFallback} argument specifies how to handle this case:
	 * <ul>
	 *     <li>{@link PrimitiveBlocks.OnFallback#ACCEPT ACCEPT}: silently accept fall-back</li>
	 *     <li>{@link PrimitiveBlocks.OnFallback#WARN WARN}: accept fall-back, but print a warning explaining why the input {@code ra} requires fall-back</li>
	 *     <li>{@link PrimitiveBlocks.OnFallback#FAIL FAIL}: throw {@code IllegalArgumentException} explaining why the input {@code ra} requires fall-back</li>
	 * </ul>
	 * The returned {@code VolatileBlockSupplier} is not thread-safe in general.
	 * Use {@link #threadSafe()} to obtain a thread-safe instance, e.g., {@code
	 * VolatileBlockSupplier.of(view).threadSafe()}.
	 *
	 * @param ra the source
	 * @return a {@code VolatileBlockSupplier} accessor for {@code ra}.
	 * @param <T> pixel type
	 */
	static < T extends Volatile< ? > & NativeType< T > > VolatileBlockSupplier< T > of(
			RandomAccessible< T > ra,
			OnFallback onFallback )
	{
		return new VolatilePrimitiveBlocksSupplier<>( VolatilePrimitiveBlocks.of( ra, onFallback ) );
	}
}
