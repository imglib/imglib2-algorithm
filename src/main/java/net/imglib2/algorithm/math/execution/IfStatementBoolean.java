package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class IfStatementBoolean< O extends RealType< O > > implements OFunction< O >
{
	private final Comparison< O > abool;
	private final OFunction< O > b, c;
	
	public IfStatementBoolean( final Comparison< O > f1, final OFunction< O > f2, final OFunction< O > f3 )
	{
		this.abool = f1;
		this.b = f2;
		this.c = f3;
	}

	@Override
	public final O eval()
	{
		return this.abool.evalBoolean() ?
				this.b.eval()
				: this.c.eval();
	}

	@Override
	public final O eval( final Localizable loc )
	{
		return this.abool.evalBoolean( loc ) ?
				this.b.eval( loc )
				: this.c.eval( loc );
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.abool, this.b, this.c );
	}
}