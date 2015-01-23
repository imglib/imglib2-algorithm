package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.algorithm.region.localneighborhood.Shape;

public class PeriodicLineShapeTest extends AbstractShapeTest
{

	private static final int[] INCREMENTS = new int[] { -2, 1, 0 };

	private static final long SPAN = 2;

	@Override
	protected Shape createShape()
	{
		return new PeriodicLineShape( SPAN, INCREMENTS );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		long previousN = Long.MAX_VALUE;
		for ( int d = 0; d < center.length; d++ )
		{
			if ( INCREMENTS[ d ] == 0 )
			{
				if ( pos[ d ] != center[ d ] ) { return false; }
			}
			else
			{
				final long dx = pos[ d ] - center[ d ];
				final long rem = dx % INCREMENTS[ d ];
				if ( rem != 0 ) { return false; }
				final long n = dx / INCREMENTS[ d ];
				if ( n < -SPAN || n > SPAN ) { return false; }
				if ( previousN == Long.MAX_VALUE )
				{
					previousN = n;
				}
				else if ( previousN != n ) { return false; }
			}
		}
		return true;
	}

}
