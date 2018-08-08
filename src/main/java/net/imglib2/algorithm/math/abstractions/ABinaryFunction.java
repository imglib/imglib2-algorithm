package net.imglib2.algorithm.math.abstractions;

abstract public class ABinaryFunction extends VarargsFunction implements IBinaryFunction
{
	protected final IFunction a, b;
	
	public ABinaryFunction( final Object o1, final Object o2 )
	{
		this.a = Util.wrap( o1 );
		this.b = Util.wrap( o2 );
	}
	
	public ABinaryFunction( final Object... obs )
	{
		final IFunction[] p = this.wrapMap( obs );
		this.a = p[ 0 ];
		this.b = p[ 1 ];
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