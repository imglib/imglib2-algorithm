package net.imglib2.algorithm.math;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
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
			final Map< String, O > bindings,
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
		if ( this.a instanceof Comparison )
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