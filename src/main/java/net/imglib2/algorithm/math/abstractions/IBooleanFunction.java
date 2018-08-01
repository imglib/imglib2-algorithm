package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;

public interface IBooleanFunction
{
	public boolean evalBoolean();
	
	public boolean evalBoolean( final Localizable loc );
}
