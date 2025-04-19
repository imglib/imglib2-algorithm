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
	
	public KDTreeSource( final List< Point > points, final I value, final double radius, final Object outside )
	{
		this( new KDTree< I >( points.stream().map( ( p ) -> value ).collect( Collectors.toList() ), points ), radius, outside, null );
	}
	
	public KDTreeSource( final List< Point > points, final I value, final double radius, final Object outside, final Interval interval )
	{
		this( new KDTree< I >( points.stream().map( ( p ) -> value ).collect( Collectors.toList() ), points ), radius, outside, interval );
	}
	
	public KDTreeSource( final List< Point > points, final List< I > values, final double radius )
	{
		this( new KDTree< I >( values, points ), radius );
	}
	
	public KDTreeSource( final List< Point > points, final List< I > values, final double radius, final Object outside )
	{
		this( new KDTree< I >( values, points ), radius, outside, null );
	}
	
	public KDTreeSource( final List< Point > points, final List< I > values, final double radius, final Object outside, final Interval interval )
	{
		this( new KDTree< I >( values, points ), radius, outside, interval );
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
