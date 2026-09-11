package net.imglib2.algorithm.blocks.convert;

import net.imglib2.Interval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.blocks.AbstractUnaryBlockOperator;
import net.imglib2.algorithm.blocks.VolatileBlockSupplier;
import net.imglib2.algorithm.blocks.VolatileUnaryBlockOperator;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks;
import net.imglib2.type.NativeType;

// TODO: Move to VolatileUnaryBlockOperator.Identity?
final class IdentityVolatileUnaryBlockOperator< T extends Volatile< ? > & NativeType< T > >
		extends AbstractUnaryBlockOperator< T, T >
		implements VolatileUnaryBlockOperator< T, T >
{
	IdentityVolatileUnaryBlockOperator( final T type )
	{
		super( type, type, 0, 0 );
	}

	@Override
	public void compute( final VolatileBlockSupplier< T > src, final Interval interval, final VolatileArray< ? > dest )
	{
		src.copy( interval, dest );
	}

	@Override
	public VolatilePrimitiveBlocks.AllValid allValid( final VolatileBlockSupplier< T > src, final Interval interval, final boolean failFast )
	{
		return src.allValid( interval, failFast );
	}

	@Override
	public VolatileUnaryBlockOperator< T, T > independentCopy()
	{
		return this;
	}

	@Override
	public VolatileBlockSupplier< T > applyTo( final VolatileBlockSupplier< T > blocks )
	{
		return blocks;
	}

	@Override
	public < U extends Volatile< ? > & NativeType< U > > VolatileUnaryBlockOperator< T, U > andThen( final VolatileUnaryBlockOperator< T, U > op )
	{
		return op;
	}
}
