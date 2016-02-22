package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Removes all pixels except those that are the branchpoints of a skeleton. This
 * operation should be applied to an image after skeletonizing. It leaves only
 * those pixels that are at the intersection of branches.
 * <p>
 * For example:
 * 
 * <pre>
 * 1 0 0 0 0    ? 0 0 0 0
 * 0 1 0 0 0    0 0 0 0 0
 * 0 0 1 0 0 -> 0 0 1 0 0
 * 0 1 0 1 0    0 0 0 0 0
 * 1 0 0 0 1    ? 0 0 0 ?
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Branchpoints extends Abstract3x3TableOperation
{
	@Override
	protected boolean[] getTable()
	{
		return table;
	}

	@Override
	protected boolean getExtendedValue()
	{
		return false;
	}

	static public < T extends BooleanType< T > > Img< T > branchpoints( final Img< T > source )
	{
		return new Branchpoints().calculate( source );
	}

	static public < T extends BooleanType< T > > void branchpoints( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Branchpoints().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, false, true, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, true, false, true, true, true, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, false, true, false, false,
			true, true, true, true, true, true, true, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, false, true, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, true, false, true, true, true, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, true, true, true, true,
			false, false, true, false, true, true, true, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, false, true, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, true, false, true, true, true, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, true, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false
	};
}
