package net.imglib2.algorithm.neighborhood;

import static org.junit.Assert.assertArrayEquals;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.Test;

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

	@Test
	public void testJumpFwd()
	{
		// cursor which will be moved via .jumpFwd()
		final Cursor< Neighborhood< UnsignedShortType >> cNeigh1 =
				shape.neighborhoodsSafe( img ).localizingCursor();
		// cursor which will be moved via .fwd() for reference
		final Cursor< Neighborhood< UnsignedShortType >> cNeigh2 =
				shape.neighborhoodsSafe( img ).localizingCursor();

		final long[] dims1 = new long[ cNeigh1.numDimensions() ];
		final long[] dims2 = new long[ cNeigh2.numDimensions() ];

		while ( cNeigh1.hasNext() )
		{
			cNeigh1.jumpFwd( 1 );
			cNeigh2.fwd();

			cNeigh1.localize( dims1 );
			cNeigh2.localize( dims2 );
			assertArrayEquals( "Incorrect position for jumpFwd()", dims2, dims1 );
		}
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
