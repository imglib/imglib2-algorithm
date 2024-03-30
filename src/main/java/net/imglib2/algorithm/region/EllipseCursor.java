/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import net.imglib2.util.Util;

/**
 * Iterates over a Bresenham 2D ellipse using the mid-point algorithm.
 * <p>
 * Each point of the ellipse is iterated exactly once, and there is no "hole" in
 * the ellipse. A bitmap excerpt from such a ellipse looks like this:
 * 
 * <pre>
 * ..........
 * OOO.......
 * ...OO.....
 * .....O....
 * .....O....
 * </pre>
 * <p>
 * The circle generated with equal X and Y radii is not strictly equivalent to
 * the circle generated by the {@link CircleCursor}.
 * 
 * @author Jean-Yves Tinevez
 */
public class EllipseCursor< T > implements Cursor< T >
{
	private final RandomAccessible< T > rai;

	private final RandomAccess< T > ra;

	private final Localizable center;

	private final long radiusX;

	private final long radiusY;

	private final int dimX;

	private final int dimY;

	private long x;

	private long y;

	private double px;

	private double py;

	private double p;

	private Octant octant;

	private boolean hasNext;

	private enum Octant
	{
		INIT,
		EAST, NORTH, WEST, SOUTH,
		REGION1_Q1, REGION1_Q2, REGION1_Q3, REGION1_Q4,
		REGION2_Q1, REGION2_Q2, REGION2_Q3, REGION2_Q4;
	}

	/**
	 * Iterates over a Bresenham ellipse in the target {@link RandomAccessible}.
	 * Each point of the ellipse is iterated exactly once, and there is no
	 * "hole" in the ellipse. The ellipse is written in a plane in dimensions 0
	 * and 1.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must be at least of dimension 2.
	 *            Dimensions 0 and 1 are used to specify the ellipse center.
	 * @param radiusX
	 *            the ellipse radius in the dimension 0.
	 * @param radiusY
	 *            the ellipse radius in the dimension 1.
	 */
	public EllipseCursor( final RandomAccessible< T > rai, final Localizable center, final long radiusX, final long radiusY )
	{
		this( rai, center, radiusX, radiusY, 0, 1 );
	}

	/**
	 * Iterates over a Bresenham ellipse in the target {@link RandomAccessible}.
	 * Each point of the ellipse is iterated exactly once, and there is no
	 * "hole" in the ellipse.
	 * 
	 * @param rai
	 *            the random accessible. It is the caller responsibility to
	 *            ensure it can be accessed everywhere the ellipse will be
	 *            iterated.
	 * @param center
	 *            the ellipse center. Must be at least of dimension 2.
	 *            Dimensions 0 and 1 are used to specify the ellipse center.
	 * @param radiusX
	 *            the ellipse radius in dimension X.
	 * @param radiusY
	 *            the ellipse radius in dimension Y.
	 * @param dimX
	 *            the first dimension of the plane in which to draw the ellipse.
	 * @param dimY
	 *            the second dimension of the plane in which to draw the
	 *            ellipse.
	 */
	public EllipseCursor( final RandomAccessible< T > rai, final Localizable center, final long radiusX, final long radiusY, final int dimX, final int dimY )
	{
		this.rai = rai;
		this.center = center;
		if ( radiusX > radiusY )
		{
			// Swap X & Y if radiusX if larger than radiusY and avoid issues
			// with slim ellipses.
			this.radiusX = radiusY;
			this.radiusY = radiusX;
			this.dimX = dimY;
			this.dimY = dimX;
		}
		else
		{
			this.radiusX = radiusX;
			this.radiusY = radiusY;
			this.dimX = dimX;
			this.dimY = dimY;
		}
		// Make an interval to signal where we will access data.
		final int numDimensions = rai.numDimensions();
		final long[] minmax = new long[ 2 * numDimensions ];
		for ( int d = 0; d < numDimensions; d++ )
		{
			if ( d == dimX )
				minmax[ d ] = center.getLongPosition( d ) - radiusX;
			else if ( d == dimY )
				minmax[ d ] = center.getLongPosition( d ) - radiusY;
			else
				minmax[ d ] = center.getLongPosition( d );
		}
		for ( int d = 0; d < numDimensions; d++ )
		{
			if ( d == dimX )
				minmax[ d + numDimensions ] = center.getLongPosition( d ) + radiusX;
			else if ( d == dimY )
				minmax[ d + numDimensions ] = center.getLongPosition( d ) + radiusY;
			else
				minmax[ d + numDimensions ] = center.getLongPosition( d );
		}
		final Interval interval = FinalInterval.createMinMax( minmax );
		this.ra = rai.randomAccess( interval );
		reset();
	}

