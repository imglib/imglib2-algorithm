package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Each pixel takes on the value of the majority that surround it (keep pixel
 * value to break ties).
 * <p>
 * For example:
 * 
 * <pre>
 * 1 1 1    1 1 1
 * 1 0 1 -> 1 1 1
 * 0 0 0    0 0 0
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Majority extends Abstract3x3TableOperation
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

	static public < T extends BooleanType< T > > Img< T > majority( final Img< T > source )
	{
		return new Majority().calculate( source );
	}

	static public < T extends BooleanType< T > > void majority( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Majority().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true
	};
}
