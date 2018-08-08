package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Maximum;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Max extends ABinaryFunction
{
	public Max( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Max( final Object... obs )
	{
		super( obs );
	}

	@Override
	public < O extends RealType< O > > Maximum< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Maximum< O >( this.a.reInit( tmp, bindings, converter, imgSources ), this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}