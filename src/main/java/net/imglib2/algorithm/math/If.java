package net.imglib2.algorithm.math;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.type.numeric.RealType;

public final class If extends ATrinaryFunction
{
	public If( final Object o1, final Object o2, final Object o3 )
	{
		super( o1, o2, o3 );
	}

	@Override
	public final void eval( final RealType<?> output ) {
		this.a.eval( this.scrap );
		if ( 0.0f != this.scrap.getRealFloat() )
		{
			// Then
			this.b.eval( output );
		} else
		{
			// Else
			this.c.eval( output );
		}
	}

	@Override
	public final void eval( final RealType<?> output, final Localizable loc ) {
		this.a.eval( this.scrap, loc );
		if ( 0.0f != this.scrap.getRealFloat() )
		{
			// Then
			this.b.eval( output, loc );
		} else
		{
			// Else
			this.c.eval( output, loc );
		}
	}

	@Override
	public If copy() {
		final If copy = new If( this.a.copy(), this.b.copy(), this.c.copy() );
		copy.setScrap( this.scrap );
		return copy;
	}
}