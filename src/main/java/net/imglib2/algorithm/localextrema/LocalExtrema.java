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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Provides
 * {@link #findLocalExtrema(RandomAccessibleInterval, LocalNeighborhoodCheck, int)}
 * to find pixels that are extrema in their local neighborhood.
 *
 * @author Tobias Pietzsch
 * @author Philipp Hanslovsky
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
	public interface LocalNeighborhoodCheck< P, T >
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
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * The task is parallelized along the longest dimension of <code>img</code>
	 * after adjusting for size based on <code>shape</code>.
	 *
	 * The number of tasks for parallelization is determined as:
	 * <code>Math.max( Math.min( maxSizeDim, numThreads * 20 ), 1 )</code>
	 *
	 * where <code>maxSizeDim</code> is the longest dimension of
	 * <code>img</code> after adjusting for the bounding box of a
	 * {@link RectangleShape} with span 1, and numThreads is
	 * <code>Runtime.getRuntime().availableProcessors()</code>
	 *
	 * {@link RectangleShape} is used as local neighborhood.
	 *
	 * Note: Pixels within 1 point of the <code>source</code> border will be
	 * ignored as local extrema candidates because the complete neighborhood
	 * would not be included in <code>source</code>. To include those pixel,
	 * expand <code>source</code> accordingly. The returned coordinate list is
	 * valid for the original <code>source</code>.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @return {@link ArrayList} of extrema
	 */
	public static < P, T > ArrayList< P > findLocalExtrema( final RandomAccessibleInterval< T > source, final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck, final ExecutorService service )
	{
		final RectangleShape shape = new RectangleShape( 1, true );
		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );
		final int nDim = source.numDimensions();
		// Get biggest dimension after border subtraction. Parallelize along
		// this dimension.
		final int maxSizeDim = IntStream.range( 0, nDim ).mapToObj( d -> new ValuePair<>( d, source.dimension( d ) - 2 * borderSize[ d ] ) ).min( ( p1, p2 ) -> Long.compare( p1.getB(), p2.getB() ) ).get().getA();
		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numTasks = Math.max( Math.min( maxSizeDim, numThreads * 20 ), 1 );

		return findLocalExtrema( source, localNeighborhoodCheck, shape, service, numTasks );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * The task is parallelized along the longest dimension of <code>img</code>
	 * after adjusting for size based on <code>shape</code>.
	 *
	 * Note: Pixels within a margin of <code>source</code> border as determined
	 * by {@link #getRequiredBorderSize(Shape, int)} will be ignored as local
	 * extrema candidates because the complete neighborhood would not be
	 * included in <code>source</code>. To include those pixel, expand
	 * <code>source</code> accordingly. The returned coordinate list is valid
	 * for the original <code>source</code>.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            <code>shape</code> does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @param numTasks
	 *            Number of tasks for parallel execution
	 * @return {@link ArrayList} of extrema
	 */
	public static < P, T > ArrayList< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape,
			final ExecutorService service,
			final int numTasks )
	{

		final int nDim = source.numDimensions();
		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );

		final long[] shrinkMin = IntStream.range( 0, nDim ).mapToLong( d -> source.min( d ) + borderSize[ d ] ).toArray();
		final long[] shrinkMax = IntStream.range( 0, nDim ).mapToLong( d -> source.max( d ) - borderSize[ d ] ).toArray();

		final int maxSizeDim = IntStream.range( 0, nDim ).mapToObj( d -> new ValuePair<>( d, source.dimension( d ) - 2 * borderSize[ d ] ) ).min( ( p1, p2 ) -> Long.compare( p1.getB(), p2.getB() ) ).get().getA();
		final long maxDimSize = source.dimension( maxSizeDim ) - 2 * borderSize[ maxSizeDim ];
		final long maxDimMax = source.max( maxSizeDim ) - borderSize[ maxSizeDim ];
		final long taskSize = Math.max( maxDimSize / numTasks, 1 );

		final ArrayList< Callable< ArrayList< P > > > tasks = new ArrayList<>();

		for ( long start = borderSize[ maxSizeDim ]; start <= maxDimMax; start += taskSize )
		{
			final long s = Math.max( start, borderSize[ maxSizeDim ] );
			// need max here instead of dimension for constructor of
			// FinalInterval
			final long S = Math.min( start + taskSize - 1, maxDimMax );
			tasks.add( () -> {
				final long[] min = shrinkMin.clone();
				final long[] max = shrinkMax.clone();
				min[ maxSizeDim ] = s;
				max[ maxSizeDim ] = S;
				return findLocalExtrema( source, new FinalInterval( min, max ), localNeighborhoodCheck, shape );
			} );
		}

		final ArrayList< P > extrema = new ArrayList<>();
		// TODO It is probably better to throw exception than to use try/catch
		// block and return potentially incomplete/inconsistent list of extrema.
		// Returning an empty list on exception could be a compromise without
		// changing the interface.
		try
		{
			final List< Future< ArrayList< P > > > futures = service.invokeAll( tasks );
			for ( final Future< ArrayList< P > > f : futures )
				try
			{
					extrema.addAll( f.get() );
			}
			catch ( final ExecutionException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch ( final InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extrema;

	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * {@link RectangleShape} is used as local neighborhood.
	 *
	 * Note: Pixels within 1 point of the <code>source</code> border will be
	 * ignored as local extrema candidates because the complete neighborhood
	 * would not be included in <code>source</code>. To include those pixel,
	 * expand <code>source</code> accordingly. The returned coordinate list is
	 * valid for the original <code>source</code>.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum.
	 * @return {@link ArrayList} of extrema
	 */
	public static < P, T > ArrayList< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck )
	{
		return findLocalExtrema( source, localNeighborhoodCheck, new RectangleShape( 1, true ) );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * Note: Pixels within a margin of <code>source</code> border as determined
	 * by {@link #getRequiredBorderSize(Shape, int)} will be ignored as local
	 * extrema candidates because the complete neighborhood would not be
	 * included in <code>source</code>. To include those pixel, expand
	 * <code>source</code> accordingly. The returned coordinate list is valid
	 * for the original <code>source</code>.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            <code>shape</code> does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood
	 * @return {@link ArrayList} of extrema
	 */
	public static < P, T > ArrayList< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape )
	{

		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );

		assert Arrays.stream( borderSize ).min().getAsLong() >= 0: "Border size cannot be smaller than zero.";

		// TODO use Intervals.expand once it is available in non-SNAPSHOT
		// version
		final int nDim = source.numDimensions();
		final long[] min = IntStream.range( 0, nDim ).mapToLong( d -> source.min( d ) + borderSize[ d ] ).toArray();
		final long[] max = IntStream.range( 0, nDim ).mapToLong( d -> source.max( d ) - borderSize[ d ] ).toArray();
		return findLocalExtrema( source, new FinalInterval( min, max ), localNeighborhoodCheck, shape );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * @param source
	 *            Find local extrema within this {@link RandomAccessible}
	 * @param interval
	 *            Specifies the domain within which to look for extrema
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            <code>shape</code> does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood
	 * @return {@link ArrayList} of extrema
	 */
	public static < P, T > ArrayList< P > findLocalExtrema(
			final RandomAccessible< T > source,
			final Interval interval,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape )
	{

		final IntervalView< T > sourceInterval = Views.interval( source, interval );

		final ArrayList< P > extrema = new ArrayList<>();

		final Cursor< T > center = Views.flatIterable( sourceInterval ).cursor();
		for ( final Neighborhood< T > neighborhood : shape.neighborhoods( sourceInterval ) )
		{
			center.fwd();
			final P p = localNeighborhoodCheck.check( center, neighborhood );
			if ( p != null )
				extrema.add( p );
		}

		return extrema;

	}

	/**
	 *
	 * Get the required border size based on the bounding box of the
	 * neighborhood specified by <code>shape</code>. This is useful for
	 * determining by how much a {@link RandomAccessibleInterval} should be
	 * expanded to include min and max positions in the local extrema search.
	 *
	 * @param shape
	 *            Defines the local neighborhood
	 * @param nDim
	 *            Number of dimensions.
	 * @return The required border size for the local neighborhood specified by
	 *         <code>shape</code>
	 */
	public static long[] getRequiredBorderSize( final Shape shape, final int nDim )
	{
		final RandomAccessible< Neighborhood< Object > > neighborhood = shape.neighborhoodsRandomAccessible( ConstantUtils.constantRandomAccessible( new Object(), nDim ) );
		final long[] min = LongStream.generate( () -> Long.MAX_VALUE ).limit( nDim ).toArray();
		final long[] max = LongStream.generate( () -> Long.MIN_VALUE ).limit( nDim ).toArray();
		final Interval bb = neighborhood.randomAccess().get().getStructuringElementBoundingBox();

		for ( int d = 0; d < nDim; ++d ) {
			min[ d ] = Math.min( bb.min( d ), min[ d ] );
			max[ d ] = Math.max( bb.max( d ), max[ d ] );
		}

		final long[] borderSize = IntStream.range( 0, nDim ).mapToLong( d -> Math.max( max[ d ], -min[ d ] ) ).toArray();

		return borderSize;
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
