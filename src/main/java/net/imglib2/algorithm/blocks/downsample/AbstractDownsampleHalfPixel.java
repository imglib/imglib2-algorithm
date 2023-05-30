package net.imglib2.algorithm.blocks.downsample;

import net.imglib2.Interval;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Intervals;

abstract class AbstractDownsampleHalfPixel< T extends AbstractDownsampleHalfPixel< T, P >, P > extends AbstractDownsample< T, P >
{
	AbstractDownsampleHalfPixel( final boolean[] downsampleInDim, final PrimitiveType primitiveType )
	{
		super( downsampleInDim, primitiveType );
	}

	AbstractDownsampleHalfPixel( T downsample )
	{
		super( downsample );
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		boolean destSizeChanged = false;
		for ( int d = 0; d < n; ++d )
		{
			final long tpos = interval.min( d );
			sourcePos[ d ] = downsampleInDim[ d ] ? tpos * 2 : tpos;

			final int tdim = safeInt( interval.dimension( d ) );
			if ( tdim != destSize[ d ] )
			{
				destSize[ d ] = tdim;
				sourceSize[ d ] = downsampleInDim[ d ] ? tdim * 2 : tdim;
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

}
