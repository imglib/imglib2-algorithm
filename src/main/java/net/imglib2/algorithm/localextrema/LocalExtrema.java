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
package net.imglib2.algorithm.localextrema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Provides
 * {@link #findLocalExtrema(RandomAccessibleInterval, LocalNeighborhoodCheck, int)}
 * to find pixels that are extrema in their local neighborhood.
 * 
 * @author Tobias Pietzsch
 */
public class LocalExtrema
{
	/**
	 * A local extremum check.
	 * 
	 * @param <P>
	 *            A representation of the extremum. For example, this could be
	 *            just a {@link Point} describing the location of the extremum.
	 *            It could contain additional information such as the value at
	 *            the extremum or an extremum type.
	 * @param <T>
	 *            pixel type.
	 */
	public interface LocalNeighborhoodCheck< P, T extends Comparable< T > >
	{
		/**
		 * Determine whether a pixel is a local extremum. If so, return a
		 * <code>P</code> that represents the maximum. Otherwise return
		 * <code>null</code>.
		 * 
		 * @param center
		 *            an access located on the pixel to test
		 * @param neighborhood
		 *            iterable neighborhood of the pixel, not containing the
		 *            pixel itself.
		 * @return null if the center not a local extremum, a P if it is.
		 */
		public < C extends Localizable & Sampler< T > > P check( C center, Neighborhood< T > neighborhood );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can is specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 * 
	 * TODO: Make neighborhood shape configurable. This will require that Shape
	 * can give a bounding box.
	 * 
	 * @param img
	 * @param localNeighborhoodCheck
	 * @param service
	 * @return
	 */
	public static < P, T extends Comparable< T > > ArrayList< P > findLocalExtrema( final RandomAccessibleInterval< T > img, final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck, final ExecutorService service )
	{
		final ArrayList< P > allExtrema = new ArrayList< P >();

		final Interval full = Intervals.expand( img, -1 );
		final int n = img.numDimensions();
		final int splitd = n - 1;
		// FIXME is there a better way to determine number of threads
		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numTasks = Math.max( Math.min( ( int ) full.dimension( splitd ), numThreads * 20 ), 1 );
		final long dsize = full.dimension( splitd ) / numTasks;
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		full.min( min );
		full.max( max );

		final RectangleShape shape = new RectangleShape( 1, true );

		final ArrayList< Future< Void > > futures = new ArrayList< Future< Void > >();
		final List< P > synchronizedAllExtrema = Collections.synchronizedList( allExtrema );
		for ( int taskNum = 0; taskNum < numTasks; ++taskNum )
		{
			min[ splitd ] = full.min( splitd ) + taskNum * dsize;
			max[ splitd ] = ( taskNum == numTasks - 1 ) ? full.max( splitd ) : min[ splitd ] + dsize - 1;
			final RandomAccessibleInterval< T > source = Views.interval( img, new FinalInterval( min, max ) );
			final ArrayList< P > extrema = new ArrayList< P >( 128 );
			final Callable< Void > r = new Callable< Void >()
			{
				@Override
				public Void call()
				{
					final Cursor< T > center = Views.flatIterable( source ).cursor();
					for ( final Neighborhood< T > neighborhood : shape.neighborhoods( source ) )
					{
						center.fwd();
						final P p = localNeighborhoodCheck.check( center, neighborhood );
						if ( p != null )
							extrema.add( p );
					}
					synchronizedAllExtrema.addAll( extrema );
					return null;
				}
			};
			futures.add( service.submit( r ) );
		}
		for ( final Future< Void > f : futures )
		{
			try
			{
				f.get();
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
			catch ( final ExecutionException e )
			{
				e.printStackTrace();
			}
		}

		return allExtrema;
	}

	/**
	 * A {@link LocalNeighborhoodCheck} to test whether a pixel is a local
	 * maximum. A pixel is considered a maximum if its value is greater than or
	 * equal to a specified minimum allowed value, and no pixel in the
	 * neighborhood has a greater value. That means that maxima are non-strict.
	 * Intensity plateaus may result in multiple maxima.
	 * 
	 * @param <T>
	 *            pixel type.
	 * 
	 * @author Tobias Pietzsch
	 */
	public static class MaximumCheck< T extends Comparable< T > > implements LocalNeighborhoodCheck< Point, T >
	{
		final T minPeakValue;

		public MaximumCheck( final T minPeakValue )
		{
			this.minPeakValue = minPeakValue;
		}

		@Override
		public < C extends Localizable & Sampler< T > > Point check( final C center, final Neighborhood< T > neighborhood )
		{
			final T c = center.get();
			if ( minPeakValue.compareTo( c ) > 0 )
				return null;

			for ( final T t : neighborhood )
				if ( t.compareTo( c ) > 0 )
					return null;

			return new Point( center );
		}
	}

	/**
	 * A {@link LocalNeighborhoodCheck} to test whether a pixel is a local
	 * minimum. A pixel is considered a minimum if its value is less than or
	 * equal to a specified maximum allowed value, and no pixel in the
	 * neighborhood has a smaller value. That means that minima are non-strict.
	 * Intensity plateaus may result in multiple minima.
	 * 
	 * @param <T>
	 *            pixel type.
	 * 
	 * @author Tobias Pietzsch
	 */
	public static class MinimumCheck< T extends Comparable< T > > implements LocalNeighborhoodCheck< Point, T >
	{
		final T maxPeakValue;

		public MinimumCheck( final T maxPeakValue )
		{
			this.maxPeakValue = maxPeakValue;
		}

		@Override
		public < C extends Localizable & Sampler< T > > Point check( final C center, final Neighborhood< T > neighborhood )
		{
			final T c = center.get();
			if ( maxPeakValue.compareTo( c ) < 0 )
				return null;

			for ( final T t : neighborhood )
				if ( t.compareTo( c ) < 0 )
					return null;

			return new Point( center );
		}
	}
}
