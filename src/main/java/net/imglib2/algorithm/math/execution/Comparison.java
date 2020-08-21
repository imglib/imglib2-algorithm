package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

abstract public class Comparison< O extends RealType< O > > extends ABooleanFunction< O >
{
	protected final OFunction< O > a, b;

	public Comparison( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap );
		this.a = a;
		this.b = b;
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
	public boolean evalBoolean()
	{
		return this.compare( this.a.eval(), this.b.eval() );
	}
	
	@Override
	public boolean evalBoolean( final Localizable loc )
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) );
	}
	
	@Override
	public final List< OFunction< O > > children()
	{
		return Arrays.asList( this.a, this.b );
	}
	
	
	@Override
	public final double evalDouble()
	{
		return this.compare( this.a.eval(), this.b.eval() ) ? 1.0 : 0.0;
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		return this.compare( this.a.eval( loc ), this.b.eval( loc ) ) ? 1.0 : 0.0;
	}
}