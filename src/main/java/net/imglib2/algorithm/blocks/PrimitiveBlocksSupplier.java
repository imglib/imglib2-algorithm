package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;

import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.type.NativeType;
import net.imglib2.util.CloseableThreadLocal;

class PrimitiveBlocksSupplier< T extends NativeType< T > > implements BlockSupplier< T >
{
	private final PrimitiveBlocks< T > blocks;

	private Supplier< ? extends BlockSupplier< T > > threadSafeSupplier;

	PrimitiveBlocksSupplier( final PrimitiveBlocks< T > blocks )
	{
		this.blocks = blocks;
	}

	private PrimitiveBlocksSupplier( final PrimitiveBlocksSupplier< T > s )
	{
		this.blocks = s.blocks;
		threadSafeSupplier = s.threadSafeSupplier;
	}

	@Override
	public T getType()
	{
		return blocks.getType();
	}

	@Override
	public void copy( final long[] srcPos, final Object dest, final int[] size )
	{
		blocks.copy( srcPos, dest, size );
	}

	@Override
	public void copy( final int[] srcPos, final Object dest, final int[] size )
	{
		blocks.copy( srcPos, dest, size );
	}

	@Override
	public Supplier< ? extends BlockSupplier< T > > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( () -> new PrimitiveBlocksSupplier<>( this ) )::get;
		return threadSafeSupplier;
	}

	@Override
	public BlockSupplier< T > threadSafe()
	{
		final Supplier< ? extends BlockSupplier< T > > supplier = threadSafeSupplier();
		return new BlockSupplier< T >()
		{
			@Override
			public T getType()
			{
				return blocks.getType();
			}

			@Override
			public void copy( final long[] srcPos, final Object dest, final int[] size )
			{
				supplier.get().copy( srcPos, dest, size );
			}

			public Supplier< ? extends BlockSupplier< T > > threadSafeSupplier()
			{
				return supplier;
			}

			@Override
			public BlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
