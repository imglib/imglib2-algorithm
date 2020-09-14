/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2018 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;


/**
 * Performs b-spline interpolation using pre-computed coefficients.
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 */
public class BSplineCoefficientsInterpolatorFunction<T extends RealType<T>> 
{
	private final T value;
	
	private final double[][] weights;
	
	private final int bsplineOrder;

	private final int kernelWidth;
	
	private final int offset;
	
	private final AbstractBsplineKernel kernel;
	
	private final RealLocalizable position;

	private final RandomAccess<Neighborhood<T>> target;

	@SuppressWarnings( "unused" )
	private BSplineCoefficientsInterpolatorFunction( final BSplineCoefficientsInterpolatorFunction< T > interpolator, final int order, final T type, 
			final boolean optimized )
	{
		this.bsplineOrder = interpolator.bsplineOrder;
		this.target = interpolator.target;
		this.position = interpolator.position;
		value = type;

		kernel = makeKernel( order );
		kernelWidth = order + 1;
		offset = order % 2 == 0 ? order / 2 : ( order + 1 ) / 2;

		weights = new double[ target.numDimensions() ][ kernelWidth ];
	}

	public BSplineCoefficientsInterpolatorFunction( final int order, final RandomAccess<Neighborhood<T>> target,
			final RealLocalizable position,
			final T type )
	{
		this( order, target, position, type, true );
	}

	public BSplineCoefficientsInterpolatorFunction( final int order, 
			final RandomAccess<Neighborhood<T>> target, 
			final RealLocalizable position,
			final T type, 
			final boolean optimized )
	{
		this.bsplineOrder = order;
		this.target = target;
		this.position = position;
		value = type;

		kernel = makeKernel( order );
		kernelWidth = order + 1;
		offset = order % 2 == 0 ? order / 2 : ( order - 1 ) / 2;

		weights = new double[ target.numDimensions() ][ kernelWidth ];
	}
	
	public T type()
	{
		return value;
	}

	public T get()
	{
		fillWeights( position );

		double accumulator = 0;
		final Cursor< T > c = target.get().cursor();
		while ( c.hasNext() )
		{
			double tmp = c.next().getRealDouble();
			for ( int d = 0; d < target.numDimensions(); d++ )
			{
				final int index = ( int ) ( c.getLongPosition( d ) - target.getLongPosition( d ) + offset );

				// This check seems necessary after using too-big rectangle shape
				if( index <  weights[d].length )
					tmp *= weights[ d ][ index ];
				else
					tmp = 0;
			}
			accumulator += tmp;
		}

		value.setReal( accumulator );
		return value;
	}

	protected void fillWeights( RealLocalizable position )
	{
		final Neighborhood< T > rect = target.get();
		for ( int d = 0; d < target.numDimensions(); d++ )
		{
			final double pos = position.getDoublePosition( d );
			final long min = rect.min( d );
			final long max = rect.max( d );

			for ( long i = min; i <= max; ++i )
			{
				weights[ d ][ ( int ) ( i - min ) ] = kernel.evaluateNorm( pos - i );
			}
		}
	} 

	public static AbstractBsplineKernel makeKernel( int order )
	{
		assert( order <= 5  && order >= 0 );

		switch ( order ){
			case 0:
				return new BsplineKernel0();
			case 1:
				return new BsplineKernel1();
			case 2: 
				return new BsplineKernel2();
			case 3:
				return new BsplineKernel3();
			case 4:
				return new BsplineKernel4();
			case 5:
				return new BsplineKernel5();
			default:
				return null;
		}
	}

}
