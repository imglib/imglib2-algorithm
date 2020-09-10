package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public final class ZeroMinus< O extends RealType< O > > implements OFunction< O >
{
	final O scrap;
	final OFunction< O > a;
	
	public ZeroMinus( final O scrap, final OFunction< O > a)
	{
		this.scrap = scrap;
		this.a = a;
	}

	@Override
	public final O eval() {
		this.scrap.setZero();
		this.scrap.sub( this.a.eval() );
		return this.scrap;
	}

	@Override
	public final O eval(Localizable loc) {
		this.scrap.setZero();
		this.scrap.sub( this.a.eval( loc ) );
		return this.scrap;
	}

	@Override
	public List< OFunction< O > > children() {
		return Arrays.asList( this.a );
	}

	@Override
	public final double evalDouble() {
		return -this.a.evalDouble();
	}

	@Override
	public double evalDouble(Localizable loc) {
		return -this.a.evalDouble();
	}
}
