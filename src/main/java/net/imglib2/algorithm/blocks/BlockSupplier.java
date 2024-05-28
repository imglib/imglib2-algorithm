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

import static net.imglib2.blocks.PrimitiveBlocks.OnFallback.WARN;
import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.Typed;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.type.NativeType;
import net.imglib2.util.Util;

public interface BlockSupplier< T extends NativeType< T > > extends Typed< T >
{
	/**
	 * Copy a block from the ({@code T}-typed) source into primitive arrays (of
	 * the appropriate type).
	 *
	 * @param srcPos
	 * 		min coordinate of the block to copy
	 * @param dest
	 * 		primitive array to copy into. Must correspond to {@code T}, for
	 * 		example, if {@code T} is {@code UnsignedByteType} then {@code dest} must
	 * 		be {@code byte[]}.
	 * @param size
	 * 		the size of the block to copy
	 */
	void copy( long[] srcPos, Object dest, int[] size );

	/**
	 * Copy a block from the ({@code T}-typed) source into primitive arrays (of
	 * the appropriate type).
	 *
	 * @param srcPos
	 * 		min coordinate of the block to copy
	 * @param dest
	 * 		primitive array to copy into. Must correspond to {@code T}, for
	 * 		example, if {@code T} is {@code UnsignedByteType} then {@code dest} must
	 * 		be {@code byte[]}.
	 * @param size
	 * 		the size of the block to copy
	 */
	default void copy( int[] srcPos, Object dest, int[] size )
	{
		copy( Util.int2long( srcPos ), dest, size );
	}

	/**
	 * Copy a block from the ({@code T}-typed) source into primitive arrays (of
	 * the appropriate type).
	 *
	 * @param interval
	 * 		the block to copy
	 * @param dest
	 * 		primitive array to copy into. Must correspond to {@code T}, for
	 * 		example, if {@code T} is {@code UnsignedByteType} then {@code dest} must
	 * 		be {@code byte[]}.
	 */
	default void copy( Interval interval, Object dest )
	{
		final long[] srcPos = interval.minAsLongArray();
		final int[] size = new int[ srcPos.length ];
		Arrays.setAll( size, d -> safeInt( interval.dimension( d ) ) );
		copy( srcPos, dest, size );
	}

	/**
	 * Get a thread-safe version of this {@code BlockSupplier}.
	 */
	BlockSupplier< T > threadSafe();

	/**
	 * Returns an instance of this {@link BlockSupplier} that can be used
	 * independently, e.g., in another thread or in another place in the same
	 * pipeline.
	 */
	BlockSupplier< T > independentCopy();

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends NativeType< U > > BlockSupplier< U > andThen( UnaryBlockOperator< T, U > operator )
	{
		return new ConcatenatedBlockSupplier<>( this.independentCopy(), operator.independentCopy() );
	}

	/**
	 * Create a {@code BlockSupplier} accessor for an arbitrary {@code
	 * RandomAccessible} source. Many View constructions (that ultimately end in
	 * {@code CellImg}, {@code ArrayImg}, etc.) are understood and will be
	 * handled by an optimized copier.
	 * <p>
	 * If the source {@code RandomAccessible} cannot be understood, a warning is
	 * printed, and a fall-back implementation (based on {@code LoopBuilder}) is
	 * returned.
	 * <p>
	 * The returned {@code BlockSupplier} is not thread-safe in general. Use
	 * {@link #threadSafe()} to obtain a thread-safe instance, e.g., {@code
	 * BlockSupplier.of(view).threadSafe()}.
	 *
	 * @param ra the source
	 * @return a {@code BlockSupplier} accessor for {@code ra}.
	 * @param <T> pixel type
	 */
	static < T extends NativeType< T > > BlockSupplier< T > of(
			RandomAccessible< T > ra )
	{
		return of( ra, WARN );
	}

	/**
	 * Create a {@code BlockSupplier} accessor for an arbitrary {@code
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
	 * The returned {@code BlockSupplier} is not thread-safe in general. Use
	 * {@link #threadSafe()} to obtain a thread-safe instance, e.g., {@code
	 * BlockSupplier.of(view).threadSafe()}.
	 *
	 * @param ra the source
	 * @return a {@code BlockSupplier} accessor for {@code ra}.
	 * @param <T> pixel type
	 */
	static < T extends NativeType< T > > BlockSupplier< T > of(
			RandomAccessible< T > ra,
			PrimitiveBlocks.OnFallback onFallback )
	{
		return new PrimitiveBlocksSupplier<>( PrimitiveBlocks.of( ra, onFallback ) );
	}

	/*
	 * Wrap the given {@code PrimitiveBlocks} as a {@code BlockSupplier}.
	 */
//	static < T extends NativeType< T > > BlockSupplier< T > of( PrimitiveBlocks< T > blocks )
//	{
//		return new PrimitiveBlocksSupplier<>( blocks );
//	}
}
