package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunction;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Var implements IVar
{
	private final String name;

	public Var( final String name )
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public < O extends RealType< O > > Variable< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new Variable< O >( this.name, bindings.get( this.name ) );
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		throw new UnsupportedOperationException();
	}
}