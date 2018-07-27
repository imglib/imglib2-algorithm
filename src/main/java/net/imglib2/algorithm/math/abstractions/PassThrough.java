package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

/**
 * Does nothing: the copy method returns a copy of the encapsulated IFunction
 */
public class PassThrough extends AUnaryFunction
{
	public PassThrough( final Object o )
	{
		super( o );
	}

	@Override
	public void eval( final RealType<?> output ) {}

	@Override
	public void eval( final RealType<?> output, final Localizable loc ) {}

	@Override
	public IFunction copy() {
		return this.a.copy();
	}
	
	@Override
	public void setScrap( final RealType< ? > output ) {}
}
