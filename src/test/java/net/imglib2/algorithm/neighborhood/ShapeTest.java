package net.imglib2.algorithm.neighborhood;

import static org.junit.Assert.assertTrue;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test default implementations of {@link Shape}.
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public class ShapeTest
{

	protected Shape shape;

	protected ArrayImg< UnsignedShortType, ShortArray > img;

	@Before
	public void setUp() throws Exception
	{
		final long[] dims = new long[] { 10, 10, 10 };
		this.img = ArrayImgs.unsignedShorts( dims );
		this.shape = new Shape()
		{

			@Override
			public < T > IterableInterval< Neighborhood< T > > neighborhoods( RandomAccessibleInterval< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

			@Override
			public < T > RandomAccessible< Neighborhood< T > > neighborhoodsRandomAccessible( RandomAccessible< T > source )
			{
				final RectangleNeighborhoodFactory< T > f = RectangleNeighborhoodUnsafe.< T >factory();
				final Interval spanInterval = new FinalInterval( new long[] { -3, -3, -3 }, new long[] { 3, 3, 3 } );
				return new NeighborhoodsAccessible<>( source, spanInterval, f );
			}

			@Override
			public < T > IterableInterval< Neighborhood< T > > neighborhoodsSafe( RandomAccessibleInterval< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

			@Override
			public < T > RandomAccessible< Neighborhood< T > > neighborhoodsRandomAccessibleSafe( RandomAccessible< T > source )
			{
				// Not used by default getStructuringElementBoundingBox()
				return null;
			}

		};
	}

	@Test
	public void testStructuringElementBoundingBox()
	{
		Interval boundingBox = shape.getStructuringElementBoundingBox(
				img.numDimensions() );
		assertTrue( Intervals.equals( boundingBox, new FinalInterval(
				new long[] { -3, -3, -3 }, new long[] { 3, 3, 3 } ) ) );
	}
}
