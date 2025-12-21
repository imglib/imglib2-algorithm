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

}
