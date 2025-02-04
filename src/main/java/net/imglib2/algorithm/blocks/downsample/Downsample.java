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
package net.imglib2.algorithm.blocks.downsample;

import java.util.Arrays;
import java.util.function.Function;

import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.AvgBlockDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.AvgBlockFloat;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.CenterDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.CenterFloat;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.HalfPixelDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.HalfPixelFloat;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import static net.imglib2.type.PrimitiveType.FLOAT;

/**
 * Downsampling by factor 2 in selected dimensions.
 * <p>
 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
 * <p>
 * For {@code T} other than {@code DoubleType} or {@code FloatType}, the input
 * will be converted to float/double for computation and the result converted
 * back to {@code T}. To avoid unnecessary conversions, if you want the result
 * as {@code FloatType} then you should explicitly convert to {@code FloatType}
 * <em>before</em> applying the downsampling operator.
 * This code:
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > downsampled = BlockSupplier.of( input )
 *         .andThen( Convert.convert( new FloatType() ) )
 *         .andThen( Downsample.downsample( Offset.HALF_PIXEL ) );
 * }</pre>
 * avoids loss of precision and is more efficient than
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > downsampled = BlockSupplier.of( input )
 *         .andThen( Downsample.downsample( Offset.HALF_PIXEL ) )
 *         .andThen( Convert.convert( new FloatType() ) );
 * }</pre>
 * <p>
 * The {@link #downsample(ComputationType, Offset)} methods return factory
 * functions that create {@code UnaryBlockOperator} to match the type and
 * dimensionality of a given input {@code BlockSupplier<T>}. For example, the
 * following code creates 2D downsampling operator for 2D {@code input}, a 3D
 * operator for 3D {@code input}, etc.
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< UnsignedByteType > downsampled = BlockSupplier.of( input )
 *         .andThen( Downsample.downsample( Offset.HALF_PIXEL ) );
 * }</pre>
 * <p>
 * An {@link Offset} specifies whether downsampled pixels are placed on corners or centers of input pixels:
 * <ul>
 * <li>{@link Offset#HALF_PIXEL} means that each downsampled pixel is computed
 *     as the average of 2 neighboring pixels (respectively 4 pixels for 2D, 8
 *     voxels for 3D, etc.) Downsampled pixels are shifted by half a pixel.
 * </li>
 * <li>{@link Offset#CENTERED} means that each downsampled pixel is computed as
 *     the weighted average (with weights {@code {.25, .5, .25}}) of 3 neighboring
 *     pixels (respectively 9 pixels for 2D, 27 voxels for 3D, etc.). Downsampled
 *     pixels are centered on input pixels.
 * </li>
 * </ul>
 * <p>
 * The {@link #downsample(ComputationType, int[])} methods support downsampling
 * with arbitrary integer factor by averaging blocks of pixels. (With {@code
 * factors={2,2,2,...}}, this is equivalent to downsampling with {@link
 * Offset#HALF_PIXEL}).
 */
public class Downsample
{
	/**
	 * Returns recommended size of the downsampled image, given an input image
	 * of size {@code imgDimensions}. In each dimension the recommended size is
	 * input size / 2, rounding up (e.g., a 5 pixel input image should be
	 * downsampled to a 3 pixel image).
	 *
	 * @param imgDimensions
	 * 		dimensions of the input image
	 *
	 * @return the recommended size of the downsampled image
	 */
	public static long[] getDownsampledDimensions( final long[] imgDimensions )
	{
		final long[] destSize = new long[ imgDimensions.length ];
		Arrays.setAll( destSize, d -> ( imgDimensions[ d ] + 1 ) / 2 );
		return destSize;
	}

