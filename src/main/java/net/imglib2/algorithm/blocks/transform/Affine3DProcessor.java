package net.imglib2.algorithm.blocks.transform;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform, using {@link
 * AffineTransform3D} and 3D source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
class Affine3DProcessor< P > extends AbstractTransformProcessor< Affine3DProcessor< P >, P >
{
	private final AffineTransform3D transformToSource;

	private final TransformLine3D< P > transformLine;

	private final double pdest[] = new double[ 3 ];

	private final double psrc[] = new double[ 3 ];

	Affine3DProcessor(
			final AffineTransform3D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		this( transformToSource, interpolation, primitiveType, TransformLine3D.of( interpolation, primitiveType ) );
	}

	private Affine3DProcessor(
			final AffineTransform3D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType,
			final TransformLine3D< P > transformLine )
	{
		super( 3, interpolation, primitiveType );
		this.transformToSource = transformToSource;
		this.transformLine = transformLine;
	}

	private Affine3DProcessor( Affine3DProcessor< P > processor )
	{
		super( processor );
		transformToSource = processor.transformToSource;
		transformLine = processor.transformLine;
	}

	@Override
	Affine3DProcessor newInstance()
	{
		return new Affine3DProcessor( this );
	}

	@Override
	RealInterval estimateBounds( final Interval interval )
	{
		return transformToSource.estimateBounds( interval );
	}

	// specific to 3D
	@Override
	public void compute( final P src, final P dest )
	{
		final float d0 = transformToSource.d( 0 ).getFloatPosition( 0 );
		final float d1 = transformToSource.d( 0 ).getFloatPosition( 1 );
		final float d2 = transformToSource.d( 0 ).getFloatPosition( 2 );
		final int ds0 = destSize[ 0 ];
		final int ss0 = sourceSize[ 0 ];
		final int ss1 = sourceSize[ 1 ] * ss0;
		pdest[ 0 ] = destPos[ 0 ];
		int i = 0;
		for ( int z = 0; z < destSize[ 2 ]; ++z )
		{
			pdest[ 2 ] = z + destPos[ 2 ];
			for ( int y = 0; y < destSize[ 1 ]; ++y )
			{
				pdest[ 1 ] = y + destPos[ 1 ];
				transformToSource.apply( pdest, psrc );
				float sf0 = ( float ) ( psrc[ 0 ] - sourcePos[ 0 ] );
				float sf1 = ( float ) ( psrc[ 1 ] - sourcePos[ 1 ] );
				float sf2 = ( float ) ( psrc[ 2 ] - sourcePos[ 2 ] );
				transformLine.apply( src, dest, i, ds0, d0, d1, d2, ss0, ss1, sf0, sf1, sf2 );
				i += ds0;
			}
		}
	}
}
