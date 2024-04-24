package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;

import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.type.NativeType;
import net.imglib2.util.CloseableThreadLocal;

class PrimitiveBlocksSupplier< T extends NativeType< T > > implements BlockSupplier< T >
{
	private final PrimitiveBlocks< T > blocks;

	private Supplier< BlockSupplier< T > > threadSafeSupplier;

	PrimitiveBlocksSupplier( final PrimitiveBlocks< T > blocks )
	{
		this.blocks = blocks;
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
	public BlockSupplier< T > independentCopy()
	{
		final PrimitiveBlocks< T > blocksCopy = blocks.independentCopy();
		return blocksCopy == blocks ? this : new PrimitiveBlocksSupplier<>( blocksCopy );
	}

	@Override
	public BlockSupplier< T > threadSafe()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::independentCopy )::get;
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
				threadSafeSupplier.get().copy( srcPos, dest, size );
			}

			@Override
			public BlockSupplier< T > independentCopy()
			{
				return PrimitiveBlocksSupplier.this.independentCopy().threadSafe();
			}

			@Override
			public BlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
