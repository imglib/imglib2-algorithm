package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

abstract public class ATrinaryFunction extends VarargsFunction implements ITrinaryFunction
{
	protected final IFunction a, b ,c;

	protected final RealType< ? > scrap;
	
	public ATrinaryFunction( final Object o1, final Object o2, final Object o3 )
	{
		this.a = Util.wrap( o1 );
		this.b = Util.wrap( o2 );
		this.c = Util.wrap( o3 );
		this.scrap = null;
	}
	
	protected ATrinaryFunction( final RealType< ? > scrap, final IFunction f1, final IFunction f2, final IFunction f3 )
	{
		this.scrap = scrap;
		this.a = f1;
		this.b = f2;
		this.c = f3;
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