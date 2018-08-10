package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Division;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Div extends ABinaryFunction
{

	public Div( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Div( final Object... obs )
	{
		super( obs );
	}

	@Override
	public < O extends RealType< O > > Division< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Division< O >( tmp.copy(), this.a.reInit( tmp, bindings, converter, imgSources ), this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}