package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Subtraction;
import net.imglib2.algorithm.math.execution.Variable;
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
	public < O extends RealType< O > > Subtraction< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Subtraction< O >( tmp.copy(),
				this.a.reInit( tmp, bindings, converter, imgSources ),
				this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}