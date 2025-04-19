/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.math.abstractions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.ImgSource;
import net.imglib2.algorithm.math.Let;
import net.imglib2.algorithm.math.NumberSource;
import net.imglib2.algorithm.math.RandomAccessibleSource;
import net.imglib2.algorithm.math.Var;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public class Util
{
	/**
	 * Check for compatibility among the iteration order of the images, and
	 * throws a RuntimeException When images have different dimensions.
	 * 
	 * @param images
	 * @return Returns true if images have the same dimensions and iterator order, and false when the iteration order is incompatible.
	 */
	static public boolean compatibleIterationOrder( final List< RandomAccessibleInterval< ? > > images )
	{
		if ( images.isEmpty() )
		{
			// Purely numeric operations
			return true;
		}

		final Iterator< RandomAccessibleInterval< ? > > it = images.iterator();
		final RandomAccessibleInterval< ? > first = it.next();
		final Object order = Views.iterable( (RandomAccessibleInterval< ? >)first ).iterationOrder();
		
		boolean same_iteration_order = true;
		
		while ( it.hasNext() )
		{
			final RandomAccessibleInterval< ? > other = it.next();
			if ( other.numDimensions() != first.numDimensions() )
			{
				throw new RuntimeException( "Images have different number of dimensions" );
			}
			
			for ( int d = 0; d < first.numDimensions(); ++d )
			{
				if ( first.realMin( d ) != other.realMin( d ) || first.realMax( d ) != other.realMax( d ) )
				{
					throw new RuntimeException( "Images have different sizes" );
				}
			}
			
			if ( ! order.equals( ( Views.iterable( other ) ).iterationOrder() ) )
			{
				// Images differ in their iteration order
				same_iteration_order = false;
			}
		}
		
		return same_iteration_order;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public final IFunction wrap( final Object o )
	{
		if ( o instanceof RandomAccessibleInterval< ? > )
		{
			return new ImgSource( ( RandomAccessibleInterval )o );
		}
		else if ( o instanceof RandomAccessible< ? > )
		{
			return new RandomAccessibleSource( ( RandomAccessible )o );
		}
		else if ( o instanceof Number )
		{
			return new NumberSource( ( ( Number )o ).doubleValue() );
		}
		else if ( o instanceof IFunction )
		{
			return ( ( IFunction ) o );
		}
		else if ( o instanceof String )
		{
			return new Var( ( String )o );
		}
		
		// Make it fail
		return null;
	}
	
	static public final Set< RandomAccessibleInterval< ? > > findImg ( final IFunction f )
	{
		final Set< RandomAccessibleInterval< ? > > images = new HashSet<>();
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			
			if ( op instanceof ImgSource )
				images.add( ( ( ImgSource< ? > )op ).getRandomAccessibleInterval() );
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
					
					if ( op instanceof ITrinaryFunction )
					{
						ops.addLast( ( ( ITrinaryFunction )op ).getThird() );
					}
				}
			}
		}
		
		return images;
	}
	
	static public final RandomAccessibleInterval< ? > findFirstImg ( final IFunction f )
	{
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		RandomAccessibleInterval< ? > extended = null;
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			
			if ( op instanceof ImgSource )
				return ( ( ImgSource< ? > )op ).getRandomAccessibleInterval();
			if ( null == extended && op instanceof RandomAccessOnly )
			{
				final RandomAccessOnly< ? > rao = ( RandomAccessOnly< ? > )op;
				if ( rao.getRandomAccessible() instanceof ExtendedRandomAccessibleInterval )
					extended = ( ( ExtendedRandomAccessibleInterval< ?, ? > )rao.getRandomAccessible() ).getSource();
			}
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
					
					if ( op instanceof ITrinaryFunction )
					{
						ops.addLast( ( ( ITrinaryFunction )op ).getThird() );
					}
				}
			}
		}
		
		return extended;
	}
	
	static public final Interval findFirstInterval ( final IFunction f )
	{
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		Interval extended = null;
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			
			if ( op instanceof SourceInterval )
				return ( ( SourceInterval )op ).getInterval();
			if ( null == extended && op instanceof RandomAccessOnly )
			{
				final RandomAccessOnly< ? > rao = ( RandomAccessOnly< ? > )op;
				if ( rao.getRandomAccessible() instanceof ExtendedRandomAccessibleInterval )
					extended = ( ( ExtendedRandomAccessibleInterval< ?, ? > )rao.getRandomAccessible() ).getSource();
			}
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
					
					if ( op instanceof ITrinaryFunction )
					{
						ops.addLast( ( ( ITrinaryFunction )op ).getThird() );
					}
				}
			}
		}
		
		return extended; // may be null
	}
	
	static public final int dimensions ( final IFunction f )
	{
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			
			if ( op instanceof SourceInterval )
				return ( ( SourceInterval )op ).getInterval().numDimensions();
			if ( op instanceof RandomAccessOnly )
				return ( ( RandomAccessOnly< ? > )op ).getRandomAccessible().numDimensions();
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
					
					if ( op instanceof ITrinaryFunction )
					{
						ops.addLast( ( ( ITrinaryFunction )op ).getThird() );
					}
				}
			}
		}
		
		return 0;
	}

	static final public IFunction[] wrapMap( final Object caller, final Object[] obs )
	{
		try {
			if ( 2 == obs.length )
				return new IFunction[]{ Util.wrap( obs[ 0 ] ), Util.wrap( obs[ 1 ]) };
			
			final Constructor< ? > constructor = caller.getClass().getConstructor( new Class[]{ Object.class, Object.class } );
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
			throw new RuntimeException( "Error with the constructor for class " + caller.getClass(), e );
		}
	}

	static public final < O extends RealType< O > > Converter< RealType< ? >, O > genericRealTypeConverter()
	{
		return new Converter< RealType< ? >, O >()
		{
			@Override
			public final void convert( final RealType< ? > input, final O output)
			{
				output.setReal( input.getRealDouble() );
			}
		};
	}
	
	static public final< I extends IntegerType< I >, O extends IntegerType< O > > Converter< I, O > genericIntegerTypeConverter()
	{
		return new Converter< I, O >()
		{
			@Override
			public final void convert( final I input, O output )
			{
				output.setInteger( input.getIntegerLong() );
			}
		};
	}

	public static final < O extends RealType< O > > IImgSourceIterable< ? > findFirstIterableImgSource( final OFunction< O > f )
	{
		final LinkedList< OFunction< O > > ops = new LinkedList<>();
		ops.addLast( f );
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final OFunction< O >  op = ops.removeFirst();
			for ( final OFunction< O > cf : op.children() )
			{
				if ( cf instanceof IImgSourceIterable< ? > )
					return ( IImgSourceIterable< ? > ) cf;
				ops.addLast( cf );
			}
		}
		
		return null;
	}
	
	public static final < O extends RealType< O > > List< IImgSourceIterable< ? > > findAllIterableImgSource( final OFunction< O > f )
	{
		final LinkedList< OFunction< O > > ops = new LinkedList<>();
		ops.addLast( f );
		
		final List< IImgSourceIterable< ? > > iis = new ArrayList<>();
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final OFunction< O >  op = ops.removeFirst();
			for ( final OFunction< O > cf : op.children() )
			{
				if ( cf instanceof IImgSourceIterable< ? > )
					iis.add( ( IImgSourceIterable< ? > ) cf );
				else
					ops.addLast( cf );
			}
		}
		
		return iis;
	}
	
	/** Generate a printable, indented {@link String} with the nested hierarchy
	 *  of operations under {@link IFunction} {@code f}. */
	public static final String hierarchy( final IFunction f )
	{
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		final StringBuilder hierarchy = new StringBuilder();
		final LinkedList< String > indents = new LinkedList<>();
		indents.add("");
		
		// Iterate into the nested operations, depth-first
		while ( ! ops.isEmpty() )
		{
			final IFunction op = ops.removeFirst();

			String indent = indents.removeFirst();
			String pre = "";
			String post = "";

			if ( op instanceof IUnaryFunction )
			{
				ops.addFirst( ( ( IUnaryFunction )op ).getFirst() );
				indents.addFirst( indent + "  " );

				if ( op instanceof IBinaryFunction )
				{
					if ( op instanceof Let )
					{
						post = " \"" + ( ( Let )op ).getVarName() + "\" -> ";
					}
					ops.add( 1, ( ( IBinaryFunction )op ).getSecond() );
					indents.add( 1, indent + "  " );

					if ( op instanceof ITrinaryFunction )
					{
						ops.add( 2, ( ( ITrinaryFunction )op ).getThird() );
						indents.add( 2, indent + "  " );
					}
				}
			}
			else if ( op instanceof Var )
			{
				pre = "\"" + ( ( Var )op ).getName() + "\" ";
			}
			

			hierarchy.append( indent + pre + op.getClass().getSimpleName() + post + "\n");
		}

		return hierarchy.toString();
	}
	
	public static final String hierarchy( final OFunction< ? > f )
	{	
		final StringBuilder hierarchy = new StringBuilder();
		final LinkedList< String > indents = new LinkedList<>();
		indents.add("");
		
		final LinkedList< OFunction< ? > > ops = new LinkedList<>();
		ops.addLast( f );
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final OFunction< ? >  op = ops.removeFirst();
			
			final String indent = indents.removeFirst();
			
			for ( final OFunction< ? > cf : op.children() )
			{
				ops.addFirst( cf );
				indents.addFirst( indent + "  " );
			}
			
			hierarchy.append( indent + op.getClass().getSimpleName() + "\n");
		}
		
		return hierarchy.toString();
	}
}
