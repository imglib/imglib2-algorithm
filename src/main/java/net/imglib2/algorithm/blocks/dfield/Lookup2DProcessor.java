package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.type.PrimitiveType;

class Lookup2DProcessor< F, P > extends AbstractLookupProcessor< F, P >
{
	private final Lookup2D< F, P > lookup;

	Lookup2DProcessor(
			final PrimitiveType dfieldPrimitiveType,
			final Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		super( primitiveType, 2 );
		lookup = Lookup2D.of( dfieldPrimitiveType, interpolation, primitiveType );
	}

	private Lookup2DProcessor( Lookup2DProcessor< F, P > processor )
	{
		super( processor );
		lookup = processor.lookup;
	}

	@Override
	public Lookup2DProcessor< F, P > independentCopy()
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
