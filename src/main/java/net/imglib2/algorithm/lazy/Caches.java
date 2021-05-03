/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.lazy;

import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;
import static net.imglib2.type.PrimitiveType.BYTE;
import static net.imglib2.type.PrimitiveType.DOUBLE;
import static net.imglib2.type.PrimitiveType.FLOAT;
import static net.imglib2.type.PrimitiveType.INT;
import static net.imglib2.type.PrimitiveType.LONG;
import static net.imglib2.type.PrimitiveType.SHORT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.GenericIntType;
import net.imglib2.type.numeric.integer.GenericLongType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * A simple method to cache an arbitrary a {@link RandomAccessibleInterval} of
 * the typical {@link NativeType} implementations in a memory cell image with
 * volatile cells.
 *
 * TODO These methods should be in imglib2-cache.
 *
 * @author Stephan Saalfeld
 */
public interface Caches
{
	/**
	 * A simple {@link CellLoader} implementation that fills a pre-allocated
	 * cell with data from a {@link RandomAccessible} source at the same
	 * coordinates.
	 *
	 * @param <T>
	 */
	public static class RandomAccessibleLoader< T extends NativeType< T > > implements CellLoader< T >
	{
		private final RandomAccessible< T > source;

		public RandomAccessibleLoader( final RandomAccessible< T > source )
		{
			super();
			this.source = source;
		}

		@Override
		public void load( final SingleCellArrayImg< T, ? > cell )
		{
			for ( Cursor< T > s = Views.flatIterable( Views.interval( source, cell ) ).cursor(),
					t = cell.cursor(); s.hasNext(); )
				t.next().set( s.next() );
		}
	}

	/**
	 * Cache a {@link RandomAccessibleInterval} of the typical
	 * {@link NativeType} implementations in a memory cell image with volatile
	 * cells. The result can be used with non-volatile types for processing but
	 * it can also be wrapped into volatile types for visualization, see
	 * {@code VolatileViews.wrapAsVolatile(RandomAccessible)}.
	 *
	 * This is a very naive method to implement this kind of cache, but it
	 * serves the purpose for this tutorial. The imglib2-cache library offers
	 * more control over caches, and you should go and test it out.
	 *
	 * @param <T>
	 * @param source
	 * @param blockSize
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static < T extends NativeType< T > > RandomAccessibleInterval< T > cache( final RandomAccessibleInterval< T > source, final int... blockSize )
	{
		final long[] dimensions = Intervals.dimensionsAsLongArray( source );
		final CellGrid grid = new CellGrid( dimensions, blockSize );

		final RandomAccessibleLoader< T > loader = new RandomAccessibleLoader< T >( Views.zeroMin( source ) );

		final T type = Util.getTypeFromInterval( source );

		final CachedCellImg< T, ? > img;
		final Cache< Long, Cell< ? > > cache = new SoftRefLoaderCache().withLoader( LoadedCellCacheLoader.get( grid, loader, type, AccessFlags.setOf( VOLATILE ) ) );

		if ( GenericByteType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( BYTE, AccessFlags.setOf( VOLATILE ) ) );
		}
		else if ( GenericShortType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( SHORT, AccessFlags.setOf( VOLATILE ) ) );
		}
		else if ( GenericIntType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( INT, AccessFlags.setOf( VOLATILE ) ) );
		}
		else if ( GenericLongType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( LONG, AccessFlags.setOf( VOLATILE ) ) );
		}
		else if ( FloatType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( FLOAT, AccessFlags.setOf( VOLATILE ) ) );
		}
		else if ( DoubleType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( DOUBLE, AccessFlags.setOf( VOLATILE ) ) );
		}
		else
		{
			img = null;
		}

		return img;
	}

	/**
	 * Trigger pre-fetching of an {@link Interval} in a {@link RandomAccessible}
	 * by concurrent sampling of values at a sparse grid.
	 *
	 * Pre-fetching is only triggered and the set of value sampling
	 * {@link Future}s is returned such that it can be used to wait for
	 * completion or ignored (typically, ignoring will be best).
	 *
	 * This method is most useful to reduce wasted time waiting for high latency
	 * data loaders (such as AWS S3 or GoogleCloud). Higher and more random
	 * latency benefit from higher parallelism, e.g. total parallelism with
	 * {@link Executors#newCachedThreadPool()}. Medium latency loaders may be
	 * served better with a limited number of threads, e.g.
	 * {@link Executors#newFixedThreadPool(int)}. The optimal solution depends
	 * also on how the rest of the application is parallelized and how much
	 * caching memory is available.
	 *
	 * We do not suggest to use this to fill a {@link CachedCellImg} with a
	 * generator because now the {@link ExecutorService} will do the complete
	 * processing work without guarantees that the generated cells will persist.
	 *
	 * @param <T>
	 * @param source
	 * @param interval
	 * @param spacing
	 * @param exec
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T > ArrayList< Future< T > > preFetch( final RandomAccessible< T > source, final Interval interval, final long[] spacing, final ExecutorService exec ) throws InterruptedException, ExecutionException
	{
		final int n = interval.numDimensions();

		final long[] max = new long[ n ];
		Arrays.setAll( max, d -> interval.max( d ) + spacing[ d ] );

		final ArrayList< Future< T > > futures = new ArrayList<>();

		final long[] offset = Intervals.minAsLongArray( interval );
		for ( int d = 0; d < n; )
		{
			final long[] offsetLocal = offset.clone();
			futures.add( exec.submit( () -> {
				final RandomAccess< T > access = source.randomAccess( interval );
				access.setPosition( offsetLocal );
				return access.get();
			} ) );

			for ( d = 0; d < n; ++d )
			{
				offset[ d ] += spacing[ d ];
				if ( offset[ d ] < max[ d ] )
				{
					offset[ d ] = Math.min( offset[ d ], interval.max( d ) );
					break;
				}
				else
					offset[ d ] = interval.min( d );
			}
		}

		return futures;
	}
}
