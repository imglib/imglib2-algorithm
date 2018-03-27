package net.imglib2.algorithm.gradient;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link PartialDerivative}.
 *
 * @author Matthias Arzt
 */
public class PartialDerivativeTest
{

	private final double DELTA = 0.0001;

	private final RandomAccessibleInterval< DoubleType > testImage =
			createImage2dMinSizeValues( /* min: */ 0, 0, /* size: */ 5, 3, /* values: */
					0.0, 0.0, 0.0, 0.0, 0.0,
					0.0, 2.0, 0.0, -1.0, 0.0,
					0.0, 0.0, 5.0, 0.0, 0.0
			);

	private final RandomAccessibleInterval< DoubleType > centralDifferenceExpected =
			createImage2dMinSizeValues( /* min: */ 1, 0, /* size: */ 3, 3, /* values: */
					0.0, 0.0, 0.0,
					0.0, -1.5, 0.0,
					2.5, 0.0, -2.5
			);

	private final RandomAccessibleInterval< DoubleType > backwardDifferenceExpected =
			createImage2dMinSizeValues( /* min: */ 1, 0, /* size: */ 4, 3, /* values: */
					0.0, 0.0, 0.0, 0.0,
					2.0, -2.0, -1.0, 1.0,
					0.0, 5.0, -5.0, 0.0
			);

	public static RandomAccessibleInterval< DoubleType > createImage2dMinSizeValues( long minX, long minY, long sizeX, long sizeY, double... values )
	{
		Interval interval = Intervals.createMinSize( minX, minY, sizeX, sizeY );
		return Views.translate( ArrayImgs.doubles( values, Intervals.dimensionsAsLongArray( interval ) ),
				Intervals.minAsLongArray( interval ) );
	}

	@Test
	public void testGradientCentralDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( centralDifferenceExpected );
		PartialDerivative.gradientCentralDifference( data, result, 0 );
		assertImagesEqual( centralDifferenceExpected, result );
	}

	@Test
	public void testGradientBackwardDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( backwardDifferenceExpected );
		PartialDerivative.gradientBackwardDifference( data, result, 0 );
		assertImagesEqual( backwardDifferenceExpected, result );
	}

	@Test
	public void testForwardDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > expected = Views.translate( backwardDifferenceExpected, -1, 0 );
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( expected );
		PartialDerivative.gradientForwardDifference( data, result, 0 );
		assertImagesEqual( expected, result );
	}

	private void assertImagesEqual( RandomAccessibleInterval< DoubleType > expected, RandomAccessibleInterval< DoubleType > actual )
	{
		assertTrue( Intervals.equals( expected, actual ) );
		assertArrayEquals( imageAsArray( expected ), imageAsArray( actual ), DELTA );
	}

	private double[] imageAsArray( RandomAccessibleInterval< DoubleType > image )
	{
		List< Double > values = new ArrayList<>();
		Views.iterable( image ).forEach( x -> values.add( x.getRealDouble() ) );
		return values.stream().mapToDouble( x -> x ).toArray();
	}

	private RandomAccessibleInterval< DoubleType > emptyArrayImg( Interval interval )
	{
		return Views.translate( ArrayImgs.doubles( Intervals.dimensionsAsLongArray( interval ) ), Intervals.minAsLongArray( interval ) );
	}
}
