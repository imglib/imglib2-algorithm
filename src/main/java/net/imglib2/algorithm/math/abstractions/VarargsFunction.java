package net.imglib2.algorithm.math.abstractions;

import java.lang.reflect.Constructor;

abstract public class VarargsFunction
{
	final public IFunction[] wrapMap( final Object[] obs )
	{	
		try {
			final Constructor< ? > constructor = this.getClass().getConstructor( new Class[]{ Object.class, Object.class } );
			ABinaryFunction a = ( ABinaryFunction )constructor.newInstance( obs[0], obs[1] );
			ABinaryFunction b;

			for ( int i = 2; i < obs.length -1; ++i )
			{
				b = ( ABinaryFunction )constructor.newInstance( a, obs[i] );
				a = b;
			}
			
			return new IFunction[]{ a, Util.wrap( obs[ obs.length - 1 ] ) };
		} catch (Exception e)
		{
			throw new RuntimeException( "Error with the constructor for class " + this.getClass(), e );
		}
	}
}