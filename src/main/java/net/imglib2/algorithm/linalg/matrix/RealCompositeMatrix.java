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

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.PhysicalStore;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         {@link Access2D} that reads data from {@link Composite} (non-copy).
 *
 * @param <T>
 */
public class RealCompositeMatrix< T extends RealType< T > > implements Access2D< Double >, Access2D.Collectable< Double, PhysicalStore< Double > >
{

	protected Composite< T > data;

	protected final int nRows;

	protected final int nCols;

	protected final int length;

	public RealCompositeMatrix( final Composite< T > data, final int nRows, final int nCols )
	{
		this( data, nRows, nCols, nRows * nCols );
	}

	protected RealCompositeMatrix( final Composite< T > data, final int nRows, final int nCols, final int length )
	{
		super();

		this.data = data;
		this.nRows = nRows;
		this.nCols = nCols;
		this.length = length;
	}

	public void setData( final Composite< T > data )
	{
		this.data = data;
	}

	public long rowAndColumnToLinear( final long row, final long col )
	{
		return row * nCols + col;
	}

	@Override
	public long countColumns()
	{
		return nCols;
	}

	@Override
	public long countRows()
	{
		return nRows;
	}

	@Override
	public void supplyTo( final PhysicalStore< Double > receiver )
	{
		final long tmpLimRows = Math.min( this.countRows(), receiver.countRows() );
		final long tmpLimCols = Math.min( this.countColumns(), receiver.countColumns() );

		for ( long j = 0L; j < tmpLimCols; j++ )
			for (long i = 0L; i < tmpLimRows; i++)
				receiver.set(i, j, this.doubleValue(i, j));
	}

	@Override
	public double doubleValue( final long row, final long col )
	{
		assert row >= 0 && row < this.nRows;
		assert col >= 0 && col < this.nCols;

		final double val = data.get( rowAndColumnToLinear( row, col ) ).getRealDouble();

		return val;
	}

	@Override
	public Double get( final long row, final long col )
	{
		return doubleValue( row, col );
	}

}
