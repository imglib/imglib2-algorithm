package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Equal extends Compare
{
	public Equal( final Object o1, final Object o2) {
		super( o1, o2 );
	}
	
	private Equal( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final boolean compare( final RealType t1, final RealType t2 )
	{
		return 0 == t1.compareTo( t2 );
	}

	@Override
	public Equal reInit( final RealType< ? > tmp, final Map< String, RealType< ? > > bindings, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		return new Equal( tmp.copy(), this.a.reInit( tmp, bindings, converter ), this.b.reInit( tmp, bindings, converter ) );
	}
}