package net.imglib2.algorithm.morphology.table2d;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Applies the interaction rules from the Game of Life, an example of a cellular
 * automaton.
 * <p>
 * More specifically:
 * 
 * <pre>
 * 1. If a pixel has less than 2 neighbors are true, set to false.
 * 2. If a pixel has 2 or 3 neighbors are true, do not change.
 * 3. If a pixel has more than 3 neighbors are true, set to false.
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Life extends Abstract3x3TableOperation
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

	static public < T extends BooleanType< T > > Img< T > life( final Img< T > source )
	{
		return new Life().calculate( source );
	}

	static public < T extends BooleanType< T > > void life( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Life().calculate( source, target );
	}

	final static private boolean[] table = {
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, false,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false
	};
}
