package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.AUnaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Logarithm;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Log extends AUnaryFunction
{
	public Log( final Object o1 )
	{
		super( o1 );
	}

	@Override
	public < O extends RealType< O > > Logarithm< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Logarithm< O >( tmp.copy(),
				this.a.reInit( tmp, bindings, converter, imgSources ) );
	}
}
