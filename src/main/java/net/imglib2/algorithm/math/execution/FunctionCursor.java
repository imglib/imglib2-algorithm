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
package net.imglib2.algorithm.math.execution;

import java.util.HashMap;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class FunctionCursor< C extends RealType< C >, O extends RealType< O > > extends AbstractCursor< O >
{
	protected final IFunction operation;
	protected final Converter< RealType< ? >, C > inConverter;
	protected final Converter< C, O > outConverter;
	
	protected OFunction< C > f;
	protected C scrapC;
	protected O scrapO;
	protected Cursor< ? > cursor;
	protected final Sampler< O > sampler;
 	
	/**
	 * 
	 * @param operation
	 * @param inConverter Can be null, and if so, a generic {@code Util#genericRealTypeConverter()} will be used.
	 * @param computeType
	 * @param outputType
	 * @param outConverter Can be null, and if so, a generic integer or real converter will be used.
	 */
	@SuppressWarnings("unchecked")
	public FunctionCursor(
			final IFunction operation,
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter )
	{
		super( Util.findFirstInterval( operation ).numDimensions() );
		this.operation = operation;
		this.inConverter = inConverter == null ? Util.genericRealTypeConverter() : inConverter;
		if ( computeType.getClass() == outputType.getClass() )
		{
			this.sampler = ( Sampler< O > )new FunctionSamplerDirect();
			this.outConverter = null;
		}
		else
		{
			if ( null == outConverter )
			{
				if ( computeType instanceof IntegerType && outputType instanceof IntegerType )
					this.outConverter = ( Converter< C, O > )Util.genericIntegerTypeConverter();
				else
					this.outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
			}
			else
				this.outConverter = outConverter;
			this.sampler = new FunctionSamplerConverter();
		}
		this.scrapC = computeType.createVariable();
		this.scrapO = outputType.createVariable();
		this.reset();
	}
	
	private final class FunctionSamplerConverter implements Sampler< O >
	{	
		@Override
		public final O get()
		{
			outConverter.convert( scrapC, scrapO ); 
			return scrapO;
		}

		@Override
		public final Sampler< O > copy() { return null; }
	}
	
	private final class FunctionSamplerDirect implements Sampler< C >
	{
		@Override
		public final C get()
		{ 
			return scrapC;
		}

		@Override
		public final Sampler< C > copy() { return null; }
	}

	@Override
	public final O get()
	{
		return this.sampler.get();
	}

	@Override
	public void fwd()
	{
		this.scrapC = this.f.eval();
	}

	@Override
	public final boolean hasNext()
	{
		return this.cursor.hasNext();
	}

	@Override
	public void reset()
	{
		this.f = this.operation.reInit( this.scrapC.createVariable() , new HashMap< String, LetBinding< C > >(), this.inConverter, null );
		this.cursor = Util.findFirstIterableImgSource( this.f ).getCursor();
	}

	@Override
	public long getLongPosition( final int d )
	{
		return this.cursor.getLongPosition( d );
	}

	@Override
	public void localize( final long[] position )
	{
			this.cursor.localize( position );
	}

	@Override
	public AbstractCursor< O > copy()
	{
		return new FunctionCursor< C, O >( this.operation, this.inConverter, this.scrapC, this.scrapO, this.outConverter );
	}
}
