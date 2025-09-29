package net.imglib2.algorithm.blocks;

import net.imglib2.RandomAccessible;
import net.imglib2.Volatile;
import net.imglib2.blocks.VolatilePrimitiveBlocks;
import net.imglib2.type.NativeType;

public interface VolatileBlockSupplier
{
	/**
	 * Create a {@code BlockSupplier} accessor for an arbitrary {@code
	 * RandomAccessible} source. Many View constructions (that ultimately end in
	 * {@code CellImg}, {@code ArrayImg}, etc.) are understood and will be
	 * handled by an optimized copier.
	 * <p>
	 * If a source {@code RandomAccessible} cannot be understood, an {@code
	 * IllegalArgumentException} is thrown, explaining why the {@code
	 * RandomAccessible} is not suitable.
	 * <p>
	 * The returned {@code BlockSupplier} is not thread-safe in general. Use
	 * {@link BlockSupplier#threadSafe()} to obtain a thread-safe instance, e.g., {@code
	 * BlockSupplier.of(view).threadSafe()}.
	 *
	 * @param ra the source
	 * @return a {@code BlockSupplier} accessor for {@code ra}.
	 * @param <T> pixel type
	 */
	static < T extends Volatile< ? > & NativeType< T > > BlockSupplier< T > of(
			RandomAccessible< T > ra )
	{
		return new PrimitiveBlocksSupplier<>( VolatilePrimitiveBlocks.of( ra ) );
	}

}
