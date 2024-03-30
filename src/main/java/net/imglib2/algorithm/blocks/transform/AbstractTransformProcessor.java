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
package net.imglib2.algorithm.blocks.transform;

import java.util.Arrays;
import java.util.function.Supplier;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.CloseableThreadLocal;
import net.imglib2.util.Intervals;

/**
 * Abstract base class for {@link Affine3DProcessor} and {@link
 * Affine2DProcessor}. Implements source/target interval computation, and {@code
 * TempArray} and thread-safe setup.
 *
 * @param <T>
 * 		recursive type of this {@code AbstractTransformProcessor} (for {@link #threadSafeSupplier})
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
abstract class AbstractTransformProcessor< T extends AbstractTransformProcessor< T, P >, P > implements BlockProcessor< P, P >
{
	PrimitiveType primitiveType;

	Transform.Interpolation interpolation;

	final int n;

	final long[] destPos;

	final int[] destSize;

	final long[] sourcePos;

	final int[] sourceSize;

	private int sourceLength;

	private final BlockProcessorSourceInterval sourceInterval;

	private final TempArray< P > tempArray;

	Supplier< T > threadSafeSupplier;

	AbstractTransformProcessor( final int n, final Transform.Interpolation interpolation, final PrimitiveType primitiveType )
	{
		this.primitiveType = primitiveType;
		this.interpolation = interpolation;
		this.n = n;
		destPos = new long[ n ];
		destSize = new int[ n ];
		sourcePos = new long[ n ];
		sourceSize = new int[ n ];
		sourceInterval = new BlockProcessorSourceInterval( this );
		tempArray = TempArray.forPrimitiveType( primitiveType );
	}

	AbstractTransformProcessor( T transform )
	{
		// re-use
		primitiveType = transform.primitiveType;
		interpolation = transform.interpolation;
		n = transform.n;
		threadSafeSupplier = transform.threadSafeSupplier;

		// init empty
		destPos = new long[ n ];
		destSize = new int[ n ];
		sourcePos = new long[ n ];
		sourceSize = new int[ n ];

		// init new instance
		sourceInterval = new BlockProcessorSourceInterval( this );
		tempArray = TempArray.forPrimitiveType( primitiveType );
	}

	abstract T newInstance();

	@Override
	public Supplier< T > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::newInstance )::get;
		return threadSafeSupplier;
	}

	abstract RealInterval estimateBounds( Interval interval );

	@Override
	public void setTargetInterval( final Interval interval )
	{
		interval.min( destPos );
		Arrays.setAll( destSize, d -> ( int ) interval.dimension( d ) );

		final RealInterval bounds = estimateBounds( interval );
		switch ( interpolation )
		{
		case NEARESTNEIGHBOR:
			Arrays.setAll( sourcePos, d -> Math.round( bounds.realMin( d ) - 0.5 ) );
			Arrays.setAll( sourceSize, d -> ( int ) ( Math.round( bounds.realMax( d ) + 0.5 ) - sourcePos[ d ] ) + 1 );
			break;
		case NLINEAR:
			Arrays.setAll( sourcePos, d -> ( long ) Math.floor( bounds.realMin( d ) - 0.5 ) );
			Arrays.setAll( sourceSize, d -> ( int ) ( ( long ) Math.floor( bounds.realMax( d ) + 0.5 ) - sourcePos[ d ] ) + 2 );
			break;
		}
		sourceLength = safeInt( Intervals.numElements( sourceSize ) );
	}

	static int safeInt( final long value )
	{
		if ( value > Integer.MAX_VALUE )
			throw new IllegalArgumentException( "value too large" );
		return ( int ) value;
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
	public P getSourceBuffer()
	{
		return tempArray.get( sourceLength );
	}
}
