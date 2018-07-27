package net.imglib2.algorithm.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.ITrinaryFunction;
import net.imglib2.algorithm.math.abstractions.IUnaryFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * An easy yet relatively high performance way to perform pixel-wise math
 * on one or more {@link RandomAccessibleInterval} instances.
 * 
 * An example in java:
 * 
 * <pre>
 * {@code
 * RandomAccessibleInterval<A> img1 = ...
 * RandomAccessibleInterval<B> img2 = ...
 * RandomAccessibleInterval<C> img3 = ...
 * 
 * RandomAccessibleInterval<O> result = ...
 * 
 * compute( div( max( img1, img2, img3 ), 3.0 ) ).into( result );
 * }
 * </pre>
 * 
 * While java compilation cares about exact types, the type erasure that happens
 * at compile time means that input types can be mixed, as long as all of them
 * extend RealType.
 * 
 * @author Albert Cardona
 *
 */
public class ImgMath
{
	static public final Compute compute( final IFunction operation )
	{
		return new Compute( operation );
	}
	
	static public final Compute compute( final IFunction operation, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		return new Compute( operation, converter );
	}
	
	static public final Add add( final Object o1, final Object o2 )
	{
		return new Add( o1, o2 );
	}
	
	static public final Add add( final Object... obs )
	{
		return new Add( obs );
	}
	
	static public final Sub sub( final Object o1, final Object o2 )
	{
		return new Sub( o1, o2 );
	}
	
	static public final Sub sub( final Object... obs )
	{
		return new Sub( obs );
	}
	
	static public final Mul mul( final Object o1, final Object o2 )
	{
		return new Mul( o1, o2 );
	}
	
	static public final Mul mul( final Object... obs )
	{
		return new Mul( obs );
	}
	
	static public final Div div( final Object o1, final Object o2 )
	{
		return new Div( o1, o2 );
	}
	
	static public final Div div( final Object... obs )
	{
		return new Div( obs );
	}
	
	static public final Max max( final Object o1, final Object o2 )
	{
		return new Max( o1, o2 );
	}
	
	static public final Max max( final Object... obs )
	{
		return new Max( obs );
	}
	
	static public final Min min( final Object o1, final Object o2 )
	{
		return new Min( o1, o2 );
	}
	
	static public final Min min( final Object... obs )
	{
		return new Min( obs );
	}
	
	static public final Let let( final String varName, final Object varValue, final Object body )
	{
		return new Let( varName, varValue, body );
	}
	
	static public final Let let( final Object[] pairs, final Object body )
	{
		return new Let( pairs, body );
	}
	
	static public final Let let( final Object... obs )
	{
		return new Let( obs );
	}
	
	static public final Var var( final String name )
	{
		return new Var( name );
	}
	
	static public final Equal EQ( final Object o1, final Object o2 )
	{
		return new Equal( o1, o2 );
	}
	
	static public final NotEqual NEQ( final Object o1, final Object o2 )
	{
		return new NotEqual( o1, o2 );
	}
	
	static public final LessThan LT( final Object o1, final Object o2 )
	{
		return new LessThan( o1, o2 );
	}
	
	static public final GreaterThan GT( final Object o1, final Object o2 )
	{
		return new GreaterThan( o1, o2 );
	}
	
	static public final If IF( final Object o1, final Object o2, final Object o3 )
	{
		return new If( o1, o2, o3 );
	}
}
