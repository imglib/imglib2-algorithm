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
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Compute
{
	private final IFunction operation;
	private final boolean compatible_iteration_order;
	
	/**
	 * Validate the {code operation}.
	 * 
	 * @param operation
	 */
	public Compute( final IFunction operation )
	{
		this.operation = operation;
		
		// Throw RuntimeException as needed to indicate incorrect construction
		this.compatible_iteration_order = this.validate( this.operation );
	}

	/**
	 * Execute the computation and store the result into the {@code target}.
	 * The computation is done using {@code Type}-based math, with the {@code Type}
	 * of the {@code target} defining the specific math implementation and numerical
	 * precision that will be used.
	 * 
	 * @param target The {@code RandomAccessibleInterval} into which to store the computation;
	 *               note its {@code Type} determines the precision of the computation and the specific
	 *               implementation of the mathematical operations.
	 * @return The {@code target}.
	 */
	public < O extends RealType< O > > RandomAccessibleInterval< O > into( final RandomAccessibleInterval< O > target )
	{
		return this.into( target, null );
	}
	
	/**
	 * Execute the mathematical operations and store the result into the given {@code RandomAccessibleInterval}.
	 * 
	 * @param target The {@code RandomAccessibleInterval} into which to store the computation;
	 *               note its {@code Type} determines the precision of the computation and the specific
	 *               implementation of the mathematical operations.
	 * 
	 * @param converter The {@code Converter} that transfers all input {@code Type} to the {@code Type}
	 *                  of the {@code target}; when null, will create one that uses double floating-point
	 *                  precision; but note that if the {@code Type} of an input {@code RandomAccessibleInterval}
	 *                  is the same as that of the {@code target}, the converter will not be used.
	 * 
	 * @return The {@code target}.
	 */
	public < O extends RealType< O > > RandomAccessibleInterval< O > into(
			final RandomAccessibleInterval< O > target,
			Converter< RealType< ? >, O > converter
			)
	{
		if ( null == converter )
			converter = new Converter< RealType< ? >, O >()
			{
				@Override
				public final void convert( final RealType< ? > input, final O output)
				{
					output.setReal( input.getRealDouble() );
				}
			};
		
		// Recursive copy: initializes interval iterators and sets temporary computation holder
		final OFunction< O > f = this.operation.reInit(
				target.randomAccess().get().createVariable(),
				new HashMap< String, O >(),
				converter, null );
		
		// Check compatible iteration order and dimensions
		if ( compatible_iteration_order )
		{
			// Evaluate function for every pixel
			for ( final O output : Views.iterable( target ) )
				output.set( f.eval() );
		}
		else
		{
			// Incompatible iteration order
			final Cursor< O > cursor = Views.iterable( target ).cursor();
			
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().set( f.eval( cursor ) );
			}
		}
		
		return target;
	}
	
	private boolean validate( final IFunction f )
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
				images.addLast( ( ( ImgSource< ? > )op ).getRandomAccessibleInterval() );
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
		
		// Check ImgSource: if they are downstream of an If statement, they should be declared in a Let before that
		
		return Util.compatibleIterationOrder( images );
	}
}
