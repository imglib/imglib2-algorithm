package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.AUnaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.algorithm.math.execution.ZeroMinus;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Minus extends AUnaryFunction
{
	public Minus( final Object o )
	{
		super( o );
	}

	@Override
	public < O extends RealType< O > > ZeroMinus< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new ZeroMinus< O >( tmp.copy(), this.a.reInit( tmp, bindings, converter, imgSources ) );
	}

}
