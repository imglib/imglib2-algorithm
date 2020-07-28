package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

/**
 * Doesn't use {@code NumericType} math because it lacks the log function.
 *
 * @param <O>
 */
public class Exponential< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a;
	private final O scrap;
	
	public Exponential( final O scrap, final OFunction< O > a )
	{
		this.scrap = scrap;
		this.a = a;
	}
	
	@Override
	public final O eval()
	{
		this.scrap.setReal( Math.exp( this.a.eval().getRealDouble() ) );
		return this.scrap;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.scrap.setReal( Math.exp( this.a.eval( loc ).getRealDouble() ) );
		return this.scrap;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.a );
	}
	
	@Override
	public final double evalDouble()
	{
		return Math.exp( this.a.evalDouble() );
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		return Math.exp( this.a.evalDouble( loc ) );
	}
}
