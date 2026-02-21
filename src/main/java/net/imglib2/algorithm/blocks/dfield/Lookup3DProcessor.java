package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.type.PrimitiveType;

class Lookup3DProcessor< F, P > extends AbstractLookupProcessor< F, P >
{
	private final Lookup3D< F, P > lookup;

	Lookup3DProcessor(
			final PrimitiveType dfieldPrimitiveType,
			final Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		super( primitiveType, 3 );
		lookup = Lookup3D.of( dfieldPrimitiveType, interpolation, primitiveType );
	}

	private Lookup3DProcessor( Lookup3DProcessor< F, P > processor )
	{
		super( processor );
		lookup = processor.lookup;
	}

	@Override
	public Lookup3DProcessor< F, P > independentCopy()
	{
		return new Lookup3DProcessor<>( this );
	}

	@Override
	public void compute( final P src, final P dest )
	{
		final double d0 = positionOffset[ 0 ];
		final double d1 = positionOffset[ 1 ];
		final double d2 = positionOffset[ 2 ];
		final int length = destSize[ 0 ] * destSize[ 1 ] * destSize[ 2 ];
		final int ss0 = sourceSize[ 0 ];
		final int ss1 = sourceSize[ 1 ] * ss0;
		lookup.apply( positionField, d0, d1, d2, src, dest, length, ss0, ss1 );
	}
}
