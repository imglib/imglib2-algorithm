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
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class IfStatement< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b, c;
	
	public IfStatement( final OFunction< O > f1, final OFunction< O > f2, final OFunction< O > f3 )
	{
		this.a = f1;
		this.b = f2;
		this.c = f3;
	}
	
	@Override
	public final O eval()
	{
		return 0 != this.a.eval().getRealFloat() ?
			// Then
			this.b.eval()
			// Else
			: this.c.eval();
	}

	@Override
	public final O eval( final Localizable loc )
	{
		return 0 != this.a.eval( loc ).getRealFloat() ?
			// Then
			this.b.eval( loc )
			// Else
			: this.c.eval( loc );
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.a, this.b, this.c );
	}
	
	
	@Override
	public final double evalDouble()
	{
		return 0 != this.a.evalDouble() ?
			// Then
			this.b.evalDouble()
			// Else
			: this.c.evalDouble();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		return 0 != this.a.evalDouble( loc ) ?
			// Then
			this.b.evalDouble( loc )
			// Else
			: this.c.evalDouble( loc );	}
}
