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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class LetBinding< O extends RealType< O > > implements OFunction< O >
{
	private final O scrap;
	private double scrap_value;
	private final String varName;
	private final OFunction< O > varValue;
	private final OFunction< O > body;
	
	public LetBinding( final O scrap, final String varName, final OFunction< O > varValue, final OFunction< O > body )
	{
		this.scrap = scrap;
		this.varName = varName;
		this.varValue = varValue;
		this.body = body;
	}
	
	public LetBinding(
			final O scrap,
			final String varName,
			final Map< String, LetBinding< O > > bindings,
			final IFunction varValue,
			final IFunction body,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		this.scrap = scrap;
		this.varName = varName;
		bindings.put( varName, this );
		this.varValue = varValue.reInit( scrap.copy(), bindings, converter, imgSources );
		this.body = body.reInit( scrap.copy(), bindings, converter, imgSources );
	}
	
	public final O getScrapValue()
	{
		return this.scrap;
	}
	
	public final double getDoubleValue()
	{
		return this.scrap_value;
	}
	
	@Override
	public final O eval()
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval() );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval();
	}

	@Override
	public final O eval( final Localizable loc)
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval( loc ) );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval( loc );
	}
	
	public final String getVarName()
	{
		return this.varName;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.varValue, this.body );
	}
	
	@Override
	public final double evalDouble()
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap_value = this.varValue.evalDouble();
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.evalDouble();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap_value = this.varValue.evalDouble( loc );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.evalDouble( loc );
	}
}
