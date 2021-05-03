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

import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Comparison;
import net.imglib2.algorithm.math.execution.IfStatement;
import net.imglib2.algorithm.math.execution.IfStatementBoolean;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class If extends ATrinaryFunction
{
	public If( final Object o1, final Object o2, final Object o3 )
	{
		super( o1, o2, o3 );
	}

	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		// Fix: if there is an ImgSource among the children of then (this.b) or else (this.c),
		// replace them with a Var, reading from a Let inserted as a parent of this If,
		// in order to guarantee that their iterators are advanced.
		final Map< Variable< O >, OFunction< O > > imgS;
		if ( null == imgSources )
			imgS = new HashMap<>();
		else
			imgS = imgSources; // There is an upstream If
		
		// Optimization: reInit as IfBoolean if the first argument is a Compare.
		//               Avoids having to set the output to 1 (true) or 0 (false), 
		//               and then having to read it out and compare it to zero to make a boolean,
		//               instead returning a boolean directly.
		final OFunction< O > instance;
		if ( this.a instanceof Compare )
			instance = new IfStatementBoolean< O >( ( Comparison< O > ) this.a.reInit( tmp, bindings, converter, imgS ),
					this.b.reInit( tmp, bindings, converter, imgS ), this.c.reInit( tmp, bindings, converter, imgS ) );
		else
			instance = new IfStatement< O >( this.a.reInit( tmp, bindings, converter, imgSources ),
					this.b.reInit( tmp, bindings, converter, imgS ), this.c.reInit( tmp, bindings, converter, imgS ) );
		
		// Check if there was an upstream If, which has preference, or there aren't any imgSource downstream
		if ( null != imgSources || imgS.isEmpty() )
			return instance;
		
		// Return a Let that declares all Vars in imgS, recursively, and places this If as the innermost body
		LetBinding< O > let = null;
		for ( final Map.Entry< Variable< O >, OFunction< O > > e : imgS.entrySet() )
			let = new LetBinding< O >( e.getKey().getScrap(), e.getKey().getName(), e.getValue(), null == let ? instance : let );
		
		return let;
	}
};
