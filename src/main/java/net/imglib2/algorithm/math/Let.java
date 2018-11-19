package net.imglib2.algorithm.math;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Let extends ViewableFunction implements IFunction, IBinaryFunction
{
	private final String varName;
	private final IFunction varValue;
	private final IFunction body;
	
	public Let( final String varName, final Object varValue, final Object body )
	{
		this.varName = varName;
		this.varValue = Util.wrap( varValue );
		this.body = Util.wrap( body );
	}
	
	public Let( final Object[] pairs, final Object body )
	{
		if ( pairs.length < 2 || 0 != pairs.length % 2 )
			throw new RuntimeException( "Let: need an even number of var-value pairs." );
		
		this.varName = ( String )pairs[0];
		this.varValue = Util.wrap( pairs[1] );
		
		if ( 2 == pairs.length )
		{
			this.body = Util.wrap( body );
		} else
		{
			final Object[] pairs2 = new Object[ pairs.length - 2 ];
			System.arraycopy( pairs, 2, pairs2, 0, pairs2.length );
			this.body = new Let( pairs2, body );
		}
	}
	
	public Let( final Object... obs )
	{
		this( fixAndValidate( obs ), obs[ obs.length - 1] );
	}
	
	static private final Object[] fixAndValidate( final Object[] obs )
	{
		if ( obs.length < 3 || 0 == obs.length % 2 )
			throw new RuntimeException( "Let: need an even number of var-value pairs plus the body at the end." );
		final Object[] obs2 = new Object[ obs.length - 1];
		System.arraycopy( obs, 0, obs2, 0, obs2.length );
		return obs2;
	}

	@Override
	public < O extends RealType< O > > LetBinding< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		final O scrap = tmp.copy();
		final Map< String, O > rebind = new HashMap<>( bindings );
		rebind.put( this.varName, scrap );
		return new LetBinding< O >( scrap, this.varName,
				this.varValue.reInit( tmp, rebind, converter, imgSources ),
				this.body.reInit( tmp, rebind, converter, imgSources ) );
	}

	@Override
	public IFunction getFirst()
	{
		return this.varValue;
	}

	@Override
	public IFunction getSecond()
	{
		return this.body;
	}
	
	public String getVarName()
	{
		return this.varName;
	}
}