	/**
	 * Returns recommended size of the downsampled image, given an input image
	 * of size {@code imgDimensions}. In each dimension (that should be
	 * downsampled), the recommended size is input size / 2, rounding up (e.g.,
	 * a 5 pixel input image should be downsampled to a 3 pixel image).
	 *
	 * @param imgDimensions
	 * 		dimensions of the input image
	 * @param downsampleInDim
	 * 		for each dimension {@code d}: if {@code downsampleInDim[d]==true} then
	 * 		the input image should be downsampled in dimension {@code d}.
	 * 		{@code downsampleInDim} is expanded or truncated to the necessary size.
	 * 		For example, if {@code downsampleInDim=={true,true,false}} and the
	 * 		operator is applied to a 2D image, {@code downsampleInDim} is truncated
	 * 		to {@code {true, true}}. If the operator is applied to a 5D image, {@code
	 * 		downsampleInDim} is expanded to {@code {true, true, false, false, false}}
	 *
	 * @return the recommended size of the downsampled image
	 */
	public static long[] getDownsampledDimensions( final long[] imgDimensions, final boolean[] downsampleInDim )
	{
		final boolean[] dDimsX = Util.expandArray( downsampleInDim, imgDimensions.length );
		final long[] destSize = new long[ imgDimensions.length ];
		Arrays.setAll( destSize, d -> dDimsX[ d ] ? ( imgDimensions[ d ] + 1 ) / 2 : imgDimensions[ d ] );
		return destSize;
	}

	/**
	 * Returns recommended size of the downsampled image, given an input image
	 * of size {@code imgDimensions}. In each dimension, the recommended size is
	 * input {@code imgDimension} / {@code downsamplingFactor}, rounding up
	 * (e.g., a 5 pixel input image downsampled with factor 2, should yield a 3
	 * pixel image).
	 *
	 * @param imgDimensions
	 * 		dimensions of the input image
	 * @param downsamplingFactors
	 * 		in each dimension {@code d}, {@code downsamplingFactors[d]}
	 * 		pixels in the input image should be averaged to one output pixel.
	 *      {@code downsamplingFactors} is expanded or truncated to the
	 * 		necessary size. For example, if {@code downsamplingFactors=={2,2,1}} and
	 * 		the operator is applied to a 2D image, {@code downsamplingFactors} is
	 * 		truncated to {@code {2, 2}}. If the operator is applied to a 5D image,
	 *      {@code downsamplingFactors} is expanded to {@code {2, 2, 1, 1, 1}}
	 *
	 * @return the recommended size of the downsampled image
	 */
	public static long[] getDownsampledDimensions( final long[] imgDimensions, final int[] downsamplingFactors )
	{
		final int[] dFactorsX = Util.expandArray( downsamplingFactors, imgDimensions.length );
		final long[] destSize = new long[ imgDimensions.length ];
		Arrays.setAll( destSize, d -> ( imgDimensions[ d ] + dFactorsX[ d ] - 1 ) / dFactorsX[ d ] );
		return destSize;
	}

	/**
	 * Specify where downsampled pixels should be placed.
	 */
	public enum Offset
	{
		/**
		 * Downsampled pixel centers fall on input pixel centers.
		 * Each downsampled pixel is computed as the weighted average (with
		 * weights {@code {.25, .5, .25}}) of 3 neighboring pixels (respectively
		 * 9 pixels for 2D, 27 voxels for 3D, etc.)
		 */
		CENTERED,

		/**
		 * Downsampled pixels are shifted by half a pixel.
		 * Each downsampled pixel is computed as the average of 2 neighboring
		 * pixels (respectively 4 pixels for 2D, 8 voxels for 3D, etc.)
		 */
		HALF_PIXEL
	}

