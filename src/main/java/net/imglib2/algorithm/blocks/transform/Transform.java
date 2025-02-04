/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.transform;

import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.function.Function;

/**
 * Affine transform in 2D/3D with n-linear or nearest-neighbor interpolation.
 */
public class Transform
{

	public enum Interpolation
	{
		NEARESTNEIGHBOR,
		NLINEAR;
	}

	/**
	 * Interpolate and affine-transform blocks of the standard ImgLib2 {@code
	 * RealType}s.
	 * <p>
	 * Only 2D and 3D are supported currently!
	 * <p>
	 * The returned factory function creates an operator matching the type a
	 * given input {@code BlockSupplier<T>}.
	 *
	 * @param transformFromSource
	 * 		a 2D or 3D affine transform
	 * @param interpolation
	 * 		which interpolation method to use
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to affine-transform blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > affine( final AffineGet transformFromSource, Interpolation interpolation )
	{
		return affine( transformFromSource, interpolation, ComputationType.AUTO );
	}

	/**
	 * Interpolate and affine-transform blocks of the standard ImgLib2 {@code
	 * RealType}s.
	 * <p>
	 * Only 2D and 3D are supported currently!
	 * <p>
	 * The returned factory function creates an operator matching the type a
	 * given input {@code BlockSupplier<T>}.
	 *
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
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to affine-transform blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > affine( final AffineGet transformFromSource, Interpolation interpolation, final ComputationType computationType )
	{
		return s -> createAffineOperator( s.getType(), transformFromSource, interpolation, computationType, ClampType.CLAMP );
	}

	/**
	 * Create a {@code UnaryBlockOperator} to interpolate and affine-transform
	 * blocks of the standard ImgLib2 {@code RealType}s.
	 * <p>
	 * Only 2D and 3D are supported currently!
	 *
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
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to affine-transform blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createAffineOperator( final T type, final AffineGet transformFromSource, Interpolation interpolation, final ComputationType computationType, final ClampType clampType )
	{
		final int n = transformFromSource.numDimensions();
		if ( n < 2 || n > 3 ) {
			throw new IllegalArgumentException( "Only 2D and 3D affine transforms are supported currently" );
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
					? _affine( transformToSource, interpolation, new FloatType() )
					: _affine( transformToSource, interpolation, new DoubleType() );
			return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, clampType );
		}
		else // if ( interpolation == Interpolation.NEARESTNEIGHBOR )
		{
			return _affine( transformToSource, interpolation, type );
		}
	}

	private static < T extends NativeType< T > > UnaryBlockOperator< T, T > _affine( final AffineGet transform, final Interpolation interpolation, final T type )
	{
		final int n = transform.numDimensions();
		return new DefaultUnaryBlockOperator<>( type, type, n, n,
				n == 2
						? new Affine2DProcessor<>( ( AffineTransform2D ) transform, interpolation, type.getNativeTypeFactory().getPrimitiveType() )
						: new Affine3DProcessor<>( ( AffineTransform3D ) transform, interpolation, type.getNativeTypeFactory().getPrimitiveType() ) );
	}

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
