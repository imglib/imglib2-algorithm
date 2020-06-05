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
package net.imglib2.algorithm.labeling;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.parallel.Parallelization;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

/**
 * Label all connected components of a binary image.
 *
 * @author Tobias Pietzsch
 */
public final class ConnectedComponents
{
	public static enum StructuringElement
	{
		FOUR_CONNECTED( Collect4NeighborLabels.factory ),
		EIGHT_CONNECTED( Collect8NeighborLabels.factory );

		private final CollectNeighborLabelsFactory factory;

		private StructuringElement( final CollectNeighborLabelsFactory factory )
		{
			this.factory = factory;
		}

		public CollectNeighborLabelsFactory getFactory()
		{
			return factory;
		}
	}

	/**
	 * Label all connected components in the given input image. In the output
	 * image, all background pixels will be labeled to {} and foreground
	 * components labeled as {1}, {2}, {3}, etc. where 1, 2, 3 are labels
	 * returned by {@code labelGenerator.next()}. {@code labelGenerator.next()}
	 * is called exactly <em>n</em> times if the input contains
	 * <em>n</em> connected components.
	 *
	 * @param input
	 *            input image with pixels != 0 belonging to foreground.
	 * @param labeling
	 *            output labeling in which the connected components will be
	 *            labeled.
	 * @param labelGenerator
	 *            produces labels for the connected components.
	 * @param se
	 *            structuring element to use. 8-connected or 4-connected
	 *            (respectively n-dimensional analog)
	 * @param service
	 *            service providing threads for multi-threading
	 */
	public static < T extends IntegerType< T >, L, I extends IntegerType< I > > void labelAllConnectedComponents(
			final RandomAccessible< T > input,
			final ImgLabeling< L, I > labeling,
			final Iterator< L > labelGenerator,
			final StructuringElement se,
			final ExecutorService service )
	{
		Parallelization.runWithExecutor( service,
				() -> labelAllConnectedComponents( input, labeling, labelGenerator, se )
		);
	}

	/**
	 * Label all connected components in the given input image. In the output
	 * image, all background pixels will be labeled to {} and foreground
	 * components labeled as {1}, {2}, {3}, etc. where 1, 2, 3 are labels
	 * returned by {@code labelGenerator.next()}. {@code labelGenerator.next()}
	 * is called exactly <em>n</em> times if the input contains
	 * <em>n</em> connected components.
	 *
	 * @param input
	 *            input image with pixels != 0 belonging to foreground.
	 * @param labeling
	 *            output labeling in which the connected components will be
	 *            labeled.
	 * @param labelGenerator
	 *            produces labels for the connected components.
	 * @param se
	 *            structuring element to use. 8-connected or 4-connected
	 *            (respectively n-dimensional analog)
	 */
	public static < T extends IntegerType< T >, L, I extends IntegerType< I > > void labelAllConnectedComponents(
			final RandomAccessible< T > input,
			final ImgLabeling< L, I > labeling,
			final Iterator< L > labelGenerator,
			final StructuringElement se )
	{
		final RandomAccessibleInterval< I > output = labeling.getIndexImg();
		for  ( final I i : Views.iterable( output ) )
			i.setZero();

		final int numLabels = labelAllConnectedComponents( input, output, se ) + 1;

		final ArrayList< Set< L > > labelSets = new ArrayList<>();
		labelSets.add( Collections.emptySet() );
		for ( int i = 1; i < numLabels; ++i )
			labelSets.add( Collections.singleton( labelGenerator.next() ) );

		labeling.getMapping().setLabelSets( labelSets );
	}

