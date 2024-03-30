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

import java.util.HashMap;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class FunctionRandomAccessDouble< O extends RealType< O > > extends Point implements RandomAccess< O >
{
	private final FunctionSampler sampler;

	public FunctionRandomAccessDouble( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( Util.findFirstInterval( operation ).numDimensions() );
		this.sampler = new FunctionSampler( this, operation, outputType, converter );
	}
	
	private final class FunctionSampler implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final O scrap;
		private final Converter< RealType< ? >, O > converter;
		private final OFunction< O > f;
		
		FunctionSampler( final Point point, final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
		{
			this.point = point;
			this.operation = operation;
			this.scrap = outputType.createVariable();
			this.converter = converter;
			this.f = operation.reInit(
					outputType.copy(),
					new HashMap< String, LetBinding< O > >(),
					null == converter ? Util.genericRealTypeConverter() : converter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSampler( this.point, this.operation, this.scrap, this.converter );
		}

		@Override
		public final O get()
		{
			this.scrap.setReal( this.f.evalDouble( this.point ) );
			return this.scrap;
		}
	}
	
	@Override
	public O get()
	{
		return this.sampler.get();
	}

	@Override
	public RandomAccess< O > copy()
	{
		return new FunctionRandomAccessDouble< O >( this.sampler.operation, this.sampler.scrap, this.sampler.converter );
	}
}
