package net.imglib2.algorithm.dog;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.util.TestImages;
import net.imglib2.img.Img;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tests {@link DifferenceOfGaussian}.
 */
public class DifferenceOfGaussianTest
{

	private ExecutorService executorService;

	private final Interval interval = Intervals.createMinMax( -5, -5, 5, 5 );

	private final RandomAccessible<DoubleType> input = TestImages.dirac2d();

	private final Img<DoubleType> expected = TestImages.doubles2d( interval,
			(x, y) -> gauss2d( 4, x, y ) - gauss2d( 2, x, y ) );

	@Before
	public void before() {
		executorService = Executors.newFixedThreadPool( 8 );
	}

	@After
	public void after() {
		executorService.shutdown();
	}

	@Test
	public void testDoG() {
			RandomAccessibleInterval<DoubleType> output = TestImages.createImg( new DoubleType(), interval );
			DifferenceOfGaussian.DoG( new double[]{ 2, 2 }, new double[]{4, 4}, input, output, executorService );
			ImgLib2Assert.assertImageEqualsRealType( expected, output, 0.0001 );
	}

	@Test
	public void testDoG_WithTmpImage() {
		RandomAccessibleInterval<DoubleType> output = TestImages.createImg( new DoubleType(), interval );
		RandomAccessibleInterval<DoubleType> tmp = TestImages.createImg( new DoubleType(), interval );
		DifferenceOfGaussian.DoG( new double[]{ 2, 2 }, new double[]{4, 4}, input, tmp, output, executorService );
		ImgLib2Assert.assertImageEqualsRealType( expected, output, 0.0001 );
	}

	private double gauss2d( double sigma, int x, int y )
	{
		return 1 / (sigma * sigma * 2 * Math.PI ) * Math.exp( - 0.5 * (x * x + y * y) / (sigma * sigma) );
	}
}
