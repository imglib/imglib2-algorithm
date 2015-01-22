package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.algorithm.region.localneighborhood.Shape;

public class CenteredRectangleShapeTest extends AbstractShapeTest
{
	final static int[] span = new int[] { 2, 3, 1 };

	@Override
	protected Shape createShape()
	{
		final boolean skipCenter = false;
		return new CenteredRectangleShape( span, skipCenter );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		for ( int d = 0; d < img.numDimensions(); d++ )
		{
			if ( pos[ d ] > center[ d ] + span[ d ] || pos[ d ] < center[ d ] - span[ d ] ) { return false; }
		}
		return true;

	}
}
