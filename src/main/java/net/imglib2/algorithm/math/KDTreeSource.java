package net.imglib2.algorithm.math;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.SourceInterval;
import net.imglib2.algorithm.math.execution.KDTreeRadiusSource;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class KDTreeSource< I extends RealType< I > > extends ViewableFunction implements IFunction, SourceInterval
{
	private final KDTree< I > kdtree;
	private final double radius;
	private final I outside;
	private final Interval interval;

	/*
	public KDTreeSource( final Collection< List< Number > > points, final List< I > values, final double radius )
	{
		this( new KDTree< I >( values, points.stream().map( ( coords ) -> Point.wrap( coords.stream().mapToLong( Number::longValue ).toArray() ) ).collect( Collectors.toList() ) ), radius );
	}
	*/
	
	public KDTreeSource( final List< Point > points, final I value, final double radius )
	{
		this( new KDTree< I >( points.stream().map( ( p ) -> value ).collect( Collectors.toList() ), points ), radius );
	}
	
	public KDTreeSource( final List< Point > points, final List< I > values, final double radius )
	{
		this( new KDTree< I >( values, points ), radius );
	}
	
	public KDTreeSource( final KDTree< I > kdtree, final double radius )
	{
		this( kdtree, radius, kdtree.firstElement().createVariable(), null ); // with outside of value zero
	}
	
	public KDTreeSource( final KDTree< I > kdtree, final double radius, final Object outside )
	{
		this( kdtree, radius, outside, null );
	}
	
	public KDTreeSource( final KDTree< I > kdtree, final double radius, final Object outside, final Interval interval )
	{
		this.kdtree = kdtree;
		this.radius = radius;
		final I first = kdtree.firstElement();
		this.outside = asInputType( first, outside );
		if ( null == interval )
		{
			// Find out the interval from the data
			final long[] min = new long[ kdtree.numDimensions() ],
					     max = new long[ kdtree.numDimensions() ];
			for ( int d = 0; d < kdtree.numDimensions(); ++d )
			{
				min[ d ] = ( long )Math.floor( kdtree.realMin( d ) - radius );
				max[ d ] = ( long )Math.ceil( kdtree.realMax( d ) + radius );
			}
			this.interval = new FinalInterval( min, max );
		}
		else
		{
			this.interval = interval;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final <I extends RealType< I > > I asInputType( final I first, final Object ob )
	{
		if ( first.getClass().isAssignableFrom( ob.getClass() ) )
			return ( I )ob;
		final I v = first.createVariable();
		if ( v instanceof IntegerType< ? > && ( ob instanceof Long || ob instanceof Integer || ob instanceof Short || ob instanceof Byte ) )
		{
			( ( IntegerType< ? >)v ).setInteger( ( ( Number )ob ).longValue() );
			return v;
		}
		if ( ob instanceof RealType< ? > )
		{
			v.setReal( ( ( RealType< ? > )ob ).getRealDouble() );
			return v;
		}
		if ( ob instanceof Number) // Float or Double or else
		{
			v.setReal( ( ( Number )ob ).doubleValue() );
			return v;
		}
		throw new UnsupportedOperationException( "KDTreeSource can't handle " + ob + " as outside value." );
	}

	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new KDTreeRadiusSource< I, O >( tmp.copy(), converter, this.kdtree, this.radius, this.outside, this.interval );
	}
	
	public KDTree< I > getKDTree()
	{
		return this.kdtree;
	}
	
	@Override
	public Interval getInterval()
	{
		return this.interval;
	}
}