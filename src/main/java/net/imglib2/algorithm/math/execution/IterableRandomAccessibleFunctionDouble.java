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

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.math.Compute;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class IterableRandomAccessibleFunctionDouble< O extends RealType< O >> extends IterableRandomAccessibleFunction< O, O >
{
	public IterableRandomAccessibleFunctionDouble( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( operation, null, outputType.createVariable(), outputType, ( Converter< O, O > )converter );
	}
	
	/**
	 * Use a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	public IterableRandomAccessibleFunctionDouble( final IFunction operation, final O outputType )
	{
		this( operation, outputType, null );
	}
	
	public IterableRandomAccessibleFunctionDouble( final IFunction operation )
	{
		super( operation );
	}
	
	@Override
	public RandomAccess< O > randomAccess()
	{
		return new Compute( this.operation ).randomAccessDouble( this.outputType, ( Converter< RealType< ? >, O > )this.outConverter );
	}

	@Override
	public Cursor< O > cursor()
	{
		return new Compute( this.operation ).cursorDouble( this.outputType, ( Converter< RealType< ? >, O > )this.outConverter );
	}
}
