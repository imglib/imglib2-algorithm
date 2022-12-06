package net.imglib2.algorithm.binary;

import net.imglib2.algorithm.util.TestImages;
import net.imglib2.img.Img;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link Thresholder}.
 *
 * @author Matthias Arzt
 */
public class ThresholderTest
{
	private final Img<DoubleType> input = TestImages.doubles2d( 100, 100, ( x, y ) ->  x );

	private final int numThreads = 4;

	private final double threshold = 50;

	@Test
	public void testThresholdAbove()
	{
		boolean above = true;
		Img<BitType> result = Thresholder.threshold( input, new DoubleType( threshold ), above, numThreads );
		Img<BitType> expected = TestImages.bits2d( 100, 100, (x, y) -> x > threshold );
		assertNotNull( result );
		ImgLib2Assert.assertImageEquals( expected, result );
	}

	@Test
	public void testThresholdBelow()
	{
		boolean above = false;
		Img<BitType> result = Thresholder.threshold( input, new DoubleType( 50 ), above, numThreads );
		Img<BitType> expected = TestImages.bits2d( 100, 100, (x, y) -> x < threshold );
		assertNotNull( result );
		ImgLib2Assert.assertImageEquals( expected, result );
	}

}
