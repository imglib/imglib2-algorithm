package net.imglib2.algorithm.region;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.Point;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

public class EllipseTest
{

	@Test
	public void testIterateOnce()
	{
		final ArrayImg< UnsignedShortType, ShortArray > img = ArrayImgs.unsignedShorts( 512l, 512l );

		final Point center = new Point( 255l, 255l );

		for ( long rx = 1l; rx < 56l; rx++ )
		{
			for ( long ry = 1l; ry < 56l; ry++ )
			{
				Ellipses.inc( img, center, rx, ry );
				final EllipseCursor< UnsignedShortType > cursor = new EllipseCursor<>( img, center, rx, ry );
				while ( cursor.hasNext() )
				{
					cursor.fwd();
					assertEquals( "For radii (" + rx + ", " + ry + "), pixel at coordinates " +
							Util.printCoordinates( cursor ) + " has not been iterated one time.",
							1, cursor.get().get() );
				}
				// Reset image to 0.
				for ( final UnsignedShortType p : img )
					p.setZero();
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

				assertEquals( "Ellipse coordinates " + Util.printCoordinates( cursor ) + " is too far from expected radius " + r + ".",
						r, dist, 0.5 );
			}
		}
	}
}
