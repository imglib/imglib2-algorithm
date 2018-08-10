package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

public interface OFunction< O extends RealType< O > >
{
	public O eval();
	
	public O eval( final Localizable loc );
}
