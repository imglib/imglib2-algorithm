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
package net.imglib2.algorithm.convolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

/**
 * Helper to implement {@link Convolution#concat}.
 *
 * @author Matthias Arzt
 */
class Concatenation< T > implements Convolution< T >
{

	private final List< Convolution< T > > steps;

	Concatenation( final List< ? extends Convolution< T > > steps )
	{
		this.steps = new ArrayList<>( steps );
	}

	@Deprecated
	@Override
	public void setExecutor( ExecutorService executor )
	{
		for ( Convolution<T> step : steps )
			step.setExecutor( executor );
	}

	@Override
	public Interval requiredSourceInterval( final Interval targetInterval )
	{
		Interval result = targetInterval;
		for ( int i = steps.size() - 1; i >= 0; i-- )
			result = steps.get( i ).requiredSourceInterval( result );
		return result;
	}

	@Override
	public T preferredSourceType( T targetType )
	{
		for ( int i = steps.size() - 1; i >= 0; i-- )
			targetType = steps.get( i ).preferredSourceType( targetType );
		return targetType;
	}

	@Override
	public void process( final RandomAccessible< ? extends T > source, final RandomAccessibleInterval< ? extends T > target )
	{
		final List< Pair< T, Interval > > srcIntervals = tmpIntervals( Util.getTypeFromInterval( target ), target );
		RandomAccessibleInterval< ? extends T > currentSource = Views.interval( source, srcIntervals.get( 0 ).getB() );
		RandomAccessibleInterval< ? extends T > available = null;

		for ( int i = 0; i < steps.size(); i++ )
		{
			final Convolution< T > step = steps.get( i );
			final T targetType = srcIntervals.get( i + 1 ).getA();
			final Interval targetInterval = srcIntervals.get( i + 1 ).getB();
			RandomAccessibleInterval< ? extends T > currentTarget =
					( i == steps.size() - 1 ) ? target : null;

			if ( currentTarget == null && available != null &&
					Intervals.contains( available, targetInterval ) &&
					Util.getTypeFromInterval( available ).getClass().equals( targetType.getClass() ) )
				currentTarget = Views.interval( available, targetInterval );

			if ( currentTarget == null )
				currentTarget = createImage( uncheckedCast( targetType ), targetInterval );

			step.process( currentSource, currentTarget );

			if ( i > 0 )
				available = currentSource;
			currentSource = currentTarget;
		}
	}

	private static < T extends NativeType< T > > RandomAccessibleInterval< T > createImage( final T targetType, final Interval targetInterval )
	{
		final long[] dimensions = Intervals.dimensionsAsLongArray( targetInterval );
		final Img< T > ts = Util.getArrayOrCellImgFactory( targetInterval, targetType ).create( dimensions );
		return Views.translate( ts, Intervals.minAsLongArray( targetInterval ) );
	}

	private static < T > T uncheckedCast( final Object in )
	{
		@SuppressWarnings( "unchecked" )
		final
		T in1 = ( T ) in;
		return in1;
	}

	private List< Pair< T, Interval > > tmpIntervals( T type, Interval interval )
	{
		final List< Pair< T, Interval > > result = new ArrayList<>( Collections.nCopies( steps.size() + 1, null ) );
		result.set( steps.size(), new ValuePair<>( type, interval ) );
		for ( int i = steps.size() - 1; i >= 0; i-- )
		{
			final Convolution< T > step = steps.get( i );
			interval = step.requiredSourceInterval( interval );
			type = step.preferredSourceType( type );
			result.set( i, new ValuePair<>( type, interval ) );
		}
		return result;
	}
}
