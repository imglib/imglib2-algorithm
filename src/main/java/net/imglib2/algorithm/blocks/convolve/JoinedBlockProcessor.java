package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;

import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractDimensionlessBlockProcessor;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.blocks.SubArrayCopy;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

abstract class JoinedBlockProcessor< I, O0, O1, P > extends AbstractDimensionlessBlockProcessor< I, P >
{
	private final BlockProcessor< I, O0 > p0;
	private final BlockProcessor< I, O1 > p1;

	private final TempArray< O0 > tempArrayDest0;
	private final TempArray< O1 > tempArrayDest1;
	private final SubArrayCopy.Typed< I, I > copy;

	private SubArrayExtractor< I > subArray;

	BlockInterval destInterval;
	int destLength;

	public < S extends NativeType< S >, T0 extends NativeType< T0 >, T1 extends NativeType< T1 > > JoinedBlockProcessor(
			final S sourceType, final T0 targetType0, final T1 targetType1,
			final BlockProcessor< I, O0 > p0,
			final BlockProcessor< I, O1 > p1 )
	{
		super( sourceType.getNativeTypeFactory().getPrimitiveType() );

		this.p0 = p0; // TODO .independentCopy() ???
		this.p1 = p1; // TODO .independentCopy() ???

		final PrimitiveType sourcePrimitiveType = sourceType.getNativeTypeFactory().getPrimitiveType();
		final PrimitiveType targetPrimitiveType0 = targetType0.getNativeTypeFactory().getPrimitiveType();
		final PrimitiveType targetPrimitiveType1 = targetType1.getNativeTypeFactory().getPrimitiveType();
		tempArrayDest0 = TempArray.forPrimitiveType( targetPrimitiveType0 );
		tempArrayDest1 = TempArray.forPrimitiveType( targetPrimitiveType1 );
		copy = SubArrayCopy.forPrimitiveType( sourcePrimitiveType );
	}

	JoinedBlockProcessor( JoinedBlockProcessor< I, O0, O1, P > processor )
	{
		super( processor );
		p0 = processor.p0.independentCopy();
		p1 = processor.p1.independentCopy();
		tempArrayDest0 = processor.tempArrayDest0.newInstance();
		tempArrayDest1 = processor.tempArrayDest1.newInstance();
		copy = processor.copy;
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		final int m = interval.numDimensions();
		if ( destInterval == null || destInterval.numDimensions() != m )
			destInterval = new BlockInterval( m );
		destInterval.setFrom( interval );
		destLength = safeInt( Intervals.numElements( destInterval ) );

		p0.setTargetInterval( interval );
		p1.setTargetInterval( interval );
		final Interval sourceInterval0 = p0.getSourceInterval();
		final Interval sourceInterval1 = p1.getSourceInterval();

		final int n = sourceInterval0.numDimensions();
		assert n == sourceInterval1.numDimensions();
		if( updateNumSourceDimensions( n ) )
		{
			subArray = new SubArrayExtractor<>( n, copy );
		}

		// Take the union of the source intervals of the merged processors.
		// Use a separate sourceBuffer TempArray (because the union could be
		// bigger than both of the processors' source intervals).
		final long[] sourcePos = getSourceInterval().min();
		final int[] sourceSize = getSourceInterval().size();
		Arrays.setAll( sourcePos, d -> Math.min( sourceInterval0.min( d ), sourceInterval1.min( d ) ) );
		Arrays.setAll( sourceSize, d -> Util.safeInt(
				Math.max(
						sourceInterval0.min( d ) + sourceInterval0.dimension( d ),
						sourceInterval1.min( d ) + sourceInterval1.dimension( d )
				) - sourcePos[ d ] ) );
	}

	@Override
	public void compute( final I src, final P dest )
	{
		// If the source interval union matches the source interval of one
		// processor (or both), use src directly as input. Otherwise, use
		// SubArrayCopy.

		final I src0 = subArray.extract( src, getSourceInterval(), p0 );
		final O0 dest0 = tempArrayDest0.get( destLength );
		p0.compute( src0, dest0 );

		final I src1 = subArray.extract( src, getSourceInterval(), p1 );
		final O1 dest1 = tempArrayDest1.get( destLength );
		p1.compute( src1, dest1 );

		reduce( dest0, dest1, dest );
	}

	abstract void reduce( final O0 dest0, final O1 dest1, final P dest );

	static final class SubArrayExtractor< I > implements EuclideanSpace
	{
		private final SubArrayCopy.Typed< I, I > copy;
		private final int[] relSourcePos;
		private final int[] sourceStrides;
		private final int[] destStrides;

		SubArrayExtractor( final int numDimensions, final SubArrayCopy.Typed< I, I > copy )
		{
			this.copy = copy;
			relSourcePos = new int[ numDimensions ];
			sourceStrides = new int[ numDimensions ];
			destStrides = new int[ numDimensions ];
		}

		/**
		 * If the given {@code interval} matches the source interval of {@code
		 * p0}, just return {@code src}. Otherwise, {@link
		 * BlockProcessor#getSourceBuffer() get a buffer} from {@code p0}, copy
		 * the appropriate region of {@code src} into it, and return it.
		 */
		I extract( final I src, final BlockInterval interval, final BlockProcessor< I, ? > p0 )
		{
			if ( Intervals.equals( interval, p0.getSourceInterval() ) )
			{
				return src;
			}
			else
			{
				final int[] sourceSize = interval.size();
				final int[] destSize = BlockInterval.asBlockInterval( p0.getSourceInterval() ).size();
				IntervalIndexer.createAllocationSteps( sourceSize, sourceStrides );
				IntervalIndexer.createAllocationSteps( destSize, destStrides );
				Arrays.setAll( relSourcePos, d -> ( int ) ( p0.getSourceInterval().min( d ) - interval.min( d ) ) );
				final int oSrc = IntervalIndexer.positionToIndex( relSourcePos, sourceSize );
				final int oDest = 0;

				I buf = p0.getSourceBuffer();
				copy.copyNDRangeRecursive( numDimensions() - 1,
						src, sourceStrides, oSrc,
						buf, destStrides, oDest,
						destSize );
				return buf;
			}
		}

		@Override
		public int numDimensions()
		{
			return relSourcePos.length;
		}
	}
}
