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
