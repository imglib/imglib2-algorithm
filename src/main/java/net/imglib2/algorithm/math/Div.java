package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Div extends ABinaryFunction implements IFunction
{

	public Div( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Div( final Object... obs )
	{
		super( obs );
	}
	
	private Div( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final void eval( final RealType output )
	{
		this.a.eval( output );
		this.b.eval( this.scrap );
		output.div( this.scrap );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final void eval( final RealType output, final Localizable loc)
	{
		this.a.eval( output, loc );
		this.b.eval( this.scrap, loc );
		output.div( this.scrap );
	}

	@Override
	public Div reInit( final RealType<?> tmp, final Map<String, RealType<?>> bindings, final Converter<RealType<?>, RealType<?>> converter )
	{
		return new Div( tmp.copy(), this.a.reInit( tmp, bindings, converter ), this.b.reInit( tmp, bindings, converter ) );
	}
}