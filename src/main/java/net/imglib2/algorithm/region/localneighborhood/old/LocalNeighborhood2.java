/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.region.localneighborhood.old;

import java.util.Iterator;

import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RandomAccessible;
import net.imglib2.RealPositionable;
import net.imglib2.util.Util;

/**
 * TODO
 *
 */
@Deprecated
public class LocalNeighborhood2< T > implements IterableInterval< T >
{
	final int numDimensions;

	final long size;

	final long[] center;

	final RandomAccessible< T > source;

	public LocalNeighborhood2( final RandomAccessible< T > source, final Localizable center )
	{
		this.numDimensions = source.numDimensions();
		this.center = new long[ numDimensions ];
		center.localize( this.center );

		this.size = Util.pow( 3, numDimensions ) - 1;

		this.source = source;
	}

	public void updateCenter( final long[] center )
	{
		for ( int d = 0; d < numDimensions; ++d )
			this.center[ d ] = center[ d ];
	}

	public void updateCenter( final Localizable center )
	{
		for ( int d = 0; d < numDimensions; ++d )
			this.center[ d ] = center.getLongPosition( d );
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public T firstElement()
	{
		return cursor().next();
	}

	@Override
	public T getType()
	{
		return source.getType();
	}

	@Override
	public Object iterationOrder()
	{
		return this; // iteration order is only compatible with ourselves
	}

	@Override
	public double realMin( final int d )
	{
		return center[ d ] - 1;
	}

	@Override
	public void realMin( final double[] min )
	{
		for ( int d = 0; d < numDimensions; ++d )
			min[ d ] = center[ d ] - 1;
	}

	@Override
	public void realMin( final RealPositionable min )
	{
		for ( int d = 0; d < numDimensions; ++d )
			min.setPosition( center[ d ] - 1, d );
	}

	@Override
	public double realMax( final int d )
	{
		return center[ d ] + 1;
	}

	@Override
	public void realMax( final double[] max )
	{
		for ( int d = 0; d < numDimensions; ++d )
			max[ d ] = center[ d ] + 1;
	}

	@Override
	public void realMax( final RealPositionable max )
	{
		for ( int d = 0; d < numDimensions; ++d )
			max.setPosition( center[ d ] + 1, d );
	}

	@Override
	public int numDimensions()
	{
		return numDimensions;
	}

	@Override
	public Iterator< T > iterator()
	{
		return cursor();
	}

	@Override
	public long min( final int d )
	{
		return center[ d ] - 1;
	}

	@Override
	public void min( final long[] min )
	{
		for ( int d = 0; d < numDimensions; ++d )
			min[ d ] = center[ d ] - 1;
	}

	@Override
	public void min( final Positionable min )
	{
		for ( int d = 0; d < numDimensions; ++d )
			min.setPosition( center[ d ] - 1, d );
	}

	@Override
	public long max( final int d )
	{
		return center[ d ] + 1;
	}

	@Override
	public void max( final long[] max )
	{
		for ( int d = 0; d < numDimensions; ++d )
			max[ d ] = center[ d ] + 1;
	}

	@Override
	public void max( final Positionable max )
	{
		for ( int d = 0; d < numDimensions; ++d )
			max.setPosition( center[ d ] - 1, d );
	}

	@Override
	public void dimensions( final long[] dimensions )
	{
		for ( int d = 0; d < numDimensions; ++d )
			dimensions[ d ] = 3;
	}

	@Override
	public long dimension( final int d )
	{
		return 3;
	}

	@Override
	public LocalNeighborhoodCursor2< T > cursor()
	{
		return new LocalNeighborhoodCursor2< T >( source.randomAccess(), center );
	}

	@Override
	public LocalNeighborhoodCursor2< T > localizingCursor()
	{
		return cursor();
	}

}
