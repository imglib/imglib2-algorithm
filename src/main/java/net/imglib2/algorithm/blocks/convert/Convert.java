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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractUnaryBlockOperator;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.converter.Converter;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

/**
 * Create {@link UnaryBlockOperator} to convert blocks between standard ImgLib2 {@code Type}s.
 * Provides rounding, optional clamping, and handling unsigned types.
 * <p>
 * Supported types:
 * <ul>
 * <li>{@code UnsignedByteType}</li>
 * <li>{@code UnsignedShortType}</li>
 * <li>{@code UnsignedIntType}</li>
 * <li>{@code ByteType}</li>
 * <li>{@code ShortType}</li>
 * <li>{@code IntType}</li>
 * <li>{@code LongType}</li>
 * <li>{@code FloatType}</li>
 * <li>{@code DoubleType}</li>
 * </ul>
 */
public class Convert
{
	/**
	 * Create {@link UnaryBlockOperator} to convert blocks from {@code S} to
	 * {@code T}.
	 * <p>
	 * Supported source/target types are {@code ByteType}, {@code
	 * UnsignedByteType}, {@code ShortType}, {@code UnsignedShortType}, {@code
	 * IntType}, {@code UnsignedIntType}, {@code LongType}, {@code
	 * UnsignedLongType}, {@code FloatType}, and {@code DoubleType}.
	 * <p>
	 * Target values are not clamped, so overflow may occur if the source type
	 * has a larger range than the target type.
	 * <p>
	 * The returned factory function creates an operator matching the type
	 * {@code S} of the given input {@code BlockSupplier<T>}.
	 *
	 * @param targetType
	 * 		an instance of the target type
	 * @param <S>
	 * 		source type
	 * @param <T>
	 * 		target type
	 *
	 * @return factory for {@code UnaryBlockOperator} to convert blocks from {@code S} to {@code T}
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	Function< BlockSupplier< S >, UnaryBlockOperator< S, T > > convert( final T targetType )
	{
		return convert( targetType, ClampType.NONE );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks from {@code S} to
	 * {@code T}.
	 * <p>
	 * Supported source/target types are {@code ByteType}, {@code
	 * UnsignedByteType}, {@code ShortType}, {@code UnsignedShortType}, {@code
	 * IntType}, {@code UnsignedIntType}, {@code LongType}, {@code
	 * UnsignedLongType}, {@code FloatType}, and {@code DoubleType}.
	 * <p>
	 * If the target type cannot represent the full range of the source type,
	 * values are clamped according to the specified {@link ClampType}.
	 * <p>
	 * The returned factory function creates an operator matching the type
	 * {@code S} of the given input {@code BlockSupplier<T>}.
	 *
	 * @param targetType
	 * 		an instance of the target type
	 * @param clamp
	 * 		ClampType
	 * @param <S>
	 * 		source type
	 * @param <T>
	 * 		target type
	 *
	 * @return factory for {@code UnaryBlockOperator} to convert blocks from {@code S} to {@code T}
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	Function< BlockSupplier< S >, UnaryBlockOperator< S, T > > convert( final T targetType, final ClampType clamp )
	{
		return s -> createOperator( s.getType(), targetType, clamp );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks from {@code S} to
	 * {@code T} with the specified {@code Converter}.
	 * <p>
	 * The returned factory function creates an operator matching the type
	 * {@code S} of the given input {@code BlockSupplier<T>}.
	 *
	 * @param targetType
	 * 		an instance of the target type
	 * @param converterSupplier
	 * 		creates new converter instances
	 * @param <S>
	 * 		source type
	 * @param <T>
	 * 		target type
	 *
	 * @return factory for {@code UnaryBlockOperator} to convert blocks from {@code S} to {@code T}
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	Function< BlockSupplier< S >, UnaryBlockOperator< S, T > > convert( final T targetType, Supplier< Converter< ? super S, T > > converterSupplier )
	{
		return s -> createOperator( s.getType(), targetType, converterSupplier );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * No clamping.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > createOperator( final S sourceType, final T targetType )
	{
		return createOperator( sourceType, targetType, ClampType.NONE );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * Clamp target values according to {@code clamp}.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > createOperator( final S sourceType, final T targetType, final ClampType clamp )
	{
		if ( Objects.equals( sourceType.getClass(), targetType.getClass() ) )
			return Cast.unchecked( new Identity<>( sourceType, 0 ) );

		return new DefaultUnaryBlockOperator<>(
				sourceType, targetType, 0, 0,
				new ConvertBlockProcessor<>( sourceType, targetType, clamp ) );
	}

	// TODO: move to upper level, make public?
	static class Identity< T extends NativeType< T > > extends AbstractUnaryBlockOperator< T, T >
	{
		protected Identity( final T type, final int numDimensions )
		{
			super( type, type, numDimensions, numDimensions );
		}

		protected Identity( final AbstractUnaryBlockOperator< T, T > op )
		{
			super( op );
		}

		@Override
		public void compute( final BlockSupplier< T > src, final Interval interval, final Object dest )
		{
			src.copy( interval, dest );
		}

		@Override
		public UnaryBlockOperator< T, T > independentCopy()
		{
			return this;
		}

		@Override
		public BlockSupplier< T > applyTo( final BlockSupplier< T > blocks )
		{
			return blocks;
		}

		@Override
		public < U extends NativeType< U > > UnaryBlockOperator< T, U > andThen( final UnaryBlockOperator< T, U > op )
		{
			return op;
		}
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType} with the specified {@code Converter}.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > createOperator( final S sourceType, final T targetType, Supplier< Converter< ? super S, T > > converterSupplier )
	{
		return new DefaultUnaryBlockOperator<>(
				sourceType, targetType, 0, 0,
				new ConverterBlockProcessor<>( sourceType, targetType, converterSupplier ) );
	}
}
