package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.NumericSource;
import net.imglib2.algorithm.math.execution.Power;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Pow extends ABinaryFunction
{
	public Pow( final Object o1, final Object o2 )
	{
		super( o1, o2 );
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
		
		if ( b.isZero() )
			return new NumericSource< O >( tmp.copy(), 1 );
		if ( a.isOne() || b.isOne() )
			return a;
		
		return new Power< O >( tmp.copy(), a, b );
	}
}