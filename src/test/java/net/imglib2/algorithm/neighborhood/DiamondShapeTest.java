package net.imglib2.algorithm.neighborhood;

import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Shape;

public class DiamondShapeTest extends AbstractShapeTest
{

	private static final long RADIUS = 3;

	@Override
	protected Shape createShape()
	{
		return new DiamondShape( RADIUS );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		long cityblock = 0;
		for ( int d = 0; d < pos.length; d++ )
		{
			cityblock += Math.abs( center[ d ] - pos[ d ] );
		}
		return cityblock <= RADIUS;
	}

}
