package net.imglib2.algorithm.blocks.dfield;

import static net.imglib2.type.PrimitiveType.FLOAT;

import net.imglib2.EuclideanSpace;
import net.imglib2.Typed;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

public class DisplacementField< T extends NativeType< T > & RealType< T > >
	implements EuclideanSpace, Typed< T >
{
	private final BlockSupplier< T > displacements;

	private final double[] displacementScale;

	private final double[] displacementTranslation;

	public DisplacementField(
			final BlockSupplier< T > displacements,
			final double[] displacementScale,
			final double[] displacementTranslation )
	{
		// TODO: verify that dimensionality of all arguments matches

		this.displacements = displacements;
		this.displacementScale = displacementScale;
		this.displacementTranslation = displacementTranslation;

	}

	@Override
	public int numDimensions()
	{
		return displacements.numDimensions();
	}

	@Override
	public T getType()
	{
		return displacements.getType();
	}


	//
	// TODO: the following should go into a separate class later
	//  --------------------------------------------------------

	/**
	 * @param type
	 * 		instance of the input type
	 * @param transformFromSource
	 * 		a 2D or 3D affine transform
	 * @param interpolation
	 * 		which interpolation method to use
	 * @param computationType
	 * 		For n-linear interpolation, this specifies in which precision
	 * 		intermediate values should be computed. For {@code AUTO}, the type
	 * 		that can represent the input/output type without loss of precision
	 * 		is picked. That is, {@code FLOAT} for u8, i8, u16, i16, i32, f32,
	 *      and otherwise {@code DOUBLE} for u32, i64, f64. For nearest-neighbor
	 *      interpolation, {@code computationType} is not used.
	 *
	 * @param <D>
	 * 		displacement field type
	 * @param <T>
	 * 		the source/target type
	 */
	public static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > >
	UnaryBlockOperator< T, T > createDisplacementFieldOperator(
			final T type,
			final AffineGet transformFromSource,
			final DisplacementField< D > displacementField,
			final Interpolation interpolation,
			final ComputationType computationType,
			final ClampType clampType )
	{
		final int n = transformFromSource.numDimensions();
		if ( n < 2 || n > 3 ) {
			throw new IllegalArgumentException( "Only 2D and 3D affine transforms are supported currently" );
		}
		if ( displacementField.numDimensions() != n ) {
			throw new IllegalArgumentException( "Number of dimension must be the same for the affine transform and the displacement field" );
		}

		final AffineGet transformToSource = invert( transformFromSource );

		if ( interpolation == Interpolation.NLINEAR )
		{
			final boolean processAsFloat;
			switch ( computationType )
			{
			case FLOAT:
				processAsFloat = true;
				break;
			case DOUBLE:
				processAsFloat = false;
				break;
			default:
			case AUTO:
				final PrimitiveType pt = type.getNativeTypeFactory().getPrimitiveType();
				processAsFloat = pt.equals( FLOAT ) || pt.getByteCount() < FLOAT.getByteCount();
				break;
			}
			final UnaryBlockOperator< ?, ? > op = processAsFloat
					? _disp( transformToSource, displacementField, interpolation, new FloatType() )
					: _disp( transformToSource, displacementField, interpolation, new DoubleType() );
			return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, clampType );
		}
		else // if ( interpolation == Interpolation.NEARESTNEIGHBOR )
		{
			return _disp( transformToSource, displacementField, interpolation, type );
		}
	}

	private static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > > UnaryBlockOperator< T, T > _disp(
			final AffineGet transform,
			final DisplacementField< D > displacementField,
			final Interpolation interpolation,
			final T type )
	{
		final int n = transform.numDimensions();
		final PrimitiveType primitiveType = type.getNativeTypeFactory().getPrimitiveType();
		final PrimitiveType dfieldPrimitiveType = displacementField.getType().getNativeTypeFactory().getPrimitiveType();
		final double[] scale = displacementField.displacementScale;
		final BlockSupplier< D > displacements = displacementField.displacements;
		final AbstractDispFieldAffineProcessor< ? > fieldProcessor = ( n == 2 )
				? new DispFieldAffine2DProcessor<>( ( AffineTransform2D ) transform, scale, interpolation, dfieldPrimitiveType )
				: new DispFieldAffine3DProcessor<>( ( AffineTransform3D ) transform, scale, interpolation, dfieldPrimitiveType );
		final AbstractLookupProcessor< ?, ? > lookupProcessor = ( n == 2 )
				? new Lookup2DProcessor<>( dfieldPrimitiveType, interpolation, primitiveType )
				: new Lookup3DProcessor<>( dfieldPrimitiveType, interpolation, primitiveType );
		return new DisplacementFieldUnaryBlockOperator<>( type, n, fieldProcessor, displacements, lookupProcessor );
	}

	// TODO: This same method is also in net.imglib2.algorithm.blocks.transform.Transform.
	//       Should we make it public? Put it into a utility class?
	private static AffineGet invert( final AffineGet transformFromSource )
	{
		switch ( transformFromSource.numDimensions() )
		{
		case 2:
		{
			final AffineTransform2D transform = new AffineTransform2D();
			transform.set( transformFromSource.inverse().getRowPackedCopy() );
			return transform;
		}
		case 3:
		{
			final AffineTransform3D transform = new AffineTransform3D();
			transform.set( transformFromSource.inverse().getRowPackedCopy() );
			return transform;
		}
		default:
			throw new IllegalArgumentException();
		}
	}

}
