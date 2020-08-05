package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Subtraction;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.algorithm.math.execution.ZeroMinus;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Sub extends ABinaryFunction
{
	public Sub( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Sub( final Object... obs )
	{
		super( obs );
	}

	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		final OFunction< O > a = this.a.reInit( tmp, bindings, converter, imgSources ),
    			 			 b = this.b.reInit( tmp, bindings, converter, imgSources );

		// Optimization: remove null ops
		if ( b.isZero() )
			return a;
		if ( a.isZero() )
			return new ZeroMinus< O >( tmp.copy(), b );
		
		return new Subtraction< O >( tmp.copy(), a, b );
	}
}