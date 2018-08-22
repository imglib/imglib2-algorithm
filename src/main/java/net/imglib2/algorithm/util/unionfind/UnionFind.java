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

package net.imglib2.algorithm.util.unionfind;

import java.util.function.LongUnaryOperator;
import java.util.function.ToLongBiFunction;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

/**
 *
 * Data structure for the union find algorithm.
 *
 * @author Philipp Hanslovsky
 *
 */
public interface UnionFind
{
	/**
	 *
	 * @param id
	 * @return Root for {@code id}
	 */
	long findRoot( long id );

	/**
	 *
	 * Join two sets represented by {@code root1} and {@code root2}
	 *
	 * @param root1
	 * @param root2
	 * @return The new representative of the joined set.
	 */
	long join( final long root1, final long root2 );

	/**
	 *
	 * @return Number of elements in this union find.
	 */
	long size();

	/**
	 *
	 * @return Number of sets in this union find.
	 */
	long setCount();

	/**
	 *
	 * Relabel all mask pixels into the representative id of their containing
	 * sets as defined by {@code unionFind}.
	 *
	 * @param mask
	 *            Boolean mask to distinguish foreground ({@code true}) from
	 *            background ({@code false}).
	 * @param labeling
	 *            Output parameter to store labeling: background pixels are
	 *            labeled zero, foreground pixels are greater than zero: 1, 2,
	 *            ..., N. Note that this is expected to be zero as background
	 *            values will not be written.
	 * @param unionFind
	 *            {@link UnionFind}
	 * @param idForPixel
	 *            Create id from pixel location and value. Multiple calls with
	 *            the same argument should always return the same result.
	 * @param idForSet
	 *            Create id for a set from the root id of a set. Multiple calls
	 *            with the same argument should always return the same result.
	 */
	static < B extends BooleanType< B >, L extends IntegerType< L > > void relabel(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final UnionFind unionFind,
			final ToLongBiFunction< Localizable, L > idForPixel,
			final LongUnaryOperator idForSet )
	{
		final Cursor< L > label = Views.flatIterable( labeling ).localizingCursor();
		final Cursor< B > maskCursor = Views.flatIterable( Views.interval( mask, labeling ) ).cursor();
		while ( label.hasNext() )
		{
			final B m = maskCursor.next();
			label.fwd();
			if ( m.get() )
			{
				final L l = label.get();
				final long root = unionFind.findRoot( idForPixel.applyAsLong( label, l ) );
				l.setInteger( idForSet.applyAsLong( root ) );
			}
		}
	}

}
