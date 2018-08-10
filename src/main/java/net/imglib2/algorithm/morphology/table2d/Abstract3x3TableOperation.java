/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Performs a binary operation on a 3x3 2-D neighborhood using a table of truth
 * values to drive the operation.
 * 
 * @author Lee Kamentsky
 * @author Leon Yang
 */
public abstract class Abstract3x3TableOperation
{
	private static final RectangleShape shape = new RectangleShape( 1, false );

	/**
	 * The index to the table that's returned is built by examining each pixel
	 * and accumulating 2^pixel number. The pixels are numbered like this:
	 * 
	 * <pre>
	 * 0 1 2
	 * 3 4 5
	 * 6 7 8
	 * </pre>
	 * 
	 * @return a 512-element table that holds the truth values for each 3x3
	 *         combination
	 */
	protected abstract boolean[] getTable();

	/**
	 * Gets default value for neighborhood pixels outside of the image. For
	 * example, an erosion operator should return true because otherwise pixels
	 * at the border would be eroded without evidence that they were not in the
	 * interior, and vice-versa for dilate.
	 * 
	 * @return the extended value for this operation
	 */
	protected abstract boolean getExtendedValue();

	protected < T extends BooleanType< T > > Img< T > calculate( final Img< T > source )
	{
		final Img< T > target = source.factory().create( source );
		final T extendedVal = source.firstElement().createVariable();
		extendedVal.set( getExtendedValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T > > extended = Views.extendValue( source, extendedVal );
		calculate( extended, target );
		return target;
	}

	protected < T extends BooleanType< T > > void calculate( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		final RandomAccessible< Neighborhood< T > > accessible = shape.neighborhoodsRandomAccessible( source );
		final RandomAccess< Neighborhood< T > > randomAccess = accessible.randomAccess( target );
		final Cursor< T > cursorTarget = target.cursor();
		final boolean[] table = getTable();
		while ( cursorTarget.hasNext() )
		{
			final T targetVal = cursorTarget.next();
			randomAccess.setPosition( cursorTarget );
			final Neighborhood< T > neighborhood = randomAccess.get();
			final Cursor< T > nc = neighborhood.cursor();
			int idx = 0;
			// Assume that the neighborhood obtained is of FlatIterationOrder,
			// and assemble the index using bitwise operations.
			while ( nc.hasNext() )
			{
				idx <<= 1;
				idx |= nc.next().get() ? 1 : 0;
			}

			targetVal.set( table[ idx ] );
		}
	}

}
