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

import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.Typed;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;

import java.util.function.Function;

import static net.imglib2.blocks.PrimitiveBlocks.OnFallback.WARN;

/**
 * Provides blocks of data from a {@code NativeType<T>} source.
 * Use the {@link BlockSupplier#copy} method to copy a block out of the
 * source into flat primitive array (of the appropriate type).
 * <p>
 * Use the static method {@link BlockSupplier#of(RandomAccessible)
 * BlockSupplier.of} to create a {@code BlockSupplier} accessor from an {@code
 * RandomAccessible} source. (This is just a thin wrapper around {@link
 * PrimitiveBlocks}).
 * <p>
 * Currently, only pixel types {@code T} are supported that map one-to-one to a
 * primitive type. (For example, {@code ComplexDoubleType} or {@code
 * Unsigned4BitType} are not supported.)
 * <p>
 * If a source {@code RandomAccessible} view construction cannot be understood,
 * {@link BlockSupplier#of(RandomAccessible) BlockSupplier.of} will return a
 * fall-back implementation. Fallback can be configured with the optional {@link
 * PrimitiveBlocks.OnFallback OnFallback} argument to {@link
 * BlockSupplier#of(RandomAccessible, PrimitiveBlocks.OnFallback)
 * BlockSupplier.of}.
 * <p>
 * Use {@link BlockSupplier#andThen} to decorate a {@code BlockSupplier} with a
 * sequence of {@code UnaryBlockOperator}s.
 * <p>
 * Use {@link BlockSupplier#toCellImg} to create {@code CachedCellImg} which
 * copies cells from a {@code BlockSupplier}.
 * <p>
 * Implementations are not thread-safe in general. Use {@link #threadSafe()} to
 * obtain a thread-safe instance (implemented using {@link ThreadLocal} copies).
 * E.g.,
 * <pre>{@code
 * 		BlockSupplier<FloatType> blocks = BlockSupplier.of(view).threadSafe();
 * }</pre>
 *
 * @param <T>
 * 		pixel type
 */
public interface BlockSupplier< T extends NativeType< T > > extends Typed< T >, EuclideanSpace
{
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
	void copy( Interval interval, Object dest );

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
	 * Returns a {@code BlockSupplier} that provide blocks using the given
	 * {@code operator} with this {@code BlockSupplier} as input.
	 */
	default < U extends NativeType< U > > BlockSupplier< U > andThen( UnaryBlockOperator< T, U > operator )
	{
		return operator.applyTo( this );
	}

	default < U extends NativeType< U > > BlockSupplier< U > andThen( Function< BlockSupplier< T >, UnaryBlockOperator< T, U > > function )
	{
		return andThen( function.apply( this ) );
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

	/**
	 * Return a {@code CachedCellImg} which copies cells from this {@code BlockSupplier}.
	 *
	 * @param dimensions
	 * 		dimensions of the {@code CachedCellImg} to create
	 * @param cellDimensions
	 * 		block size of the {@code CachedCellImg} to create.
	 * 		This is extended or truncated as necessary.
	 * 		For example if {@code cellDimensions={64,32}} then for creating a 3D
	 * 		image it will be augmented to {@code {64,32,32}}. For creating a 1D image
	 * 		it will be truncated to {@code {64}}.
	 *
	 * @return a {@code CachedCellImg} which copies cells from this {@code BlockSupplier}.
	 */
	default CachedCellImg< T, ? > toCellImg( final long[] dimensions, final int... cellDimensions )
	{
		return BlockAlgoUtils.cellImg( this, dimensions, cellDimensions );
	}

	/**
	 * Returns a new {@code BlockSupplier} that handles {@link #copy} requests
	 * by splitting into {@code tileSize} portions that are each handled by this
	 * {@code BlockSupplier} and assembled into the final result.
	 * <p>
	 * Example use cases:
	 * <ul>
	 * <li>Compute large outputs (e.g. for writing to N5 or wrapping as {@code
	 *     ArrayImg}) with operators that have better performance with small
	 *     block sizes.</li>
	 * <li>Avoid excessively large blocks when chaining downsampling
	 *     operators.</li>
	 * </ul>
	 *
	 * @param tileSize
	 * 		(maximum) dimensions of a request to the {@code srcSupplier}.
	 *      {@code tileSize} is expanded or truncated to the necessary size. For
	 * 		example, if {@code tileSize=={64}} and this {@code BlockSupplier} is 3D,
	 * 		then {@code tileSize} is expanded to {@code {64, 64, 64}}.
	 */
	default BlockSupplier< T > tile( int... tileSize )
	{
		return this.andThen( new TilingUnaryBlockOperator<>( getType(), numDimensions(), tileSize ) );
	}
}
