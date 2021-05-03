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
package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalNot< O extends RealType< O > > extends ABooleanFunction< O >
{
	protected final OFunction< O > a;
	
	public LogicalNot( final O scrap, final OFunction< O > a )
	{
		super( scrap );
		this.a = a;
	}

	@Override
	public boolean evalBoolean()
	{
		// It's true if not zero, hence reverse
		//return ! (0 != this.a.eval().getRealDouble() );
		return 0 == this.a.eval().getRealDouble();
	}

	@Override
	public boolean evalBoolean( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		//return ! (0 != this.a.eval().getRealDouble() );
		return 0 == this.a.eval( loc ).getRealDouble();
	}

	@Override
	public final O eval()
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval().getRealDouble() ? one : zero;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval( loc ).getRealDouble() ? one : zero;
	}

	@Override
	public final List< OFunction< O > > children()
	{
		return Arrays.asList( this.a );
	}

	@Override
	public final double evalDouble()
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval().getRealDouble() ? 1.0 : 0.0;
	}

	@Override
	public final double evalDouble( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval( loc ).getRealDouble() ? 1.0 : 0.0;
	}
}
