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

package net.imglib2.algorithm.fill;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.Type;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

/**
 * Iterative n-dimensional flood fill for arbitrary neighborhoods.
 *
 * @author Philipp Hanslovsky
 * @author Stephan Saalfeld
 */
public class FloodFill
{
	// int or long? current TLongList cannot store more than Integer.MAX_VALUE
	private static final int CLEANUP_THRESHOLD = ( int ) 1e5;

	/**
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet.
	 *
	 * Convenience call to
	 * {@link #fill(RandomAccessible, RandomAccessible, Localizable, Type, Shape, BiPredicate)}.
	 * seedLabel is extracted from source at seed location.
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param fillLabel
	 *            Immutable. Value to be written into valid flood fill
	 *            locations.
	 * @param shape
	 *            Defines neighborhood that is considered for connected
	 *            components, e.g.
	 *            {@link net.imglib2.algorithm.neighborhood.DiamondShape}
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	public static < T extends Type< T >, U extends Type< U > > void fill(
			final RandomAccessible< T > source,
			final RandomAccessible< U > target,
			final Localizable seed,
			final U fillLabel,
			final Shape shape )
	{
		final RandomAccess< T > access = source.randomAccess();
		access.setPosition( seed );
		final T seedValue = access.get().copy();
		final BiPredicate< T, U > filter = ( t, u ) -> t.valueEquals( seedValue ) && !u.valueEquals( fillLabel );
		fill( source, target, seed, fillLabel, shape, filter );
	}

	/**
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet.
	 *
	 * Convenience call to
	 * {@link FloodFill#fill(RandomAccessible, RandomAccessible, Localizable, Shape, BiPredicate, Consumer)}
	 * with {@link Type#set} as writer.
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param fillLabel
	 *            Immutable. Value to be written into valid flood fill
	 *            locations.
	 * @param shape
	 *            Defines neighborhood that is considered for connected
	 *            components, e.g.
	 *            {@link net.imglib2.algorithm.neighborhood.DiamondShape}
	 * @param filter
	 *            Returns true if pixel has not been visited yet and should be
	 *            written into. Returns false if target pixel has been visited
	 *            or source pixel is not part of the same connected component.
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	public static < T, U extends Type< U > > void fill(
			final RandomAccessible< T > source,
			final RandomAccessible< U > target,
			final Localizable seed,
			final U fillLabel,
			final Shape shape,
			final BiPredicate< T, U > filter )
	{
		fill( source, target, seed, shape, filter, targetPixel -> targetPixel.set( fillLabel ) );
	}

	/**
	 *
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet.
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param shape
	 *            Defines neighborhood that is considered for connected
	 *            components, e.g.
	 *            {@link net.imglib2.algorithm.neighborhood.DiamondShape}
	 * @param filter
	 *            Returns true if pixel has not been visited yet and should be
	 *            written into. Returns false if target pixel has been visited
	 *            or source pixel is not part of the same connected component.
	 * @param writer
	 *            Defines how fill label is written into target at current
	 *            location.
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	public static < T, U > void fill(
			final RandomAccessible< T > source,
			final RandomAccessible< U > target,
			final Localizable seed,
			final Shape shape,
			final BiPredicate< T, U > filter,
			final Consumer< U > writer )
	{
		final int n = source.numDimensions();

		final RandomAccessible< Pair< T, U > > paired = Views.pair( source, target );

		TLongList coordinates = new TLongArrayList();
		for ( int d = 0; d < n; ++d )
		{
			coordinates.add( seed.getLongPosition( d ) );
		}

		final int cleanupThreshold = n * CLEANUP_THRESHOLD;

		final RandomAccessible< Neighborhood< Pair< T, U > > > neighborhood = shape.neighborhoodsRandomAccessible( paired );
		final RandomAccess< Neighborhood< Pair< T, U > > > neighborhoodAccess = neighborhood.randomAccess();

		final RandomAccess< U > targetAccess = target.randomAccess();
		targetAccess.setPosition( seed );
		writer.accept( targetAccess.get() );

		for ( int i = 0; i < coordinates.size(); i += n )
		{
			for ( int d = 0; d < n; ++d )
				neighborhoodAccess.setPosition( coordinates.get( i + d ), d );

			final Cursor< Pair< T, U > > neighborhoodCursor = neighborhoodAccess.get().cursor();

			while ( neighborhoodCursor.hasNext() )
			{
				final Pair< T, U > p = neighborhoodCursor.next();
				if ( filter.test( p.getA(), p.getB() ) )
				{
					writer.accept( p.getB() );
					for ( int d = 0; d < n; ++d )
						coordinates.add( neighborhoodCursor.getLongPosition( d ) );
				}
			}

			if ( i > cleanupThreshold )
			{
				// TODO should it start from i + n?
				coordinates = coordinates.subList( i, coordinates.size() );
				i = 0;
			}

		}

	}

	/**
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet (comparator evaluates to 0).
	 *
	 * Convenience call to
	 * {@link FloodFill#fill(RandomAccessible, RandomAccessible, Localizable, Object, Type, Shape, Filter)}
	 * . seedLabel is extracted from source at seed location.
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param fillLabel
	 *            Immutable. Value to be written into valid flood fill
	 *            locations.
	 * @param filter
	 *            Returns true if pixel has not been visited yet and should be
	 *            written into. Returns false if target pixel has been visited
	 *            or source pixel is not part of the same connected component.
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	@Deprecated
	public static < T extends Type< T >, U extends Type< U > > void fill( final RandomAccessible< T > source, final RandomAccessible< U > target, final Localizable seed, final U fillLabel, final Shape shape, final Filter< Pair< T, U >, Pair< T, U > > filter )
	{
		final RandomAccess< T > access = source.randomAccess();
		access.setPosition( seed );
		fill( source, target, seed, access.get().copy(), fillLabel, shape, filter );
	}

	/**
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet (comparator evaluates to 0).
	 *
	 * Convenience call to
	 * {@link FloodFill#fill(RandomAccessible, RandomAccessible, Localizable, Object, Object, Shape, Filter, Writer)}
	 * with {@link TypeWriter} as writer.
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param seedLabel
	 *            Immutable. Reference value of input at seed location.
	 * @param fillLabel
	 *            Immutable. Value to be written into valid flood fill
	 *            locations.
	 * @param filter
	 *            Returns true if pixel has not been visited yet and should be
	 *            written into. Returns false if target pixel has been visited
	 *            or source pixel is not part of the same connected component.
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	@Deprecated
	public static < T, U extends Type< U > > void fill( final RandomAccessible< T > source, final RandomAccessible< U > target, final Localizable seed, final T seedLabel, final U fillLabel, final Shape shape, final Filter< Pair< T, U >, Pair< T, U > > filter )
	{
		fill( source, target, seed, seedLabel, fillLabel, shape, filter, new TypeWriter< U >() );
	}

	/**
	 *
	 * Iterative n-dimensional flood fill for arbitrary neighborhoods: Starting
	 * at seed location, write fillLabel into target at current location and
	 * continue for each pixel in neighborhood defined by shape if neighborhood
	 * pixel is in the same connected component and fillLabel has not been
	 * written into that location yet (comparator evaluates to 0).
	 *
	 * @param source
	 *            input
	 * @param target
	 *            {@link RandomAccessible} to be written into. May be the same
	 *            as input.
	 * @param seed
	 *            Start flood fill at this location.
	 * @param seedLabel
	 *            Immutable. Reference value of input at seed location.
	 * @param fillLabel
	 *            Immutable. Value to be written into valid flood fill
	 *            locations.
	 * @param shape
	 *            Defines neighborhood that is considered for connected
	 *            components, e.g.
	 *            {@link net.imglib2.algorithm.neighborhood.DiamondShape}
	 * @param filter
	 *            Returns true if pixel has not been visited yet and should be
	 *            written into. Returns false if target pixel has been visited
	 *            or source pixel is not part of the same connected component.
	 * @param writer
	 *            Defines how fillLabel is written into target at current
	 *            location.
	 * @param <T>
	 *            input pixel type
	 * @param <U>
	 *            fill label type
	 */
	@Deprecated
	public static < T, U > void fill( final RandomAccessible< T > source, final RandomAccessible< U > target, final Localizable seed, final T seedLabel, final U fillLabel, final Shape shape, final Filter< Pair< T, U >, Pair< T, U > > filter, final Writer< U > writer )
	{
		final int n = source.numDimensions();

		final ValuePair< T, U > reference = new ValuePair<>( seedLabel, fillLabel );

		final RandomAccessible< Pair< T, U > > paired = Views.pair( source, target );

		final TLongList[] coordinates = new TLongList[ n ];
		for ( int d = 0; d < n; ++d )
		{
			coordinates[ d ] = new TLongArrayList();
			coordinates[ d ].add( seed.getLongPosition( d ) );
		}

		final RandomAccessible< Neighborhood< Pair< T, U > > > neighborhood = shape.neighborhoodsRandomAccessible( paired );
		final RandomAccess< Neighborhood< Pair< T, U > > > neighborhoodAccess = neighborhood.randomAccess();

		final RandomAccess< U > targetAccess = target.randomAccess();
		targetAccess.setPosition( seed );
		writer.write( fillLabel, targetAccess.get() );

		for ( int i = 0; i < coordinates[ 0 ].size(); ++i )
		{
			for ( int d = 0; d < n; ++d )
				neighborhoodAccess.setPosition( coordinates[ d ].get( i ), d );

			final Cursor< Pair< T, U > > neighborhoodCursor = neighborhoodAccess.get().cursor();

			while ( neighborhoodCursor.hasNext() )
			{
				final Pair< T, U > p = neighborhoodCursor.next();
				if ( filter.accept( p, reference ) )
				{
					writer.write( fillLabel, p.getB() );
					for ( int d = 0; d < n; ++d )
						coordinates[ d ].add( neighborhoodCursor.getLongPosition( d ) );
				}
			}

			if ( i > CLEANUP_THRESHOLD )
			{
				for ( int d = 0; d < coordinates.length; ++d )
				{
					final TLongList c = coordinates[ d ];
					coordinates[ d ] = c.subList( i, c.size() );
				}
				i = 0;
			}

		}

	}

}
