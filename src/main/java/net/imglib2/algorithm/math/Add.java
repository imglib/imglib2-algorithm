package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Add extends ABinaryFunction
{
	public Add( final Object o1, final Object o2 )
	{
		super( o1, o2 );
	}
	
	public Add( final Object... obs )
	{
		super( obs );
	}
	
	private Add( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public final RealType< ? > eval()
	{
		this.scrap.set( this.a.eval() );
		this.scrap.add( this.b.eval() );
		return this.scrap;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		this.scrap.set( this.a.eval( loc ) );
		this.scrap.add( this.b.eval( loc ) );
		return this.scrap;
	}

	@Override
	public Add reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter<RealType<?>, RealType<?>> converter,
			Map< IVar, IFunction > imgSources )
	{
		return new Add( tmp.copy(), this.a.reInit( tmp, bindings, converter, imgSources ), this.b.reInit( tmp, bindings, converter, imgSources ) );
	}
}