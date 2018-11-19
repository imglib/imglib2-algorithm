package net.imglib2.algorithm.math.abstractions;

abstract public class ATrinaryFunction extends ViewableFunction implements ITrinaryFunction
{
	protected final IFunction a, b ,c;
	
	public ATrinaryFunction( final Object o1, final Object o2, final Object o3 )
	{
		this.a = Util.wrap( o1 );
		this.b = Util.wrap( o2 );
		this.c = Util.wrap( o3 );
	}
	
	public final IFunction getFirst()
	{
		return this.a;
	}
	
	public final IFunction getSecond()
	{
		return this.b;
	}
	
	public final IFunction getThird()
	{
		return this.c;
	}
}