package net.imglib2.algorithm.gauss3;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class SeparableSymmetricConvolutionTest
{

	private ExecutorService service = Executors.newFixedThreadPool( 1 );

	private final double[][] halfKernels = new double[][] { { 8, 3, 1 }, { 2, 1 } };

	private final RandomAccessibleInterval< DoubleType > expected = Views.translate( ArrayImgs.doubles( new double[] {
			1, 3, 8, 3, 1,
			2, 6, 16, 6, 2,
			1, 3, 8, 3, 1
	}, 5, 3 ), -2, -1 );

	@Test
	public void testConvolve() throws IncompatibleTypeException
	{
		RandomAccessibleInterval< DoubleType > target = createImg( expected );
		SeparableSymmetricConvolution.convolve( halfKernels, getDirac( halfKernels.length ), target, service );
		ImgLib2Assert.assertImageEquals( expected, target );
	}

	@Test
	public void testConvolve2()
	{
		RandomAccessibleInterval< DoubleType > target = createImg( expected );
		ConvolverFactory< DoubleType, DoubleType > factory = FloatConvolverRealType.factory();
		SeparableSymmetricConvolution.convolve( halfKernels, getDirac( halfKernels.length ), target,
				factory, factory, factory, factory, new ArrayImgFactory<>(), new DoubleType(), service );
		ImgLib2Assert.assertImageEquals( expected, target );
	}

	@Test
	public void testConcolve1d()
	{
		double[] kernel = { 2, 1 };
		RandomAccessible< DoubleType > source = Views.extendBorder( ArrayImgs.doubles( new double[] { 1, 2, 0, 1, 3 }, 5 ) );
		RandomAccessibleInterval< DoubleType > target = ArrayImgs.doubles( new double[] { 5, 5, 3, 5 }, 4 );
		ConvolverFactory< DoubleType, DoubleType > factory = FloatConvolverRealType.factory();
		SeparableSymmetricConvolution.convolve1d( kernel, source, target, factory, service );
	}

	private RandomAccessible< DoubleType > getDirac( int n )
	{
		long[] dimensions = IntStream.range( 0, n ).mapToLong( k -> 1 ).toArray();
		RandomAccessibleInterval< DoubleType > one = ArrayImgs.doubles( new double[] { 1 }, dimensions );
		return Views.extendZero( one );
	}

	public static RandomAccessibleInterval< DoubleType > createImg( Interval interval )
	{
		// TODO: better name, move to ArrayImgs class
		Img< DoubleType > image = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( interval ) );
		return Views.translate( image, Intervals.minAsLongArray( interval ) );
	}
}
