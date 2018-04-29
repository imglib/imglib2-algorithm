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
package net.imglib2.algorithm.morphology;

import java.util.Vector;

import net.imglib2.Cursor;
import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.operators.Sub;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class MorphologyUtils
{

	/**
	 * Static util to compute the final image dimensions and required offset
	 * when performing a full dilation with the specified strel.
	 *
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the strel to use for dilation.
	 * @return a 2-elements {@code long[][]}:
	 *         <ol start="0">
	 *         <li>a {@code long[]} array with the final image target
	 *         dimensions.
	 *         <li>a {@code long[]} array with the offset to apply to the
	 *         source image.
	 *         </ol>
	 */
	public static final < T > long[][] computeTargetImageDimensionsAndOffset( final Interval source, final Shape strel )
	{
		/*
		 * Compute target image size
		 */

		final long[] targetDims;

		/*
		 * Get a neighborhood to play with. Note: if we would have a dedicated
		 * interface for structuring elements, that would extend Shape and
		 * Dimensions, we would need to do what we are going to do now. On top
		 * of that, this is the part that causes the full dilation not to be a
		 * real full dilation: if the structuring element has more dimensions
		 * than the source, they are ignored. This is because we use the source
		 * as the Dimension to create the sample neighborhood we play with.
		 */
		final Neighborhood< BitType > sampleNeighborhood = MorphologyUtils.getNeighborhood( strel, source );
		int ndims = sampleNeighborhood.numDimensions();
		ndims = Math.max( ndims, source.numDimensions() );
		targetDims = new long[ ndims ];
		for ( int d = 0; d < ndims; d++ )
		{
			long d1;
			if ( d < source.numDimensions() )
			{
				d1 = source.dimension( d );
			}
			else
			{
				d1 = 1;
			}

			long d2;
			if ( d < sampleNeighborhood.numDimensions() )
			{
				d2 = sampleNeighborhood.dimension( d );
			}
			else
			{
				d2 = 1;
			}

			targetDims[ d ] = d1 + d2 - 1;
		}

		// Offset coordinates so that they match the source coordinates, which
		// will not be extended.
		final long[] offset = new long[ source.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			if ( d < sampleNeighborhood.numDimensions() )
			{
				offset[ d ] = -sampleNeighborhood.min( d );
			}
			else
			{
				offset[ d ] = 0;
			}
		}

		return new long[][] { targetDims, offset };
	}

	static final void appendLine( final RandomAccess< BitType > ra, final long maxX, final StringBuilder str )
	{
		// Top line
		str.append( '┌' );
		for ( long x = 0; x < maxX; x++ )
		{
			str.append( '─' );
		}
		str.append( "┐\n" );
		// Center
		str.append( '│' );
		for ( long x = 0; x < maxX; x++ )
		{
			ra.setPosition( x, 0 );
			if ( ra.get().get() )
			{
				str.append( '█' );
			}
			else
			{
				str.append( ' ' );
			}
		}
		str.append( "│\n" );
		// Bottom line
		str.append( '└' );
		for ( long x = 0; x < maxX; x++ )
		{
			str.append( '─' );
		}
		str.append( "┘\n" );
	}

	private static final void appendManySlice( final RandomAccess< BitType > ra, final long maxX, final long maxY, final long maxZ, final StringBuilder str )
	{
		// Z names
		final long width = Math.max( maxX + 3, 9l );
		for ( int z = 0; z < maxZ; z++ )
		{
			final String sample = "Z = " + z + ":";
			str.append( sample );
			for ( int i = 0; i < width - sample.length(); i++ )
			{
				str.append( ' ' );
			}
		}
		str.append( '\n' );

		// Top line
		for ( int z = 0; z < maxZ; z++ )
		{
			str.append( '┌' );
			for ( long x = 0; x < maxX; x++ )
			{
				str.append( '─' );
			}
			str.append( "┐ " );
			for ( int i = 0; i < width - maxX - 3; i++ )
			{
				str.append( ' ' );
			}
		}
		str.append( '\n' );

		// Neighborhood
		for ( long y = 0; y < maxY; y++ )
		{
			ra.setPosition( y, 1 );

			for ( int z = 0; z < maxZ; z++ )
			{
				ra.setPosition( z, 2 );
				str.append( '│' );
				for ( long x = 0; x < maxX; x++ )
				{
					ra.setPosition( x, 0 );
					if ( ra.get().get() )
					{
						str.append( '█' );
					}
					else
					{
						str.append( ' ' );
					}
				}
				str.append( '│' );
				for ( int i = 0; i < width - maxX - 2; i++ )
				{
					str.append( ' ' );
				}
			}
			str.append( '\n' );
		}

		// Bottom line
		for ( int z = 0; z < maxZ; z++ )
		{
			str.append( '└' );
			for ( long x = 0; x < maxX; x++ )
			{
				str.append( '─' );
			}
			str.append( "┘ " );
			for ( int i = 0; i < width - maxX - 3; i++ )
			{
				str.append( ' ' );
			}
		}
		str.append( '\n' );
	}

	private static final void appendSingleSlice( final RandomAccess< BitType > ra, final long maxX, final long maxY, final StringBuilder str )
	{
		// Top line
		str.append( '┌' );
		for ( long x = 0; x < maxX; x++ )
		{
			str.append( '─' );
		}
		str.append( "┐\n" );
		for ( long y = 0; y < maxY; y++ )
		{
			str.append( '│' );
			ra.setPosition( y, 1 );
			for ( long x = 0; x < maxX; x++ )
			{
				ra.setPosition( x, 0 );
				if ( ra.get().get() )
				{
					str.append( '█' );
				}
				else
				{
					str.append( ' ' );
				}
			}
			str.append( "│\n" );
		}
		// Bottom line
		str.append( '└' );
		for ( long x = 0; x < maxX; x++ )
		{
			str.append( '─' );
		}
		str.append( "┘\n" );
	}

	static < T extends Type< T > > void copy( final IterableInterval< T > source, final RandomAccessible< T > target, final int numThreads )
	{
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( source.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology copy thread " + i )
			{
				@Override
				public void run()
				{
					final Cursor< T > sourceCursor = source.localizingCursor();
					sourceCursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > targetRandomAccess = target.randomAccess();

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						sourceCursor.fwd();
						targetRandomAccess.setPosition( sourceCursor );
						targetRandomAccess.get().set( sourceCursor.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}

	static < T extends Type< T > > void copy2( final RandomAccessible< T > source, final IterableInterval< T > target, final int numThreads )
	{
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( target.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology copy2 thread " + i )
			{
				@Override
				public void run()
				{
					final Cursor< T > targetCursor = target.localizingCursor();
					targetCursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > sourceRandomAccess = source.randomAccess();

					// iterate over the input cursor
					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						targetCursor.fwd();
						sourceRandomAccess.setPosition( targetCursor );
						targetCursor.get().set( sourceRandomAccess.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}

	static < T extends Type< T > > Img< T > copyCropped( final Img< T > largeSource, final Interval interval, final int numThreads )
	{
		final long[] offset = new long[ largeSource.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			offset[ d ] = ( largeSource.dimension( d ) - interval.dimension( d ) ) / 2;
		}
		final Img< T > create = largeSource.factory().create( interval );

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( create.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology copyCropped thread " + i )
			{
				@Override
				public void run()
				{
					final IntervalView< T > intervalView = Views.offset( largeSource, offset );
					final Cursor< T > cursor = create.cursor();
					cursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > randomAccess = intervalView.randomAccess();
					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						cursor.fwd();
						randomAccess.setPosition( cursor );
						cursor.get().set( randomAccess.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
		return create;
	}

	/**
	 * Get an instance of type T from a {@link RandomAccess} on accessible that
	 * is positioned at the min of interval.
	 *
	 * @param accessible
	 * @param interval
	 * @return type instance
	 */
	static < T extends Type< T >> T createVariable( final RandomAccessible< T > accessible, final Interval interval )
	{
		final RandomAccess< T > a = accessible.randomAccess();
		interval.min( a );
		return a.get().createVariable();
	}

	public static final Neighborhood< BitType > getNeighborhood( final Shape shape, final EuclideanSpace space )
	{
		final int numDims = space.numDimensions();
		final long[] dimensions = Util.getArrayFromValue( 1l, numDims );
		final ArrayImg< BitType, LongArray > img = ArrayImgs.bits( dimensions );
		final IterableInterval< Neighborhood< BitType >> neighborhoods = shape.neighborhoods( img );
		final Neighborhood< BitType > neighborhood = neighborhoods.cursor().next();
		return neighborhood;
	}

	/**
	 * Returns a string representation of the specified flat structuring element
	 * (given as a {@link Shape}), cast over the dimensionality specified by an
	 * {@link EuclideanSpace}.
	 * <p>
	 * This method only prints the first 3 dimensions of the structuring
	 * element. Dimensions above 3 are skipped.
	 *
	 * @param shape
	 *            the structuring element to print.
	 * @param dimensionality
	 *            the dimensionality to cast it over. This is required as
	 *            {@link Shape} does not carry a dimensionality, and we need one
	 *            to generate a neighborhood to iterate.
	 * @return a string representation of the structuring element.
	 */
	public static final String printNeighborhood( final Shape shape, final int dimensionality )
	{
		final Img< BitType > neighborhood;
		{
			final long[] dimensions = Util.getArrayFromValue( 1l, dimensionality );

			final ArrayImg< BitType, LongArray > img = ArrayImgs.bits( dimensions );
			final ArrayRandomAccess< BitType > randomAccess = img.randomAccess();
			randomAccess.setPosition( Util.getArrayFromValue( 0, dimensions.length ) );
			randomAccess.get().set( true );
			neighborhood = Dilation.dilateFull( img, shape, 1 );
		}

		final StringBuilder str = new StringBuilder();
		for ( int d = 3; d < neighborhood.numDimensions(); d++ )
		{
			if ( neighborhood.dimension( d ) > 1 )
			{
				str.append( "Cannot print structuring elements with n dimensions > 3.\n" + "Skipping dimensions beyond 3.\n\n" );
				break;
			}
		}

		final RandomAccess< BitType > randomAccess = neighborhood.randomAccess();
		if ( neighborhood.numDimensions() > 2 )
		{
			appendManySlice( randomAccess, neighborhood.dimension( 0 ), neighborhood.dimension( 1 ), neighborhood.dimension( 2 ), str );
		}
		else if ( neighborhood.numDimensions() > 1 )
		{
			appendSingleSlice( randomAccess, neighborhood.dimension( 0 ), neighborhood.dimension( 1 ), str );
		}
		else if ( neighborhood.numDimensions() > 0 )
		{
			appendLine( randomAccess, neighborhood.dimension( 0 ), str );
		}
		else
		{
			str.append( "Void structuring element.\n" );
		}

		return str.toString();
	}

	/**
	 * Does A = A - B. Writes the results in A.
	 *
	 * @param A
	 *            A
	 * @param B
	 *            B
	 * @param numThreads
	 */
	static < T extends Sub< T > > void subAAB( final RandomAccessible< T > A, final IterableInterval< T > B, final int numThreads )
	{
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( B.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology subAAB thread " + i )
			{
				@Override
				public void run()
				{
					final Cursor< T > Bcursor = B.localizingCursor();
					Bcursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > Ara = A.randomAccess();

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						Bcursor.fwd();
						Ara.setPosition( Bcursor );
						Ara.get().sub( Bcursor.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}


	/**
	 * Does A = A - B. Writes the results in A.
	 *
	 * @param A
	 *            A
	 * @param B
	 *            B
	 * @param numThreads
	 */
	static < T extends Sub< T > > void subAAB2( final IterableInterval< T > A, final RandomAccessible< T > B, final int numThreads )
	{
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( A.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology subAAB2 thread " + i )
			{
				@Override
				public void run()
				{
					final Cursor< T > Acursor = A.localizingCursor();
					Acursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > Bra = B.randomAccess(); // LOL

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						Acursor.fwd();
						Bra.setPosition( Acursor );
						Acursor.get().sub( Bra.get() );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}


	/**
	 * Does A = B - A. Writes the results in A.
	 *
	 * @param source
	 *            A
	 * @param target
	 *            B
	 * @param numThreads
	 */
	static < T extends Sub< T > & Type< T >> void subABA( final RandomAccessible< T > source, final IterableInterval< T > target, final int numThreads )
	{
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( target.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology subABA thread " + i )
			{
				@Override
				public void run()
				{
					final T tmp = createVariable( source, target );
					final Cursor< T > targetCursor = target.localizingCursor();
					targetCursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > sourceRandomAccess = source.randomAccess();

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						targetCursor.fwd();
						sourceRandomAccess.setPosition( targetCursor );

						tmp.set( targetCursor.get() );
						tmp.sub( sourceRandomAccess.get() );

						sourceRandomAccess.get().set( tmp );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}

	/**
	 * Does A = B - A. Writes the results in A.
	 *
	 * @param source
	 *            A
	 * @param target
	 *            B
	 * @param numThreads
	 */
	static < T extends Sub< T > & Type< T >> void subABA2( final RandomAccessibleInterval< T > source, final RandomAccessible< T > target, final int numThreads )
	{
		final long size = Intervals.numElements( source );
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( size, numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology subABA2 thread " + i )
			{
				@Override
				public void run()
				{
					final T tmp = createVariable( target, source );
					final Cursor< T > sourceCursor = Views.iterable( source ).localizingCursor();
					sourceCursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > targetRandomAccess = target.randomAccess( source );

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{

					}
					while ( sourceCursor.hasNext() )
					{
						sourceCursor.fwd();
						targetRandomAccess.setPosition( sourceCursor );

						tmp.set( targetRandomAccess.get() );
						tmp.sub( sourceCursor.get() );

						targetRandomAccess.get().set( tmp );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}

	/**
	 * Does B = A - B. Writes the results in B.
	 *
	 * @param A
	 *            A
	 * @param B
	 *            B
	 * @param numThreads
	 */
	static < T extends Type< T > & Sub< T > > void subBAB( final RandomAccessible< T > A, final IterableInterval< T > B, final int numThreads )
	{
		final long size = Intervals.numElements( B );
		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( size, numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology subBAB thread " + i )
			{
				@Override
				public void run()
				{
					final T tmp = createVariable( A, B );
					final Cursor< T > BCursor = B.localizingCursor();
					BCursor.jumpFwd( chunk.getStartPosition() );
					final RandomAccess< T > Ara = A.randomAccess();

					for ( long step = 0; step < chunk.getLoopSize(); step++ )
					{
						BCursor.fwd();
						Ara.setPosition( BCursor );

						tmp.set( Ara.get() );
						tmp.sub( BCursor.get() );

						BCursor.get().set( tmp );
					}
				}
			};
		}

		SimpleMultiThreading.startAndJoin( threads );
	}

}
