/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2022 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.region;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.operators.Add;

/**
 * Write ellipses in an image using the midpoint algorithm.
 * 
 * @author Jean-Yves Tinevez
 * @see EllipseCursor {@link EllipseCursor}
 * @see EllipseCursor {@link EllipseCursor}
 */
public class Ellipses
{

	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>incrementing</b> the pixel values by 1 along the ellipse.
	 * The ellipse is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must be at least of dimension 2.
	 *            Dimensions 0 and 1 are used to specify the ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends RealType< T > > void inc( final RandomAccessible< T > rai, final Localizable center, final long radiusX, final long radiusY )
	{
		inc( rai, center, radiusX, radiusY, 0, 1 );
	}

	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>incrementing</b> the pixel values by 1 along the ellipse.
	 * The ellipse is written in a plane in specified dimensions.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must contain at least dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the ellipse.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the
	 *            ellipse.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends RealType< T > > void inc( final RandomAccessible< T > rai, final Localizable center,
			final long radiusX, final long radiusY, final int dimX, final int dimY )
	{
		final Cursor< T > cursor = new EllipseCursor< T >( rai, center, radiusX, radiusY, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().inc();
		}
	}

	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>setting</b> the pixel values with the specified value. The
	 * ellipse is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the target random accessible. It is the caller responsibility
	 *            to ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must be at least of dimension 2.
	 *            Dimensions 0 and 1 are used to specify the ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param value
	 *            the value to write along the ellipse.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Type< T > > void set( final RandomAccessible< T > rai, final Localizable center, final long radiusX, final long radiusY, final T value )
	{
		set( rai, center, radiusX, radiusY, 0, 1, value );
	}

	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>setting</b> the pixel values with the specified value.
	 * 
	 * @param rai
	 *            the target random accessible. It is the caller responsibility
	 *            to ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must contain at least dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the ellipse.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the
	 *            ellipse.
	 * @param value
	 *            the value to write along the ellipse.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Type< T > > void set( final RandomAccessible< T > rai, final Localizable center,
			final long radiusX, final long radiusY, final int dimX, final int dimY, final T value )
	{
		final Cursor< T > cursor = new EllipseCursor< T >( rai, center, radiusX, radiusY, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().set( value );
		}
	}
	
	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>adding</b> the specified value to the pixel values already
	 * in the image. The ellipse is written in a plane in dimensions 0 and 1.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must be at least of dimension 2.
	 *            Dimensions 0 and 1 are used to specify the ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param value
	 *            the value to add along the ellipse.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Add< T > > void add( final RandomAccessible< T > rai, final Localizable center, final long radiusX, final long radiusY, final T value )
	{
		add( rai, center, radiusX, radiusY, 0, 1, value );
	}

	/**
	 * Writes an ellipse in the target {@link RandomAccessible}. The ellipse is
	 * written by <b>adding</b> the specified value to the pixel values already
	 * in the image.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must contain at least dimensions
	 *            <code>dimX</code> and <code>dimY</code>, used to specify the
	 *            ellipse center.
	 * @param radiusX
	 *            the ellipse X radius.
	 * @param radiusY
	 *            the ellipse Y radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the ellipse.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the
	 *            ellipse.
	 * @param value
	 *            the value to add along the ellipse.
	 * @param <T>
	 *            the type of the target image.
	 */
	public static < T extends Add< T > > void add( final RandomAccessible< T > rai, final Localizable center,
			final long radiusX, final long radiusY, final int dimX, final int dimY, final T value )
	{
		final Cursor< T > cursor = new EllipseCursor< T >( rai, center, radiusX, radiusY, dimX, dimY );
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().add( value );
		}
	}
	
	private Ellipses()
	{}
}
