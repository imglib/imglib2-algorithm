/*-
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
package net.imglib2.algorithm.region;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;

/**
 * Iterates over a Bresenham 2D circle using the mid-point algorithm.
 * <p>
 * Each point of the circle is iterated exactly once, and there is no "hole" in
 * the circle. Contrary to the algorithm linked below, the circles generated by
 * this cursor are "slim": each pixel of the circle is connected with
 * 8-connectivity. For instance, a bitmap excerpt from such a circle looks like
 * this:
 * 
 * <pre>
 * ..........
 * OOO.......
 * ...OO.....
 * .....O....
 * .....O....
 * </pre>
 * 
 * @author Jean-Yves Tinevez
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Midpoint_circle_algorithm">Midpoint
 *      circle algorithm on Wikipedia.</a>
 *
 */
public class CircleCursor< T > implements Cursor< T >
{
	private final RandomAccessible< T > rai;

	private final RandomAccess< T > ra;

	private final Localizable center;

	private final long radius;

	private final int dimX;

	private final int dimY;

	private long x;

	private long y;

	private long dx;

	private long dy;

	private long f;

	private Octant octant;

	private boolean hasNext;

	private enum Octant
	{
		INIT, EAST, NORTH, WEST, SOUTH, O1, O2, O3, O4, O5, O6, O7, O8;
	}

	/**
	 * Iterates over a Bresenham circle in the target {@link RandomAccessible}.
	 * Each point of the circle is iterated exactly once, and there is no "hole"
	 * in the circle.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must be at least of dimension 2. Dimensions
	 *            0 and 1 are used to specify the circle center.
	 * @param radius
	 *            the circle radius. The circle is written in a plane in
	 *            dimensions 0 and 1.
	 */
	public CircleCursor( final RandomAccessible< T > rai, final Localizable center, final long radius )
	{
		this( rai, center, radius, 0, 1 );
	}

	/**
	 * Iterates over a Bresenham circle in the target {@link RandomAccessible}.
	 * Each point of the circle is iterated exactly once, and there is no "hole"
	 * in the circle.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the circle will be
	 *            iterated.
	 * @param center
	 *            the circle center. Must at least contain dimensions specified
	 *            <code>dimX</code> and <code>dimY</code>.
	 * @param radius
	 *            the circle radius.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the circle.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the circle.
	 */
	public CircleCursor( final RandomAccessible< T > rai, final Localizable center, final long radius, final int dimX, final int dimY )
	{
		this.rai = rai;
		this.center = center;
		this.radius = radius;
		this.dimX = dimX;
		this.dimY = dimY;
		// Make an interval to signal where we will access data.
		final int numDimensions = rai.numDimensions();
		final long[] minmax = new long[ 2 * numDimensions ];
		for ( int d = 0; d < numDimensions; d++ )
		{
			if ( d == dimX || d == dimY )
				minmax[ d ] = center.getLongPosition( d ) - radius;
			else
				minmax[ d ] = center.getLongPosition( d );
		}
		for ( int d = 0; d < numDimensions; d++ )
		{
			if ( d == dimX || d == dimY )
				minmax[ d + numDimensions ] = center.getLongPosition( d ) + radius;
			else
				minmax[ d + numDimensions ] = center.getLongPosition( d );
		}
		final Interval interval = FinalInterval.createMinMax( minmax );
		this.ra = rai.randomAccess( interval );
		reset();
	}

	// copy constructor
	private CircleCursor( final CircleCursor< T > c )
	{
		rai = c.rai;
		ra = c.ra.copy();
		center = new Point( c.center );
		radius = c.radius;
		dimX = c.dimX;
		dimY = c.dimY;
		x = c.x;
		y = c.y;
		dx = c.dx;
		dy = c.dy;
		f = c.f;
		octant = c.octant;
		hasNext = c.hasNext;
	}

	@Override
	public void reset()
	{
		x = 0;
		y = radius;
		f = 1 - radius;
		dx = 1;
		dy = -2 * radius;
		octant = Octant.INIT;
		ra.setPosition( center );
		hasNext = true;
	}

	@Override
	public void fwd()
	{
		switch ( octant )
		{
		default:
		case INIT:
			ra.setPosition( center.getLongPosition( dimY ) + radius, dimY );
			octant = Octant.NORTH;
			break;

		case NORTH:
			ra.setPosition( center.getLongPosition( dimY ) - radius, dimY );
			octant = Octant.SOUTH;
			break;

		case SOUTH:
			ra.setPosition( center.getLongPosition( dimX ) - radius, dimX );
			ra.setPosition( center.getLongPosition( dimY ), dimY );
			octant = Octant.WEST;
			break;

		case WEST:
			ra.setPosition( center.getLongPosition( dimX ) + radius, dimX );
			octant = Octant.EAST;
			break;

		case EAST:
			x = x + 1;
			dx = dx + 2;
			f = f + dx;
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			ra.setPosition( center.getLongPosition( dimY ) + y, dimY );
			octant = Octant.O1;
			break;

		case O1:
			ra.setPosition( center.getLongPosition( dimX ) - x, dimX );
			octant = Octant.O2;
			break;

		case O2:
			ra.setPosition( center.getLongPosition( dimY ) - y, dimY );
			octant = Octant.O3;
			break;

		case O3:
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			octant = Octant.O4;
			// Stop here if x==y, lest the 45º will be iterated twice.
			if ( x >= y )
				hasNext = false;
			break;

		case O4:
			ra.setPosition( center.getLongPosition( dimX ) + y, dimX );
			ra.setPosition( center.getLongPosition( dimY ) - x, dimY );
			octant = Octant.O5;
			break;

		case O5:
			ra.setPosition( center.getLongPosition( dimX ) - y, dimX );
			octant = Octant.O6;
			break;

		case O6:
			ra.setPosition( center.getLongPosition( dimY ) + x, dimY );
			octant = Octant.O7;
			break;

		case O7:
			ra.setPosition( center.getLongPosition( dimX ) + y, dimX );
			octant = Octant.O8;
			// Stop here if dx would cross y.
			if ( x >= y - 1 )
				hasNext = false;
			break;

		case O8:
			if ( f > 0 )
			{
				y = y - 1;
				dy = dy + 2;
				f = f + dy;
			}
			x = x + 1;
			dx = dx + 2;
			f = f + dx;
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			ra.setPosition( center.getLongPosition( dimY ) + y, dimY );
			octant = Octant.O1;
			break;
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public void localize( final float[] position )
	{
		ra.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		ra.localize( position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ra.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return ra.getDoublePosition( d );
	}

	@Override
	public int numDimensions()
	{
		return ra.numDimensions();
	}

	@Override
	public T get()
	{
		return ra.get();
	}

	@Override
	public T getType()
	{
		return ra.getType();
	}

	@Override
	public void jumpFwd( final long steps )
	{
		for ( int i = 0; i < steps; i++ )
			fwd();
	}

	@Override
	public T next()
	{
		fwd();
		return get();
	}

	@Override
	public void localize( final int[] position )
	{
		ra.localize( position );
	}

	@Override
	public void localize( final long[] position )
	{
		ra.localize( position );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return ra.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return ra.getLongPosition( d );
	}

	@Override
	public Cursor< T > copy()
	{
		return new CircleCursor<>( this );
	}
}
