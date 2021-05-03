/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.bspline;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.position.transform.Round;
import net.imglib2.type.numeric.RealType;

/**
 * Computes b-spline coefficients for splines of even order.
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 */
public class BSplineCoefficientsInterpolatorEven<T extends RealType<T>> extends Round< RandomAccess< Neighborhood< T >>> implements BSplineCoefficientsInterpolator<T>
{
	final protected int bsplineOrder;

	protected final RectangleShape shape;

	private final BSplineCoefficientsInterpolatorFunction<T> kernel;
	
	private BSplineCoefficientsInterpolatorEven( final BSplineCoefficientsInterpolatorEven< T > interpolator, final int order, final T type )
	{
		this( interpolator, order, type, true );
	}

	private BSplineCoefficientsInterpolatorEven( final BSplineCoefficientsInterpolatorEven< T > interpolator, final int order, final T type, 
			final boolean optimized )
	{
		super( interpolator.target.copyRandomAccess() );
		assert( order % 2 == 0 );
		
		this.bsplineOrder = interpolator.bsplineOrder;
		this.shape = BSplineCoefficientsInterpolator.shapeFromOrder( bsplineOrder  );
		kernel = new BSplineCoefficientsInterpolatorFunction<>( order, target, this, type );
	}

	public BSplineCoefficientsInterpolatorEven( final int order, final RandomAccessible< T > coefficients, final T type )
	{
		this( order, coefficients, type, BSplineCoefficientsInterpolator.shapeFromOrder( order ));
	}

	private BSplineCoefficientsInterpolatorEven( 
			final int order, 
			final RandomAccessible< T > coefficients, final T type, 
			final RectangleShape shape )
	{
		super( shape.neighborhoodsRandomAccessible( coefficients ).randomAccess() );
		assert( order % 2 == 0 );

		this.shape = shape;
		this.bsplineOrder = order;
		kernel = new BSplineCoefficientsInterpolatorFunction<>( order, target, this, type );
	}

	@Override
	public T get()
	{
		return kernel.get();
	}

	@Override
	public BSplineCoefficientsInterpolatorEven<T> copy()
	{
		return new BSplineCoefficientsInterpolatorEven<T>( this, this.bsplineOrder, kernel.type().copy() );
	}

	@Override
	public long getLongPosition( int d )
	{
		return ( long ) Math.floor( position[ d ] );
	}

	@Override
	public int getIntPosition( int d )
	{
		return ( int ) Math.floor( position[ d ] );
	}

	@Override
	public void localize( int[] position )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			position[ d ] = getIntPosition( d );
	}

	@Override
	public void localize( long[] position )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			position[ d ] = getLongPosition( d );
	}

}