	/**
	 * "Label" all connected components in the given input image. In the output
	 * image, all background pixels will be set to 0 and foreground components
	 * set to 1, 2, 3, etc.
	 *
	 * <p>
	 * <em>Note, that the {@code output} image must be cleared to 0!</em>
	 *
	 * @param input
	 *            input image with pixels &gt; 0 belonging to foreground.
	 * @param output
	 *            output image, must be filled with 0.
	 * @param se
	 *            structuring element to use. 8-connected or 4-connected
	 *            (respectively n-dimensional analog)
	 * @param service
	 *            service providing threads for multi-threading
	 * @return the number of connected components (that is, the highest value
	 *         occurring in the output image.
	 */
	public static < T extends IntegerType< T >, L extends IntegerType< L > > int labelAllConnectedComponents(
			final RandomAccessible< T > input,
			final RandomAccessibleInterval< L > output,
			final StructuringElement se,
			final ExecutorService service )
	{
		return Parallelization.runWithExecutor( service,
				() -> labelAllConnectedComponents( input, output, se )
		);
	}

	/**
	 * "Label" all connected components in the given input image. In the output
	 * image, all background pixels will be set to 0 and foreground components
	 * set to 1, 2, 3, etc.
	 *
	 * <p>
	 * <em>Note, that the {@code output} image must be cleared to 0!</em>
	 * </p>
	 *
	 * @param input
	 *            input image with pixels &gt; 0 belonging to foreground.
	 * @param output
	 *            output image, must be filled with 0.
	 * @param se
	 *            structuring element to use. 8-connected or 4-connected
	 *            (respectively n-dimensional analog)
	 * @return the number of connected components (that is, the highest value
	 *         occurring in the output image.
	 */
	public static < T extends IntegerType< T >, L extends IntegerType< L > > int labelAllConnectedComponents(
			final RandomAccessible< T > input,
			final RandomAccessibleInterval< L > output,
			final StructuringElement se )
	{
		final int n = output.numDimensions();
		final int splitDim = n - 1;
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		output.min( min );
		output.max( max );
		final long splitDimMax = max[ splitDim ];

		final int numThreads = Runtime.getRuntime().availableProcessors();
		int numTasks = numThreads > 1 ? numThreads * 2 : 1;
		numTasks = ( int ) Math.max( 1, Math.min( numTasks, output.dimension( splitDim ) / 4 ) );
		final long taskSize = output.dimension( splitDim ) / numTasks;

		@SuppressWarnings( "unchecked" )
		final Fragment< T, L >[] fragments = new Fragment[ numTasks ];
		final CollectNeighborLabels< L > collectNeighborLabels = se.getFactory().< L >newInstance( n );
		for ( int i = 0; i < numTasks; ++i )
		{
			max[ splitDim ] = ( i == numTasks - 1 ) ? splitDimMax : min[ splitDim ] + taskSize - 1;
			fragments[ i ] = new Fragment< T, L >( input, Views.interval( output, min, max ), collectNeighborLabels );
			min[ splitDim ] += taskSize;
		}

		Parallelization.getTaskExecutor().forEach( Arrays.asList( fragments ), Fragment::mark );

		final TIntArrayList merged = mergeCanonicalLists( fragments );
		for ( int i = 1; i < numTasks; ++i )
			fragments[ i ].linkToPreviousFragment( fragments[ i - 1 ], merged );
		final int numComponents = splitCanonicalLists( fragments, merged );

		Parallelization.getTaskExecutor().forEach( Arrays.asList( fragments ), Fragment::relabel );

		return numComponents;
	}

	private static final class Fragment< T extends IntegerType< T >, L extends IntegerType< L > >
	{
		private final int n;

		private final TIntArrayList canonicalLabels;

		private final RandomAccessible< T > input;

		private final RandomAccessibleInterval< L > output;

		private final CollectNeighborLabels< L > collectNeighborLabels;

		private int offset;

		public Fragment(
				final RandomAccessible< T > input,
				final RandomAccessibleInterval< L > output,
				final CollectNeighborLabels< L > collectNeighborLabels )
		{
			n = output.numDimensions();
			this.input = input;
			this.output = output;
			this.collectNeighborLabels = collectNeighborLabels;
			canonicalLabels = new TIntArrayList( 1000 );
			canonicalLabels.add( 0 );
		}

