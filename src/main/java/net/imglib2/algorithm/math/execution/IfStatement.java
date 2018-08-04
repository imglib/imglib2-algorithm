package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class IfStatement< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b, c;
	
	public IfStatement( final OFunction< O > f1, final OFunction< O > f2, final OFunction< O > f3 )
	{
		this.a = f1;
		this.b = f2;
		this.c = f3;
	}
	
	@Override
	public final O eval()
	{
		return 0 != this.a.eval().getRealFloat() ?
			// Then
			this.b.eval()
			// Else
			: this.c.eval();
	}

	@Override
	public final O eval( final Localizable loc )
	{
		return 0 != this.a.eval().getRealFloat() ?
			// Then
			this.b.eval()
			// Else
			: this.c.eval();
	}
}
