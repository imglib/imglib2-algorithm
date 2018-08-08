package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Maximum< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b;
	
	public Maximum( final OFunction< O > a, final OFunction< O > b )
	{
		this.a = a;
		this.b = b;
	}
	
	public final O eval()
	{
		final O x = this.a.eval();
		final O y = this.b.eval();
		return x.compareTo( y ) > 0 ? x : y;
	}

	public final O eval( final Localizable loc )
	{
		final O x = this.a.eval( loc );
		final O y = this.b.eval( loc );
		return x.compareTo( y ) > 0 ? x : y;
	}
}
