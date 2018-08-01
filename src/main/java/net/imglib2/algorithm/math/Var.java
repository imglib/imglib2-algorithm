package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Var implements IVar
{
	private final String name;
	private final RealType< ? > value;

	public Var( final String name ) {
		this( null, name );
	}
	
	public Var( final RealType< ? > value, final String name )
	{
		this.value = value;
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public RealType< ? > getScrap()
	{
		return this.value;
	}

	@Override
	public final RealType< ? > eval()
	{
		return this.value;
	}

	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		return this.value;
	}

	@Override
	public Var reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return new Var( bindings.get( this.name ), this.name );
	}
}