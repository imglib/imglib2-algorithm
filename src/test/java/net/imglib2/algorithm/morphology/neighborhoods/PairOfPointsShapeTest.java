package net.imglib2.algorithm.morphology.neighborhoods;

import java.util.Arrays;

import net.imglib2.algorithm.region.localneighborhood.Shape;

public class PairOfPointsShapeTest extends AbstractShapeTest
{

	private static final long[] OFFSET = new long[] { 3, 1, 0 };

	@Override
	protected Shape createShape()
	{
		return new PairOfPointsShape( OFFSET );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		if ( Arrays.equals( pos, center ) ) { return true; }
		for ( int d = 0; d < center.length; d++ )
		{
			final long dx = pos[ d ] - center[ d ];
			if ( dx != OFFSET[ d ] ) { return false; }
		}
		return true;
	}

}
