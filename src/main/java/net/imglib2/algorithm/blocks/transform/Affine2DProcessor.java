package net.imglib2.algorithm.blocks.transform;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform, using {@link
 * AffineTransform2D} and 2D source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
class Affine2DProcessor< P > extends AbstractTransformProcessor< Affine2DProcessor< P >, P >
{
	private final AffineTransform2D transformToSource;

	private final TransformLine2D< P > transformLine;

	private final double pdest[] = new double[ 2 ];

	private final double psrc[] = new double[ 2 ];

	Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		this( transformToSource, interpolation, primitiveType, TransformLine2D.of( interpolation, primitiveType ) );
	}

	private Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType,
			final TransformLine2D< P > transformLine )
	{
		super( 2, interpolation, primitiveType );
		this.transformToSource = transformToSource;
		this.transformLine = transformLine;
	}

	private Affine2DProcessor( Affine2DProcessor< P > processor )
	{
		super( processor );
		transformToSource = processor.transformToSource;
		transformLine = processor.transformLine;
	}

	@Override
	Affine2DProcessor newInstance()
	{
		return new Affine2DProcessor( this );
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
		final int ds0 = destSize[ 0 ];
		final int ss0 = sourceSize[ 0 ];
		pdest[ 0 ] = destPos[ 0 ];
		int i = 0;
		for ( int y = 0; y < destSize[ 1 ]; ++y )
		{
			pdest[ 1 ] = y + destPos[ 1 ];
			transformToSource.apply( pdest, psrc );
			float sf0 = ( float ) ( psrc[ 0 ] - sourcePos[ 0 ] );
			float sf1 = ( float ) ( psrc[ 1 ] - sourcePos[ 1 ] );
			transformLine.apply( src, dest, i, ds0, d0, d1, ss0, sf0, sf1 );
			i += ds0;
		}
	}

}
