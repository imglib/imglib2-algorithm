/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
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

package net.imglib2.algorithm.integral;

import java.util.Vector;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;

/**
 * A cursor implementation that returns specific corner values of
 * {@link RectangleNeighborhood}s.
 *
 * The cursor returns, for example in 2D, the values at the following
 * positions:
 *
 * <ol>
 * <li>(neighMin, neighMin),</li>
 * <li>(neighMax-1, neighMin),</li>
 * <li>(neighMin, neighMax-1), and</li>
 * <li>(neighMax-1, neighMax-1).</li>
 * </ol>
 *
 * The mechanism naturally extends to nD. The current position can be
 * obtained from {@code getVector()} with 0s encoding neighMin and 1s
 * encoding neighMax-1.
 *
 * @author Stefan Helfrich
 */
public class IntegralCursor< T > extends AbstractEuclideanSpace implements Cursor< T >
{

	private int index = 0;

	private final int maxIndex;

	private final RandomAccess< T > source;

	private final RectangleNeighborhood< T > neighborhood;

	public IntegralCursor( final RectangleNeighborhood< T > neighborhood )
	{
		super( neighborhood.numDimensions() );
		this.neighborhood = neighborhood;
		source = neighborhood.getSourceRandomAccess();
		maxIndex = ( int ) Math.round( Math.pow( 2, neighborhood.numDimensions() ) );
		reset();
	}

	protected IntegralCursor( final IntegralCursor< T > cursor )
	{
		super( cursor.numDimensions() );
		neighborhood = cursor.neighborhood; // FIXME?
		source = cursor.source.copyRandomAccess();
		index = cursor.index;
		maxIndex = cursor.maxIndex;
	}

	@Override
	public void reset()
	{
		long[] min = new long[ neighborhood.numDimensions() ];
		neighborhood.min( min );
		source.setPosition( min );
		source.bck( 0 );
		index = 0;
	}

	@Override
	public void fwd()
	{
		// Extract each dimension individually from currentPosition
		final long[] cornerPosition = new long[ neighborhood.numDimensions() ];
		for ( int d = 0; d < neighborhood.numDimensions(); ++d )
		{
			int valueInDimension = index >> d;
			valueInDimension = valueInDimension & 1;

			if ( valueInDimension == 1 )
			{
				// if bit in dimension is set
				cornerPosition[ d ] = neighborhood.max( d ) - 1;
			}
			else
			{
				// if not
				cornerPosition[ d ] = neighborhood.min( d );
			}
		}

		source.setPosition( cornerPosition );
		++index;
	}

	@Override
	public void jumpFwd( final long steps )
	{
		for ( long i = 0; i < steps; ++i )
			fwd();
	}

	@Override
	public T get()
	{
		return source.get();
	}

	@Override
	public boolean hasNext()
	{
		return index < maxIndex;
	}

	@Override
	public T next()
	{
		fwd();
		return get();
	}

	@Override
	public IntegralCursor< T > copy()
	{
		return new IntegralCursor< T >( this );
	}

	@Override
	public IntegralCursor< T > copyCursor()
	{
		return copy();
	}

	public int getCornerInteger()
	{
		return index;
	}

	public Vector< Integer > getCornerVector()
	{
		final Vector< Integer > vec = new Vector< Integer >();

		// Extract each dimension individually from currentPosition
		for ( int d = 0; d < neighborhood.numDimensions(); ++d )
		{
			int valueInDimension = index >> d;
			valueInDimension = valueInDimension & 1;

			vec.add( valueInDimension );
		}

		return vec;
	}

	@Override
	public void remove()
	{
		// NB: no action.
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return source.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return source.getDoublePosition( d );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return source.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return source.getLongPosition( d );
	}

	@Override
	public void localize( final long[] position )
	{
		source.localize( position );
	}

	@Override
	public void localize( final float[] position )
	{
		source.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		source.localize( position );
	}

	@Override
	public void localize( final int[] position )
	{
		source.localize( position );
	}

}
