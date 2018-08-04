package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.AUnaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.Printing;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Print extends AUnaryFunction
{
	private final String title;
	
	public Print( final String title, final Object o )
	{
		super( o );
		this.title = title;
	}

	@Override
	public < O extends RealType< O > > Printing< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Printing< O >( this.title, this.a.reInit( tmp, bindings, converter, imgSources ) );
	}
}
