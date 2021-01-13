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

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.algorithm.neighborhood.GeneralRectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.type.numeric.RealType;


/**
 * Performs b-spline interpolation for order up to and including 5.
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 */
public interface BSplineCoefficientsInterpolator<T extends RealType<T>> extends RealRandomAccess< T >, InterpolatorFactory< T, RandomAccessibleInterval<T> >, Localizable 
{
	public static <S extends RealType<S>> BSplineCoefficientsInterpolator<S> build(
		final int order, final RandomAccessible< S > coefficients, final S type )
	{
		if( order % 2 == 0 )
			return new BSplineCoefficientsInterpolatorEven<S>( order, coefficients, type );
		else
			return new BSplineCoefficientsInterpolatorOdd<S>( order, coefficients, type );
	}
	
	@Override
	public BSplineCoefficientsInterpolator<T> copy();
	
	@Override
	default RealRandomAccess<T> create( RandomAccessibleInterval<T> f )
	{
		return copy();
	}

	@Override
	default RealRandomAccess< T > create(RandomAccessibleInterval< T > f, RealInterval interval)
	{
		return copy();
	}
	
	@Override
	default RealRandomAccess< T > copyRealRandomAccess()
	{
		return copy();
	}

	/**
	 * Return a RectangleShape 
	 * 
	 * This is necessary because rectangles always have an extents equal to an 
	 * odd number of pixels.  Need to round up to the nearest odd number when 
	 * an even number of samples are needed.
	 * 
	 * @return the rectangle shape
	 */
	public static RectangleShape shapeFromOrder( int bsplineOrder )
	{
		assert( bsplineOrder <= 5  && bsplineOrder >= 0 );

		switch ( bsplineOrder ){
			case 0:
				return new RectangleShape( 0, false ); // need one sample - correct
			case 1:
				return new RectangleShape( 1, false ); // need two samples - round up
			case 2: 
				return new RectangleShape( 1, false ); // need three samples - correct
			case 3:
				return new GeneralRectangleShape( 4, -1, false ); // need four samples - round up
			case 4:
				return new RectangleShape( 2, false ); // need five samples - round up
			case 5:
				return new GeneralRectangleShape( 6, -2, false ); // need four samples - round up
			default:
				return null;
		}
	}

}
