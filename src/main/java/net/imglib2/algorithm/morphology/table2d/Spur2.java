package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Helper class for {@link Spur}.
 * 
 * @author Lee Kamentsky
 */
class Spur2 extends Abstract3x3TableOperation
{
	@Override
	protected boolean[] getTable()
	{
		return table;
	}

	@Override
	protected boolean getExtendedValue()
	{
		return true;
	}

	static public < T extends BooleanType< T > > Img< T > spur2( final Img< T > source )
	{
		return new Spur2().calculate( source );
	}

	static public < T extends BooleanType< T > > void spur2( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Spur2().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, true, true, true, true, true,
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
			false, true, true, true, true, true, true, true,
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
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true
	};
}
