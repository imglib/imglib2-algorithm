package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalNotBoolean< O extends RealType< O > > extends LogicalNot< O >
{
	private final IBooleanFunction abool;
	
	public LogicalNotBoolean( final O scrap, final OFunction< O > a )
	{
		super( scrap, a );
		this.abool = ( IBooleanFunction )a;
	}

	@Override
	public final boolean evalBoolean()
	{
		return !this.abool.evalBoolean();
	}

	@Override
	public final boolean evalBoolean( final Localizable loc )
	{
		return !this.abool.evalBoolean( loc );
	}
}