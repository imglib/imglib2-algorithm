package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

abstract public class ATrinaryFunction extends AFunction implements ITrinaryFunction
{
	protected final IFunction a, b ,c;

	protected RealType< ? > scrap;
	
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
	
	public void setScrap( final RealType< ? > output )
	{
		if ( null == output ) return; 
		this.scrap = output.copy();
		this.a.setScrap( output );
		this.b.setScrap( output );
		this.c.setScrap( output );
	}
}