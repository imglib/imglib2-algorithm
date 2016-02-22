package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

/**
 * Dilates the exteriors of objects where that dilation does not 8-connect the
 * object with another. The image is labeled and the labeled objects are filled.
 * Unlabeled points adjacent to uniquely labeled points change from background
 * to foreground.
 * <p>
 * For example:
 * 
 * <pre>
 * 0 0 0    ? ? ?
 * 0 0 0 -> ? 1 ?
 * 0 0 1    ? ? ?
 * 
 * 1 0 0    ? ? ?
 * 0 0 0 -> ? 0 ?
 * 0 0 1    ? ? ?
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Thicken extends Abstract3x3TableOperation
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

	static public < T extends BooleanType< T > > Img< T > thicken( final Img< T > source )
	{
		return new Thicken().calculate( source );
	}

	static public < T extends BooleanType< T > > void thicken( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Thicken().calculate( source, target );
	}

	final static private boolean[] table = {
			false, true, true, true, true, false, true, true,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			false, false, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			false, false, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			false, false, false, false, false, false, false, false,
			false, false, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, false, false, false, false, false, false,
			true, true, true, true, false, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, false, true, true, true, false, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true
	};
}
