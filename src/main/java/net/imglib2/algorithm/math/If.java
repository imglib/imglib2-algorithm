package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IFunction;
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
	public RealType< ? > eval()
	{
		return 0 != this.a.eval().getRealFloat() ?
			// Then
			this.b.eval()
			// Else
			: this.c.eval();
	}

	@Override
	public RealType< ? > eval( final Localizable loc )
	{
		return 0 != this.a.eval().getRealFloat() ?
				// Then
				this.b.eval()
				// Else
				: this.c.eval();
	}

	@Override
	public If reInit( final RealType< ? > tmp, final Map< String, RealType< ? > > bindings, final Converter<RealType<?>, RealType<?>> converter )
	{
		// Optimization: reInit as IfBoolean if the first argument is a Compare.
		//               Avoids having to set the output to 1 (true) or 0 (false), 
		//               and then having to read it out and compare it to zero to make a boolean,
		//               instead returning a boolean directly.
		if ( this.a instanceof Compare )
			return new IfBoolean( tmp.copy(), ( Compare ) this.a.reInit( tmp, bindings, converter ), this.b.reInit( tmp, bindings, converter ), this.c.reInit( tmp, bindings, converter ) );
		return new If( tmp.copy(), this.a.reInit( tmp, bindings, converter ), this.b.reInit( tmp, bindings, converter ), this.c.reInit( tmp, bindings, converter ) );
	}
}