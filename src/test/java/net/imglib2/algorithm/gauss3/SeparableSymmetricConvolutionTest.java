package net.imglib2.algorithm.gauss3;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.ImgLib2TestUtils;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
// FIXME: really test SeparableSymmetricConvolution
public class SeparableSymmetricConvolutionTest {

	private ExecutorService service = Executors.newFixedThreadPool(1);

	private final double[][] halfKernels = new double[][]{{8, 3, 1}, {2, 1}};

	private final RandomAccessibleInterval<DoubleType> expected = Views.translate(ArrayImgs.doubles(new double[]{
			1, 3, 8, 3, 1,
			2, 6,16, 6, 2,
			1, 3, 8, 3, 1
	}, 5, 3), -2, -1);

	@Test
	public void testConvolve() throws IncompatibleTypeException {
		RandomAccessibleInterval<DoubleType> target = ImgLib2TestUtils.createImg(expected);
		SeparableKernelConvolution.convolve( Kernel1D.symmetric(halfKernels), getDirac(halfKernels.length), target, service);
		ImgLib2TestUtils.assertImagesEqual(expected, target, 0.0);
	}

	private RandomAccessible<DoubleType> getDirac(int n) {
		long[] dimensions = IntStream.range(0, n).mapToLong(k -> 1).toArray();
		RandomAccessibleInterval<DoubleType> one = ArrayImgs.doubles(new double[]{1}, dimensions);
		return Views.extendZero(one);
	}
}
