package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalAndBoolean< O extends RealType< O > > extends LogicalAnd< O >
{
	private final IBooleanFunction abool, bbool;
	
	public LogicalAndBoolean( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap, a, b );
		this.abool = ( IBooleanFunction )a;
		this.bbool = ( IBooleanFunction )b;
	}

	@Override
	public final boolean evalBoolean()
	{
		return this.abool.evalBoolean() && this.bbool.evalBoolean();
	}

	@Override
	public final boolean evalBoolean( final Localizable loc)
	{
		return this.abool.evalBoolean( loc ) && this.bbool.evalBoolean( loc );
	}
}