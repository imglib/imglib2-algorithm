package net.imglib2.algorithm.gauss3;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
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
}
