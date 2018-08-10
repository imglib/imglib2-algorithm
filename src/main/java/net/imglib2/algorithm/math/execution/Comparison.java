package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

abstract public class Comparison< O extends RealType< O > > implements OFunction< O >, IBooleanFunction
{
	private final OFunction< O > a, b;
	private final O zero, one;
	
	public Comparison( final O scrap, final OFunction< O > a, final OFunction< O > b) {
		this.a = a;
		this.b = b;
		this.zero = scrap.createVariable();
		this.zero.setZero();
		this.one = scrap.createVariable();
		this.one.setOne();
	}
	
	abstract protected boolean compare( final O t1, final O t2 );

	@Override
	public final O eval()
	{
		return this.compare( this.a.eval(), this.b.eval() ) ? one : zero;
	}

	@Override
	public final O eval( final Localizable loc)
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) ) ? one : zero;
	}
	
	@Override
	public final boolean evalBoolean()
	{
		return this.compare( this.a.eval(), this.b.eval() );
	}
	
	@Override
	public final boolean evalBoolean( final Localizable loc )
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) );
	}
}