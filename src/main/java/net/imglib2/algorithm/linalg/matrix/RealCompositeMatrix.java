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

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         {@link RealMatrix} that reads data from {@link Composite} (non-copy).
 *
 * @param <T>
 */
public class RealCompositeMatrix< T extends RealType< T > > extends AbstractRealMatrix
{

	protected Composite< T > data;

	protected final int nRows;

	protected final int nCols;

	protected final int length;

	public RealCompositeMatrix( final Composite< T > data, final int nRows, final int nCols )
	{
		this( data, nRows, nCols, nRows * nCols );
	}

	public RealCompositeMatrix( final Composite< T > data, final int nRows, final int nCols, final int length )
	{
		super();

		assert length == expectedLength( nRows, nCols );

		this.data = data;
		this.nRows = nRows;
		this.nCols = nCols;
		this.length = length;
	}

	public void setData( final Composite< T > data )
	{
		this.data = data;
	}

	@Override
	public RealMatrix copy()
	{
		// Supposed to be a deep copy, cf apache docs:
		// http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/linear/RealMatrix.html#copy()
		final RealCompositeMatrix< T > result = ( RealCompositeMatrix< T > ) createMatrix( nRows, nCols );
		for ( int i = 0; i < length; ++i )
			result.data.get( i ).set( this.data.get( i ) );
		return result;
	}

	@Override
	public RealMatrix createMatrix( final int nRows, final int nCols ) throws NotStrictlyPositiveException
	{
		final T t = this.data.get( 0 );
		final Img< T > img;
		final int length = expectedLength( nRows, nCols );
		if ( NativeType.class.isInstance( t ) )
			img = ( ( NativeType ) t ).createSuitableNativeImg( new ArrayImgFactory<>(), new long[] { 1, length } );
		else
			img = new ListImgFactory< T >().create( new long[] { 1, length }, t );

		return createMatrix( Views.collapseReal( img ).randomAccess().get(), nRows, nCols, length );
	}

	public < U extends RealType< U > > RealCompositeMatrix< U > createMatrix( final Composite< U > data, final int nRows, final int nCols, final int length )
	{
		return new RealCompositeMatrix<>( data, nRows, nCols, length );
	}

	@Override
	public int getColumnDimension()
	{
		return this.nCols;
	}

	@Override
	public double getEntry( final int row, final int col )
	{
		assert row >= 0 && row < this.nRows;
		assert col >= 0 && col < this.nCols;

		final double val = data.get( rowAndColumnToLinear( row, col ) ).getRealDouble();

		return val;
	}

	@Override
	public int getRowDimension()
	{
		return this.nRows;
	}

	@Override
	public void setEntry( final int row, final int col, final double val ) throws OutOfRangeException
	{
		if ( row < 0 || row >= this.nRows )
			throw new OutOfRangeException( row, 0, this.nRows );
		else if ( col < 0 || col >= this.nCols )
			throw new OutOfRangeException( col, 0, this.nCols );

		data.get( rowAndColumnToLinear( row, col ) ).setReal( val );

	}

	public int rowAndColumnToLinear( final int row, final int col )
	{
		return row * nCols + col;
	}

	public int expectedLength( final int nRows, final int nCols )
	{
		return nRows * nCols;
	}


}
