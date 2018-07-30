package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.AUnaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
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
	public final RealType<?> eval()
	{
		final RealType< ? > result = this.a.eval();
		System.out.println( title + " :: " + result );
		return result;
	}

	@Override
	public final RealType<?> eval( final Localizable loc)
	{
		final RealType< ? > result = this.a.eval( loc );
		System.out.println( title + " :: " + result );
		return result;
	}

	@Override
	public Print reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return new Print( this.title, this.a.reInit( tmp, bindings, converter, imgSources ) );
	}
}