	/**
	 * Downsample (by factor 2, in all dimensions) blocks of the standard
	 * ImgLib2 {@code RealType}s.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision. That is, {@code FLOAT} for
	 * u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for u32, i64,
	 * f64.
	 * <p>
	 * Downsampled pixels are computed as the average of 2 neighboring pixels
	 * (respectively 4 pixels for 2D, 8 voxels for 3D, etc.) This means
	 * downsampled pixels are shifted by half a pixel.
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample()
	{
		return downsample( Offset.HALF_PIXEL );
	}

	/**
	 * Downsample (by factor 2, in all dimensions) blocks of the standard
	 * ImgLib2 {@code RealType}s.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision. That is, {@code FLOAT} for
	 * u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for u32, i64,
	 * f64.
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 * 		as the average of 2 neighboring pixels (respectively 4 pixels for
	 * 		2D, 8 voxels for 3D, etc.), and	downsampled pixels are shifted by
	 * 		half a pixel.
	 * 		{@code CENTERED} means that each downsampled pixel is computed as
	 * 		the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 * 		neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 * 		etc.) Downsampled pixels are centered on input pixels.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample( final Offset offset )
	{
		return downsample( ComputationType.AUTO, offset );
	}

	/**
	 * Downsample (by factor 2, in all dimensions) blocks of the standard
	 * ImgLib2 {@code RealType}s.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 * 		as the average of 2 neighboring pixels (respectively 4 pixels for
	 * 		2D, 8 voxels for 3D, etc.), and	downsampled pixels are shifted by
	 * 		half a pixel.
	 * 		{@code CENTERED} means that each downsampled pixel is computed as
	 * 		the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 * 		neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 * 		etc.) Downsampled pixels are centered on input pixels.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample( final ComputationType computationType, final Offset offset )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			return createOperator( type, computationType, offset, n );
		};
	}

	/**
	 * Downsample (by factor 2) blocks of the standard ImgLib2 {@code
	 * RealType}s. The {@code downsampleInDim} argument specifies in which
	 * dimensions the input should be downsampled.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 * 		as the average of 2 neighboring pixels (respectively 4 pixels for
	 * 		2D, 8 voxels for 3D, etc.), and	downsampled pixels are shifted by
	 * 		half a pixel.
	 * 		{@code CENTERED} means that each downsampled pixel is computed as
	 * 		the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 * 		neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 * 		etc.) Downsampled pixels are centered on input pixels.
	 * @param downsampleInDim
	 * 		for each dimension {@code d}: if {@code downsampleInDim[d]==true} then
	 * 		the input image should be downsampled in dimension {@code d}.
	 * 		{@code downsampleInDim} is expanded or truncated to the necessary size.
	 * 		For example, if {@code downsampleInDim=={true,true,false}} and the
	 * 		operator is applied to a 2D image, {@code downsampleInDim} is truncated
	 * 		to {@code {true, true}}. If the operator is applied to a 5D image, {@code
	 * 		downsampleInDim} is expanded to {@code {true, true, false, false, false}}
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample( final ComputationType computationType, final Offset offset, final boolean[] downsampleInDim )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			final boolean[] expandedDownsampleInDim = Util.expandArray( downsampleInDim, n );
			return createOperator( type, computationType, offset, expandedDownsampleInDim );
		};
	}

	/**
	 * Downsample (by the given {@code downsamplingFactors}) blocks of the
	 * standard ImgLib2 {@code RealType}s.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision. That is, {@code FLOAT} for
	 * u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for u32, i64,
	 * f64.
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param downsamplingFactors
	 * 		in each dimension {@code d}, {@code downsamplingFactors[d]}
	 * 		pixels in the input image should be averaged to one output pixel.
	 *      {@code downsamplingFactors} is expanded or truncated to the
	 * 		necessary size. For example, if {@code downsamplingFactors=={2,2,1}} and
	 * 		the operator is applied to a 2D image, {@code downsamplingFactors} is
	 * 		truncated to {@code {2, 2}}. If the operator is applied to a 5D image,
	 *      {@code downsamplingFactors} is expanded to {@code {2, 2, 1, 1, 1}}
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample( final int[] downsamplingFactors )
	{
		return downsample( ComputationType.AUTO, downsamplingFactors );
	}

	/**
	 * Downsample (by the given {@code downsamplingFactors}) blocks of the
	 * standard ImgLib2 {@code RealType}s.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param downsamplingFactors
	 * 		in each dimension {@code d}, {@code downsamplingFactors[d]}
	 * 		pixels in the input image should be averaged to one output pixel.
	 *      {@code downsamplingFactors} is expanded or truncated to the
	 * 		necessary size. For example, if {@code downsamplingFactors=={2,2,1}} and
	 * 		the operator is applied to a 2D image, {@code downsamplingFactors} is
	 * 		truncated to {@code {2, 2}}. If the operator is applied to a 5D image,
	 *      {@code downsamplingFactors} is expanded to {@code {2, 2, 1, 1, 1}}
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > downsample( final ComputationType computationType, final int[] downsamplingFactors )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			final int[] expandedDownsamplingFactors = Util.expandArray( downsamplingFactors, n );
			return createOperator( type, computationType, expandedDownsamplingFactors );
		};
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by factor 2) blocks of
	 * the standard ImgLib2 {@code RealType}. The {@code downsampleInDim}
	 * argument specifies in which dimensions the input should be downsampled.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 * 		as the average of 2 neighboring pixels (respectively 4 pixels for
	 * 		2D, 8 voxels for 3D, etc.), and downsampled pixels are shifted by
	 * 		half a pixel.
	 * 		{@code CENTERED} means that each downsampled pixel is computed as
	 * 		the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 * 		neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 * 		etc.) Downsampled pixels are centered on input pixels.
	 * @param downsampleInDim
	 * 		for each dimension {@code d}: if {@code downsampleInDim[d]==true} then
	 * 		the input image should be downsampled in dimension {@code d}.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final ComputationType computationType, final Offset offset, final boolean[] downsampleInDim )
	{
		final UnaryBlockOperator< ?, ? > op = processAsFloat( computationType, type )
				? downsampleFloat( offset, downsampleInDim )
				: downsampleDouble( offset, downsampleInDim );
		return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, ClampType.NONE );
	}

	private static < T extends NativeType< T > > boolean processAsFloat( final ComputationType computationType, final T type )
	{
		switch ( computationType )
		{
		case FLOAT:
			return true;
		case DOUBLE:
			return true;
		case AUTO:
		default:
			final PrimitiveType pt = type.getNativeTypeFactory().getPrimitiveType();
			return pt.equals( FLOAT ) || pt.getByteCount() < FLOAT.getByteCount();
		}
	}

	private static UnaryBlockOperator< FloatType, FloatType > downsampleFloat( final Offset offset, final boolean[] downsampleInDim )
	{
		final FloatType type = new FloatType();
		final int n = downsampleInDim.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n,
				offset == Offset.HALF_PIXEL
						? new HalfPixelFloat( downsampleInDim )
						: new CenterFloat( downsampleInDim ) );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > downsampleDouble( final Offset offset, final boolean[] downsampleInDim )
	{
		final DoubleType type = new DoubleType();
		final int n = downsampleInDim.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n,
				offset == Offset.HALF_PIXEL
						? new HalfPixelDouble( downsampleInDim )
						: new CenterDouble( downsampleInDim ) );
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by factor 2, in all
	 * dimensions) blocks of the standard ImgLib2 {@code RealType}.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 * 		as the average of 2 neighboring pixels (respectively 4 pixels for
	 * 		2D, 8 voxels for 3D, etc.), and	downsampled pixels are shifted by
	 * 		half a pixel.
	 * 		{@code CENTERED} means that each downsampled pixel is computed as
	 * 		the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 * 		neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 * 		etc.) Downsampled pixels are centered on input pixels.
	 * @param numDimensions
	 * 		number of dimensions in input/output image.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final ComputationType computationType, final Offset offset, final int numDimensions )
	{
		final boolean[] downsampleInDim = new boolean[ numDimensions ];
		Arrays.fill( downsampleInDim, true );
		return createOperator( type, computationType, offset, downsampleInDim );
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by the given {@code
	 * downsamplingFactors}) blocks of the standard ImgLib2 {@code RealType}.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param downsamplingFactors
	 * 		in each dimension {@code d}, {@code downsamplingFactors[d]}
	 * 		pixels in the input image should be averaged to one output pixel.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final ComputationType computationType, final int[] downsamplingFactors )
	{
		final UnaryBlockOperator< ?, ? > op = processAsFloat( computationType, type )
				? downsampleFloat( downsamplingFactors )
				: downsampleDouble( downsamplingFactors );
		return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, ClampType.NONE );
	}

	private static UnaryBlockOperator< FloatType, FloatType > downsampleFloat( final int[] downsamplingFactors )
	{
		final FloatType type = new FloatType();
		final int n = downsamplingFactors.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n, new AvgBlockFloat( downsamplingFactors ) );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > downsampleDouble( final int[] downsamplingFactors )
	{
		final DoubleType type = new DoubleType();
		final int n = downsamplingFactors.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n, new AvgBlockDouble( downsamplingFactors ) );
	}

}
