package net.imglib2.algorithm.math.abstractions;

import net.imglib2.type.numeric.RealType;

public abstract class ABooleanFunction< O extends RealType< O > > implements IBooleanFunction, OFunction< O >
{
	protected final O zero, one;
	
	public ABooleanFunction( final O scrap )
	{
		this.zero = scrap.createVariable();
		this.zero.setZero();
		this.one = scrap.createVariable();
		this.one.setOne();
	}
}