		public void mark()
		{
			final long[] min = new long[ n ];
			final long[] max = new long[ n ];
			output.min( min );
			output.max( max );

			// a list to collect labels of non-zero neighbors of a pixel
			final TIntArrayList neighborLabels = new TIntArrayList( n );

			final TIntArrayList updateLabels = new TIntArrayList( 10 );

			final Cursor< T > in = Views.flatIterable( Views.interval( input, output ) ).localizingCursor();
			final RandomAccess< L > la = output.randomAccess();

			while ( in.hasNext() )
			{
				if ( in.next().getInteger() > 0 )
				{
					la.setPosition( in );
					collectNeighborLabels.collect( la, neighborLabels, min, max );
					final int numLabeledNeighbors = neighborLabels.size();
					if ( numLabeledNeighbors == 0 )
					{
						// create new Label
						final int label = canonicalLabels.size();
						canonicalLabels.add( label );
						la.get().setInteger( label );
					}
					else if ( numLabeledNeighbors == 1 )
					{
						la.get().setInteger( canonicalLabels.get( neighborLabels.get( 0 ) ) );
					}
					else
					{
						// assign canonical label
						int canonical = canonicalLabels.get( neighborLabels.get( 0 ) );
						boolean makeCanonical = false;
						for ( int i = 1; i < neighborLabels.size(); ++i )
						{
							if ( canonicalLabels.get( neighborLabels.get( i ) ) != canonical )
							{
								makeCanonical = true;
								break;
							}
						}
						if ( makeCanonical )
						{
							updateLabels.clear();
							canonical = Integer.MAX_VALUE;
							for ( int i = 0; i < neighborLabels.size(); ++i )
							{
								int label = neighborLabels.get( i );
								while ( canonicalLabels.get( label ) != label )
								{
									updateLabels.add( label );
									canonical = Math.min( canonical, label );
									label = canonicalLabels.get( label );
								}
								updateLabels.add( label );
								canonical = Math.min( canonical, label );
							}
							for ( int i = 0; i < updateLabels.size(); ++i )
								canonicalLabels.set( updateLabels.get( i ), canonical );
						}
						la.get().setInteger( canonical );
					}
				}
			}
		}

		public void linkToPreviousFragment( final Fragment< T, L > previous, final TIntArrayList merged )
		{
			final int previousOffset = previous.offset;
			final int splitDim = n - 1;
			final long[] min = new long[ n ];
			final long[] max = new long[ n ];
			output.min( min );
			output.max( max );
			max[ splitDim ] = min[ splitDim ];

			// a list to collect labels of labeled neighbors of a pixel
			final TIntArrayList neighborLabels = new TIntArrayList( n );

			final TIntArrayList updateLabels = new TIntArrayList( 10 );

			final Cursor< L > in = Views.iterable( Views.interval( output, min, max ) ).localizingCursor();
			min[ splitDim ] -= 1;
			final RandomAccess< L > la = output.randomAccess( new FinalInterval( min, max ) );

			while ( in.hasNext() )
			{
				int label = in.next().getInteger();
				if ( label != 0 )
				{
					label += offset;
					la.setPosition( in );
					collectNeighborLabels.collectAtPreviousFragmentBorder( la, neighborLabels, min, max );
					final int numLabeledNeighbors = neighborLabels.size();
					if ( numLabeledNeighbors != 0 )
					{
						for ( int i = 0; i < numLabeledNeighbors; ++i )
							neighborLabels.set( i, neighborLabels.get( i ) + previousOffset );
						int canonical = merged.get( label );
						boolean makeCanonical = false;
						for ( int i = 0; i < neighborLabels.size(); ++i )
						{
							if ( merged.get( neighborLabels.get( i ) ) != canonical )
							{
								neighborLabels.add( label );
								makeCanonical = true;
								break;
							}
						}
						if ( makeCanonical )
						{
							updateLabels.clear();
							canonical = Integer.MAX_VALUE;
							for ( int i = 0; i < neighborLabels.size(); ++i )
							{
								label = neighborLabels.get( i );
								while ( merged.get( label ) != label )
								{
									updateLabels.add( label );
									canonical = Math.min( canonical, label );
									label = merged.get( label );
								}
								updateLabels.add( label );
								canonical = Math.min( canonical, label );
							}
							for ( int i = 0; i < updateLabels.size(); ++i )
								merged.set( updateLabels.get( i ), canonical );
						}
					}
				}
			}
		}

