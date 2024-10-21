package net.imglib2.algorithm.blocks;

import net.imglib2.Interval;
import net.imglib2.type.NativeType;

class ConcatenatedUnaryBlockOperator<
		S extends NativeType< S >,
		T extends NativeType< T >,
		U extends NativeType< U > >
		implements UnaryBlockOperator< S, U >
{
	private final UnaryBlockOperator< S, T > op1;
	private final UnaryBlockOperator< T, U > op2;
	private final int numSourceDimensions;
	private final int numTargetDimensions;

	ConcatenatedUnaryBlockOperator( UnaryBlockOperator< S, T > op1, UnaryBlockOperator< T, U > op2 )
	{
		this.op1 = op1;
		this.op2 = op2;

		final boolean op1HasDims = op1.numSourceDimensions() > 0;
		final boolean op2HasDims = op2.numSourceDimensions() > 0;
		if ( op2HasDims && op1HasDims && op1.numTargetDimensions() != op2.numSourceDimensions() ) {
			throw new IllegalArgumentException( "UnaryBlockOperator cannot be concatenated: number of dimensions mismatch." );
		}
		this.numSourceDimensions = op1HasDims ? op1.numSourceDimensions() : op2.numSourceDimensions();
		this.numTargetDimensions = op2HasDims ? op2.numTargetDimensions() : op1.numTargetDimensions();
	}

	private ConcatenatedUnaryBlockOperator( ConcatenatedUnaryBlockOperator< S, T, U > op )
	{
		this.op1 = op.op1.independentCopy();
		this.op2 = op.op2.independentCopy();
		this.numSourceDimensions = op.numSourceDimensions;
		this.numTargetDimensions = op.numTargetDimensions;
	}

	@Override
	public void compute( final BlockSupplier< S > src, final Interval interval, final Object dest )
	{
		applyTo( src ).copy( interval, dest );
	}

	@Override
	public S getSourceType()
	{
		return op1.getSourceType();
	}

	@Override
	public U getTargetType()
	{
		return op2.getTargetType();
	}

	@Override
	public int numSourceDimensions()
	{
		return 0;
	}

	@Override
	public int numTargetDimensions()
	{
		return 0;
	}

	@Override
	public UnaryBlockOperator< S, U > independentCopy()
	{
		return new ConcatenatedUnaryBlockOperator<>( this );
	}

	@Override
	public BlockSupplier< U > applyTo( final BlockSupplier< S > blocks )
	{
		return op2.applyTo( op1.applyTo( blocks ) );
	}
}
