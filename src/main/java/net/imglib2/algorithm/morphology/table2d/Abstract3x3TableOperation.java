package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Performs a binary operation on a 3x3 2-D neighborhood using a table of truth
 * values to drive the operation.
 * 
 * @author Lee Kamentsky
 * @author Leon Yang
 */
public abstract class Abstract3x3TableOperation
{
	final static private RectangleShape shape = new RectangleShape( 1, false );

	/**
	 * The index to the table that's returned is built by examining each pixel
	 * and accumulating 2^pixel number. The pixels are numbered like this:
	 * 
	 * <pre>
	 * 0 1 2
	 * 3 4 5
	 * 6 7 8
	 * </pre>
	 * 
	 * @return a 512-element table that holds the truth values for each 3x3
	 *         combination
	 */
	protected abstract boolean[] getTable();

	/**
	 * Gets default value for neighborhood pixels outside of the image. For
	 * example, an erosion operator should return true because otherwise pixels
	 * at the border would be eroded without evidence that they were not in the
	 * interior, and vice-versa for dilate.
	 * 
	 * @return the extended value for this operation
	 */
	protected abstract boolean getExtendedValue();

	protected < T extends BooleanType< T > > Img< T > calculate( final Img< T > source )
	{
		final Img< T > target = source.factory().create( source, source.firstElement().copy() );
		final T extendedVal = source.firstElement().createVariable();
		extendedVal.set( getExtendedValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T > > extended = Views.extendValue( source, extendedVal );
		calculate( extended, target );
		return target;
	}

	protected < T extends BooleanType< T > > void calculate( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		final RandomAccessible< Neighborhood< T > > accessible = shape.neighborhoodsRandomAccessible( source );
		final RandomAccess< Neighborhood< T > > randomAccess = accessible.randomAccess( target );
		final Cursor< T > cursorTarget = target.cursor();
		final boolean[] table = getTable();
		while ( cursorTarget.hasNext() )
		{
			final T targetVal = cursorTarget.next();
			randomAccess.setPosition( cursorTarget );
			final Neighborhood< T > neighborhood = randomAccess.get();
			final Cursor< T > nc = neighborhood.cursor();
			int idx = 0;
			// Assume that the neighborhood obtained is of FlatIterationOrder,
			// and assemble the index using bitwise operations.
			while ( nc.hasNext() )
			{
				idx <<= 1;
				idx |= nc.next().get() ? 1 : 0;
			}

			targetVal.set( table[ idx ] );
		}
	}

}