		public void relabel()
		{
			for ( final L label : Views.iterable( output ) )
				label.setInteger( canonicalLabels.get( label.getInteger() ) );
		}
	}

	private static < T extends IntegerType< T >, L extends IntegerType< L > > TIntArrayList mergeCanonicalLists( final Fragment< T, L >[] fragments )
	{
		int size = 0;
		for ( final Fragment< T, L > fragment : fragments )
		{
			fragment.offset = size;
			size += fragment.canonicalLabels.size() - 1; // -1 is for background
		}
		final TIntArrayList merged = new TIntArrayList( size + 1 );
		merged.add( 0 ); // background
		for ( final Fragment< T, L > fragment : fragments )
		{
			final TIntArrayList fl = fragment.canonicalLabels;
			final int o = fragment.offset;
			for ( int i = 1; i < fl.size(); ++i )
				merged.add( fl.get( i ) + o );
		}
		return merged;
	}

	private static < T extends IntegerType< T >, L extends IntegerType< L > > int splitCanonicalLists( final Fragment< T, L >[] fragments, final TIntArrayList merged )
	{
		int nextLabel = 1;
		for ( int i = 1; i < merged.size(); ++i )
			if ( merged.get( i ) == i )
				merged.set( i, nextLabel++ );
			else
				merged.set( i, merged.get( merged.get( i ) ) );

		for ( final Fragment< T, L > fragment : fragments )
		{
			final TIntArrayList fl = fragment.canonicalLabels;
			final int o = fragment.offset;
			for ( int i = 1; i < fl.size(); ++i )
				fl.set( i, merged.get( i + o ) );
		}

		return nextLabel - 1;
	}

	private static interface CollectNeighborLabels< L extends IntegerType< L > >
	{
		public void collect( RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax );

		public void collectAtPreviousFragmentBorder( RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax );
	}

	private static interface CollectNeighborLabelsFactory
	{
		public < L extends IntegerType< L > > CollectNeighborLabels< L > newInstance( final int n );
	}

	private static final class Collect4NeighborLabels< L extends IntegerType< L > > implements CollectNeighborLabels< L >
	{
		private final int n;

		private Collect4NeighborLabels( final int n )
		{
			this.n = n;
		}

		@Override
		public void collect( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			neighborLabels.clear();
			for ( int d = 0; d < n; ++d )
			{
				if ( la.getLongPosition( d ) > labelsMin[ d ] )
				{
					la.bck( d );
					final int l = la.get().getInteger();
					if ( l != 0 )
						neighborLabels.add( l );
					la.fwd( d );
				}
			}
		}

		private static final CollectNeighborLabelsFactory factory = new CollectNeighborLabelsFactory()
		{
			@Override
			public < L extends IntegerType< L > > CollectNeighborLabels< L > newInstance( final int n )
			{
				return new Collect4NeighborLabels< L >( n );
			}
		};

		@Override
		public void collectAtPreviousFragmentBorder( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			neighborLabels.clear();
			la.bck( n - 1 );
			final int l = la.get().getInteger();
			if ( l != 0 )
				neighborLabels.add( l );
			la.fwd( n - 1 );
		}
	}

	private static final class Collect8NeighborLabels< L extends IntegerType< L > > implements CollectNeighborLabels< L >
	{
		private final int n;

		private final long[][] offsets;

		private final long[] pos;

		private final long[] previousFragmentPos;

		private final int numPreviousFragmentOffsets;

