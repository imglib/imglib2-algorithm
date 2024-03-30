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
package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IImgSourceIterable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgSourceIterable< I extends RealType< I >, O extends RealType< O > > implements OFunction< O >, IImgSourceIterable< I >
{
	private final RandomAccessibleInterval< I > rai;
	private final Cursor< I > it;
	private final RandomAccess< I > ra;
	private Converter< I, O > converter;
	private final O scrap;
	
	public ImgSourceIterable( final O scrap, final Converter< I, O > converter, final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).cursor();
		this.ra = rai.randomAccess();
		this.converter = converter;
		this.scrap = scrap;
	}
	
	@Override
	public final O eval()
	{
		this.converter.convert( this.it.next(), this.scrap );
		return this.scrap;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), this.scrap );
		return this.scrap;
	}
	
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}

	@Override
	public Cursor< I > getCursor()
	{
		return this.it;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
	
	@Override
	public final double evalDouble()
	{
		return this.it.next().getRealDouble();
	}

	@Override
	public final double evalDouble( final Localizable loc )
	{
		this.ra.setPosition( loc );
		return this.ra.get().getRealDouble();
	}
}
