package net.imglib2.algorithm.blocks.downsample;

import java.util.Arrays;
import net.imglib2.algorithm.blocks.convert.ClampType;
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

import static net.imglib2.type.PrimitiveType.FLOAT;

/**
 * Downsampling by factor 2.
 * Coordinates of downsampled pixel: either centered, or shifted by half a pixel.
 */
public class Downsample
{
	/**
	 * Returns recommended size of the downsampled image, given an input image
	 * of size {@code imgDimensions}. In each dimension (that should be
	 * downsampled), the recommended size is input size / 2, rounding up (e.g.,
	 * a 5 pixel input image should be downsampled to a 3 pixel image).
	 *
	 * @param imgDimensions
	 * dimensions of the input image
	 * @param downsampleInDim
	 * for each dimension (same length as imgDimensions}: {@code true} if the input image should be down
	 * @return
	 */
	public static long[] getDownsampledDimensions( final long[] imgDimensions, final boolean[] downsampleInDim )
	{
		final int n = imgDimensions.length;
		if ( downsampleInDim.length != n )
			throw new IllegalArgumentException();
		final long[] destSize = new long[ n ];
		Arrays.setAll( destSize, d -> downsampleInDim[ d ] ? ( imgDimensions[ d ] + 1 ) / 2 : imgDimensions[ d ] );
		return destSize;
	}

	/**
	 * Specify in which precision should intermediate values be computed. (For
	 * {@code AUTO}, the type that can represent the input/output type without
	 * loss of precision is picked. That is, {@code FLOAT} for u8, i8, u16, i16,
	 * i32, f32, and otherwise {@code DOUBLE} for u32, i64, f64.
	 */
	public enum ComputationType
	{
		FLOAT, DOUBLE, AUTO
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
		 * 9 pixels for 2D, 27 voxels for 3D, etc).
		 */
		CENTERED,

		/**
		 * Downsampled pixels are shifted by half a pixel.
		 * Each downsampled pixel is computed as the average of 2 neighboring
		 * pixels (respectively 4 pixels for 2D, 8 voxels for 3D, etc).
		 */
		HALF_PIXEL
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by factor 2) blocks of
	 * the standard ImgLib2 {@code RealType}. The {@code downsampleInDim}
	 * argument spwecifies in which dimensions the input should be downsampled.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 *      computed. For {@code AUTO}, the type that can represent the
	 *      input/output type without loss of precision is picked. That is,
	 *      {@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 *      DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 *      as the average of 2 neighboring pixels (respectively 4 pixels for
	 *      2D, 8 voxels for 3D, etc), and	downsampled pixels are shifted by
	 *      half a pixel.
	 *      {@code CENTERED} means that each downsampled pixel is computed as
	 *      the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 *      neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 *      etc). Downsampled pixels are centered on input pixels.
	 * @param downsampleInDim
	 *      for each dimension {@code d}: if {@code downsampleInDim[d]==true} if
	 *      the input image should be downsampled in dimension {@code d}.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > downsample( final T type, final ComputationType computationType, final Offset offset, final boolean[] downsampleInDim )
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
				? downsampleFloat( offset, downsampleInDim )
				: downsampleDouble( offset, downsampleInDim );
		return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, ClampType.NONE );
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
	 *      computed. For {@code AUTO}, the type that can represent the
	 *      input/output type without loss of precision is picked. That is,
	 *      {@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 *      DOUBLE} for u32, i64, f64.
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 *      as the average of 2 neighboring pixels (respectively 4 pixels for
	 *      2D, 8 voxels for 3D, etc), and	downsampled pixels are shifted by
	 *      half a pixel.
	 *      {@code CENTERED} means that each downsampled pixel is computed as
	 *      the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 *      neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 *      etc). Downsampled pixels are centered on input pixels.
	 * @param numDimensions
	 *      number of dimensions in input/output image.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > downsample( final T type, final ComputationType computationType, final Offset offset, final int numDimensions )
	{
		final boolean[] downsampleInDim = new boolean[ numDimensions ];
		Arrays.fill( downsampleInDim, true );
		return downsample( type, computationType, offset, downsampleInDim );
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by factor 2, in all
	 * dimensions) blocks of the standard ImgLib2 {@code RealType}.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision is picked. That is, {@code
	 * FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for
	 * u32, i64, f64.
	 *
	 * @param type
	 * 		instance of the input type
	 * @param offset
	 * 		Specifies where downsampled pixels should be placed.
	 * 		{@code HALF_PIXEL} means that each downsampled pixel is computed
	 *      as the average of 2 neighboring pixels (respectively 4 pixels for
	 *      2D, 8 voxels for 3D, etc), and	downsampled pixels are shifted by
	 *      half a pixel.
	 *      {@code CENTERED} means that each downsampled pixel is computed as
	 *      the weighted average (with weights {@code {.25, .5, .25}}) of 3
	 *      neighboring pixels (respectively 9 pixels for 2D, 27 voxels for 3D,
	 *      etc). Downsampled pixels are centered on input pixels.
	 * @param numDimensions
	 *      number of dimensions in input/output image.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > downsample( final T type, final Offset offset, final int numDimensions )
	{
		return downsample( type, ComputationType.AUTO, offset, numDimensions );
	}

	/**
	 * Create a {@code UnaryBlockOperator} to downsample (by factor 2, in all
	 * dimensions) blocks of the standard ImgLib2 {@code RealType}.
	 * <p>
	 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
	 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
	 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision is picked. That is, {@code
	 * FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for
	 * u32, i64, f64.
	 * <p>
	 * Downsampled pixels are computed as the average of 2 neighboring pixels
	 * (respectively 4 pixels for 2D, 8 voxels for 3D, etc). This means
	 * downsampled pixels are shifted by half a pixel.
	 *
	 * @param type
	 * 		instance of the input type
	 * @param numDimensions
	 *      number of dimensions in input/output image.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > downsample( final T type, final int numDimensions )
	{
		return downsample( type, ComputationType.AUTO, Offset.HALF_PIXEL, numDimensions );
	}



	private static UnaryBlockOperator< FloatType, FloatType > downsampleFloat( Offset offset, final boolean[] downsampleInDim )
	{
		final FloatType type = new FloatType();
		return new DefaultUnaryBlockOperator<>( type, type,
				offset == Offset.HALF_PIXEL
						? new HalfPixelFloat( downsampleInDim )
						: new CenterFloat( downsampleInDim ) );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > downsampleDouble( Offset offset, final boolean[] downsampleInDim )
	{
		final DoubleType type = new DoubleType();
		return new DefaultUnaryBlockOperator<>( type, type,
				offset == Offset.HALF_PIXEL
						? new HalfPixelDouble( downsampleInDim )
						: new CenterDouble( downsampleInDim ) );
	}
}
