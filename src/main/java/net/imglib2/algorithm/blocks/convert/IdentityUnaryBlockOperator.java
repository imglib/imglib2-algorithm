package net.imglib2.algorithm.blocks.convert;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractUnaryBlockOperator;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.type.NativeType;

// TODO: Move to UnaryBlockOperator.Identity?
final class IdentityUnaryBlockOperator< T extends NativeType< T > >
		extends AbstractUnaryBlockOperator< T, T >
		implements UnaryBlockOperator< T, T >
{
	IdentityUnaryBlockOperator( final T type )
	{
		super( type, type, 0, 0 );
	}

	@Override
	public void compute( final BlockSupplier< T > src, final Interval interval, final Object dest )
	{
		src.copy( interval, dest );
	}

	@Override
	public UnaryBlockOperator< T, T > independentCopy()
	{
		return this;
	}

	@Override
	public BlockSupplier< T > applyTo( final BlockSupplier< T > blocks )
	{
		return blocks;
	}

	@Override
	public < U extends NativeType< U > > UnaryBlockOperator< T, U > andThen( final UnaryBlockOperator< T, U > op )
	{
		return op;
	}
}
