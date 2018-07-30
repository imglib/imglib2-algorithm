package net.imglib2.algorithm.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.ITrinaryFunction;
import net.imglib2.algorithm.math.abstractions.IUnaryFunction;
import net.imglib2.algorithm.math.abstractions.ImgSource;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Compute
{
	private final IFunction operation;
	private final Converter< RealType< ? >, RealType< ? > > converter;
	
	/**
	 * Prepare the operation for computation with a default converter.
	 * The converter will be used only if necessary, that is, only if
	 * the input image type is not the same or a sublclass of the output
	 * image type as specified in the {@link Compute#into(RandomAccessibleInterval)}.
	 * 
	 * @param operation
	 */
	public Compute( final IFunction operation )
	{
		this( operation, new Converter< RealType< ? >, RealType< ? > >()
		{
			@Override
			public final void convert( final RealType<?> input, RealType<?> output)
			{
				output.setReal( input.getRealDouble() );
			}
		});
	}
	
	/**
	 * Prepare the operation for computation.
	 * The converter will be used only if necessary, that is, only if
	 * the input image type is not the same or a sublclass of the output
	 * image type as specified in the {@link Compute#into(RandomAccessibleInterval)}.
	 * 
	 * @param operation
	 * @param converter
	 */
	public Compute(
			final IFunction operation,
			final Converter< RealType< ? >, RealType< ? > > converter
			)
	{
		this.operation = operation;
		this.converter = converter;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public < O extends RealType< O > > RandomAccessibleInterval< O > into( final RandomAccessibleInterval< O > target )
	{
		// Recursive copy: initializes interval iterators and sets temporary computation holder
		final IFunction f = this.operation.reInit(
				target.randomAccess().get().createVariable(),
				new HashMap< String, RealType< ? > >(),
				this.converter );
		
		final boolean compatible_iteration_order = this.setup( f );
		
		// Check compatible iteration order and dimensions
		if ( compatible_iteration_order )
		{
			// Evaluate function for every pixel
			for ( final RealType output : Views.iterable( target ) )
				output.set( f.eval() );
		}
		else
		{
			// Incompatible iteration order
			final Cursor< RealType > cursor = ( Cursor< RealType > )Views.iterable( target ).cursor();
			
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				final RealType output = cursor.get();
				output.set( f.eval( cursor ) );
			}
		}
		
		return target;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private boolean setup( final IFunction f )
	{	
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// child-parent map
		final HashMap< IFunction, IFunction > cp = new HashMap<>();
		
		// Collect images to later check their iteration order
		final LinkedList< RandomAccessibleInterval< ? > > images = new LinkedList<>();
		
		// Collect Var instances to check that each corresponds to an upstream Let
		final ArrayList< Var > vars = new ArrayList<>();
		
		// Collect Let instances to check that their declared variables are used
		final HashSet< Let > lets = new HashSet<>();
		
		IFunction parent = null;
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			cp.put( op, parent );
			parent = op;
			
			if ( op instanceof ImgSource )
			{
				images.addLast( ( ( ImgSource )op ).getRandomAccessibleInterval() );
			}
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
					
					if ( op instanceof Let )
					{
						lets.add( ( Let )op );
					}
					
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
		final HashSet< Let > used = new HashSet<>();
		all: for ( final Var var : vars )
		{
			parent = var;
			while ( null != ( parent = cp.get( parent ) ) )
			{
				if ( parent instanceof Let )
				{
					Let let = ( Let )parent;
					if ( let.getVarName() != var.getName() )
						continue;
					// Else, found: Var is in use
					used.add( let );
					continue all;
				}
			}
			// No upstream Let found
			throw new RuntimeException( "The Var(\"" + var.getName() + "\") does not read from any upstream Let. " );
		}
		
		// Check Lets: are their declared variables used in downstream Vars?
		if ( lets.size() != used.size() )
		{
			lets.removeAll( used );
			String msg = "The Let-declared variable" + ( 1 == lets.size() ? "" : "s" );
			for ( final Let let : lets )
				msg += " \"" + let.getVarName() + "\"";
			msg += " " + ( 1 == lets.size() ? "is" : "are") + " not used by any downstream Var.";
			throw new RuntimeException( msg );
		}
		
		return Util.compatibleIterationOrder( images );
	}
}
