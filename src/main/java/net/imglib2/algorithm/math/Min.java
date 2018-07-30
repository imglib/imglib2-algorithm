package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Min extends ABinaryFunction
{
	public Min( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Min( final Object... obs )
	{
		super( obs );
	}
	
	private Min( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final RealType< ? > eval()
	{
		final RealType x = this.a.eval();
		final RealType y = this.b.eval();
		return x.compareTo( y ) < 0 ? x : y;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final RealType< ? > eval( final Localizable loc)
	{
		final RealType x = this.a.eval( loc );
		final RealType y = this.b.eval( loc );
		return x.compareTo( y ) < 0 ? x : y;
	}

	@Override
	public Min reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return new Min( tmp.copy(), this.a.reInit(tmp, bindings, converter, imgSources), this.b.reInit(tmp, bindings, converter, imgSources) );
	}
}