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
package net.imglib2.algorithm.blocks.convert;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;
import java.util.function.Supplier;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.converter.Converter;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.NativeImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;

/**
 * Convert primitive arrays between ImgLib2 {@code NativeType}s using a {@link }Converter}.
 *
 * @param <S>
 * 		source type
 * @param <T>
 * 		target type
 * @param <I>
 * 		input primitive array type, e.g., float[]. Must correspond to S.
 * @param <O>
 * 		output primitive array type, e.g., float[]. Must correspond to T.
 */
class ConverterBlockProcessor< S extends NativeType< S >, T extends NativeType< T >, I, O > implements BlockProcessor< I, O >
{
	private final S sourceType;

	private final T targetType;

	private final Supplier< Converter< ? super S, T > > converterSupplier;

	private final TempArray< I > tempArray;

	private long[] sourcePos;

	private int[] sourceSize;

	private int sourceLength;

	private final BlockProcessorSourceInterval sourceInterval;

	private final Converter< ? super S, T > converter;

	private final Wrapper< S > wrapSource;

	private final Wrapper< T > wrapTarget;

	public ConverterBlockProcessor( final S sourceType, final T targetType, final Supplier< Converter< ? super S, T > > converterSupplier )
	{
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.converterSupplier = converterSupplier;

		tempArray = TempArray.forPrimitiveType( sourceType.getNativeTypeFactory().getPrimitiveType() );
		sourceInterval = new BlockProcessorSourceInterval( this );
		converter = converterSupplier.get();
		wrapSource = Wrapper.of( sourceType );
		wrapTarget = Wrapper.of( targetType );
	}

	private ConverterBlockProcessor( ConverterBlockProcessor< S, T, I, O > convert )
	{
		sourceType = convert.sourceType;
		targetType = convert.targetType;
		converterSupplier = convert.converterSupplier;

		tempArray = convert.tempArray.newInstance();
		sourceInterval = new BlockProcessorSourceInterval( this );
		converter = converterSupplier.get();
		wrapSource = Wrapper.of( sourceType );
		wrapTarget = Wrapper.of( targetType );
	}

	@Override
	public BlockProcessor< I, O > independentCopy()
	{
		return new ConverterBlockProcessor<>( this );
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

	@Override
	public void setTargetInterval( final long[] pos, final int[] size )
	{
		final int n = pos.length;
		if ( sourcePos == null || sourcePos.length != n )
		{
			sourcePos = new long[ n ];
			sourceSize = new int[ n ];
		}
		System.arraycopy( pos, 0, sourcePos, 0, n );
		System.arraycopy( size, 0, sourceSize, 0, n );
		sourceLength = safeInt( Intervals.numElements( sourceSize ) );
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
		final S in = wrapSource.wrap( src );
		final T out = wrapTarget.wrap( dest );
		for ( int i = 0; i < sourceLength; i++ )
		{
			in.index().set( i );
			out.index().set( i );
			converter.convert( in, out );
		}
	}

	// ------------------------------------------------------------------------
	//
	//   Wrap primitive array into a Type that can be passed to the Converter
	//

	private interface Wrapper< T extends NativeType< T > >
	{
		T wrap( final Object array );

		static < T extends NativeType< T > > Wrapper<T> of( T type )
		{
			return new WrapperImpl<>( type );
		}
	}

	private static class WrapperImpl< T extends NativeType< T >, A > extends AbstractImg< T > implements NativeImg< T, A >, Wrapper< T >
	{
		private final PrimitiveTypeProperties< ?, A > props;
		private Object array;
		private final T wrapper;

		WrapperImpl( T type )
		{
			super( new long[ 0 ] );
			final NativeTypeFactory< T, A > nativeTypeFactory = Cast.unchecked( type.getNativeTypeFactory() );
			props = Cast.unchecked( PrimitiveTypeProperties.get( nativeTypeFactory.getPrimitiveType() ) );
			wrapper = nativeTypeFactory.createLinkedType( this );
		}

		@Override
		public T wrap( final Object array )
		{
			this.array = array;
			wrapper.updateContainer( null );
			return wrapper;
		}

		@Override
		public A update( final Object updater )
		{
			return props.wrap( array );
		}

		@Override public void setLinkedType( final T type ) {throw new UnsupportedOperationException();}
		@Override public T createLinkedType() {throw new UnsupportedOperationException();}
		@Override public Cursor< T > cursor() {throw new UnsupportedOperationException();}
		@Override public Cursor< T > localizingCursor() {throw new UnsupportedOperationException();}
		@Override public Object iterationOrder() {throw new UnsupportedOperationException();}
		@Override public RandomAccess< T > randomAccess() {throw new UnsupportedOperationException();}
		@Override public ImgFactory< T > factory() {throw new UnsupportedOperationException();}
		@Override public Img< T > copy() {throw new UnsupportedOperationException();}
		@Override public T getType() {throw new UnsupportedOperationException();}
	}
}
