package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Var implements IFunction
{
	private final String name;
	private final RealType< ? > scrap;

	public Var( final String name ) {
		this( null, name );
	}
	
	private Var( final RealType< ? > scrap, final String name )
	{
		this.scrap = scrap;
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}

	@Override
	public final RealType< ? > eval()
	{
		return this.scrap;
	}

	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		return this.scrap;
	}

	@Override
	public Var reInit( final RealType< ? > tmp, final Map< String, RealType< ? > > bindings, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		return new Var( bindings.get( this.name ), this.name );
	}
}