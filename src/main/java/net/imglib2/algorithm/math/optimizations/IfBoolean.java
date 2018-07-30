package net.imglib2.algorithm.math.optimizations;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class IfBoolean extends ATrinaryFunction
{
	/**
	 * Same as this.a, to avoid casting.
	 */
	private final IBooleanFunction abool;
	
	public IfBoolean( final RealType< ? > scrap, final Compare f1, final IFunction f2, final IFunction f3 )
	{
		super ( scrap, f1, f2, f3 );
		this.abool = f1; // same as this.a
	}

	@Override
	public final RealType< ? > eval()
	{
		return this.abool.evalBoolean() ?
				this.b.eval()
				: this.c.eval();
	}

	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		return this.abool.evalBoolean( loc ) ?
				this.b.eval( loc )
				: this.c.eval( loc );
	}

	@Override
	public IfBoolean reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		return new IfBoolean( tmp.copy(), ( Compare ) this.a.reInit( tmp, bindings, converter, imgSources ), this.b.reInit( tmp, bindings, converter, imgSources ), this.c.reInit( tmp, bindings, converter, imgSources ) );
	}
}