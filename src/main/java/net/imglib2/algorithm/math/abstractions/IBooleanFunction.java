package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

public interface IBooleanFunction
{
	public boolean evalBoolean( final RealType< ? > output );
	
	public boolean evalBoolean( final RealType< ? > output, final Localizable loc );
}
