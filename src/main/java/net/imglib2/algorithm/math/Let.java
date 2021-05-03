/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.util.HashMap;
import java.util.Map;

import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Let extends ViewableFunction implements IFunction, IBinaryFunction
{
	private final String varName;
	private final IFunction varValue;
	private final IFunction body;
	
	public Let( final String varName, final Object varValue, final Object body )
	{
		this.varName = varName;
		this.varValue = Util.wrap( varValue );
		this.body = Util.wrap( body );
	}
	
	public Let( final Object[] pairs, final Object body )
	{
		if ( pairs.length < 2 || 0 != pairs.length % 2 )
			throw new RuntimeException( "Let: need an even number of var-value pairs." );
		
		this.varName = ( String )pairs[0];
		this.varValue = Util.wrap( pairs[1] );
		
		if ( 2 == pairs.length )
		{
			this.body = Util.wrap( body );
		} else
		{
			final Object[] pairs2 = new Object[ pairs.length - 2 ];
			System.arraycopy( pairs, 2, pairs2, 0, pairs2.length );
			this.body = new Let( pairs2, body );
		}
	}
	
	public Let( final Object... obs )
	{
		this( fixAndValidate( obs ), obs[ obs.length - 1] );
	}
	
	static private final Object[] fixAndValidate( final Object[] obs )
	{
		if ( obs.length < 3 || 0 == obs.length % 2 )
			throw new RuntimeException( "Let: need an even number of var-value pairs plus the body at the end." );
		final Object[] obs2 = new Object[ obs.length - 1];
		System.arraycopy( obs, 0, obs2, 0, obs2.length );
		return obs2;
	}

	@Override
	public < O extends RealType< O > > LetBinding< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		final O scrap = tmp.copy();
		final Map< String, LetBinding< O > > rebind = new HashMap<>( bindings );
		// The LetBinding constructor will add itself to the rebind prior to reInit the two IFunction varValue and body.
		return new LetBinding< O >( scrap, this.varName,
				rebind,
				this.varValue, //this.varValue.reInit( tmp, rebind, converter, imgSources ),
				this.body, //this.body.reInit( tmp, rebind, converter, imgSources ) );
				converter, imgSources );
	}

	@Override
	public IFunction getFirst()
	{
		return this.varValue;
	}

	@Override
	public IFunction getSecond()
	{
		return this.body;
	}
	
	public String getVarName()
	{
		return this.varName;
	}
}
