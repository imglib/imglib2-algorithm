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
 * This is intented as a base class for {@code BlockProcessor} with an adaptable
 * number of dimensions (such as converters). For {@code BlockProcessor} with a
 * fixed number of source dimensions, see {@link AbstractBlockProcessor}.
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
public abstract class AbstractDimensionlessBlockProcessor< I, O > implements BlockProcessor< I, O >
{
	private final TempArray< I > tempArray;

	protected long[] sourcePos;

	protected int[] sourceSize;

	private final BlockProcessorSourceInterval sourceInterval = new BlockProcessorSourceInterval( this );

	protected AbstractDimensionlessBlockProcessor( final PrimitiveType sourcePrimitiveType )
	{
		tempArray = TempArray.forPrimitiveType( sourcePrimitiveType );
	}

	protected AbstractDimensionlessBlockProcessor( final AbstractDimensionlessBlockProcessor< I, O > proc )
	{
		tempArray = proc.tempArray.newInstance();
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		updateNumSourceDimsensions( interval.numDimensions() );
		interval.min( sourcePos );
		Arrays.setAll( sourceSize, d -> safeInt( interval.dimension( d ) ) );
	}

	/**
	 * Re-allocates {@code sourcePos} and {@code sourceSize} arrays if they do
	 * not already exist and have {@code length==n}.
	 *
	 * @param n
	 * 		new number of source dimensions
	 *
	 * @return {@code true} if {@code sourcePos} and {@code sourceSize} arrays were re-allocated
	 */
	protected boolean updateNumSourceDimsensions( final int n )
	{
		if ( sourcePos == null || sourcePos.length != n )
		{
			sourcePos = new long[ n ];
			sourceSize = new int[ n ];
			return true;
		}
		return false;
	}

	protected int sourceLength()
	{
		return safeInt( Intervals.numElements( sourceSize ) );
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
