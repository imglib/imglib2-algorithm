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
 * new ImgMath<A, O>( Div<O>( Max<O>( img1, img2, img3 ), 3.0 ) ).into( result );
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
	private final IFunction operation;
	private final Converter< RealType< ? >, RealType< ? > > converter;
	
	public ImgMath( final IFunction operation )
	{
		this( operation,
			  new Converter< RealType< ? >, RealType< ? > >()
		{
			@Override
			public final void convert( final RealType< ? > input, final RealType< ? > output) {
				output.setReal( input.getRealDouble() );
			}
		});
	}
	
	public ImgMath(
			final IFunction operation,
			final Converter< RealType< ? >, RealType< ? > > converter
			)
	{
		this.operation = operation;
		this.converter = converter;
	}
	
	public < O extends RealType< O > > RandomAccessibleInterval< O > into( final RandomAccessibleInterval< O > target )
	{
		// Recursive copy: initializes interval iterators
		final IFunction f = this.operation.copy();
		// Set temporary computation holders
		final O scrap = target.randomAccess().get().createVariable();
		f.setScrap( scrap );
		
		final boolean compatible_iteration_order = this.setup( f, converter );
		
		// Check compatible iteration order and dimensions
		if ( compatible_iteration_order )
		{
			// Evaluate function for every pixel
			for ( final O output : Views.iterable( target ) )
				f.eval( output );
		}
		else
		{
			// Incompatible iteration order
			final Cursor< O > cursor = Views.iterable( target ).cursor();
			
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				f.eval( cursor.get(), cursor );
			}
		}
		
		return target;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean setup( final IFunction f, final Converter< ?, ? > converter )
	{	
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// child-parent map
		final HashMap< IFunction, IFunction > cp = new HashMap<>();
		
		// Collect images to later check their iteration order
		final LinkedList< RandomAccessibleInterval< ? > > images = new LinkedList<>();
		
		// Collect Var instances to check that each corresponds to an upstream Let
		final ArrayList< Var > vars = new ArrayList<>();
		
		IFunction parent = null;
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			cp.put( op, parent );
			parent = op;
			
			if ( op instanceof IterableImgSource )
			{
				final IterableImgSource iis = ( IterableImgSource )op;
				// Side effect: set the converter from input to output types
				iis.setConverter( converter );
				images.addLast( iis.rai );
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
			else if ( op instanceof Var )
			{
				final Var var = ( Var )op;
				vars.add( var );
			}
		}
		
		// Check Vars: are they all using names declared in upstream Lets
		all: for ( final Var var : vars )
		{
			parent = var;
			while ( null != ( parent = cp.get( parent ) ) )
			{
				if ( parent instanceof Let )
				{
					Let let = ( Let )parent;
					if ( let.varName != var.name )
						continue;
					// Else, found: Var is in use
					continue all;
				}
			}
			// No upstream Let found
			throw new RuntimeException( "The Var(\"" + var.name + "\") does not read from any upstream Let. " );
		}		
		
		return Util.compatibleIterationOrder( images );
	}
	
	static public final ImgMath compute( final IFunction operation )
	{
		return new ImgMath( operation );
	}
	
	static public final ImgMath compute( final IFunction operation, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		return new ImgMath( operation, converter );
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
