package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Minimum;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Min extends ABinaryFunction
{
	public Min( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Min( final Object... obs )
	{
		super( obs );
	}
	
	@Override
	public < O extends RealType< O > > Minimum< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Minimum< O >(
				this.a.reInit( tmp, bindings, converter, imgSources ),
				this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}