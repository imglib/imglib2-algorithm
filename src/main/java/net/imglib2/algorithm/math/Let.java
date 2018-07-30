package net.imglib2.algorithm.math;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class Let implements IFunction, IBinaryFunction
{
	private final String varName;
	private final IFunction varValue;
	private final IFunction body;
	@SuppressWarnings("rawtypes")
	private final RealType scrap;
	
	public Let( final String varName, final Object varValue, final Object body )
	{
		this.varName = varName;
		this.varValue = Util.wrap( varValue );
		this.body = Util.wrap( body );
		this.scrap = null;
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
		
		this.scrap = null;
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
	
	private Let( final RealType< ? > scrap, final String varName, final IFunction varValue, final IFunction body )
	{
		this.scrap = scrap;
		this.varName = varName;
		this.varValue = varValue;
		this.body = body;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final RealType< ? > eval()
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval() );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final RealType< ? > eval( final Localizable loc)
	{
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.scrap.set( this.varValue.eval( loc ) );
		// The body may contain Vars that will use this.varValue via this.scrap
		return this.body.eval( loc );
	}

	@Override
	public Let reInit( final RealType< ? > tmp, final Map< String, RealType< ? > > bindings, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		final RealType< ? > scrap = tmp.copy();
		final Map< String, RealType< ? > > rebind = new HashMap<>( bindings );
		rebind.put( this.varName, scrap );
		return new Let( scrap, this.varName, this.varValue.reInit( tmp, rebind, converter ), this.body.reInit( tmp, rebind, converter ) );
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