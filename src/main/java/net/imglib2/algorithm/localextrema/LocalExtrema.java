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

import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.readwrite.WriteConvertedRandomAccessible;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Provides {@link #findLocalExtrema} to find pixels that are extrema in their
 * local neighborhood.
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
		 * {@code P} that represents the maximum. Otherwise return
		 * {@code null}.
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
	 * The task is parallelized along the last dimension of {@code source}.
	 *
	 * The number of tasks for parallelization is determined as:
	 * {@code Math.max( Math.min( maxSizeDim, numThreads * 20 ), 1 )}
	 *
	 * where {@code maxSizeDim} is the longest dimension of
	 * {@code img} after adjusting for the bounding box of a
	 * {@link RectangleShape} with span 1, and numThreads is
	 * {@code Runtime.getRuntime().availableProcessors()}
	 *
	 * {@link RectangleShape} is used as local neighborhood.
	 *
	 * Note: Pixels within 1 point of the {@code source} border will be
	 * ignored as local extrema candidates because the complete neighborhood
	 * would not be included in {@code source}. To include those pixel,
	 * expand {@code source} accordingly. The returned coordinate list is
	 * valid for the original {@code source}.
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
	@Deprecated
	public static < P, T > ArrayList< P > findLocalExtrema( final RandomAccessibleInterval< T > source, final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck, final ExecutorService service )
	{
		final RectangleShape shape = new RectangleShape( 1, true );
		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );
		final int nDim = source.numDimensions();
		final int splitDim = nDim - 1;
		// Get biggest dimension after border subtraction. Parallelize along
		// this dimension.
		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numTasks = Math.max( Math.min( ( int ) shrink( source, borderSize ).dimension( splitDim ), numThreads * 20 ), 1 );

		try
		{
			return ( ArrayList< P > ) findLocalExtrema( source, shrink( source, borderSize ), localNeighborhoodCheck, shape, service, numTasks, splitDim );
		}
		catch ( InterruptedException | ExecutionException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * The task is parallelized along the longest dimension of
	 * {@code source} after adjusting for size based on {@code shape}.
	 *
	 * Note: Pixels within a margin of {@code source} border as determined
	 * by {@link #getRequiredBorderSize(Shape, int)} will be ignored as local
	 * extrema candidates because the complete neighborhood would not be
	 * included in {@code source}. To include those pixel, expand
	 * {@code source} accordingly. The returned coordinate list is valid
	 * for the original {@code source}.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @param numTasks
	 *            Number of tasks for parallel execution
	 * @return {@link List} of extrema
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape,
			final ExecutorService service,
			final int numTasks ) throws InterruptedException, ExecutionException
	{
		final int splitDim = getBiggestDimension( shrink( source, getRequiredBorderSize( shape, source.numDimensions() ) ) );
		return findLocalExtrema( source, localNeighborhoodCheck, shape, service, numTasks, splitDim );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * Note: Pixels within a margin of {@code source} border as determined
	 * by {@link #getRequiredBorderSize(Shape, int)} will be ignored as local
	 * extrema candidates because the complete neighborhood would not be
	 * included in {@code source}. To include those pixel, expand
	 * {@code source} accordingly. The returned coordinate list is valid
	 * for the original {@code source}.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @param numTasks
	 *            Number of tasks for parallel execution
	 * @param splitDim
	 *            Dimension along which input should be split for parallization
	 * @return {@link List} of extrema
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape,
			final ExecutorService service,
			final int numTasks,
			final int splitDim ) throws InterruptedException, ExecutionException
	{
		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );
		return findLocalExtrema( source, shrink( source, borderSize ), localNeighborhoodCheck, shape, service, numTasks, splitDim );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * The task is parallelized along the longest dimension of
	 * {@code interval}
	 *
	 * @param source
	 *            Find local extrema of the function defined by this
	 *            {@link RandomAccessible}
	 * @param interval
	 *            Domain in which to look for local extrema. It is the callers
	 *            responsibility to ensure that {@code source} is defined
	 *            in all neighborhoods of {@code interval}.
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @param numTasks
	 *            Number of tasks for parallel execution
	 * @return {@link List} of extrema
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessible< T > source,
			final Interval interval,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape,
			final ExecutorService service,
			final int numTasks ) throws InterruptedException, ExecutionException
	{
		final int splitDim = getBiggestDimension( interval );
		return findLocalExtrema( source, interval, localNeighborhoodCheck, shape, service, numTasks, splitDim );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * @param source
	 *            Find local extrema of the function defined by this
	 *            {@link RandomAccessible}
	 * @param interval
	 *            Domain in which to look for local extrema. It is the callers
	 *            responsibility to ensure that {@code source} is defined
	 *            in all neighborhoods of {code interval}.
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood.
	 * @param service
	 *            {@link ExecutorService} handles parallel tasks
	 * @param numTasks
	 *            Number of tasks for parallel execution
	 * @param splitDim
	 *            Dimension along which input should be split for parallization
	 * @return {@link List} of extrema
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessible< T > source,
			final Interval interval,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape,
			final ExecutorService service,
			final int numTasks,
			final int splitDim ) throws InterruptedException, ExecutionException
	{
		TaskExecutor taskExecutor = TaskExecutors.forExecutorServiceAndNumTasks( service, numTasks );
		return Parallelization.runWithExecutor( taskExecutor, () -> findLocalExtrema( source, interval, localNeighborhoodCheck, shape ) );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * {@link RectangleShape} is used as local neighborhood.
	 *
	 * Note: Pixels within 1 point of the {@code source} border will be
	 * ignored as local extrema candidates because the complete neighborhood
	 * would not be included in {@code source}. To include those pixel,
	 * expand {@code source} accordingly. The returned coordinate list is
	 * valid for the original {@code source}.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum.
	 * @return {@link List} of extrema
	 */
	public static < P, T > List< P > findLocalExtrema(
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
	 * Note: Pixels within a margin of {@code source} border as determined
	 * by {@link #getRequiredBorderSize(Shape, int)} will be ignored as local
	 * extrema candidates because the complete neighborhood would not be
	 * included in {@code source}. To include those pixel, expand
	 * {@code source} accordingly. The returned coordinate list is valid
	 * for the original {@code source}.
	 *
	 * @param source
	 *            Find local extrema within this
	 *            {@link RandomAccessibleInterval}
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood
	 * @return {@link List} of extrema
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessibleInterval< T > source,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape )
	{

		final long[] borderSize = getRequiredBorderSize( shape, source.numDimensions() );

		assert Arrays.stream( borderSize ).min().getAsLong() >= 0: "Border size cannot be smaller than zero.";

		// TODO use Intervals.expand once it is available in non-SNAPSHOT
		// version
		return findLocalExtrema( source, shrink( source, borderSize ), localNeighborhoodCheck, shape );
	}

	/**
	 * Find pixels that are extrema in their local neighborhood. The specific
	 * test for being an extremum can be specified as an implementation of the
	 * {@link LocalNeighborhoodCheck} interface.
	 *
	 * The local neighborhood is defined as {@link RectangleShape} with span 1.
	 *
	 * @param source
	 *            Find local extrema within this {@link RandomAccessible}
	 * @param interval
	 *            Specifies the domain within which to look for extrema
	 * @param localNeighborhoodCheck
	 *            Check if current pixel qualifies as local maximum. It is the
	 *            callers responsibility to pass a
	 *            {@link LocalNeighborhoodCheck} that avoids the center pixel if
	 *            {@code shape} does not skip the center pixel.
	 * @return {@link List} of extrema
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessible< T > source,
			final Interval interval,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck )
	{
		return findLocalExtrema( source, interval, localNeighborhoodCheck, new RectangleShape( 1, true ) );
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
	 *            {@code shape} does not skip the center pixel.
	 * @param shape
	 *            Defines the local neighborhood
	 * @return {@link List} of extrema
	 */
	public static < P, T > List< P > findLocalExtrema(
			final RandomAccessible< T > source,
			final Interval interval,
			final LocalNeighborhoodCheck< P, T > localNeighborhoodCheck,
			final Shape shape )
	{
		WriteConvertedRandomAccessible< T, RandomAccess< T > > randomAccessible = new WriteConvertedRandomAccessible<>( source, sampler -> (RandomAccess< T >) sampler );
		RandomAccessibleInterval< RandomAccess< T > > centers = Views.interval( randomAccessible, interval);
		RandomAccessibleInterval< Neighborhood< T > > neighborhoods = Views.interval( shape.neighborhoodsRandomAccessible( source ), interval );
		List< List< P > > extremas = LoopBuilder.setImages( centers, neighborhoods ).multiThreaded().forEachChunk( chunk -> {
			List< P > extrema = new ArrayList<>();
			chunk.forEachPixel( ( center, neighborhood ) -> {
				P p = localNeighborhoodCheck.check( center, neighborhood );
				if ( p != null )
					extrema.add( p );
			} );
			return extrema;
		} );
		return concatenate( extremas );
	}

	private static < P > List<P> concatenate( Collection<List<P>> lists )
	{
		int size = lists.stream().mapToInt( List::size ).sum();
		List< P > result = new ArrayList<>( size );
		for ( List< P > list : lists )
			result.addAll( list );
		return result;
	}

	/**
	 *
	 * Get the required border size based on the bounding box of the
	 * neighborhood specified by {@code shape}. This is useful for
	 * determining by how much a {@link RandomAccessibleInterval} should be
	 * expanded to include min and max positions in the local extrema search.
	 *
	 * @param shape
	 *            Defines the local neighborhood
	 * @param nDim
	 *            Number of dimensions.
	 * @return The required border size for the local neighborhood specified by
	 *         {@code shape}
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
	 * Shrink a {@link RandomAccessibleInterval} symmetrically, i.e. the margin
	 * is applied both to min and max.
	 *
	 * @param source
	 * @param margin
	 * @return
	 */
	public static < T > IntervalView< T > shrink( final RandomAccessibleInterval< T > source, final long[] margin )
	{
		assert margin.length == source.numDimensions(): "Dimensionality mismatch.";
		assert Arrays.stream( margin ).min().getAsLong() >= 0: "Margin cannot be negative";
		assert IntStream.range( 0, margin.length ).mapToLong( d -> source.dimension( d ) - 2 * margin[ d ] ).min().getAsLong() >= 0: "Margin bigger than input";

		return Views.expandBorder( source, Arrays.stream(margin).map(m -> -m).toArray());
	}

	/**
	 *
	 * @param interval
	 * @return The biggest dimension of interval.
	 */
	public static int getBiggestDimension( final Interval interval )
	{
		final int nDim = interval.numDimensions();
		final int splitDim = IntStream.range( 0, nDim ).mapToObj( d -> new ValuePair<>( d, interval.dimension( d ) ) ).max( ( p1, p2 ) -> Long.compare( p1.getB(), p2.getB() ) ).get().getA();
		return splitDim;
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
