package net.imglib2.algorithm.math;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.algorithm.math.optimizations.IfBoolean;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class If extends ATrinaryFunction
{
	public If( final Object o1, final Object o2, final Object o3 )
	{
		super( o1, o2, o3 );
	}
	
	protected If( final RealType< ? > scrap, final IFunction f1, final IFunction f2, final IFunction f3 )
	{
		super ( scrap, f1, f2, f3 );
	}

	@Override
	public final RealType< ? > eval()
	{
		return 0 != this.a.eval().getRealFloat() ?
			// Then
			this.b.eval()
			// Else
			: this.c.eval();
	}

	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		return 0 != this.a.eval().getRealFloat() ?
				// Then
				this.b.eval()
				// Else
				: this.c.eval();
	}

	@Override
	public IFunction reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter<RealType<?>, RealType<?>> converter,
			final Map< IVar, IFunction > imgSources )
	{
		// Fix: if there is an ImgSource among the children of then (this.b) or else (this.c),
		// replace them with a Var, reading from a Let inserted as a parent of this If,
		// in order to guarantee that their iterators are advanced.
		final Map< IVar, IFunction > imgS;
		if ( null == imgSources )
			imgS = new HashMap<>();
		else
			imgS = imgSources; // There is an upstream If
		
		// Optimization: reInit as IfBoolean if the first argument is a Compare.
		//               Avoids having to set the output to 1 (true) or 0 (false), 
		//               and then having to read it out and compare it to zero to make a boolean,
		//               instead returning a boolean directly.
		final IFunction instance;
		if ( this.a instanceof Compare )
			instance = new IfBoolean( tmp.copy(), ( Compare ) this.a.reInit( tmp, bindings, converter, imgS ),
					this.b.reInit( tmp, bindings, converter, imgS ), this.c.reInit( tmp, bindings, converter, imgS ) );
		else
			instance = new If( tmp.copy(), this.a.reInit( tmp, bindings, converter, imgSources ),
					this.b.reInit( tmp, bindings, converter, imgS ), this.c.reInit( tmp, bindings, converter, imgS ) );
		
		// Check if there was an upstream If, which has preference, or there aren't any imgSource downstream
		if ( null != imgSources || imgS.isEmpty() )
			return instance;
		
		// Return a Let that declares all Vars in imgS, recursively, and places this If as the innermost body
		Let let = null;
		for ( final Map.Entry< IVar, IFunction > e : imgS.entrySet() )
			let = new Let( e.getKey().getScrap(), e.getKey().getName(), e.getValue(), null == let ? instance : let );
		
		return let;
	}
};