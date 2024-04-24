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

	private Supplier< BlockSupplier< T > > threadSafeSupplier;

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
		p0 = s.p0.independentCopy();
		p1 = s.p1.independentCopy();
		type = s.type;
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

	@Override
	public BlockSupplier< T > independentCopy()
	{
		return new ConcatenatedBlockSupplier<>( this );
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
				return type;
			}

			@Override
			public void copy( final long[] srcPos, final Object dest, final int[] size )
			{
				threadSafeSupplier.get().copy( srcPos, dest, size );
			}

			@Override
			public BlockSupplier< T > independentCopy()
			{
				return ConcatenatedBlockSupplier.this.independentCopy().threadSafe();
			}

			@Override
			public BlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
