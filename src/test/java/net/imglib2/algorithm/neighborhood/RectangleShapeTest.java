package net.imglib2.algorithm.neighborhood;

/**
 * Test {@link RectangleShape}.
 * 
 * @author Jonathan Hale (University of Konstanz)
 */
public class RectangleShapeTest extends AbstractShapeTest
{

	private static final int SPAN = 3;

	@Override
	protected Shape createShape()
	{
		return new RectangleShape( SPAN, false );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		for ( int i = 0; i < pos.length; ++i )
		{
			if ( pos[ i ] < center[ i ] - SPAN || pos[ i ] > center[ i ] + SPAN ) { return false; }
		}

		return true;
	}

}
