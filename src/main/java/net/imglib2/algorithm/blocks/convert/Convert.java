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

import java.util.function.Supplier;

import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.converter.Converter;
import net.imglib2.type.NativeType;

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
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * No clamping.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > convert( final S sourceType, final T targetType )
	{
		return convert( sourceType, targetType, ClampType.NONE );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * Clamp target values according to {@code clamp}.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > convert( final S sourceType, final T targetType, final ClampType clamp )
	{
		return new DefaultUnaryBlockOperator<>( sourceType, targetType, new ConvertBlockProcessor<>( sourceType, targetType, clamp ) );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType} with the specified {@code Converter}.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > convert( final S sourceType, final T targetType, Supplier< Converter< ? super S, T > > converterSupplier )
	{
		return new DefaultUnaryBlockOperator<>( sourceType, targetType, new ConverterBlockProcessor<>( sourceType, targetType, converterSupplier ) );
	}
}
