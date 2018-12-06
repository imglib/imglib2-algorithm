package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Minimum< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b;
	
	public Minimum( final OFunction< O > a, final OFunction< O > b )
	{
		this.a = a;
		this.b = b;
	}
	
	@Override
	public final O eval()
	{
		final O x = this.a.eval();
		final O y = this.b.eval();
		return x.compareTo( y ) < 0 ? x : y;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		final O x = this.a.eval( loc );
		final O y = this.b.eval( loc );
		return x.compareTo( y ) < 0 ? x : y;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.a, this.b );
	}
}
