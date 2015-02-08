package net.imglib2.algorithm.neighborhood;

import net.imglib2.algorithm.neighborhood.DiamondTipsShape;
import net.imglib2.algorithm.neighborhood.Shape;

public class DiamondTipsShapeTest extends AbstractShapeTest
{

	private static final long RADIUS = 3;

	@Override
	protected Shape createShape()
	{
		return new DiamondTipsShape( RADIUS );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		for ( int d = 0; d < center.length; d++ )
		{
			if ( Math.abs( pos[ d ] - center[ d ] ) == RADIUS )
			{
				boolean aligned = true;
				for ( int id = 0; id < center.length; id++ )
				{
					if ( id == d )
					{
						continue;
					}
					if ( pos[ id ] != center[ id ] )
					{
						aligned = false;
						break;
					}
				}
				if ( aligned ) { return true; }
			}
		}
		return false;
	}
}
