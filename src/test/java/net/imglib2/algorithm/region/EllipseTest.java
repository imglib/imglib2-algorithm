package net.imglib2.algorithm.region;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.Point;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class EllipseTest
{

	@Test
	public void testDistanceToCenter()
	{
		final ArrayImg< UnsignedShortType, ShortArray > img = ArrayImgs.unsignedShorts( 512l, 512l );

		final Point center = new Point( 255l, 255l );
		final long[] pos = new long[ img.numDimensions() ];

		for ( long r = 1l; r < 256l; r++ )
		{
			final EllipseCursor< UnsignedShortType > cursor = new EllipseCursor<>( img, center, r, r );
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.localize( pos );

				double d2 = 0.;
				for ( int d = 0; d < img.numDimensions(); d++ )
				{
					final double dx = center.getDoublePosition( d ) - cursor.getDoublePosition( d );
					d2 += dx * dx;
				}
				final double dist = Math.sqrt( d2 );

				assertEquals( r, dist, 0.5 );
			}
		}
	}
}
