package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.algorithm.region.localneighborhood.Shape;

public class HorizontalLineShapeTest extends AbstractShapeTest
{

	private static final long SPAN = 3;

	private static final int DIM = 1;

	private static final boolean SKIPCENTER = false;

	@Override
	protected Shape createShape()
	{
		return new HorizontalLineShape( SPAN, DIM, SKIPCENTER );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		for ( int d = 0; d < center.length; d++ )
		{
			if ( d == DIM )
			{
				if ( Math.abs( pos[ d ] - center[ d ] ) > SPAN ) { return false; }
			}
			else
			{
				if ( pos[ d ] != center[ d ] ) { return false; }
			}
		}
		return true;
	}

}
