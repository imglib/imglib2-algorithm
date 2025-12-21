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

/**
 * A normalized displacement field, and its scale and offset with respect to a
 * source image.
 * <p>
 * A "normalized" field expresses displacements in units relative to the
 * displacement grid's own pixel spacing. Consequently, downsampling the grid by
 * a factor of N requires scaling the displacement vectors by 1/N to maintain
 * normalization.
 *
 * @param <T> displacement component type. should be {@code DoubleType} or {@code FloatType}
 */
public class DisplacementField< T extends NativeType< T > & RealType< T > >
	implements EuclideanSpace, Typed< T >
{
	private final BlockSupplier< T > displacements;

	private final double[] scale;

	private final double[] translation;

	public DisplacementField(
			final BlockSupplier< T > displacements,
			final double[] scale,
			final double[] translation )
	{
		// TODO: verify that dimensionality of all arguments matches
		if ( displacements.numDimensions() != scale.length || translation.length != scale.length ) {
			throw new IllegalArgumentException( "Dimensionality of scale and translation must match the number of dimensions in the displacement field" );
		}

		this.displacements = displacements;
		this.scale = scale;
		this.translation = translation;

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

	/**
	 * Displacement vector field.
	 * <p>
	 * The components of the displacement vector are in dimension 0, shifting
	 * all other dimensions by 1. (The returned {@code BlockSupplier} has {@link
	 * #numDimensions()} + 1 dimensions.)
	 *
	 * @return the displacement field
	 */
	public BlockSupplier< T > displacements()
	{
		return displacements;
	}

	/**
	 * The {@link #displacements() displacement field} coordinates and
	 * displacement vectors should be scaled by this factor when looking up
	 * intensities in a source image.
	 * <p>
	 * In other words, this is the spacing of the displacement field grid in
	 * units of source image pixels.
	 *
	 * @return relative scale of the displacement field wrt the source image
	 */
	public double[] scale()
	{
		return scale;
	}

	/**
	 * When interpolating {@link #displacements()} into a position field for
	 * value look-up in the source image, this translation vector is added.
	 * (This happens after scaling, so the translation is in units of source
	 * image pixels).
	 * <p>
	 * For example, this can be used to compensate for half-pixel offsets when
	 * applying downsampled displacement fields to full-resolution images.
	 *
	 * @return position field offset in source pixels
	 */
	public double[] translation()
	{
		return translation;
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
		final double[] scale = displacementField.scale;
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
