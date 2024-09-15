package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;

import net.imglib2.type.NativeType;
import net.imglib2.util.CloseableThreadLocal;

/**
 * Boilerplate for {@link BlockSupplier} to simplify implementations.
 * <p>
 * Implements {@link BlockSupplier#threadSafe()} as a wrapper that makes {@link
 * ThreadLocal} {@link BlockSupplier#independentCopy()} copies.
 */
public abstract class AbstractBlockSupplier< T extends NativeType< T > > implements BlockSupplier< T >
{
	private Supplier< BlockSupplier< T > > threadSafeSupplier;

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
				return AbstractBlockSupplier.this.getType();
			}

			@Override
			public int numDimensions()
			{
				return AbstractBlockSupplier.this.numDimensions();
			}

			@Override
			public void copy( final long[] srcPos, final Object dest, final int[] size )
			{
				threadSafeSupplier.get().copy( srcPos, dest, size );
			}

			@Override
			public BlockSupplier< T > independentCopy()
			{
				return AbstractBlockSupplier.this.independentCopy().threadSafe();
			}

			@Override
			public BlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
