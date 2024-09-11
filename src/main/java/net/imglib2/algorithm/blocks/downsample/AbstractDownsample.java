/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.downsample;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractBlockProcessor;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Intervals;

abstract class AbstractDownsample< T extends AbstractDownsample< T, P >, P > extends AbstractBlockProcessor< P, P >
{
	PrimitiveType primitiveType;

	final int n;
	final int[] destSize;

	final boolean[] downsampleInDim;
	final int[] downsampleDims;
	final int steps;

	// sources for every per-dimension downsampling step.
	// dest is the tempArray of the next step, or final dest for the last step.
	// tempArrays[0] can be used to copy the source block into.
	private final TempArray< P >[] tempArrays;
	final int[] tempArraySizes;

	AbstractDownsample( final boolean[] downsampleInDim, final PrimitiveType primitiveType )
	{
		super( primitiveType, downsampleInDim.length );
		this.primitiveType = primitiveType;

		n = downsampleInDim.length;
		destSize = new int[ n ];

		this.downsampleInDim = downsampleInDim;
		downsampleDims = downsampleDimIndices( downsampleInDim );
		steps = downsampleDims.length;

		tempArrays = createTempArrays( steps, primitiveType );
		tempArraySizes = new int[ steps ];
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
		final TempArray< P >[] tempArrays = new TempArray[ steps ];
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
		super( downsample );

		// re-use
		primitiveType = downsample.primitiveType;
		n = downsample.n;
		downsampleInDim = downsample.downsampleInDim;
		downsampleDims = downsample.downsampleDims;
		steps = downsample.steps;

		// init empty
		destSize = new int[ n ];
		tempArraySizes = new int[ steps ];

		// init new instance
		tempArrays = createTempArrays( steps, primitiveType );
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
			recomputeTempArraySizes();
	}

	protected void recomputeTempArraySizes()
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
