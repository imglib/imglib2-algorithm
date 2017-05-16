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

package net.imglib2.algorithm.gradient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * @author Tobias Pietzsch
 * @author Philipp Hanslovsky
 *
 */
public class PartialDerivative
{
	// nice version...
	/**
	 * Compute the partial derivative of source in a particular dimension.
	 *
	 * @param source
	 *            source image, has to provide valid data in the interval of the
	 *            gradient image plus a one pixel border in dimension.
	 * @param gradient
	 *            output image
	 * @param dimension
	 *            along which dimension the partial derivatives are computed
	 */
	public static < T extends NumericType< T > > void gradientCentralDifference2( final RandomAccessible< T > source, final RandomAccessibleInterval< T > gradient, final int dimension )
	{
		final Cursor< T > front = Views.flatIterable( Views.interval( source, Intervals.translate( gradient, 1, dimension ) ) ).cursor();
		final Cursor< T > back = Views.flatIterable( Views.interval( source, Intervals.translate( gradient, -1, dimension ) ) ).cursor();

		for ( final T t : Views.flatIterable( gradient ) )
		{
			t.set( front.next() );
			t.sub( back.next() );
			t.mul( 0.5 );
		}
	}

	// parallel version...
	/**
	 * Compute the partial derivative of source in a particular dimension.
	 *
	 * @param source
	 *            source image, has to provide valid data in the interval of the
	 *            gradient image plus a one pixel border in dimension.
	 * @param gradient
	 *            output image
	 * @param dimension
	 *            along which dimension the partial derivatives are computed
	 * @param nTasks
	 *            Number of tasks for gradient computation.
	 * @param es
	 *            {@link ExecutorService} providing workers for gradient
	 *            computation. Service is managed (created, shutdown) by caller.
	 */
	public static < T extends NumericType< T > > void gradientCentralDifferenceParallel(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< T > gradient,
			final int dimension,
			final int nTasks,
			final ExecutorService es ) throws InterruptedException, ExecutionException
	{
		final int nDim = source.numDimensions();
		if ( nDim < 2 )
		{
			gradientCentralDifference( source, gradient, dimension );
			return;
		}

		long dimensionMax = Long.MIN_VALUE;
		int dimensionArgMax = -1;

		for ( int d = 0; d < nDim; ++d )
		{
			final long size = gradient.dimension( d );
			if ( d != dimension && size > dimensionMax )
			{
				dimensionMax = size;
				dimensionArgMax = d;
			}
		}

		final long stepSize = Math.max( dimensionMax / nTasks, 1 );
		final long stepSizeMinusOne = stepSize - 1;
		final long min = gradient.min( dimensionArgMax );
		final long max = gradient.max( dimensionArgMax );

		final ArrayList< Callable< Void > > tasks = new ArrayList<>();
		for ( long currentMin = min, minZeroBase = 0; minZeroBase < dimensionMax; currentMin += stepSize, minZeroBase += stepSize )
		{
			final long currentMax = Math.min( currentMin + stepSizeMinusOne, max );
			final long[] mins = new long[ nDim ];
			final long[] maxs = new long[ nDim ];
			gradient.min( mins );
			gradient.max( maxs );
			mins[ dimensionArgMax ] = currentMin;
			maxs[ dimensionArgMax ] = currentMax;
			final IntervalView< T > currentInterval = Views.interval( gradient, new FinalInterval( mins, maxs ) );
			tasks.add( () -> {
				gradientCentralDifference( source, currentInterval, dimension );
				return null;
			} );
		}

		final List< Future< Void > > futures = es.invokeAll( tasks );

		for ( final Future< Void > f : futures )
			f.get();
	}

	// fast version
	/**
	 * Compute the partial derivative of source in a particular dimension.
	 *
	 * @param source
	 *            source image, has to provide valid data in the interval of the
	 *            gradient image plus a one pixel border in dimension.
	 * @param gradient
	 *            output image
	 * @param dimension
	 *            along which dimension the partial derivatives are computed
	 */
	public static < T extends NumericType< T > > void gradientCentralDifference( final RandomAccessible< T > source, final RandomAccessibleInterval< T > gradient, final int dimension )
	{
		final int n = gradient.numDimensions();

		final long[] min = new long[ n ];
		gradient.min( min );
		final long[] max = new long[ n ];
		gradient.max( max );
		final long[] shiftback = new long[ n ];
		for ( int d = 0; d < n; ++d )
			shiftback[ d ] = min[ d ] - max[ d ];

		final RandomAccess< T > result = gradient.randomAccess();
		final RandomAccess< T > back = source.randomAccess( Intervals.translate( gradient, 1, dimension ) );
		final RandomAccess< T > front = source.randomAccess( Intervals.translate( gradient, -1, dimension ) );

		result.setPosition( min );
		back.setPosition( min );
		back.bck( dimension );
		front.setPosition( min );
		front.fwd( dimension );

		final long max0 = max[ 0 ];
		while ( true )
		{
			// process pixel
			final T t = result.get();
			t.set( front.get() );
			t.sub( back.get() );
			t.mul( 0.5 );

			// move to next pixel
			// check dimension 0 separately to avoid the loop over d in most
			// iterations
			if ( result.getLongPosition( 0 ) == max0 )
			{
				if ( n == 1 )
					return;
				result.move( shiftback[ 0 ], 0 );
				back.move( shiftback[ 0 ], 0 );
				front.move( shiftback[ 0 ], 0 );
				// now check the remaining dimensions
				for ( int d = 1; d < n; ++d )
					if ( result.getLongPosition( d ) == max[ d ] )
					{
						result.move( shiftback[ d ], d );
						back.move( shiftback[ d ], d );
						front.move( shiftback[ d ], d );
						if ( d == n - 1 )
							return;
					}
					else
					{
						result.fwd( d );
						back.fwd( d );
						front.fwd( d );
						break;
					}
			}
			else
			{
				result.fwd( 0 );
				back.fwd( 0 );
				front.fwd( 0 );
			}
		}
	}
}
