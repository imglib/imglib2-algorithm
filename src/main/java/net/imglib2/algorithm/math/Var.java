package net.imglib2.algorithm.math;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.type.numeric.RealType;

public final class Var implements IFunction
{
	final String name;
	private RealType< ? > scrap;

	public Var( final String name ) {
		this.name = name;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void eval( final RealType output ) {
		output.set( this.scrap );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void eval( final RealType output, final Localizable loc) {
		output.set( this.scrap );
	}

	@Override
	public Var copy() {
		return new Var( this.name );
	}

	@Override
	public void setScrap( final RealType< ? > output ) {
		this.scrap = output;
	}
}