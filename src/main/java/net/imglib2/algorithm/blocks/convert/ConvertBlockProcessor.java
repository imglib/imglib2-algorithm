/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.convert;

import java.util.Arrays;
import java.util.function.Supplier;
import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.algorithm.blocks.util.UnaryOperatorType;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.NativeType;
import net.imglib2.util.CloseableThreadLocal;
import net.imglib2.util.Intervals;

/**
 * Convert primitive arrays between standard ImgLib2 {@code Type}s.
 * Provides rounding, optional clamping, and handling unsigned types.
 *
 * @param <I>
 * 		input primitive array type, e.g., float[]
 * @param <O>
 * 		output primitive array type, e.g., float[]
 */
class ConvertBlockProcessor< S extends NativeType< S >, T extends NativeType< T >, I, O > implements BlockProcessor< I, O >
{
	private final S sourceType;

	private final T targetType;

	private final TempArray< I > tempArray;

	private final ConvertLoop< I, O > loop;

	private Supplier< ConvertBlockProcessor< S, T, I, O > > threadSafeSupplier;

	private long[] sourcePos;

	private int[] sourceSize;

	private int sourceLength;

	private final BlockProcessorSourceInterval sourceInterval;

	public ConvertBlockProcessor( final S sourceType, final T targetType, final ClampType clamp )
	{
		this.sourceType = sourceType;
		this.targetType = targetType;
		tempArray = TempArray.forPrimitiveType( sourceType.getNativeTypeFactory().getPrimitiveType() );
		loop = ConvertLoops.get( UnaryOperatorType.of( sourceType, targetType ), clamp );
		sourceInterval = new BlockProcessorSourceInterval( this );
	}

	private ConvertBlockProcessor( ConvertBlockProcessor< S, T, I, O > convert )
	{
		sourceType = convert.sourceType;
		targetType = convert.targetType;
		tempArray = convert.tempArray.newInstance();
		loop = convert.loop;
		sourceInterval = new BlockProcessorSourceInterval( this );
		threadSafeSupplier = convert.threadSafeSupplier;
	}

	private ConvertBlockProcessor< S, T, I, O > newInstance()
	{
		return new ConvertBlockProcessor<>( this );
	}

	@Override
	public Supplier< ? extends BlockProcessor< I, O > > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::newInstance )::get;
		return threadSafeSupplier;
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		final int n = interval.numDimensions();
		if ( sourcePos == null || sourcePos.length != n )
		{
			sourcePos = new long[ n ];
			sourceSize = new int[ n ];
		}
		interval.min( sourcePos );
		Arrays.setAll( sourceSize, d -> safeInt( interval.dimension( d ) ) );
		sourceLength = safeInt( Intervals.numElements( sourceSize ) );
	}

	private static int safeInt( final long value )
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
	public I getSourceBuffer()
	{
		return tempArray.get( sourceLength );
	}

	@Override
	public void compute( final I src, final O dest )
	{
		loop.apply( src, dest, sourceLength );
	}
}
