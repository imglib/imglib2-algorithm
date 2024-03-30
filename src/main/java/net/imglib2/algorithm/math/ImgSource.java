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
package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.SourceInterval;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.ImgSourceIterable;
import net.imglib2.algorithm.math.execution.ImgSourceIterableDirect;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class ImgSource< I extends RealType< I > > extends ViewableFunction implements IFunction, SourceInterval
{
	private final RandomAccessibleInterval< I > rai;

	public ImgSource( final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
	}

	@SuppressWarnings("unchecked")
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		// Optimization: if input image type is the same or a subclass of
		// the output image type (represented here by tmp), then avoid the converter
		// but only if the targetImg is different than this.rai: otherwise, an intermediate
		// computation result holder must be used (the scrap).
		final OFunction< O > s;
		if ( tmp.getClass().isAssignableFrom( this.rai.randomAccess().get().getClass() ) )
			s = new ImgSourceIterableDirect< O >( ( RandomAccessibleInterval< O > )this.rai );
		else
			s = new ImgSourceIterable< I, O >( tmp.copy(), ( Converter< I, O > )converter, this.rai );
		
		// Addressing "If" branching: replace with a Var
		if ( null != imgSources )
		{
			final Variable< O > var = new Variable< O >( "#imgSource#" + imgSources.size(), tmp.copy() );
			imgSources.put( var, s );
			return var;
		}
		
		return s;
	}
	
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}
	
	public Interval getInterval()
	{
		return this.rai;
	}
}
