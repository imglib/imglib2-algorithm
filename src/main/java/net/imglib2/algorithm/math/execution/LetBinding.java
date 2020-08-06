package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class LetBinding< O extends RealType< O > > implements OFunction< O >
{
	private final O scrap;
	private double scrap_value;
	private final String varName;
	private final OFunction< O > varValue;
	private final OFunction< O > body;
	
	public LetBinding( final O scrap, final String varName, final OFunction< O > varValue, final OFunction< O > body )
	{
		this.scrap = scrap;
		this.varName = varName;
		this.varValue = varValue;
		this.body = body;
	}
	
	public LetBinding(
			final O scrap,
			final String varName,
			final Map< String, LetBinding< O > > bindings,
			final IFunction varValue,
			final IFunction body,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		this.scrap = scrap;
		this.varName = varName;
		bindings.put( varName, this );
		this.varValue = varValue.reInit( scrap.copy(), bindings, converter, imgSources );
		this.body = body.reInit( scrap.copy(), bindings, converter, imgSources );
	}
	
	public final O getScrapValue()
	{
		return this.scrap;
	}
	
	public final double getDoubleValue()
	{
		return this.scrap_value;
	}
	
	@Override
	public final O eval()
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval() );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval();
	}

	@Override
	public final O eval( final Localizable loc)
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval( loc ) );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval( loc );
	}
	
	public final String getVarName()
	{
		return this.varName;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList( this.varValue, this.body );
	}
	
	@Override
	public final double evalDouble()
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap_value = this.varValue.evalDouble();
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.evalDouble();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap_value = this.varValue.evalDouble( loc );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.evalDouble( loc );
	}
}
