package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.algorithm.blocks.convolve.Convolve.gaussKernels;
import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.Arrays;
import java.util.function.Function;

import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.convert.ClampType;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Difference of Gaussian.
 * <p>
 * TODO: document supported types etc similar to Convolve
 */
public class DoG
{

	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final Convolve.ComputationType computationType, final double k, final double... sigmaSmaller )
	{
		final double[] sigmaLarger = new double[ sigmaSmaller.length ];
		Arrays.setAll( sigmaLarger, i -> sigmaSmaller[ i ] * k );
		return DoG( computationType, sigmaSmaller, sigmaLarger );
	}

	public static < T extends NativeType< T > >
	Function< BlockSupplier< T >, UnaryBlockOperator< T, T > > DoG( final Convolve.ComputationType computationType, final double[] sigmaSmaller, final double[] sigmaLarger )
	{
		return s -> {
			final T type = s.getType();
			final int n = s.numDimensions();
			return createOperator( type, computationType, gaussKernels( n, sigmaSmaller ), gaussKernels( n, sigmaLarger ) );
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
	 * 		        {@code FLOAT} for u8, i8, u16, i16, i32, f32, and otherwise {@code
     * 		DOUBLE} for u32, i64, f64.
     * @param kernelsSmaller
     * 		kernel to apply in each dimension (smaller Gaussian)
     * @param kernelsLarger
     * 		kernel to apply in each dimension (larger Gaussian)
     * @param <T>
     * 		the input/output type
     *
     * @return {@code UnaryBlockOperator} to compute DoG for blocks of type {@code T}
     */
	public static < T extends NativeType< T > >
	UnaryBlockOperator< T, T > createOperator( final T type, final Convolve.ComputationType computationType, final Kernel1D[] kernelsSmaller, final Kernel1D[] kernelsLarger )
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
				? dogFloat( kernelsSmaller, kernelsLarger )
				: dogDouble( kernelsSmaller, kernelsLarger );
		return op.adaptSourceType( type, ClampType.NONE ).adaptTargetType( type, ClampType.CLAMP );
	}

	private static UnaryBlockOperator< FloatType, FloatType > dogFloat( final Kernel1D[] kernelsSmaller, final Kernel1D[] kernelsLarger )
	{
		final FloatType type = new FloatType();
		final int n = kernelsLarger.length;
		BlockProcessor p0 = new ConvolveProcessors.ConvolveFloat( kernelsSmaller );
		BlockProcessor p1 = new ConvolveProcessors.ConvolveFloat( kernelsLarger );
		// if we put it in a package other than convolve, use the following instead:
//			BlockProcessor p0 = Convolve.createOperator( type, ComputationType.AUTO, kernelsSmaller ).blockProcessor();
//			BlockProcessor p1 = Convolve.createOperator( type, ComputationType.AUTO, kernelsLarger ).blockProcessor();
		BlockProcessor proc = new DoGProcessor<>( type, p0, p1 );
		return new DefaultUnaryBlockOperator<>( type, type, n, n, proc );
	}

	private static UnaryBlockOperator< DoubleType, DoubleType > dogDouble( final Kernel1D[] kernelsSmaller, final Kernel1D[] kernelsLarger )
	{
		final DoubleType type = new DoubleType();
		final int n = kernelsLarger.length;
		BlockProcessor p0 = new ConvolveProcessors.ConvolveDouble( kernelsSmaller );
		BlockProcessor p1 = new ConvolveProcessors.ConvolveDouble( kernelsLarger );
		BlockProcessor proc = new DoGProcessor<>( type, p0, p1 );
		return new DefaultUnaryBlockOperator<>( type, type, n, n, proc );
	}
}
