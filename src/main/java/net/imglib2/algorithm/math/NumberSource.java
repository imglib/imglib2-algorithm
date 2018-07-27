package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class NumberSource implements IFunction
{
	private final double number;
	
	public NumberSource( final Number number ) {
		this.number = number.doubleValue();
	}

	@Override
	public void eval( final RealType< ? > output ) {
		output.setReal( this.number );
	}

	@Override
	public void eval( final RealType< ? > output, final Localizable loc) {
		output.setReal( this.number );
	}

	@Override
	public NumberSource reInit( final RealType<?> tmp, final Map<String, RealType<?>> bindings, final Converter<RealType<?>, RealType<?>> converter)
	{
		return new NumberSource( this.number );
	}
}