package net.imglib2.algorithm.neighborhood;

/**
 * Test {@link HyperSphereShape}.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class HypersphereShapeTest extends AbstractShapeTest
{

	private static final int RADIUS = 3;

	@Override
	protected Shape createShape()
	{
		return new HyperSphereShape( RADIUS );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		double squlen = 0;
		for ( int d = 0; d < pos.length; ++d )
			squlen += ( pos[ d ] - center[ d ] ) * ( pos[ d ] - center[ d ] );
		return squlen <= RADIUS * RADIUS;
	}

}
