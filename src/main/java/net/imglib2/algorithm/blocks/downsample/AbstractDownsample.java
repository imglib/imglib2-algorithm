package net.imglib2.algorithm.blocks.downsample;

import java.util.Arrays;
import java.util.function.Supplier;
import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.CloseableThreadLocal;
import net.imglib2.util.Intervals;

abstract class AbstractDownsample< T extends AbstractDownsample< T, P >, P > implements BlockProcessor< P, P >
{
	PrimitiveType primitiveType;

	final int n;
	final int[] destSize;
	final long[] sourcePos;
	final int[] sourceSize;

	final boolean[] downsampleInDim;
	final int[] downsampleDims;
	final int steps;

	// sources for every per-dimension downsampling step.
	// dest is the tempArray of the next step, or final dest for the last step.
	// tempArrays[0] can be used to copy the source block into.
	private final TempArray< P > tempArrays[];
	final int[] tempArraySizes;

	private final BlockProcessorSourceInterval sourceInterval;

	Supplier< T > threadSafeSupplier;

	AbstractDownsample( final boolean[] downsampleInDim, final PrimitiveType primitiveType )
	{
		n = downsampleInDim.length;
		this.primitiveType = primitiveType;
		destSize = new int[ n ];
		sourceSize = new int[ n ];
		sourcePos = new long[ n ];

		this.downsampleInDim = downsampleInDim;
		downsampleDims = downsampleDimIndices( downsampleInDim );
		steps = downsampleDims.length;

		tempArrays = createTempArrays( steps, primitiveType );
		tempArraySizes = new int[ steps ];

		sourceInterval = new BlockProcessorSourceInterval( this );
	}

	private static int[] downsampleDimIndices( final boolean[] downsampleInDim )
	{
		final int n = downsampleInDim.length;
		final int[] dims = new int[ n ];
		int j = 0;
		for ( int i = 0; i < n; i++ )
			if ( downsampleInDim[ i ] )
				dims[ j++ ] = i;
		return Arrays.copyOf( dims, j );
	}

	private static < P > TempArray< P >[] createTempArrays( final int steps, final PrimitiveType primitiveType )
	{
		final TempArray< P > tempArrays[] = new TempArray[ steps ];
		tempArrays[ 0 ] = TempArray.forPrimitiveType( primitiveType );
		if ( steps >= 2 )
		{
			tempArrays[ 1 ] = TempArray.forPrimitiveType( primitiveType );
			if ( steps >= 3 )
			{
				tempArrays[ 2 ] = TempArray.forPrimitiveType( primitiveType );
				for ( int i = 3; i < steps; ++i )
					tempArrays[ i ] = tempArrays[ i - 2 ];
			}
		}
		return tempArrays;
	}

	AbstractDownsample( T downsample )
	{
		// re-use
		primitiveType = downsample.primitiveType;
		n = downsample.n;
		downsampleInDim = downsample.downsampleInDim;
		downsampleDims = downsample.downsampleDims;
		steps = downsample.steps;
		threadSafeSupplier = downsample.threadSafeSupplier;

		// init empty
		destSize = new int[ n ];
		sourcePos = new long[ n ];
		sourceSize = new int[ n ];
		tempArraySizes = new int[ steps ];

		// init new instance
		tempArrays = createTempArrays( steps, primitiveType );
		sourceInterval = new BlockProcessorSourceInterval( this );
	}

	abstract T newInstance();

	@Override
	public synchronized Supplier< T > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::newInstance )::get;
		return threadSafeSupplier;
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		boolean destSizeChanged = false;
		for ( int d = 0; d < n; ++d )
		{
			final long tpos = interval.min( d );
			sourcePos[ d ] = downsampleInDim[ d ] ? tpos * 2 - 1 : tpos;

			final int tdim = safeInt( interval.dimension( d ) );
			if ( tdim != destSize[ d ] )
			{
				destSize[ d ] = tdim;
				sourceSize[ d ] = downsampleInDim[ d ] ? tdim * 2 + 1 : tdim;
				destSizeChanged = true;
			}
		}

		if ( destSizeChanged )
		{
			int size = safeInt( Intervals.numElements( sourceSize ) );
			tempArraySizes[ 0 ] = size;
			for ( int i = 1; i < steps; ++i )
			{
				final int d = downsampleDims[ i - 1 ];
				size = size / sourceSize[ d ] * destSize[ d ];
				tempArraySizes[ i ] = size;
			}
		}
	}

	static int safeInt( final long value )
	{
		if ( value > Integer.MAX_VALUE )
			throw new IllegalArgumentException( "value too large" );
		return ( int ) value;
	}

	@Override
	public int[] getSourceSize()
	{
		return sourceSize;
	}

	@Override
	public long[] getSourcePos()
	{
		return sourcePos;
	}

	@Override
	public Interval getSourceInterval()
	{
		return sourceInterval;
	}

	// optional. also other arrays can be passed to compute()
	@Override
	public P getSourceBuffer()
	{
		return getSourceBuffer( 0 );
	}

	private P getSourceBuffer( int i )
	{
		return tempArrays[ i ].get( tempArraySizes[ i ] );
	}

	@Override
	public void compute( final P src, final P dest )
	{
		P itSrc = src;
		final int[] itDestSize = sourceSize.clone();
		for ( int i = 0; i < steps; ++i )
		{
			final int d = downsampleDims[ i ];
			itDestSize[ d ] = destSize[ d ];
			final boolean lastStep = ( i == steps - 1 );
			final P itDest = lastStep ? dest : getSourceBuffer( i + 1 );
			downsample( itSrc, itDestSize, itDest, d );
			itSrc = itDest;
		}
	}

	abstract void downsample( final P source, final int[] destSize, final P dest, final int dim );
}
