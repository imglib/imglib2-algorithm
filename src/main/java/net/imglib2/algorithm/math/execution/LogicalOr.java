package net.imglib2.algorithm.math.execution;

import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalOr< O extends RealType< O > > extends Comparison< O >
{	
	public LogicalOr( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		super( scrap, a, b );
	}
	
	@Override
	public boolean compare( final O t1, final O t2 )
	{
		// Evaluate both first, otherwise second may not get evaluated and its cursors not advanced
		final boolean first = 0 != t1.getRealDouble(),
					  second = 0 != t2.getRealDouble();
		return first || second;
	}
}