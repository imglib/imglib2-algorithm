package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.algorithm.blocks.ClampType.NONE;
import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.function.Function;

import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.convolve.ConvolveProcessors.ConvolveDouble;
import net.imglib2.algorithm.blocks.convolve.ConvolveProcessors.ConvolveFloat;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

/**
 * Separable convolution.
 * <p>
 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
 * <p>
 * For {@code T} other than {@code DoubleType} or {@code FloatType}, the input
 * will be converted to float/double for computation and the result converted
 * back to {@code T}. To avoid unnecessary conversions, if you want the result
 * as {@code FloatType} then you should explicitly convert to {@code FloatType}
 * <em>before</em> applying the convolve operator.
 * This code:
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > convolved = BlockSupplier.of( input )
 *         .andThen( Convert.convert( new FloatType() ) )
 *         .andThen( Convolve.convolve( kernels ) );
 * }</pre>
 * avoids loss of precision and is more efficient than
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > convolved = BlockSupplier.of( input )
 *         .andThen( Convolve.convolve( kernels ) )
 *         .andThen( Convert.convert( new FloatType() ) );
 * }</pre>
 *
 */
public class Convolve
{

	/**
	 * Convolve blocks of the standard ImgLib2 {@code RealType}s with a Gaussian kernel.
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision. That is, {@code FLOAT} for
	 * u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for u32, i64,
	 * f64.
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param sigma
	 *      sigmas in each dimension. if the image has fewer or more dimensions
	 *      than values given, values will be truncated or the final value
	 *      repeated as necessary.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to convolve blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > gauss( final double... sigma )
	{
		return gauss( ComputationType.AUTO, sigma );
	}

	/**
	 * Convolve blocks of the standard ImgLib2 {@code RealType}s with a Gaussian kernel.
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
	 * @param sigma
	 *      sigmas in each dimension. if the image has fewer or more dimensions
	 *      than values given, values will be truncated or the final value
	 *      repeated as necessary.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to convolve blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > gauss( final ComputationType computationType, final double... sigma )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			return createOperator( type, computationType, ClampType.NONE, gaussKernels( Util.expandArray( sigma, n ) ) );
		};
	}

	static Kernel1D[] gaussKernels( final double[] sigma )
	{
		final Kernel1D[] kernels = new Kernel1D[ sigma.length ];
		for ( int d = 0; d < sigma.length; d++ )
			if ( sigma[ d ] > 0 )
				kernels[ d ] = Kernel1D.symmetric( Gauss3.halfkernel( sigma[ d ] ) );
		return kernels;
	}

	/**
	 * Create a {@code UnaryBlockOperator} to convolve with the given {@code kernels}.
	 * {@code kernels.length} must match the dimensionality of the input images.
	 * If {@code kernels[d]==null}, the convolution for dimension {@code d} is
	 * skipped (equivalent to convolution with the kernel {@code {1}}).
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
	 * @param kernels
	 * 		kernel to apply in each dimension
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to downsample blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final ComputationType computationType, final ClampType clampType, final Kernel1D[] kernels )
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
				? convolveFloat( kernels )
				: convolveDouble( kernels );
		return op.adaptSourceType( type, NONE ).adaptTargetType( type, clampType );
	}

	private static UnaryBlockOperator< FloatType, FloatType > convolveFloat( final Kernel1D[] kernels )
	{
		final FloatType type = new FloatType();
		final int n = kernels.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n, new ConvolveFloat( kernels ) );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > convolveDouble( final Kernel1D[] kernels )
	{
		final DoubleType type = new DoubleType();
		final int n = kernels.length;
		return new DefaultUnaryBlockOperator<>( type, type, n, n, new ConvolveDouble( kernels ) );
	}
}
