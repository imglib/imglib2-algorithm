package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.util.Util;

/**
 * Thin lines preserving the Euler number using the thinning algorithm # 1
 * described in Guo, "Parallel Thinning with Two Subiteration Algorithms",
 * <i>Communications of the ACM</i>, Vol 32 #3, page 359. The result generally
 * preserves the lines in an image while eroding their thickness.
 * <p>
 * For example:
 * 
 * <pre>
 * 1 1 1 1 1    1 1 1 1 1
 * 1 1 0 1 0    1 1 0 1 0
 * 1 1 1 1 0 -> 1 1 1 0 0
 * 1 1 0 1 0    1 0 0 1 0
 * 1 0 0 0 0    1 0 0 0 0
 * </pre>
 * 
 * Notice that this implementation decides to returns the above result, instead
 * of this following one, which is equivalently valid:
 * 
 * <pre>
 * 1 1 1 1 1
 * 1 1 0 1 0
 * 1 1 1 1 0
 * 1 0 0 0 0
 * 1 0 0 0 0
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Thin
{
	static public < T extends BooleanType< T > > Img< T > thin( final Img< T > source )
	{
		return new Thin2().calculate( new Thin1().calculate( source ) );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	static public < T extends BooleanType< T > > void thin( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		long[] targetDims = new long[] { target.dimension( 0 ), target.dimension( 1 ) };
		final T extendedVal = target.firstElement().createVariable();
		extendedVal.set( false );

		final ImgFactory< T > factory;
		if ( extendedVal instanceof NativeType )
			factory = Util.getArrayOrCellImgFactory( target, ( NativeType ) extendedVal );
		else
			factory = new ListImgFactory< T >();
		Img< T > temp = factory.create( targetDims, extendedVal );

		new Thin1().calculate( source, temp );
		new Thin2().calculate( temp, target );
	}

}
