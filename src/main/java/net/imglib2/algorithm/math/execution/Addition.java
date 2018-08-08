package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Addition< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b;
	private final O scrap;
	
	public Addition( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		this.scrap = scrap;
		this.a = a;
		this.b = b;
	}
	
	public final O eval()
	{
		this.scrap.set( this.a.eval() );
		this.scrap.add( this.b.eval() );
		return this.scrap;
	}

	public final O eval( final Localizable loc )
	{
		this.scrap.set( this.a.eval( loc ) );
		this.scrap.add( this.b.eval( loc ) );
		return this.scrap;
	}
}
