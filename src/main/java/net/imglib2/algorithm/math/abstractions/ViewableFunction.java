package net.imglib2.algorithm.math.abstractions;

import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunction;
import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunctionDouble;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public abstract class ViewableFunction implements IFunction
{
	public < C extends RealType< C >, O extends RealType< O > > IterableRandomAccessibleFunction< C, O > view()
	{
		return new IterableRandomAccessibleFunction< C, O >( this );
	}
	
	public < C extends RealType< C >, O extends RealType< O > > IterableRandomAccessibleFunction< C, O > view( final O outputType )
	{
		return new IterableRandomAccessibleFunction< C, O >( this, outputType );
	}
	
	public < C extends RealType< C >, O extends RealType< O > > IterableRandomAccessibleFunction< C, O > view( final C computeType, final O outputType )
	{
		return new IterableRandomAccessibleFunction< C, O >( this, null, computeType, outputType, null );
	}
	
	public < C extends RealType< C >, O extends RealType< O > > IterableRandomAccessibleFunction< C, O > view( final O outputType, final Converter< C, O > converter )
	{
		return new IterableRandomAccessibleFunction< C, O >( this, outputType, converter );
	}
	
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble()
	{
		return new IterableRandomAccessibleFunctionDouble< O >( this );
	}
	
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble( final O outputType )
	{
		return new IterableRandomAccessibleFunctionDouble< O >( this, outputType );
	}
	
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		return new IterableRandomAccessibleFunctionDouble< O >( this, outputType, converter );
	}
}