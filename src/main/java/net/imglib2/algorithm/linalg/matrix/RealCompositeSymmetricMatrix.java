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

package net.imglib2.algorithm.linalg.matrix;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Symmetric {@link RealCompositeMatrix}
 *
 * @param <T>
 */
public class RealCompositeSymmetricMatrix< T extends RealType< T > > extends RealCompositeSquareMatrix< T >
{

	public RealCompositeSymmetricMatrix( final Composite< T > data, final int nRowsOrCols )
	{
		this( data, nRowsOrCols, nRowsOrCols * ( nRowsOrCols + 1 ) / 2 );
	}

	protected RealCompositeSymmetricMatrix( final Composite< T > data, final int nRowsOrCols, final int length )
	{
		super( data, nRowsOrCols, length );
	}

	@Override
	public long rowAndColumnToLinear( final long row, final long col )
	{

		// total number of elements: length = nRows * ( nRows + 1 ) / 2
		// row - 1 complete rows
		// number elements in non-complete rows: n = ( nRows - ( row - 1 ) ) * (
		// nRows -row ) / 2
		// number of elements total: length - n + ( col - row )

		if ( row < col )
		{
			final long rowDiff = nRows - row;
			final long n = rowDiff * ( rowDiff + 1 ) / 2;
			return length - n + col - row;
		}
		else
		{
			final long rowDiff = nRows - col;
			final long n = rowDiff * ( rowDiff + 1 ) / 2;
			return length - n + row - col;
		}
	}

}
