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
package net.imglib2.interpolation.randomaccess;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.algorithm.bspline.BSplineCoefficientsInterpolator;
import net.imglib2.algorithm.bspline.BSplineCoefficientsInterpolatorEven;
import net.imglib2.algorithm.bspline.BSplineCoefficientsInterpolatorOdd;
import net.imglib2.algorithm.bspline.BSplineDecomposition;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class BSplineCoefficientsInterpolatorFactory<T extends RealType<T>, S extends RealType<S>> implements InterpolatorFactory< S, RandomAccessible< T > >
{
	protected final int order;

	protected final boolean clipping;

	protected final RandomAccessibleInterval<S> coefficients;
	
	protected OutOfBoundsFactory<?,?> oobFactory;

	/**
	 * Creates a new {@link BSplineCoefficientsInterpolatorFactory} using the BSpline 
	 * interpolation in a certain window
	 *
	 * @param img
	 * 			the random accessible to be interpolated
	 * @param interval
	 * 			the interval over which to do the interpolation and store
	 * 			coefficients
	 * @param order
	 *            the order of the bspline
	 * @param clipping
	 *            the bspline-interpolation can create values that are bigger or
	 *            smaller than the original values, so they can be clipped to
	 *            the range of the {@link Type} if wanted
	 * @param coefficientFactory
	 * 			a factory used to create the coefficient image
	 * @param oobFactory
	 * 	  	    a factory specifying the out-of-bound policy to use 
	 */
	public BSplineCoefficientsInterpolatorFactory( 
			final RandomAccessible<T> img,
			final Interval interval,
			final int order, 
			final boolean clipping, 
			final ImgFactory<S> coefficientFactory,
			final OutOfBoundsFactory<? extends RealType<?>, ?> oobFactory )
	{
		this.order = order;
		this.clipping = clipping;
		this.oobFactory = oobFactory;

		@SuppressWarnings("unchecked")
		ExtendedRandomAccessibleInterval<T, IntervalView<T>> extendedImg = Views.extend( 
				Views.interval( img, interval ), 
				((OutOfBoundsFactory<T,RandomAccessibleInterval<T>>)oobFactory));

		BSplineDecomposition<T,S> decomp = new BSplineDecomposition<>( order, extendedImg );

		long[] min = Intervals.minAsLongArray( interval );
		if( Arrays.stream( min ).allMatch( x -> x == 0 ) )
		{
			coefficients = coefficientFactory.create( interval );
		}
		else
		{
			Img<S> coefficientsBase = coefficientFactory.create( interval );
			coefficients = Views.translate( coefficientsBase, min );
		}

		decomp.accept( coefficients );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int order, final boolean clipping,
			S coefficientType )
	{
		this( img, interval, order, clipping, Util.getSuitableImgFactory( interval, coefficientType ),
				new OutOfBoundsConstantValueFactory( Util.getTypeFromInterval( Views.interval( img , interval ))));
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int order, final boolean clipping )
	{
		this( img, interval, order, clipping, (S)new DoubleType() );
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int order )
	{
		this( img, interval, order, true, (S)new DoubleType() );
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval )
	{
		this( img, interval, 3, true, (S)new DoubleType());
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img )
	{
		this( img, img, 3, true, (S)new DoubleType());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img,  final int order, final boolean clipping,
			S coefficientType )
	{
		this( img, img, order, clipping, Util.getSuitableImgFactory( img, coefficientType ),
				new OutOfBoundsConstantValueFactory( Util.getTypeFromInterval( img )));
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img, final int order, final boolean clipping )
	{
		this( img, img, order, clipping, (S)new DoubleType() );
	}

	@SuppressWarnings("unchecked")
	public BSplineCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img, final int order )
	{
		this( img, img, order, true, (S)new DoubleType() );
	}

	@Override
	public BSplineCoefficientsInterpolator<S> create( RandomAccessible<T> f )
	{
		@SuppressWarnings("unchecked")
		ExtendedRandomAccessibleInterval<S, RandomAccessibleInterval<S>> coefExt = Views.extend( 
				coefficients,
				((OutOfBoundsFactory<S,RandomAccessibleInterval<S>>)oobFactory));

		S type = Util.getTypeFromInterval( coefficients );
		if( order % 2 == 0 )
			return new BSplineCoefficientsInterpolatorEven<S>( order, coefExt, type.copy() );
		else
			return new BSplineCoefficientsInterpolatorOdd<S>( order, coefExt, type.copy() );
	}

	@Override
	public RealRandomAccess<S> create( RandomAccessible<T> f, RealInterval interval )
	{
		return create( f );
	}
	
}
