package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalXorBoolean< O extends RealType< O > > extends LogicalAnd< O >
{
	private final IBooleanFunction abool, bbool;
	
	public LogicalXorBoolean( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap, a, b );
		this.abool = ( IBooleanFunction )a;
		this.bbool = ( IBooleanFunction )b;
	}

	@Override
	public final boolean evalBoolean()
	{
		// Evaluate both first, otherwise second may not get evaluated and its cursors not advanced
		final boolean first = this.abool.evalBoolean(),
					  second = this.bbool.evalBoolean();
		return first ^ second;
	}

	@Override
	public final boolean evalBoolean( final Localizable loc)
	{
		// Evaluate both first, otherwise second may not get evaluated and its cursors not advanced
		final boolean first = this.abool.evalBoolean( loc ),
					  second = this.bbool.evalBoolean( loc );
		return first ^ second;
	}
}