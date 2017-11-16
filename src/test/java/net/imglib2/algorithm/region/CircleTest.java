package net.imglib2.algorithm.region;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.Point;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

public class CircleTest
{

	@Test
	public void testIterateOnce()
	{
		final ArrayImg< UnsignedShortType, ShortArray > img = ArrayImgs.unsignedShorts( 512l, 512l );

		final Point center = new Point( 255l, 255l );
		for ( long r = 1l; r < 256l; r++ )
		{
			Circles.inc( img, center, r );
			
			final CircleCursor< UnsignedShortType > cursor = new CircleCursor<>( img, center, r );
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				assertEquals( "Pixel at coordinates " + Util.printCoordinates( cursor ) + " has not been iterated one time.", 1, cursor.get().get() );
			}
		}
	}

	@Test
	public void testDistanceToCenter()
	{
		final ArrayImg< UnsignedShortType, ShortArray > img = ArrayImgs.unsignedShorts( 512l, 512l );

		final Point center = new Point( 255l, 255l );
		final long[] pos = new long[ img.numDimensions() ];

		for ( long r = 1l; r < 256l; r++ )
		{
			final CircleCursor< UnsignedShortType > cursor = new CircleCursor<>( img, center, r );
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

				assertEquals( "Circle coordinates " + Util.printCoordinates( cursor ) + " is too far from expected radius " + r + ".",
						r, dist, 0.5 );
			}
		}
	}
}
