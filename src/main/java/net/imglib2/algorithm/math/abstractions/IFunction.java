package net.imglib2.algorithm.math.abstractions;

import java.util.Map;

import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public interface IFunction
{	
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources
			);
}