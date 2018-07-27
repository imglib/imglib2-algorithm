package net.imglib2.algorithm.math;

import java.util.ArrayList;
import java.util.HashMap;
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

public class Compute
{
	private final IFunction operation;
	private final Converter< RealType< ? >, RealType< ? > > converter;
	
	public Compute( final IFunction operation )
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
	
	public Compute(
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
}
