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
 * TODO: document supported types etc similar to Convolve
 */
public class DifferenceOfGaussian
{

	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final double k, final double... sigmaSmaller )
	{
		return DoG( ComputationType.AUTO, k, sigmaSmaller );
	}

	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		return DoG( ComputationType.AUTO, sigmaSmaller, sigmaLarger );
	}

	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final ComputationType computationType, final double k, final double... sigmaSmaller )
	{
		final double[] sigmaLarger = new double[ sigmaSmaller.length ];
		Arrays.setAll( sigmaLarger, i -> sigmaSmaller[ i ] * k );
		return DoG( computationType, sigmaSmaller, sigmaLarger );
	}

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
	 * TODO: javadoc
	 * <p>
	 * Create a {@code UnaryBlockOperator} to compute DoG with the given sigmas...
	 * TODO: see Convolve.createOperator javadoc
	 *
	 * @param type
	 * 		instance of the input type
	 * @param computationType
	 * 		specifies in which precision intermediate values should be
	 * 		computed. For {@code AUTO}, the type that can represent the
	 * 		input/output type without loss of precision is picked. That is,
	 *        {@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
	 * 		DOUBLE} for u32, i64, f64.
	 * @param sigmaSmaller
	 *      sigmas in each dimension for smaller Gaussian
	 * @param sigmaLarger
	 *      sigmas in each dimension for larger Gaussian
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
