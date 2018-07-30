package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

abstract public class Compare extends ABinaryFunction implements IBooleanFunction
{
	@SuppressWarnings("rawtypes")
	private final RealType zero, one;
	
	public Compare( final Object o1, final Object o2) {
		super( o1, o2 );
		this.one = null;
		this.zero = null;
	}
	
	protected Compare( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
		this.zero = scrap.createVariable();
		this.zero.setZero();
		this.one = scrap.createVariable();
		this.one.setOne();
	}
	
	abstract protected boolean compare( final RealType< ? > t1, final RealType< ? > t2 );

	@Override
	public final RealType< ? > eval()
	{
		return this.compare( this.a.eval(), this.b.eval() ) ? one : zero;
	}

	@Override
	public final RealType< ? > eval( final Localizable loc)
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