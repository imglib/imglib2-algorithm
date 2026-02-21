/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.blocks.dfield;

import static net.imglib2.algorithm.blocks.transform.Transform.invert;
import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.function.Function;

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
 * Affine transform 2D/3D displacement fields.
 */
public class DisplacementFieldTransform
{
	/**
	 * Interpolate and transform blocks of the standard ImgLib2 {@code
	 * RealType}s using an affine-transformed displacement field.
	 * <p>
	 * Only 2D and 3D are supported currently!
	 * <p>
	 * The returned factory function creates an operator matching the type a
	 * given input {@code BlockSupplier<T>}.
	 *
	 * @param transformFromField
	 * 		a 2D or 3D affine transform from displacementField coordinates to
	 * 		target coordinates
	 * @param displacementField
	 * 		the (normalized) displacement field
	 * @param interpolation
	 * 		which interpolation method to use for lookup in the source image.
	 * 		(displacement field is always linearly interpolated)
	 * @param <D>
	 * 		displacement field type
	 * @param <T>
	 * 		the source/target type
	 *
	 * @return factory for {@code UnaryBlockOperator} to transform blocks of type {@code T}
	 */
	public static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > displacementFieldAffine( final AffineGet transformFromField, final DisplacementField< D > displacementField, final Interpolation interpolation )
	{
		return displacementFieldAffine( transformFromField, displacementField, interpolation, ComputationType.AUTO );
	}

	/**
	 * Interpolate and transform blocks of the standard ImgLib2 {@code
	 * RealType}s using an affine-transformed displacement field.
	 * <p>
	 * Only 2D and 3D are supported currently!
	 * <p>
	 * The returned factory function creates an operator matching the type a
	 * given input {@code BlockSupplier<T>}.
	 *
	 * @param transformFromField
	 * 		a 2D or 3D affine transform from displacementField coordinates to
	 * 		target coordinates
	 * @param displacementField
	 * 		the (normalized) displacement field
	 * @param interpolation
	 * 		which interpolation method to use for lookup in the source image.
	 * 		(displacement field is always linearly interpolated)
	 * @param computationType
	 * 		For n-linear interpolation, this specifies in which precision
	 * 		intermediate values should be computed. For {@code AUTO}, the type
	 * 		that can represent the input/output type without loss of precision
	 * 		is picked. That is, {@code FLOAT} for u8, i8, u16, i16, i32, f32,
	 *      and otherwise {@code DOUBLE} for u32, i64, f64. For nearest-neighbor
	 *      interpolation, {@code computationType} is not used.
	 * @param <D>
	 * 		displacement field type
	 * @param <T>
	 * 		the source/target type
	 *
	 * @return factory for {@code UnaryBlockOperator} to transform blocks of type {@code T}
	 */
	public static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > displacementFieldAffine( final AffineGet transformFromField, final DisplacementField< D > displacementField, final Interpolation interpolation, final ComputationType computationType )
	{
		return s -> createDisplacementFieldOperator( s.getType(), transformFromField, displacementField, interpolation, computationType, ClampType.CLAMP );
	}

	/**
	 * Combines an affine transformation into a (linearly interpolated)
	 * displacement field, and look-up with resulting position field vectors in
	 * a source image of type {@code T}, to produce an output image of type
	 * {@code T}.
	 *
	 * @param type
	 * 		instance of the source/target type
	 * @param transformFromField
	 * 		a 2D or 3D affine transform from displacementField coordinates to
	 * 		target coordinates
	 * @param displacementField
	 *      the (normalized) displacement field
	 * @param interpolation
	 * 		which interpolation method to use for lookup in the source image.
	 * 		(displacement field is always linearly interpolated)
	 * @param computationType
	 * 		For n-linear interpolation, this specifies in which precision
	 * 		intermediate values should be computed. For {@code AUTO}, the type
	 * 		that can represent the input/output type without loss of precision
	 * 		is picked. That is, {@code FLOAT} for u8, i8, u16, i16, i32, f32,
	 * 		and otherwise {@code DOUBLE} for u32, i64, f64. For nearest-neighbor
	 * 		interpolation, {@code computationType} is not used.
	 * @param clampType
	 * 		For n-linear interpolation this specifies how interpolated values
	 * 		(which are always float or double) should be clamped when converting
	 * 		back to the source/target {@code type}. For nearest-neighbor
	 * 		interpolation, {@code clampType} is not used.
	 * @param <D>
	 * 		displacement field type
	 * @param <T>
	 * 		the source/target type
	 */
	public static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > >
	UnaryBlockOperator< T, T > createDisplacementFieldOperator(
			final T type,
			final AffineGet transformFromField,
			final DisplacementField< D > displacementField,
			final Interpolation interpolation,
			final ComputationType computationType,
			final ClampType clampType )
	{
		final int n = transformFromField.numDimensions();
		if ( n < 2 || n > 3 ) {
			throw new IllegalArgumentException( "Only 2D and 3D affine transforms are supported currently" );
		}
		if ( displacementField.numDimensions() != n ) {
			throw new IllegalArgumentException( "Number of dimension must be the same for the affine transform and the displacement field" );
		}

		final AffineGet transformToField = invert( transformFromField );

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
					? _disp( transformToField, displacementField, interpolation, new FloatType() )
					: _disp( transformToField, displacementField, interpolation, new DoubleType() );
			return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, clampType );
		}
		else // if ( interpolation == Interpolation.NEARESTNEIGHBOR )
		{
			return _disp( transformToField, displacementField, interpolation, type );
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
		final double[] scale = displacementField.scale();
		final double[] translation = displacementField.translation();
		final BlockSupplier< D > displacements = displacementField.displacements();
		final AbstractDispFieldAffineProcessor< ? > fieldProcessor = ( n == 2 )
				? new DispFieldAffine2DProcessor<>( ( AffineTransform2D ) transform, scale, translation, interpolation, dfieldPrimitiveType )
				: new DispFieldAffine3DProcessor<>( ( AffineTransform3D ) transform, scale, translation, interpolation, dfieldPrimitiveType );
		final AbstractLookupProcessor< ?, ? > lookupProcessor = ( n == 2 )
				? new Lookup2DProcessor<>( dfieldPrimitiveType, interpolation, primitiveType )
				: new Lookup3DProcessor<>( dfieldPrimitiveType, interpolation, primitiveType );
		return new DisplacementFieldUnaryBlockOperator<>( type, n, fieldProcessor, displacements, lookupProcessor );
	}
}
