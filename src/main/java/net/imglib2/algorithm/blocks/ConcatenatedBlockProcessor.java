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

import net.imglib2.Interval;

class ConcatenatedBlockProcessor< I, K, O > implements BlockProcessor< I, O >
{
	private final BlockProcessor< I, K > p0;

	private final BlockProcessor< K, O > p1;

	public ConcatenatedBlockProcessor(
			BlockProcessor< I, K > p0,
			BlockProcessor< K, O > p1 )
	{
		this.p0 = p0;
		this.p1 = p1;
	}

	@Override
	public ConcatenatedBlockProcessor< I, K, O > independentCopy()
	{
		return new ConcatenatedBlockProcessor<>( p0.independentCopy(), p1.independentCopy() );
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		p1.setTargetInterval( interval );
		p0.setTargetInterval( p1.getSourceInterval() );
	}

	@Override
	public long[] getSourcePos()
	{
		return p0.getSourcePos();
	}

	@Override
	public int[] getSourceSize()
	{
		return p0.getSourceSize();
	}

	@Override
	public Interval getSourceInterval()
	{
		return p0.getSourceInterval();
	}

	@Override
	public I getSourceBuffer()
	{
		return p0.getSourceBuffer();
	}

	@Override
	public void compute( final I src, final O dest )
	{
		final K temp = p1.getSourceBuffer();
		p0.compute( src, temp );
		p1.compute( temp, dest );
	}
}
