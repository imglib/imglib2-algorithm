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
package net.imglib2.algorithm.blocks;

import static net.imglib2.util.Util.safeInt;

import net.imglib2.Interval;
import net.imglib2.blocks.BlockInterval;
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
 * A {@link BlockInterval} is exposed through {@link #getSourceInterval()}
 * (after {@link #setTargetInterval} has been called). The {@code protected}
 * method {@link #sourceLength()} returns the number of elements in the source
 * interval.
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

	protected BlockInterval sourceInterval;

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
		updateNumSourceDimensions( interval.numDimensions() );
		sourceInterval.setFrom( interval );
	}

	/**
	 * Re-allocates {@code sourceInterval} if it does not already exist and
	 * match {@code numDimensions()==n}.
	 *
	 * @param n
	 * 		new number of source dimensions
	 *
	 * @return {@code true} if {@code sourceInterval} was re-allocated
	 */
	protected boolean updateNumSourceDimensions( final int n )
	{
		if ( sourceInterval == null || sourceInterval.numDimensions() != n )
		{
			sourceInterval = new BlockInterval( n );
			return true;
		}
		return false;
	}

	protected int sourceLength()
	{
		return safeInt( Intervals.numElements( sourceInterval ) );
	}

	@Override
	public BlockInterval getSourceInterval()
	{
		return sourceInterval;
	}

	@Override
	public I getSourceBuffer()
	{
		return tempArray.get( sourceLength() );
	}
}