		private Collect8NeighborLabels( final int n )
		{
			this.n = n;
			int nOffsets = 0;
			for ( int d = 0; d < n; ++d )
				nOffsets = 3 * nOffsets + 1;
			numPreviousFragmentOffsets = ( int ) Math.pow( 3, n - 1 );
			offsets = new long[ nOffsets ][];
			pos = new long[ n ];
			previousFragmentPos = new long[ n ];
			final long[] min = new long[ n ];
			Arrays.fill( min, -1 );
			final long[] max = new long[ n ];
			Arrays.fill( max, 1 );
			final IntervalIterator idx = new IntervalIterator( new FinalInterval( min, max ) );
			for ( int i = 0; i < offsets.length; ++i )
			{
				offsets[ i ] = new long[ n ];
A:				while ( true )
				{
					idx.fwd();
					idx.localize( offsets[ i ] );
					for ( int d = n - 1; d >= 0; --d )
						if ( offsets[ i ][ d ] < 0 )
							break A;
				}
				for ( int d = 0; d < n; ++d )
				{
					offsets[ i ][ d ] -= pos[ d ];
					pos[ d ] += offsets[ i ][ d ];
				}
				if ( i == numPreviousFragmentOffsets - 1 )
					for ( int d = 0; d < n; ++d )
						previousFragmentPos[ d ] = -pos[ d ];
			}
			for ( int d = 0; d < n; ++d )
				pos[ d ] = -pos[ d ];
		}

		@Override
		public void collect( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			for ( int d = 0; d < n; ++d )
				if ( la.getLongPosition( d ) <= labelsMin[ d ] || la.getLongPosition( d ) >= labelsMax[ d ] )
				{
					collectChecked( la, neighborLabels, labelsMin, labelsMax );
					return;
				}
			collectUnchecked( la, neighborLabels );
		}

		private void collectChecked( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			neighborLabels.clear();
A:			for ( int i = 0; i < offsets.length; ++i )
			{
				la.move( offsets[ i ] );
				for ( int d = 0; d < n; ++d )
					if ( la.getLongPosition( d ) < labelsMin[ d ] || la.getLongPosition( d ) > labelsMax[ d ] )
						continue A;
				final int l = la.get().getInteger();
				if ( l != 0 )
					neighborLabels.add( l );
			}
			la.move( pos );
		}

		private void collectUnchecked( final RandomAccess< L > la, final TIntArrayList neighborLabels )
		{
			neighborLabels.clear();
			for ( int i = 0; i < offsets.length; ++i )
			{
				la.move( offsets[ i ] );
				final int l = la.get().getInteger();
				if ( l != 0 )
					neighborLabels.add( l );
			}
			la.move( pos );
		}

		@Override
		public void collectAtPreviousFragmentBorder( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			for ( int d = 0; d < n - 1; ++d )
				if ( la.getLongPosition( d ) <= labelsMin[ d ] || la.getLongPosition( d ) >= labelsMax[ d ] )
				{
					collectAtPreviousFragmentBorderChecked( la, neighborLabels, labelsMin, labelsMax );
					return;
				}
			collectAtPreviousFragmentBorderUnchecked( la, neighborLabels );
		}

		private void collectAtPreviousFragmentBorderChecked( final RandomAccess< L > la, final TIntArrayList neighborLabels, final long[] labelsMin, final long[] labelsMax )
		{
			neighborLabels.clear();
A:			for ( int i = 0; i < numPreviousFragmentOffsets; ++i )
			{
				la.move( offsets[ i ] );
				for ( int d = 0; d < n - 1; ++d )
					if ( la.getLongPosition( d ) < labelsMin[ d ] || la.getLongPosition( d ) > labelsMax[ d ] )
						continue A;
				final int l = la.get().getInteger();
				if ( l != 0 )
					neighborLabels.add( l );
			}
			la.move( previousFragmentPos );
		}

		private void collectAtPreviousFragmentBorderUnchecked( final RandomAccess< L > la, final TIntArrayList neighborLabels )
		{
			neighborLabels.clear();
			for ( int i = 0; i < numPreviousFragmentOffsets; ++i )
			{
				la.move( offsets[ i ] );
				final int l = la.get().getInteger();
				if ( l != 0 )
					neighborLabels.add( l );
			}
			la.move( previousFragmentPos );
		}

		private static final CollectNeighborLabelsFactory factory = new CollectNeighborLabelsFactory()
		{
			@Override
			public < L extends IntegerType< L > > CollectNeighborLabels< L > newInstance( final int n )
			{
				return new Collect8NeighborLabels< L >( n );
			}
		};
	}
}
