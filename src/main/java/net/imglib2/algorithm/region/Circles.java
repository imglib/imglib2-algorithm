package net.imglib2.algorithm.region;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.operators.Add;

/**
 * Write circles in an image using the midpoint algorithm.
 * 
 * @author Jean-Yves Tinevez
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Midpoint_circle_algorithm">Midpoint
 *      circle algorithm on Wikipedia.</a>
 * @see CircleCursor {@link CircleCursor}
 */
public class Circles
{

	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>incrementing</b> the pixel values by 1 along the circle.
	 * The circle is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must be at least of dimension 2. Dimensions
	 *            0 and 1 are used to specify the circle center.
	 * @param radius
	 *            the circle radius.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends RealType< T > > void inc( final RandomAccessible< T > rai, final Localizable center, final long radius )
	{
		inc( rai, center, radius, 0, 1 );
	}

	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>incrementing</b> the pixel values by 1 along the circle.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must contain at least of dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            circle center.
	 * @param radius
	 *            the circle radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the circle.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the circle.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends RealType< T > > void inc( final RandomAccessible< T > rai, final Localizable center,
			final long radius, final int dimX, final int dimY )
	{
		final Cursor< T > cursor = new CircleCursor< T >( rai, center, radius, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().inc();
		}
	}

	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>setting</b> the pixel values with the specified value. The
	 * circle is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the target random accessible. It is the caller responsibility
	 *            to ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must be at least of dimension 2. Dimensions
	 *            0 and 1 are used to specify the circle center.
	 * @param radius
	 *            the circle radius.
	 * @param value
	 *            the value to write along the circle.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Type< T > > void set( final RandomAccessible< T > rai, final Localizable center, final long radius, final T value )
	{
		set( rai, center, radius, 0, 1, value );
	}

	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>setting</b> the pixel values with the specified value.
	 * 
	 * @param rai
	 *            the target random accessible. It is the caller responsibility
	 *            to ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must contain at least of dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            circle center.
	 * @param radius
	 *            the circle radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the circle.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the circle.
	 * @param value
	 *            the value to write along the circle.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Type< T > > void set( final RandomAccessible< T > rai, final Localizable center, final long radius, final int dimX, final int dimY, final T value )
	{
		final Cursor< T > cursor = new CircleCursor< T >( rai, center, radius, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().set( value );
		}
	}
	
	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>adding</b> the specified value to the pixel values already
	 * in the image. The circle is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must be at least of dimension 2. Dimensions
	 *            0 and 1 are used to specify the circle center.
	 * @param radius
	 *            the circle radius.
	 * @param value
	 *            the value to add along the circle.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Add< T > > void add( final RandomAccessible< T > rai, final Localizable center, final long radius, final T value )
	{
		add( rai, center, radius, 0, 1, value );
	}

	/**
	 * Writes a circle in the target {@link RandomAccessible}. The circle is
	 * written by <b>adding</b> the specified value to the pixel values already
	 * in the image.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must contain at least of dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            circle center.
	 * @param radius
	 *            the circle radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the circle.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the circle.
	 * @param value
	 *            the value to add along the circle.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Add< T > > void add( final RandomAccessible< T > rai, final Localizable center, final long radius, final int dimX, final int dimY, final T value )
	{
		final Cursor< T > cursor = new CircleCursor< T >( rai, center, radius, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().add( value );
		}
	}
	
	private Circles()
	{}
}
