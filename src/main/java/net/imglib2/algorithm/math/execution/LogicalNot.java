package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class LogicalNot< O extends RealType< O > > extends ABooleanFunction< O >
{
	protected final OFunction< O > a;
	
	public LogicalNot( final O scrap, final OFunction< O > a )
	{
		super( scrap );
		this.a = a;
	}

	@Override
	public boolean evalBoolean()
	{
		// It's true if not zero, hence reverse
		//return ! (0 != this.a.eval().getRealDouble() );
		return 0 == this.a.eval().getRealDouble();
	}

	@Override
	public boolean evalBoolean( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		//return ! (0 != this.a.eval().getRealDouble() );
		return 0 == this.a.eval( loc ).getRealDouble();
	}

	@Override
	public final O eval()
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval().getRealDouble() ? one : zero;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval( loc ).getRealDouble() ? one : zero;
	}

	@Override
	public final List< OFunction< O > > children()
	{
		return Arrays.asList( this.a );
	}

	@Override
	public final double evalDouble()
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval().getRealDouble() ? 1.0 : 0.0;
	}

	@Override
	public final double evalDouble( final Localizable loc )
	{
		// It's true if not zero, hence reverse
		return 0 == this.a.eval( loc ).getRealDouble() ? 1.0 : 0.0;
	}
}