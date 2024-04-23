package net.imglib2.algorithm.blocks;

import java.util.Arrays;
import java.util.function.Supplier;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;
import net.imglib2.util.CloseableThreadLocal;

class ConcatenatedBlockSupplier< T extends NativeType< T > > implements BlockSupplier< T >
{
	private final BlockSupplier< ? > p0;

	private final BlockProcessor< ?, ? > p1;

	private final T type;

	public < S extends NativeType< S > > ConcatenatedBlockSupplier(
			final BlockSupplier< S > srcSupplier,
			final UnaryBlockOperator< S, T > operator )
	{
		this.p0 = srcSupplier;
		this.p1 = operator.blockProcessor();
		this.type = operator.getTargetType();
	}

	private ConcatenatedBlockSupplier( final ConcatenatedBlockSupplier< T > s )
	{
		p0 = s.p0.threadSafeSupplier().get();
		p1 = s.p1.threadSafeSupplier().get();
		type = s.type;
		threadSafeSupplier = s.threadSafeSupplier;
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public void copy( final long[] srcPos, final Object dest, final int[] size )
	{
//			p1.setTargetInterval( srcPos, size ); // TODO?
		p1.setTargetInterval( interval( srcPos, size ) );
		final Object src = p1.getSourceBuffer();
		p0.copy( p1.getSourcePos(), src, p1.getSourceSize() );
		p1.compute( Cast.unchecked( src ), Cast.unchecked( dest ) );
	}

	private static Interval interval( long[] srcPos, int[] size )
	{
		final long[] srcMax = new long[ srcPos.length ];
		Arrays.setAll( srcMax, d -> srcPos[ d ] + size[ d ] - 1 );
		return FinalInterval.wrap( srcPos, srcMax );
	}

	private Supplier< ? extends BlockSupplier< T > > threadSafeSupplier;

	@Override
	public Supplier< ? extends BlockSupplier< T > > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( () -> new ConcatenatedBlockSupplier<>( this ) )::get;
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
				return type;
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