	// copy constructor
	private EllipseCursor( final EllipseCursor< T > c )
	{
		rai = c.rai;
		ra = c.ra.copy();
		center = new Point( c.center );
		radiusX = c.radiusX;
		radiusY = c.radiusY;
		dimX = c.dimX;
		dimY = c.dimY;
		x = c.x;
		y = c.y;
		px  = c.px;
		py = c.py;
		p = c.p;
		octant = c.octant;
		hasNext = c.hasNext;
	}

	@Override
	public void reset()
	{
		// Prepare for region 1.
		x = 0l;
		y = radiusY;
		px = 0.;
		py = 2. * radiusX * radiusX * y;
		p = radiusY * radiusY - ( radiusX * radiusX * radiusY ) + ( 0.25 * radiusX * radiusX );

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
			ra.setPosition( center.getLongPosition( dimY ) + radiusY, dimY );
			octant = Octant.NORTH;
			break;

		case NORTH:
			ra.setPosition( center.getLongPosition( dimY ) - radiusY, dimY );
			octant = Octant.SOUTH;
			break;

		case SOUTH:
			ra.setPosition( center.getLongPosition( dimX ) - radiusX, dimX );
			ra.setPosition( center.getLongPosition( dimY ), dimY );
			octant = Octant.WEST;
			break;

		case WEST:
			ra.setPosition( center.getLongPosition( dimX ) + radiusX, dimX );
			octant = Octant.EAST;

			// Special case: rx = ry = 1.
			if ( radiusX == 1 && radiusY == 1 )
				hasNext = false;

			break;

		case EAST:
			// Move to region 1.
			x++;
			px += 2. * radiusY * radiusY;
			if ( p < 0 )
			{
				p += ( radiusY * radiusY ) + px;
			}
			else
			{
				y--;
				py -= 2 * ( radiusX * radiusX );
				p += ( radiusY * radiusY ) + px - py;
			}

			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			ra.setPosition( center.getLongPosition( dimY ) + y, dimY );
			octant = Octant.REGION1_Q1;
			break;

		case REGION1_Q1:
			ra.setPosition( center.getLongPosition( dimX ) - x, dimX );
			octant = Octant.REGION1_Q2;
			break;

		case REGION1_Q2:
			ra.setPosition( center.getLongPosition( dimY ) - y, dimY );
			octant = Octant.REGION1_Q3;
			break;

		case REGION1_Q3:
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			octant = Octant.REGION1_Q4;
			
			// Don't overwrite equator for small radii.
			if ( y == 1 && x <= 2 )
				hasNext = false;
			
			break;

		case REGION1_Q4:

			if ( px < py )
			{
				// Back to Q1 of Region 1.
				x++;
				px += 2. * radiusY * radiusY;
				if ( p < 0 )
				{
					p += ( radiusY * radiusY ) + px;
				}
				else
				{
					y--;
					py -= 2 * ( radiusX * radiusX );
					p += ( radiusY * radiusY ) + px - py;
				}

				octant = Octant.REGION1_Q1;
			}
			else
			{
				// Maybe go to Region 2.
				p = radiusY * radiusY * ( x + 0.5 ) * ( x + 0.5 ) + radiusX * radiusX * ( y - 1 ) * ( y - 1 ) - radiusX * radiusX * radiusY * radiusY;
				y--;
				py -= 2. * radiusX * radiusX;
				if ( p > 0 )
				{
					p += radiusX * radiusX - py;
				}
				else
				{
					x++;
					px += 2. * radiusY * radiusY;
					p += radiusX * radiusX - py + px;
				}
				octant = Octant.REGION2_Q1;
			}

			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			ra.setPosition( center.getLongPosition( dimY ) + y, dimY );
			break;

		case REGION2_Q1:
			ra.setPosition( center.getLongPosition( dimX ) - x, dimX );
			octant = Octant.REGION2_Q2;
			break;

		case REGION2_Q2:
			ra.setPosition( center.getLongPosition( dimY ) - y, dimY );
			octant = Octant.REGION2_Q3;
			break;

		case REGION2_Q3:
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			octant = Octant.REGION2_Q4;
			if ( y <= 1 )
				hasNext = false;
			break;

		case REGION2_Q4:
			y--;
			py -= 2. * radiusX * radiusX;
			if ( p > 0 )
			{
				p += radiusX * radiusX - py;
			}
			else
			{
				x++;
				px += 2. * radiusY * radiusY;
				p += radiusX * radiusX - py + px;
			}
			octant = Octant.REGION2_Q1;
			ra.setPosition( center.getLongPosition( dimX ) + x, dimX );
			ra.setPosition( center.getLongPosition( dimY ) + y, dimY );
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
	public Cursor< T > copy()
	{
		return new EllipseCursor<>( this );
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
	public String toString()
	{
		return super.toString() + " pos=" + Util.printCoordinates( this ) + ",\t" + octant + ",\tx = " + x + ",\ty = " + y;
	}
}
