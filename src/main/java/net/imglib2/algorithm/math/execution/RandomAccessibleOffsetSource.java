/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2022 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class RandomAccessibleOffsetSource< I extends RealType< I >, O extends RealType< O > > implements OFunction< O >, RandomAccessOnly< I >
{
	private final RandomAccessible< I > src;
	protected final RandomAccess< I > ra;
	protected final O scrap;
	private final Converter< RealType< ? >, O > converter;
	
	public RandomAccessibleOffsetSource( final O scrap,  final Converter< RealType< ? >, O > converter, final RandomAccessible< I > src, final long[] offset )
	{
		this.scrap = scrap;
		this.src = src;
		this.converter = converter;
		boolean zeros = true;
		for ( int i = 0; i < offset.length; ++i )
			if ( 0 != offset[ i ] )
			{
				zeros = false;
				break;
			}
		if ( zeros )
			this.ra = src.randomAccess();
		else
		{
			final long[] offsetNegative = new long[ offset.length ];
			for ( int i = 0; i < offset.length; ++i )
				offsetNegative[ i ] = - offset[ i ];
			this.ra = Views.translate( src, offsetNegative ).randomAccess();
		}
	}
	
	@Override
	public final O eval()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), this.scrap );
		return this.scrap;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
	
	@Override
	public final double evalDouble()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		this.ra.setPosition( loc );
		return this.ra.get().getRealDouble();
	}
	
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.src;
	}
}
