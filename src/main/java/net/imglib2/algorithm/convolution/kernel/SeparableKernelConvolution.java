package net.imglib2.algorithm.convolution.kernel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.LineConvolution;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * Convolution with a customizable separable kernel.
 *
 * @author Matthias Arzt
 */
public class SeparableKernelConvolution
{
	/**
	 * Return an object, that performs the separable convolution with the given
	 * kernel. It additionally allows to set the
	 * {@link java.util.concurrent.ExecutorService} to use or to query for the
	 * required input image size, ...
	 * <p>
	 * A small example how it works:
	 * <pre>
	 * {@code
	 * double[][] values = { { 1, 2, 1 }, { -1, 0, 1 } }; int[] center = { 1, 1 };
	 * Kernel1D[] sobelKernel = Kernel1D.asymmetric( values, center );
	 * SeparableKernelConvolution.convolution( sobelKernel ).process( Views.extendBorder( inputImage ), outputImage );
	 * }
	 * </pre>
	 *
	 * @see Convolution
	 */
	public static Convolution< NumericType< ? > > convolution( final Kernel1D... kernels )
	{
		final List< Convolution< NumericType< ? > > > steps = IntStream.range( 0, kernels.length )
				.mapToObj( i -> convolution1d( kernels[ i ], i ) )
				.collect( Collectors.toList() );
		return Convolution.concat( steps );
	}

	/**
	 * Apply a convolution only in one dimension. For example calculate central
	 * differences:
	 * <pre>
	 * {@code
	 * double[] values = { -0.5, 0, 0.5 }; int center = 1;
	 * Kernel1D[] sobelKernel = Kernel1D.asymmetric( values, center );
	 * SeparableKernelConvolution.convolution1d( sobelKernel, 0 ).process( Views.extendBorder( inputImage ), outputImage );
	 * }
	 * </pre>
	 *
	 * @see Convolution
	 */
	public static Convolution< NumericType< ? > > convolution1d( final Kernel1D kernel, final int direction )
	{
		return new LineConvolution<>( new KernelConvolverFactory( kernel ), direction );
	}

	/**
	 * Convolve source with a separable kernel and write the result to output.
	 * In-place operation (source==target) is supported.
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 *
	 * @param kernels
	 *            an array containing kernels for every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (use e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)})
	 *            the required source interval.
	 * @param target
	 *            target image.
	 */
	public static void convolve( final Kernel1D[] kernels,
			final RandomAccessible< ? extends NumericType< ? > > source,
			final RandomAccessibleInterval< ? extends NumericType< ? > > target )
	{
		convolution( kernels ).process( source, target );
	}
}
