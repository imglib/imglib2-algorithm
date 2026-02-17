/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.Interval;
import net.imglib2.blocks.BlockInterval;

/**
 * Abstract base class for ...
 * <p>
 * TODO: javadoc
 *
 * @param <F>
 * 		position field array type (must be float[] or double[])
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
abstract class AbstractLookupFunction< F, P >
{
	final long[] destPos;

	final int[] destSize;

	F positionField;

	/**
	 * The offset to apply to {@code positionField} vectors when interpolating
	 * into the source img block.
	 */
	final double[] positionOffset;

	AbstractLookupFunction( final int n )
	{
		destPos = new long[ n ];
		destSize = new int[ n ];
		positionOffset = new double[ n ];
	}

	AbstractLookupFunction( AbstractLookupFunction< F, P > transform )
	{
		this( transform.destPos.length );
	}

	public void setTargetInterval( final Interval interval )
	{
		BlockInterval.wrap( destPos, destSize ).setFrom( interval );
	}

	void setPositionOffset( final double[] offset )
	{
		System.arraycopy( offset, 0, positionOffset, 0, positionOffset.length );
	}

	void setPositionField( final F field )
	{
		positionField = field;
	}

	public abstract void compute( final P dest );

	public abstract AbstractLookupFunction< F, P > independentCopy();
}
