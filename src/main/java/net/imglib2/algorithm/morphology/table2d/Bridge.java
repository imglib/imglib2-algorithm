package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Sets a pixel to 1 if it has two non-zero neighbors that are on opposite sides
 * of this pixel.
 * <p>
 * For example:
 *
 * <pre>
 * 1 0 0    1 0 0
 * 0 0 0 -> 0 1 0
 * 0 0 1    0 0 1
 * </pre>
 *
 * @author Lee Kamentsky
 */
public class Bridge extends Abstract3x3TableOperation
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

	static public < T extends BooleanType< T > > Img< T > bridge( final Img< T > source )
	{
		return new Bridge().calculate( source );
	}

	static public < T extends BooleanType< T > > void bridge( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Bridge().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, true, false, false,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			true, true, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			true, true, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true,
			false, false, false, false, true, true, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, true, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true
	};
}
