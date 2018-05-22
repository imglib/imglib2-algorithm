package net.imglib2.algorithm.convolution;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
// TODO: Find a better place for this class
public class ImgLib2TestUtils {

	public static RandomAccessibleInterval<DoubleType> createImg(Interval interval) {
		// TODO: better name, move to ArrayImgs class
		Img<DoubleType> image = ArrayImgs.doubles(Intervals.dimensionsAsLongArray(interval));
		return Views.translate(image, Intervals.minAsLongArray(interval));
	}

	public static void assertImagesEqual(RandomAccessibleInterval<DoubleType> expected, RandomAccessibleInterval<DoubleType> actual, double delta) {
		assertTrue(Intervals.equals(expected, actual));
		Views.interval(Views.pair(expected, actual), expected).forEach(p -> assertEquals(p.getA().getRealDouble(), p.getB().getRealDouble(), delta));
	}

	public static <S extends ComplexType<S>, T extends ComplexType<T>> double psnr(
			RandomAccessibleInterval<S> expected, RandomAccessibleInterval<T> actual)
	{
		double meanSquareError = meanSquareError(expected, actual);
		if(meanSquareError == 0.0)
			return Float.POSITIVE_INFINITY;
		return (20 * Math.log10(max(expected)) - 10 * Math.log10(meanSquareError));
	}

	private static <S extends ComplexType<S>, T extends ComplexType<T>> double meanSquareError(
			RandomAccessibleInterval<S> a, RandomAccessibleInterval<T> b)
	{
		DoubleType sum = new DoubleType(0.0f);
		Views.interval(Views.pair(a, b), a).forEach(x -> sum.set(sum.get() + sqr(x.getA().getRealDouble() - x.getB().getRealDouble())));
		return sum.get() / Intervals.numElements(a);
	}

	private static <T extends ComplexType<T>> double max(RandomAccessibleInterval<T> a) {
		IntervalView<T> interval = Views.interval(a, a);
		T result = interval.firstElement().createVariable();
		interval.forEach(x -> result.setReal(Math.max(result.getRealDouble(), x.getRealDouble())));
		return result.getRealDouble();
	}

	private static double sqr( double v )
	{
		return v * v;
	}
}
