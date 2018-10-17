package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class SeparableKernelConvolutionTest
{

	private final ExecutorService executorService = Executors.newFixedThreadPool( 1 );

	@Test
	public void testCalculateConvolutionSourceInterval()
	{
		Kernel1D kernel = Kernel1D.asymmetric( new double[ 4 ], 1 );
		Interval result = SeparableKernelConvolution.convolution1d( kernel, 1 )
				.requiredSourceInterval( Intervals.createMinMax( 1, 0, 5, 7, 10, 5 ) );
		Interval expected = Intervals.createMinMax( 1, -2, 5, 7, 11, 5 );
		assertTrue( Intervals.equals( expected, result ) );
	}

	@Test
	public void test1DConvolution()
	{
		double[][] kernels = { { 1.0, 2.0, 3.0, 4.0 } };
		int[] centers = { 2 };
		testSeparableConvolution( Kernel1D.asymmetric( kernels, centers ) );

	}

	@Test
	public void test2DConvolution()
	{
		double[][] kernels = { { 1.0, 0.0, -1.0 }, { 1.0, 2.0, 1.0 } };
		testSeparableConvolution( Kernel1D.centralAsymmetric( kernels ) );

	}

	@Test
	public void test3DConvolution()
	{
		double[][] kernels = { { 1, 6, 8, 6 }, { 2, 7, -5 }, { 1, 2 } };
		int[] centers = { 0, 3, 4 };
		testSeparableConvolution( Kernel1D.asymmetric( kernels, centers ) );

	}

	private void testSeparableConvolution( Kernel1D[] kernels1d )
	{
		// NB: The result of a convolution between a dirac signal and a kernel is again the kernel.
		RandomAccessible< DoubleType > dirac = getDirac( kernels1d.length );
		RandomAccessibleInterval< DoubleType > expected = getKernel( kernels1d );
		RandomAccessibleInterval< DoubleType > result = createImg( expected );
		SeparableKernelConvolution.convolve( kernels1d, dirac, result );
		ImgLib2Assert.assertImageEquals( expected, result );
	}

	private RandomAccessible< DoubleType > getDirac( int n )
	{
		long[] dimensions = IntStream.range( 0, n ).mapToLong( k -> 1 ).toArray();
		RandomAccessibleInterval< DoubleType > one = ArrayImgs.doubles( new double[] { 1 }, dimensions );
		return Views.extendZero( one );
	}

	private RandomAccessibleInterval< DoubleType > getKernel( Kernel1D[] kernels )
	{
		long[] kernelSizes = Stream.of( kernels ).mapToLong( kernel -> kernel.size() ).toArray();
		double[][] fullKernels = Stream.of( kernels ).map( kernel -> kernel.fullKernel() ).toArray( double[][]::new );
		Img< DoubleType > result = ArrayImgs.doubles( kernelSizes );
		Cursor< DoubleType > cursor = result.cursor();
		while ( cursor.hasNext() )
		{
			cursor.next().setReal(
					IntStream.range( 0, kernels.length )
							.mapToDouble( d -> fullKernels[ d ][ cursor.getIntPosition( d ) ] )
							.reduce( 1, ( a, b ) -> a * b )
			);
		}
		long[] minusCenters = Stream.of( kernels ).mapToLong( kernel -> kernel.min() ).toArray();
		return Views.translate( result, minusCenters );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testTypeMisMatch()
	{
		Img< FloatType > source = ArrayImgs.floats( 1 );
		Img< ARGBType > target = ArrayImgs.argbs( 1 );
		SeparableKernelConvolution.convolution1d( Kernel1D.symmetric( 1 ), 0 )
				.process( Views.extendBorder( source ), target );
	}

	private static RandomAccessibleInterval< DoubleType > createImg( Interval interval )
	{
		Img< DoubleType > image = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( interval ) );
		return Views.translate( image, Intervals.minAsLongArray( interval ) );
	}

	@Test
	public void testPrecisionOfTemporaryImages() {
		// NB: This test assures that for convolution on UnsignedByteType,
		// the pixel type to store intermediate results can store 0.1, hence
		// is FloatType or DoubleType.
		final Kernel1D[] kernels = { Kernel1D.symmetric( 0.1 ), Kernel1D.symmetric( 10 ) };
		Img< UnsignedByteType > input = ArrayImgs.unsignedBytes( new byte[] { 1 }, 1, 1 );
		Img< UnsignedByteType > output = ArrayImgs.unsignedBytes( new byte[] { 0 }, 1, 1 );
		SeparableKernelConvolution.convolution( kernels ).process( input, output );
		ImgLib2Assert.assertImageEquals( input, output );
	}

	@Test
	public void testPreferredSourceType() {
		Convolution< NumericType< ? > > convolution = SeparableKernelConvolution.convolution( Kernel1D.symmetric( 1.0 ) );
		assertTrue( convolution.preferredSourceType( new UnsignedByteType() ) instanceof FloatType );
		assertTrue( convolution.preferredSourceType( new DoubleType() ) instanceof DoubleType );
		assertTrue( convolution.preferredSourceType( new FloatType() ) instanceof FloatType );
		assertTrue( convolution.preferredSourceType( new ARGBType() ) instanceof ARGBType );
	}
}
