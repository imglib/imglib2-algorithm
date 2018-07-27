package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

abstract public class Compare extends ABinaryFunction
{
	public Compare( final Object o1, final Object o2) {
		super( o1, o2 );
	}
	
	protected Compare( final RealType< ? > scrap, final IFunction f1, final IFunction f2 )
	{
		super( scrap, f1, f2 );
	}
	
	abstract protected boolean compare( final RealType<?> t1, final RealType<?> t2 );

	@SuppressWarnings("rawtypes")
	@Override
	public void eval( final RealType output ) {
		this.a.eval( this.scrap );
		this.b.eval( output );
		if ( this.compare( this.scrap, output ) )
			output.setOne();
		else
			output.setZero();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void eval( final RealType output, final Localizable loc) {
		this.a.eval( this.scrap, loc );
		this.b.eval( output, loc );
		if ( this.compare( this.scrap, output ) )
			output.setOne();
		else
			output.setZero();
	}
}