package net.imglib2.algorithm.blocks;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Intervals;

/**
 * Boilerplate for {@link BlockProcessor} to simplify implementations.
 * <p>
 * This is intented as a base class for {@code BlockProcessor} that have source
 * dimensionality fixed at construction. For {@code BlockProcessor} with
 * adaptable number of dimensions (such as converters), see {@link
 * AbstractDimensionlessBlockProcessor}.
 * <p>
 * {@link BlockProcessor#getSourcePos() getSourcePos()}, {@link
 * BlockProcessor#getSourceSize() getSourceSize()}, and {@link
 * BlockProcessor#getSourceInterval() getSourceInterval()} are implemented to
 * return the {@code protected} fields {@code long[] sourcePos} and {@code
 * }int[] sourceSize}. The {@code }protected} method {@code }int sourceLength()}
 * can be used to get the number of elements in the source interval.
 * <p>
 * {@link BlockProcessor#getSourceBuffer() getSourceBuffer()} is implemented
 * according to the {@code sourcePrimitiveType} specified at construction.
 *
 * @param <I>
 * 		input primitive array type, e.g., float[]
 * @param <O>
 * 		output primitive array type, e.g., float[]
 */
public abstract class AbstractBlockProcessor< I, O > implements BlockProcessor< I, O >
{
	private final TempArray< I > tempArray;

	protected final long[] sourcePos;

	protected final int[] sourceSize;

	private final BlockProcessorSourceInterval sourceInterval = new BlockProcessorSourceInterval( this );

	protected AbstractBlockProcessor( final PrimitiveType sourcePrimitiveType, final int numSourceDimensions )
	{
		tempArray = TempArray.forPrimitiveType( sourcePrimitiveType );
		sourcePos = new long[ numSourceDimensions ];
		sourceSize = new int[ numSourceDimensions ];
	}

	protected AbstractBlockProcessor( final AbstractBlockProcessor< I, O > proc )
	{
		tempArray = proc.tempArray.newInstance();
		final int numSourceDimensions = proc.sourcePos.length;
		sourcePos = new long[ numSourceDimensions ];
		sourceSize = new int[ numSourceDimensions ];
	}

	protected int sourceLength()
	{
		return safeInt( Intervals.numElements( sourceSize ) );
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		interval.min( sourcePos );
		Arrays.setAll( sourceSize, d -> safeInt( interval.dimension( d ) ) );
	}

	@Override
	public long[] getSourcePos()
	{
		return sourcePos;
	}

	@Override
	public int[] getSourceSize()
	{
		return sourceSize;
	}

	@Override
	public Interval getSourceInterval()
	{
		return sourceInterval;
	}

	@Override
	public I getSourceBuffer()
	{
		return tempArray.get( sourceLength() );
	}
}
