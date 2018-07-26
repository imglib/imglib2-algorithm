package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

abstract public class ABinaryFunction extends AFunction implements IBinaryFunction
{
	protected final IFunction a, b;

	protected RealType< ? > scrap;
	
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
	
	public void setScrap( final RealType< ? > output )
	{
		if ( null == output ) return; 
		this.scrap = output.copy();
		this.a.setScrap( output );
		this.b.setScrap( output );
	}
}