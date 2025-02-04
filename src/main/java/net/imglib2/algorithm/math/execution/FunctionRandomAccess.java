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
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class FunctionRandomAccess< C extends RealType< C >, O extends RealType< O > > extends Point implements RandomAccess< O >
{
	private final IFunction operation;
	private final C computeType;
	private final O outputType;
	private final Converter< RealType< ? >, C > inConverter;
	private final Converter< C, O > outConverter;
	private final Sampler< O > sampler;

	@SuppressWarnings("unchecked")
	public FunctionRandomAccess(
			final IFunction operation,
			Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			Converter< C, O > outConverter )
	{
		super( Util.findFirstInterval( operation ).numDimensions() );
		if ( null == inConverter )
			inConverter = Util.genericRealTypeConverter();
		
		final boolean are_same_type = computeType.getClass() == outputType.getClass();
		
		if ( null == outConverter && !are_same_type )
		{
			if ( computeType instanceof IntegerType && outputType instanceof IntegerType )
				outConverter = ( Converter< C, O > )Util.genericIntegerTypeConverter();
			else
				outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
		}
		
		if ( are_same_type )
			this.sampler = new FunctionSamplerDirect( this, operation, ( Converter< RealType< ? >, O > )inConverter, outputType );
		else
			this.sampler = new FunctionSampler( this, operation, inConverter, computeType, outputType, outConverter );
		
		this.operation = operation;
		this.inConverter = inConverter;
		this.outConverter = outConverter;
		this.computeType = computeType;
		this.outputType = outputType;
	}
	
	private final class FunctionSampler implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final C computeType;
		private final O outputType;
		private final Converter< RealType< ? >, C > inConverter;
		private final Converter< C, O > outConverter;
		private final OFunction< C > f;
		
		FunctionSampler(
				final Point point,
				final IFunction operation,
				final Converter< RealType< ? >, C > inConverter,
				final C computeType,
				final O outputType,
				final Converter< C, O > outConverter
				)
		{
			this.point = point;
			this.operation = operation;
			this.computeType = computeType;
			this.outputType = outputType.createVariable();
			this.inConverter = inConverter;
			this.outConverter = outConverter;
			this.f = operation.reInit(
					computeType.createVariable(),
					new HashMap< String, LetBinding< C > >(),
					inConverter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSampler( this.point, this.operation, this.inConverter, this.computeType, this.outputType, this.outConverter );
		}

		@Override
		public O get()
		{
			this.outConverter.convert( this.f.eval( this.point ), outputType );
			return this.outputType;
		}

		@Override
		public O getType()
		{
			return outputType;
		}
	}
	
	private final class FunctionSamplerDirect implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final O outputType;
		private final Converter< RealType< ? >, O > inConverter;
		private final OFunction< O > f;
		
		FunctionSamplerDirect(
				final Point point,
				final IFunction operation,
				final Converter< RealType< ? >, O > inConverter,
				final O outputType
				)
		{
			this.point = point;
			this.operation = operation;
			this.outputType = outputType.createVariable();
			this.inConverter = inConverter;
			this.f = operation.reInit(
					outputType.createVariable(),
					new HashMap< String, LetBinding< O > >(),
					inConverter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSamplerDirect( this.point, this.operation, this.inConverter, this.outputType );
		}

		@Override
		public final O get()
		{
			return this.f.eval( this.point );
		}

		@Override
		public O getType()
		{
			return outputType;
		}
	}

	@Override
	public O get()
	{
		return this.sampler.get();
	}

	@Override
	public O getType()
	{
		return outputType;
	}

	@Override
	public RandomAccess< O > copy()
	{
		return new FunctionRandomAccess< C, O >(
				this.operation,
				this.inConverter,
				this.computeType,
				this.outputType,
				this.outConverter );
	}
}
