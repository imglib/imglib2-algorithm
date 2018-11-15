package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.IsGreaterThan;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class GreaterThan extends Compare
{
	public GreaterThan( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}

	@Override
	public < O extends RealType< O > > IsGreaterThan< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new IsGreaterThan< O >( tmp.copy(),
				this.a.reInit( tmp, bindings, converter, imgSources ),
				this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}