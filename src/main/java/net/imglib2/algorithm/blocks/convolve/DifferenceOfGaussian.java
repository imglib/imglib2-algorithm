package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.algorithm.blocks.convolve.Convolve.gaussKernels;
import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.Arrays;
import java.util.function.Function;

import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

/**
 * Difference of Gaussian.
 * <p>
 * Supported types are {@code UnsignedByteType}, {@code UnsignedShortType},
 * {@code UnsignedIntType}, {@code ByteType}, {@code ShortType}, {@code
 * IntType}, {@code LongType}, {@code FloatType}, {@code DoubleType}).
 * <p>
 * For {@code T} other than {@code DoubleType} or {@code FloatType}, the input
 * will be converted to float/double for computation and the result converted
 * back to {@code T}. To avoid unnecessary conversions, if you want the result
 * as {@code FloatType} then you should explicitly convert to {@code FloatType}
 * <em>before</em> applying the DoG operator.
 * This code:
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > convolved = BlockSupplier.of( input )
 *         .andThen( Convert.convert( new FloatType() ) )
 *         .andThen( DifferenceOfGaussian.DoG( sigmaSmaller, sigmaLarger ) );
 * }</pre>
 * avoids loss of precision and is more efficient than
 * <pre>{@code
 * RandomAccessible< UnsignedByteType > input;
 * BlockSupplier< FloatType > convolved = BlockSupplier.of( input )
 *         .andThen( DifferenceOfGaussian.DoG( sigmaSmaller, sigmaLarger ) )
 *         .andThen( Convert.convert( new FloatType() ) );
 * }</pre>
 *
 */
public class DifferenceOfGaussian
{

	/**
	 * Compute Difference-of-Gaussian for blocks of the standard ImgLib2 {@code
	 * RealType}s.
	 * <p>
	 * Like {@link #DoG(double[], double[])}, but {@code sigmaLarger} is
	 * determined as {@code k * sigmaSmaller}.
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final double k, final double... sigmaSmaller )
	{
		return DoG( ComputationType.AUTO, k, sigmaSmaller );
	}

	/**
	 * Compute Difference-of-Gaussian for blocks of the standard ImgLib2 {@code
	 * RealType}s: Input convolved with Gaussian of {@code sigmaSmaller} is
	 * subtracted from input convolved with Gaussian of {@code sigmaLarger}.
	 * <p>
	 * Precision for intermediate values is chosen as to represent the
	 * input/output type without loss of precision. That is, {@code FLOAT} for
	 * u8, i8, u16, i16, i32, f32, and otherwise {@code DOUBLE} for u32, i64,
	 * f64.
	 * <p>
	 * The returned factory function creates an operator matching the
	 * type and dimensionality of a given input {@code BlockSupplier<T>}.
	 *
	 * @param sigmaSmaller
	 * 		sigmas in each dimension for smaller Gaussian.
	 * 		if the image has fewer or more dimensions than values given,
	 * 		values will be truncated or the final value repeated as necessary.
	 * @param sigmaLarger
	 * 		sigmas in each dimension for larger Gaussian
	 * 		({@code sigmaLarger > sigmaSmaller}).
	 * 		if the image has fewer or more dimensions than values given,
	 * 		values will be truncated or the final value repeated as necessary.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to compute DoG for blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		return DoG( ComputationType.AUTO, sigmaSmaller, sigmaLarger );
	}

	/**
	 * Compute Difference-of-Gaussian for blocks of the standard ImgLib2 {@code
	 * RealType}s.
	 * <p>
	 * Like {@link #DoG(ComputationType, double[], double[])}, but {@code
	 * sigmaLarger} is determined as {@code k * sigmaSmaller}.
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final ComputationType computationType, final double k, final double... sigmaSmaller )
	{
		final double[] sigmaLarger = new double[ sigmaSmaller.length ];
		Arrays.setAll( sigmaLarger, i -> sigmaSmaller[ i ] * k );
		return DoG( computationType, sigmaSmaller, sigmaLarger );
	}

	/**
	 * Compute Difference-of-Gaussian for blocks of the standard ImgLib2 {@code
	 * RealType}s: Input convolved with Gaussian of {@code sigmaSmaller} is
	 * subtracted from input convolved with Gaussian of {@code sigmaLarger}.
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
	 * @param sigmaSmaller
	 * 		sigmas in each dimension for smaller Gaussian.
	 * 		if the image has fewer or more dimensions than values given,
	 * 		values will be truncated or the final value repeated as necessary.
	 * @param sigmaLarger
	 * 		sigmas in each dimension for larger Gaussian
	 * 		({@code sigmaLarger > sigmaSmaller}).
	 * 		if the image has fewer or more dimensions than values given,
	 * 		values will be truncated or the final value repeated as necessary.
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return factory for {@code UnaryBlockOperator} to compute DoG for blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final ComputationType computationType, final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			return createOperator( type, computationType, ClampType.CLAMP,
					Util.expandArray( sigmaSmaller, n ),
					Util.expandArray( sigmaLarger, n ) );
		};
	}

	/**
	 * Create a {@code UnaryBlockOperator} to compute Difference-of-Gaussian
	 * with the given sigmas. (Input convolved with Gaussian of {@code
	 * sigmaSmaller} is subtracted from input convolved with Gaussian of {@code
	 * sigmaLarger}). {@code sigmaSmaller.length} and {@code sigmaLarger.length}
	 * must match the dimensionality of the input image.
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 * 		{@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param sigmaSmaller
	 * 		sigmas in each dimension for smaller Gaussian
	 * @param sigmaLarger
	 * 		sigmas in each dimension for larger Gaussian
	 * 		({@code sigmaLarger > sigmaSmaller})
	 * @param <T>
	 * 		the input/output type
	 *
	 * @return {@code UnaryBlockOperator} to compute DoG for blocks of type {@code T}
	 */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final ComputationType computationType, final ClampType clampType, final double[] sigmaSmaller, final double[] sigmaLarger )
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
				? dogFloat( sigmaSmaller, sigmaLarger )
				: dogDouble( sigmaSmaller, sigmaLarger );
		return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, clampType );
	}

	private static UnaryBlockOperator< FloatType, FloatType > dogFloat( final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		final FloatType type = new FloatType();
		final int n = sigmaSmaller.length;
		final BlockProcessor< float[], float[] > p0 = new ConvolveProcessors.ConvolveFloat( gaussKernels( sigmaSmaller ) );
		final BlockProcessor< float[], float[] > p1 = new ConvolveProcessors.ConvolveFloat( gaussKernels( sigmaLarger ) );
		final BlockProcessor< float[], float[] > proc = new DoGProcessor<>( type, p0, p1 );
		return new DefaultUnaryBlockOperator<>( type, type, n, n, proc );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > dogDouble( final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		final DoubleType type = new DoubleType();
		final int n = sigmaSmaller.length;
		final BlockProcessor< double[], double[] > p0 = new ConvolveProcessors.ConvolveDouble( gaussKernels( sigmaSmaller )  );
		final BlockProcessor< double[], double[] > p1 = new ConvolveProcessors.ConvolveDouble( gaussKernels( sigmaLarger ) );
		final BlockProcessor< double[], double[] > proc = new DoGProcessor<>( type, p0, p1 );
		return new DefaultUnaryBlockOperator<>( type, type, n, n, proc );
	}
}
