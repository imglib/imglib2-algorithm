package net.imglib2.algorithm.math.abstractions;

import java.util.Map;

import net.imglib2.Localizable;
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
	public RealType< ? > eval() {
		return null;
	}

	@Override
	public RealType< ? > eval( final Localizable loc ) {
		return null;
	}

	@Override
	public IFunction reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return this.a.reInit(tmp, bindings, converter, imgSources);
	}
}
