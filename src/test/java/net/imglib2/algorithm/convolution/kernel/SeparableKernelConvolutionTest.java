package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.ImgLib2TestUtils;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
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

	private final ExecutorService executorService = Executors.newFixedThreadPool(1);

	@Test
	public void testCalculateConvolutionSourceInterval() {
		Kernel1D kernel = Kernel1D.asymmetric(new double[4], 1);
		Interval result = SeparableKernelConvolution.convolve1dSourceInterval( kernel, Intervals.createMinMax( 1, 0, 5, 7, 10, 5 ), 1 );
		Interval expected = Intervals.createMinMax(1,-2,5,7,11,5);
		assertTrue(Intervals.equals(expected, result));
	}

	@Test
	public void test1DConvolution() throws IncompatibleTypeException {
		double[][] kernels = { {1.0, 2.0, 3.0, 4.0} };
		int[] centers = {2};
		testSeparableConvolution(kernels, centers);
	}

	@Test
	public void test2DConvolution() throws IncompatibleTypeException {
		double[][] kernels = { {1.0, 0.0, -1.0}, {1.0, 2.0, 1.0} };
		int[] centers = {1, 1};
		testSeparableConvolution(kernels, centers);
	}

	@Test
	public void test3DConvolution() throws IncompatibleTypeException {
		double[][] kernels = { {1, 6, 8, 6}, {2, 7, -5}, {1, 2} };
		int[] centers = {0, 3, 4};
		testSeparableConvolution(kernels, centers);
	}

	private void testSeparableConvolution(double[][] kernels, int[] centers) throws IncompatibleTypeException {
		Kernel1D[] kernels1d = Kernel1D.asymmetric(kernels, centers);
		// NB: The result of a convolution between a dirac signal and a kernel is again the kernel.
		RandomAccessible<DoubleType> dirac = getDirac(kernels1d.length);
		RandomAccessibleInterval<DoubleType> expected = getKernel(kernels1d);
		RandomAccessibleInterval<DoubleType> result = ImgLib2TestUtils.createImg(expected);
		SeparableKernelConvolution.convolve(kernels1d, dirac, result, executorService);
		ImgLib2TestUtils.assertImagesEqual(expected, result, 0.0);
	}

	private RandomAccessible<DoubleType> getDirac(int n) {
		long[] dimensions = IntStream.range(0, n).mapToLong(k -> 1).toArray();
		RandomAccessibleInterval<DoubleType> one = ArrayImgs.doubles(new double[]{1}, dimensions);
		return Views.extendZero(one);
	}

	private RandomAccessibleInterval<DoubleType> getKernel(Kernel1D[] kernels) {
		long[] kernelSizes = Stream.of(kernels).mapToLong(kernel -> kernel.size()).toArray();
		double[][] fullKernels = Stream.of(kernels).map(kernel -> kernel.fullKernel()).toArray(double[][]::new);
		Img<DoubleType> result = ArrayImgs.doubles(kernelSizes);
		Cursor<DoubleType> cursor = result.cursor();
		while(cursor.hasNext()) {
			cursor.next().setReal(
					IntStream.range(0, kernels.length)
					.mapToDouble(d -> fullKernels[d][cursor.getIntPosition(d)])
					.reduce(1, (a, b) -> a * b)
			);
		}
		long[] minusCenters = Stream.of(kernels).mapToLong(kernel -> kernel.min()).toArray();
		return Views.translate(result, minusCenters);
	}
}
