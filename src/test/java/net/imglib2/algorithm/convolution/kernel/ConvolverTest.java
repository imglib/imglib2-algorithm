package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Matthias Arzt
 */
public class ConvolverTest {

	private final double[] halfkernel = new double[]{1, 2, 3, 4, 5, 6, 7, 8};

	private final long lineLength = 100;
	private final long[] size = new long[]{lineLength + 2 * halfkernel.length};
	private final RandomAccessibleInterval<DoubleType> in = initIn();

	private Img<DoubleType> initIn() {
		Random random = new Random(42);
		Img<DoubleType> img = ArrayImgs.doubles(size);
		img.forEach(x -> x.setReal(random.nextDouble()));
		return img;
	}

	@Test
	public void testDoubleConvolver() {
		testConvolver(DoubleConvolverRealType.class);
	}

	@Test
	public void testFloatConvolver() {
		testConvolver(FloatConvolverRealType.class);
	}

	@Test
	public void testNativeConvolver() {
		testConvolver(ConvolverNativeType.class);
	}

	@Test
	public void testNumericConvolver() {
		testConvolver(ConvolverNumericType.class);
	}

	public void testConvolver( Class< ? extends Runnable > factoryClass ) {
		double[] halfKernel = {3, 2, 1};
		double[] in = {5, 0, 1, 0, 0, 0, 0, 0, 0, 0};
		double[] expected = {8, 2, 1, 0, 0, 0};
		int lineLength = in.length - 2 * (halfKernel.length - 1);
		double[] actual = new double[lineLength];

		Img<DoubleType> inImg = ArrayImgs.doubles(in, in.length);
		Img<DoubleType> outImg = ArrayImgs.doubles(actual, actual.length);
		ConvolverFactory<DoubleType, DoubleType> factory = KernelConvolverFactories.get(Kernel1D.symmetric(halfKernel),
				new DoubleType(), new DoubleType());
		factory.getConvolver( inImg.randomAccess(), outImg.randomAccess(), 0, lineLength).run();

		assertArrayEquals(expected, actual, 0.0001);
	}
}
