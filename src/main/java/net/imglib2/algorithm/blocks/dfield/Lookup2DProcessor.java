package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.algorithm.blocks.transform.Transform;
import net.imglib2.type.PrimitiveType;

public // TODO: make package private again (public for testing)
class Lookup2DProcessor< F, P > extends AbstractLookupProcessor< F, P >
{
	private final Lookup2D< F, P > lookup;

	public // TODO: make package private again (public for testing)
	Lookup2DProcessor(
			final PrimitiveType dfieldPrimitiveType,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		super( 2, interpolation, primitiveType );
		lookup = Lookup2D.of( dfieldPrimitiveType, interpolation, primitiveType );
	}

	private Lookup2DProcessor( Lookup2DProcessor< F, P > processor )
	{
		super( processor );
		lookup = processor.lookup;
	}

	@Override
	public AbstractLookupProcessor< F, P > independentCopy()
	{
		return new Lookup2DProcessor<>( this );
	}

	@Override
	public void compute( final P src, final P dest )
	{
		final double d0 = positionOffset[ 0 ];
		final double d1 = positionOffset[ 1 ];
		final int length = destSize[ 0 ] * destSize[ 1 ];
		final int ss0 = sourceSize[ 0 ];
		lookup.apply( positionField, d0, d1, src, dest, length, ss0 );
	}
}
