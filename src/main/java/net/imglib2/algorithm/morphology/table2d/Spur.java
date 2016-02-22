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
 * Removes spur pixels, i.e., pixels that have exactly one 8-connected neighbor.
 * This operation essentially removes the endpoints of lines.
 * <p>
 * For example:
 * 
 * <pre>
 * 0 0 0 0    0 0 0 0
 * 0 1 0 0    0 0 0 0
 * 0 0 1 0 -> 0 0 1 0
 * 1 1 1 1    1 1 1 1
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Spur
{
	static public < T extends BooleanType< T > > Img< T > spur( final Img< T > source )
	{
		return new Spur2().calculate( new Spur1().calculate( source ) );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	static public < T extends BooleanType< T > > void spur( final RandomAccessible< T > source, final IterableInterval< T > target )
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

		new Spur1().calculate( source, temp );
		new Spur2().calculate( temp, target );
	}

}
