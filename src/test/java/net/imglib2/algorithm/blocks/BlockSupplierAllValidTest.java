/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks;

import static net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import static net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid.TRUE;
import static net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid.UNKNOWN;
import static net.imglib2.util.Util.safeInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.IntPredicate;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.blocks.SubArrayCopy;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.ListLocalizingCursor;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import net.imglib2.util.Cast;
import net.imglib2.util.Fraction;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.Test;

/**
 * {@link VolatileBlockSupplier#allValid} must answer for the source data that
 * computing a block actually reads, all the way through
 * {@link VolatileBlockSupplier#andThen} chains.
 * <p>
 * Every answer is cross-checked against copying the block and asking the
 * resulting {@code Validity}, which is the thing {@code allValid} claims to
 * predict without copying.
 */
public class BlockSupplierAllValidTest
{
	private static final long[] DIMENSIONS = { 13, 11, 7 };

	private static final int[] CELL_DIMENSIONS = { 4, 5, 3 };

	private static final int N = DIMENSIONS.length;

	/**
	 * fully inside one cell; straddling min; straddling max; covering
	 * everything; entirely outside.
	 */
	private static final long[][] MIN = {
			{ 0, 0, 0 },
			{ 4, 5, 3 },
			{ -3, -2, -4 },
			{ 9, 7, 4 },
			{ -4, -3, -2 },
			{ 20, 20, 20 } };

	private static final int[][] SIZE = {
			{ 3, 3, 2 },
			{ 3, 4, 2 },
			{ 8, 6, 7 },
			{ 8, 7, 6 },
			{ 21, 17, 11 },
			{ 4, 4, 4 } };

	/**
	 * Which cells (by flattened grid index) hold valid data.
	 */
	private static final IntPredicate[] PATTERNS = {
			i -> true,
			i -> false,
			i -> i % 2 == 0,
			i -> i % 7 != 0,
			i -> i >= 30 };

	private static final int[] MARGIN = { 2, 1, 3 };

	private static VolatileUnsignedShortType type()
	{
		return new VolatileUnsignedShortType();
	}

	private static VolatileBlockSupplier< VolatileUnsignedShortType > volatileSource( final IntPredicate cellValid )
	{
		return VolatileBlockSupplier.of( Views.extendZero( createVolatileCellImg( cellValid ) ) );
	}

	/**
	 * The answer {@code allValid} has to predict: copy the block and ask the
	 * {@code Validity} that the copy produced.
	 */
	private static AllValid copiedAllValid( final VolatileBlockSupplier< ? > blocks, final Interval interval )
	{
		final int length = safeInt( Intervals.numElements( interval ) );
		final VolatileArray< short[] > dest = new VolatileArray<>( new short[ length ], new byte[ length ] );
		blocks.copy( interval, dest );
		return AllValid.of( dest.getValidity().allValid() );
	}

	private static Interval expandByMargin( final Interval interval )
	{
		final long[] min = new long[ N ];
		final int[] size = new int[ N ];
		for ( int d = 0; d < N; ++d )
		{
			min[ d ] = interval.min( d ) - MARGIN[ d ];
			size[ d ] = safeInt( interval.dimension( d ) ) + 2 * MARGIN[ d ];
		}
		return BlockInterval.wrap( min, size );
	}

	private static Interval block( final int b )
	{
		return BlockInterval.wrap( MIN[ b ].clone(), SIZE[ b ].clone() );
	}

	private static String msg( final int p, final int b )
	{
		return "pattern " + p + ", block " + b;
	}

