package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;
import net.imglib2.type.numeric.RealType;

public interface IFunction
{
	public void eval( RealType< ? > output );
	
	public void eval( RealType< ? > output, Localizable loc );
	
	public IFunction copy();
	
	public void setScrap( RealType< ? > output );
}