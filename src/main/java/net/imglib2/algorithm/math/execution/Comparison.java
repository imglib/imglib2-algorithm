/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

abstract public class Comparison< O extends RealType< O > > extends ABooleanFunction< O >
{
	protected final OFunction< O > a, b;

	public Comparison( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap );
		this.a = a;
		this.b = b;
	}
	
	abstract protected boolean compare( final O t1, final O t2 );

	@Override
	public final O eval()
	{
		return this.compare( this.a.eval(), this.b.eval() ) ? one : zero;
	}

	@Override
	public final O eval( final Localizable loc)
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) ) ? one : zero;
	}
	
	@Override
	public boolean evalBoolean()
	{
		return this.compare( this.a.eval(), this.b.eval() );
	}
	
	@Override
	public boolean evalBoolean( final Localizable loc )
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) );
	}
	
	@Override
	public final List< OFunction< O > > children()
	{
		return Arrays.asList( this.a, this.b );
	}
	
	
	@Override
	public final double evalDouble()
	{
		return this.compare( this.a.eval(), this.b.eval() ) ? 1.0 : 0.0;
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) ) ? 1.0 : 0.0;
	}
}