	@Test
	public void volatileSourceAnswersLikeCopying()
	{
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				final AllValid expected = copiedAllValid( src, interval );
				assertEquals( msg( p, b ), expected, src.allValid( interval, true ) );
				assertEquals( msg( p, b ) + " (visit all cells)", expected, src.allValid( interval, false ) );
				assertEquals( msg( p, b ) + " (threadSafe)", expected, src.threadSafe().allValid( interval, true ) );
			}
		}
	}

	@Test
	public void convertKeepsTheAnswer()
	{
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			final VolatileBlockSupplier< VolatileFloatType > chain = src.andThen( Convert.volatileConvert( new VolatileFloatType() ) );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				assertEquals( msg( p, b ), src.allValid( interval, true ), chain.allValid( interval, true ) );
			}
		}
	}

	/**
	 * An operator whose source interval is larger than its target interval has
	 * to make the chain ask about the larger interval.
	 */
	@Test
	public void expandingOperatorAsksAboutTheExpandedInterval()
	{
		boolean sawADifference = false;
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			final VolatileBlockSupplier< VolatileUnsignedShortType > chain = src.andThen( paddedOperator() );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				final Interval expanded = expandByMargin( interval );
				assertEquals( msg( p, b ), src.allValid( expanded, true ), chain.allValid( interval, true ) );
				// and the chain really does produce what it promised
				assertEquals( msg( p, b ) + " (copied)", copiedAllValid( chain, interval ), chain.allValid( interval, true ) );
				sawADifference |= src.allValid( expanded, true ) != src.allValid( interval, true );
			}
		}
		// otherwise this test would also pass for an operator that forwards the
		// target interval unchanged
		assertTrue( "no block distinguishes the expanded interval from the target interval", sawADifference );
	}

	@Test
	public void chainedOperatorsComposeTheirIntervals()
	{
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			final VolatileBlockSupplier< VolatileUnsignedShortType > chain = src
					.andThen( paddedOperator() )
					.andThen( Convert.volatileConvert( new VolatileFloatType() ) )
					.andThen( Convert.volatileConvert( type() ) )
					.andThen( paddedOperator() );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				final Interval expandedTwice = expandByMargin( expandByMargin( interval ) );
				assertEquals( msg( p, b ), src.allValid( expandedTwice, true ), chain.allValid( interval, true ) );
			}
		}
	}

	/**
	 * Same chain, but concatenated as operators rather than as suppliers.
	 */
	@Test
	public void concatenatedOperatorComposesTheIntervals()
	{
		final VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType > op =
				paddedOperator().andThen( paddedOperator() );
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			final VolatileBlockSupplier< VolatileUnsignedShortType > chain = src.andThen( op );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				final Interval expandedTwice = expandByMargin( expandByMargin( interval ) );
				assertEquals( msg( p, b ), src.allValid( expandedTwice, true ), chain.allValid( interval, true ) );
			}
		}
	}

	/**
	 * Operators can also be asked directly, instead of through a chained {@code
	 * BlockSupplier}. For a concatenated operator and for {@link
	 * Convert.VolatileIdentity} that is the only way: both collapse away when they are
	 * applied to a {@code BlockSupplier}.
	 */
	@Test
	public void operatorsAnswerWhenAskedDirectly()
	{
		final VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType > concatenated =
				paddedOperator().andThen( paddedOperator() );
		final VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType > identity =
				Convert.createVolatileOperator( type(), type() );
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( PATTERNS[ p ] );
			for ( int b = 0; b < MIN.length; ++b )
			{
				final Interval interval = block( b );
				assertEquals( msg( p, b ) + " (concatenated)",
						src.allValid( expandByMargin( expandByMargin( interval ) ), true ),
						concatenated.allValid( src, interval, false ) );
				assertEquals( msg( p, b ) + " (identity)",
						src.allValid( interval, true ),
						identity.allValid( src, interval, false ) );
			}
		}
	}

	/**
	 * An operator that does not know how its target interval maps to its source
	 * interval must not let the chain claim validity.
	 */
	@Test
	public void operatorWithUnknownMappingAnswersUnknown()
	{
		// every cell valid, so everything else in the chain says TRUE
		final VolatileBlockSupplier< VolatileUnsignedShortType > src = volatileSource( i -> true );
		final VolatileBlockSupplier< VolatileUnsignedShortType > chain = src.andThen( new OpaqueOperator() );
		for ( int b = 0; b < MIN.length; ++b )
		{
			assertEquals( "block " + b, TRUE, src.allValid( block( b ), true ) );
			// not FALSE: nothing is known to be invalid, the chain just cannot tell
			assertEquals( "block " + b, UNKNOWN, chain.allValid( block( b ), true ) );
		}
	}

	/**
	 * {@code UNKNOWN} from one stage has to survive the stages above it, rather
	 * than being turned back into an answer about the data.
	 */
	@Test
	public void unknownPropagatesThroughTheRestOfTheChain()
	{
		for ( int p = 0; p < PATTERNS.length; ++p )
		{
			final VolatileBlockSupplier< VolatileFloatType > chain = volatileSource( PATTERNS[ p ] )
					.andThen( new OpaqueOperator() )
					.andThen( paddedOperator() )
					.andThen( Convert.volatileConvert( new VolatileFloatType() ) );
			for ( int b = 0; b < MIN.length; ++b )
				assertEquals( msg( p, b ), UNKNOWN, chain.allValid( block( b ), true ) );
		}
	}

	/**
	 * The plain factories refuse volatile types with a message, instead of
	 * building an operator that fails on the first copy. (Chaining a plain
	 * operator to a {@code VolatileBlockSupplier} does not compile at all.)
	 */
	@Test
	public void plainFactoriesRefuseVolatileTypes()
	{
		final IllegalArgumentException e = assertThrows( IllegalArgumentException.class,
				() -> Convert.createOperator( type(), new VolatileFloatType() ) );
		assertTrue( e.getMessage().contains( "createVolatileOperator" ) );
	}

	private static VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType > paddedOperator()
	{
		return new DefaultVolatileUnaryBlockOperator<>( type(), type(), N, N, new PaddedProcessor() );
	}

	/**
	 * Reads a block padded by {@link #MARGIN} on both sides of every dimension,
	 * and produces an output in which every pixel depends on every pixel of
	 * that padded block. That is the dependency structure an interval-level
	 * {@code allValid} assumes, and it is what makes the chained answer exact:
	 * the output is fully valid exactly when the padded input block is.
	 */
	private static class PaddedProcessor extends AbstractBlockProcessor< VolatileArray< short[] >, VolatileArray< short[] > >
	{
		private final int[] destSize;

		private final int[] zero;

		private final SubArrayCopy.Typed< short[], short[] > copyData;

		PaddedProcessor()
		{
			super( PrimitiveType.SHORT, true, N );
			destSize = new int[ N ];
			zero = new int[ N ];
			copyData = SubArrayCopy.forPrimitiveType( PrimitiveType.SHORT );
		}

		private PaddedProcessor( final PaddedProcessor proc )
		{
			super( proc );
			destSize = new int[ N ];
			zero = proc.zero;
			copyData = proc.copyData;
		}

		@Override
		public void setTargetInterval( final Interval interval )
		{
			for ( int d = 0; d < N; ++d )
			{
				sourcePos[ d ] = interval.min( d ) - MARGIN[ d ];
				destSize[ d ] = safeInt( interval.dimension( d ) );
				sourceSize[ d ] = destSize[ d ] + 2 * MARGIN[ d ];
			}
		}

		@Override
		public void compute( final VolatileArray< short[] > src, final VolatileArray< short[] > dest )
		{
			// the data is just the centre of the padded block; only the
			// validity bookkeeping matters here
			copyData.copy( src.data(), sourceSize, MARGIN, dest.data(), destSize, zero, destSize );

			final byte[] srcValid = src.valid();
			byte valid = 1;
			for ( int i = 0; i < sourceLength(); ++i )
				valid &= srcValid[ i ];
			Arrays.fill( dest.validForWriting(), 0, safeInt( Intervals.numElements( destSize ) ), valid );
		}

		@Override
		public BlockProcessor< VolatileArray< short[] >, VolatileArray< short[] > > independentCopy()
		{
			return new PaddedProcessor( this );
		}
	}

	/**
	 * An operator that does not reveal how its target interval maps to its
	 * source interval, and therefore cannot answer {@code allValid}.
	 */
	private static class OpaqueOperator
			extends AbstractUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType >
			implements VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType >
	{
		OpaqueOperator()
		{
			super( type(), type(), N, N );
		}

		@Override
		public void compute( final VolatileBlockSupplier< VolatileUnsignedShortType > src, final Interval interval, final VolatileArray< ? > dest )
		{
			src.copy( interval, dest );
		}

		@Override
		public VolatileUnaryBlockOperator< VolatileUnsignedShortType, VolatileUnsignedShortType > independentCopy()
		{
			return this;
		}
	}

	/**
	 * A {@code CellImg} with {@code VolatileShortArray} cells, valid exactly
	 * where {@code cellValid} says so. {@code CellImgFactory} always creates
	 * non-volatile accesses, so the cells are built by hand.
	 */
	private static CellImg< VolatileUnsignedShortType, VolatileShortArray > createVolatileCellImg( final IntPredicate cellValid )
	{
		final VolatileUnsignedShortType type = type();
		final NativeTypeFactory< VolatileUnsignedShortType, ? super VolatileShortArray > typeFactory =
				Cast.unchecked( type.getNativeTypeFactory() );
		final CellImgFactory< VolatileUnsignedShortType > factory = new CellImgFactory<>( type, CELL_DIMENSIONS );

		final Fraction entitiesPerPixel = type.getEntitiesPerPixel();
		final CellGrid grid = new CellGrid( DIMENSIONS, CELL_DIMENSIONS );
		final long[] gridDimensions = new long[ N ];
		grid.gridDimensions( gridDimensions );

		final Cell< VolatileShortArray > cellType = new Cell<>( new int[] { 1 }, new long[] { 1 }, null );
		final ListImg< Cell< VolatileShortArray > > cells = new ListImg<>( gridDimensions, cellType );

		final long[] cellGridPosition = new long[ N ];
		final ListLocalizingCursor< Cell< VolatileShortArray > > cellCursor = cells.localizingCursor();
		int index = 0;
		while ( cellCursor.hasNext() )
		{
			cellCursor.fwd();
			cellCursor.localize( cellGridPosition );
			final long[] cellMin = new long[ N ];
			final CellGrid.CellDimensionsAndSteps dimsAndSteps = grid.getCellDimensions( cellGridPosition, cellMin );
			final int numEntities = ( int ) entitiesPerPixel.mulCeil( dimsAndSteps.numPixels() );
			final short[] data = new short[ numEntities ];
			for ( int i = 0; i < numEntities; ++i )
				data[ i ] = ( short ) ( index * 1000 + i );
			cellCursor.set( new Cell<>( dimsAndSteps, cellMin,
					new VolatileShortArray( data, cellValid.test( index ) ) ) );
			++index;
		}

		final CellImg< VolatileUnsignedShortType, VolatileShortArray > img =
				new CellImg<>( factory, grid, cells, entitiesPerPixel );
		img.setLinkedType( typeFactory.createLinkedType( img ) );
		return img;
	}
}
