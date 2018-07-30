package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

abstract public class ABinaryFunction extends AFunction implements IBinaryFunction
{
	protected final IFunction a, b;

	@SuppressWarnings("rawtypes")
	protected final RealType scrap;
	
	public ABinaryFunction( final Object o1, final Object o2 )
	{
		this.a = Util.wrap( o1 );
		this.b = Util.wrap( o2 );
		this.scrap = null;
	}
	
	public ABinaryFunction( final Object... obs )
	{
		final IFunction[] p = this.wrapMap( obs );
		this.a = p[ 0 ];
		this.b = p[ 1 ];
		this.scrap = null;
	}
	
	protected ABinaryFunction( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		this.scrap = scrap;
		this.a = f1;
		this.b = f2;
	}
	
	public final IFunction getFirst()
	{
		return this.a;
	}
	
	public final IFunction getSecond()
	{
		return this.b;
	}
}