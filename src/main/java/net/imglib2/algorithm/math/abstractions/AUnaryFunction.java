package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

abstract public class AUnaryFunction extends ViewableFunction implements IUnaryFunction
{
	protected final IFunction a;

	protected RealType< ? > scrap;
	
	public AUnaryFunction( final Object o1 )
	{
		this.a = Util.wrap( o1 );
	}
	
	public IFunction getFirst()
	{
		return this.a;
	}
}