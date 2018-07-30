package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class NumberSource implements IFunction
{
	private final Number number;
	private final RealType< ? > scrap;
	
	public NumberSource( final Number number ) {
		this.scrap = null;
		this.number = number;
	}
	
	private NumberSource( final RealType< ? > scrap, final Number number )
	{
		this.scrap = scrap;
		this.number = number;
		this.scrap.setReal( this.number.doubleValue() );
	}

	@Override
	public final RealType< ? > eval( ) {
		return this.scrap;
	}

	@Override
	public final RealType< ? > eval( final Localizable loc) {
		return this.scrap;
	}

	@Override
	public NumberSource reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return new NumberSource( tmp.copy(), this.number );
	}
}