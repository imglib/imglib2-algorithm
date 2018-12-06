package net.imglib2.algorithm.math.abstractions;

import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public abstract class ViewableFunction implements IFunction
{
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view()
	{
		return new IterableRandomAccessibleFunction< O >( this );
	}
	
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType )
	{
		return new IterableRandomAccessibleFunction< O >( this, outputType );
	}
	
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		return new IterableRandomAccessibleFunction< O >( this, outputType, converter );
	}
}