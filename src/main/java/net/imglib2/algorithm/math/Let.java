package net.imglib2.algorithm.math;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.ATrinaryFunction;
import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.ITrinaryFunction;
import net.imglib2.algorithm.math.abstractions.IUnaryFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.type.numeric.RealType;

public final class Let implements IFunction, IBinaryFunction
{
	final String varName;
	private final IFunction varValue;
	private final IFunction body;
	private RealType< ? > scrap;
	
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
	
	/**
	 * Recursive search for Var instances of this.varName
	 * 
	 * @param o
	 */
	private final void setupVars( final IFunction o, final boolean[] used )
	{
		if ( o instanceof IUnaryFunction )
		{
			final IUnaryFunction uf = ( IUnaryFunction )o;
			
			if ( uf.getFirst() instanceof Var )
			{
				final Var var = ( Var )uf.getFirst();
				if ( var.name == this.varName )
				{
					var.setScrap( this.scrap );
					used[0] = true;
				}
			} else
			{
				setupVars( uf.getFirst(), used );
			}
			
			if ( o instanceof IBinaryFunction )
			{
				final IBinaryFunction bf = ( IBinaryFunction )o;
				
				if ( bf.getSecond() instanceof Var )
				{
					final Var var = ( Var )bf.getSecond();
					if ( var.name == this.varName )
					{
						var.setScrap( this.scrap );
						used[0] = true;
					}
				} else
				{
					setupVars( bf.getSecond(), used );
				}
				
				if ( o instanceof ITrinaryFunction )
				{
					final ATrinaryFunction tf = ( ATrinaryFunction )o;
					
					if ( tf.getThird() instanceof Var )
					{
						final Var var = ( Var )tf.getThird();
						if ( var.name == this.varName )
						{
							var.setScrap( this.scrap );
							used[0] = true;
						}
					} else
					{
						setupVars( tf.getThird(), used );
					}
				}
			}
		}
	}
	

	@Override
	public void eval( final RealType< ? > output ) {
		// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
		this.varValue.eval( this.scrap );
		// The body may contain Vars that will use this.varValue via this.scrap
		this.body.eval( output );
	}

	@Override
	public void eval( final RealType< ? > output, final Localizable loc) {
		this.varValue.eval( this.scrap, loc );
		this.body.eval( output, loc );
	}

	@Override
	public Let copy() {
		final Let copy = new Let( this.varName, this.varValue.copy(), this.body.copy() );
		copy.setScrap( this.scrap );
		return copy;
	}

	@Override
	public void setScrap( final RealType< ? > output ) {
		if ( null == output ) return;
		this.scrap = output.copy();
		this.varValue.setScrap( output );
		this.body.setScrap( output );
		
		// Setup Var instances that read this varName's value
		// and ensure that it is read at least once
		final boolean[] used = new boolean[]{ false };
		setupVars( this.body, used );
		if ( ! used[0] )
			throw new RuntimeException( "Let-declared variable \"" + this.varName + "\" is never read by a Var(\"" + this.varName + "\")." );
	}

	@Override
	public IFunction getFirst() {
		return this.varValue;
	}

	@Override
	public IFunction getSecond() {
		return this.body;
	}
}