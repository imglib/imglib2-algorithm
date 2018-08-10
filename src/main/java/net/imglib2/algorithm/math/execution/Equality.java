package net.imglib2.algorithm.math.execution;

import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Equality< O extends RealType< O > > extends Comparison< O >
{
	public Equality( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap, a, b );
	}

	@Override
	public final boolean compare( final O t1, final O t2 )
	{
		return 0 == t1.compareTo( t2 );
	}
}
