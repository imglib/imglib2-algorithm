package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Variable< O extends RealType< O > > implements OFunction< O >
{
	private final String name;
	private final O value;
	
	public Variable( final String name, final O value )
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public final O eval()
	{
		return this.value;
	}
	
	@Override
	public final O eval( final Localizable loc )
	{
		return this.value;
	}
	
	public final O getScrap()
	{
		return this.value;
	}
	
	public final String getName()
	{
		return this.name;
	}
}
