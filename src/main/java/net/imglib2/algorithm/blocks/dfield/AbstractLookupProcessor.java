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
import net.imglib2.algorithm.blocks.AbstractBlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.type.PrimitiveType;

/**
 * TODO: javadoc
 * <p>
 * Abstract base class for ???. Implements source/target interval computation,
 * and {@code TempArray} and thread-safe setup.
 *
 * @param <F>
 * 		position field array type (must be float[] or double[])
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
public // TODO: make package private again (public for testing)
abstract class AbstractLookupProcessor< F, P > extends AbstractBlockProcessor< P, P >
{
	PrimitiveType primitiveType;

	Transform.Interpolation interpolation;

	final int n;

	final long[] destPos;

	final int[] destSize;

	F positionField;

	final double[] positionOffset; // TODO: what is this exactly, ans where is it set?

	AbstractLookupProcessor( final int n, final Transform.Interpolation interpolation, final PrimitiveType primitiveType )
	{
		super( primitiveType, n );
		this.primitiveType = primitiveType;
		this.interpolation = interpolation;
		this.n = n;
		destPos = new long[ n ];
		destSize = new int[ n ];
		positionOffset = new double[ n ];
	}

	AbstractLookupProcessor( AbstractLookupProcessor< F, P > transform )
	{
		super( transform );

		// re-use
		primitiveType = transform.primitiveType;
		interpolation = transform.interpolation;
		n = transform.n;

		// init empty
		destPos = new long[ n ];
		destSize = new int[ n ];
		positionOffset = new double[ n ];
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		BlockInterval.wrap( destPos, destSize ).setFrom( interval );
	}

	public void setSourceInterval( final Interval interval )
	{
		getSourceInterval().setFrom( interval );
	}

	public void setPositionField( final F field )
	{
		positionField = field;
	}

	@Override
	public abstract AbstractLookupProcessor< F, P > independentCopy();
}
