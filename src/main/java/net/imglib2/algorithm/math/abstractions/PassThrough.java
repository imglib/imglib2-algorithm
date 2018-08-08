package net.imglib2.algorithm.math.abstractions;

import java.util.Map;

import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

/**
 * Does nothing: the reInit method returns a copy of the encapsulated IFunction
 */
public class PassThrough extends AUnaryFunction
{
	public PassThrough( final Object o )
	{
		super( o );
	}

	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return this.a.reInit(tmp, bindings, converter, imgSources);
	}
}
