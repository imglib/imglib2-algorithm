package net.imglib2.algorithm.math.abstractions;

import java.util.Iterator;
import java.util.LinkedList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.IterableImgSource;
import net.imglib2.algorithm.math.NumberSource;
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
	static public boolean compatibleIterationOrder( final LinkedList< RandomAccessibleInterval< ? > > images )
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
			return new IterableImgSource( (RandomAccessibleInterval) o );
		}
		else if ( o instanceof Number )
		{
			return new NumberSource( ( (Number) o ).doubleValue() );
		}
		else if ( o instanceof IFunction )
		{
			return ( (IFunction) o );
		}
		
		// Make it fail
		return null;
	}
}
