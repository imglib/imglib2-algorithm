package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Removes pixels that are otherwise surrounded by others (4 connected). The
 * effect is to leave the perimeter of a solid object.
 * <p>
 * For example:
 * 
 * <pre>
 * 0 1 0    0 1 0
 * 1 1 1 -> 1 0 1
 * 0 1 0    0 1 0
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Remove extends Abstract3x3TableOperation
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

	static public < T extends BooleanType< T > > Img< T > remove( final Img< T > source )
	{
		return new Remove().calculate( source );
	}

	static public < T extends BooleanType< T > > void remove( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Remove().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false
	};
}
