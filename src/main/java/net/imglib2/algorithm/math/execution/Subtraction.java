package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Subtraction< O extends RealType< O > > implements OFunction< O >
{
	private final OFunction< O > a, b;
	private final O scrap;
	
	public Subtraction( final O scrap, final OFunction< O > a, final OFunction< O > b )
	{
		this.scrap = scrap;
		this.a = a;
		this.b = b;
	}
	
	@Override
	public final O eval()
	{
		this.scrap.set( this.a.eval() );
		this.scrap.sub( this.b.eval() );
		return this.scrap;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.scrap.set( this.a.eval( loc ) );
		this.scrap.sub( this.b.eval( loc ) );
		return this.scrap;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.a, this.b );
	}
}
