package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

public interface IBooleanFunction
{
	public boolean evalBoolean();
	
	public boolean evalBoolean( final Localizable loc );
}
