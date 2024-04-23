package net.imglib2.algorithm.blocks;

import static net.imglib2.blocks.PrimitiveBlocks.OnFallback.WARN;

import java.util.Arrays;
import java.util.function.Supplier;

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
		Arrays.setAll( size, d -> ( int ) interval.dimension( d ) );
		copy( srcPos, dest, size );
	}

	/**
	 * Get a thread-safe version of this {@code BlockSupplier}.
	 */
	BlockSupplier< T > threadSafe();

	Supplier< ? extends BlockSupplier< T > > threadSafeSupplier();

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends NativeType< U > > BlockSupplier< U > andThen( UnaryBlockOperator< T, U > operator )
	{
		return new ConcatenatedBlockSupplier< U >( this, operator );
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
	static < T extends NativeType< T >, R extends NativeType< R > > BlockSupplier< T > of(
			RandomAccessible< T > ra,
			PrimitiveBlocks.OnFallback onFallback )
	{
		return new PrimitiveBlocksSupplier<>( PrimitiveBlocks.of( ra, onFallback ) );
	}
}
