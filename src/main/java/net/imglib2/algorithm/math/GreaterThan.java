package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class GreaterThan extends Compare
{
	public GreaterThan( final Object o1, final Object o2) {
		super(o1, o2);
	}
	
	private GreaterThan( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean compare( final RealType t1, final RealType t2 )
	{
		return 1 == t1.compareTo( t2 );
	}

	@Override
	public GreaterThan reInit(
			final RealType<?> tmp,
			final Map<String, RealType<?>> bindings,
			final Converter<RealType<?>, RealType<?>> converter,
			Map< IVar, IFunction > imgSources )
	{
		return new GreaterThan( tmp.copy(), this.a.reInit( tmp, bindings, converter, imgSources ), this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}