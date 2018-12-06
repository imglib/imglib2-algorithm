package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Printing< O extends RealType< O > > implements OFunction< O >
{
	private final String title;
	private final OFunction< O > a;
	
	public Printing( final String title, final OFunction< O > a )
	{
		this.title = title;
		this.a = a;
	}
	
	@Override
	public final O eval()
	{
		final O result = this.a.eval();
		System.out.println( this.title + " :: " + result );
		return result;
	}

	@Override
	public final O eval( final Localizable loc)
	{
		final O result = this.a.eval( loc );
		System.out.println( this.title + " :: " + result );
		return result;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.a );
	}
}
