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
package net.imglib2.algorithm.bspline;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolator;
import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class BSplineCorrectnessTest
{
	@Test
	public void test3Spline1d()
	{
		RandomAccessibleInterval< DoubleType > p0 = ConstantUtils.constantRandomAccessibleInterval( new DoubleType( 1 ), new FinalInterval( 32 ));
		RandomAccessibleInterval<DoubleType> p1 = Views.interval( polynomialImg1dC( new double[]{ 1, 1 }), new FinalInterval( 32 ));
		
		BSplineInterpolatorFactory<DoubleType> factory4 = new BSplineInterpolatorFactory<>( true, 4 );
		BSplineInterpolatorFactory<DoubleType> factory8 = new BSplineInterpolatorFactory<>( true, 8 );

		BSplineInterpolator< DoubleType > spline3p0 = factory4.create( p0 );
		BSplineInterpolator< DoubleType > spline3p1 = factory4.create( p1 );

		spline3p0.setPosition( 15.5, 0 );
		Assert.assertEquals("spline3 p0", 1.0, spline3p0.get().get(), 1e-3 );

		spline3p1.setPosition( 15.5, 0 );
		Assert.assertEquals("spline3 p1", 16.5, spline3p1.get().get(), 0.1 );


		BSplineInterpolator< DoubleType > spline3p0Wider = factory8.create( p0 );
		BSplineInterpolator< DoubleType > spline3p1Wider = factory8.create( p1 );

		spline3p0Wider.setPosition( 15.5, 0 );
		Assert.assertEquals("spline3 p0", 1.0, spline3p0.get().get(), 1e-3 );

		spline3p1Wider.setPosition( 15.5, 0 );
		Assert.assertEquals("spline3 p1", 16.5, spline3p1.get().get(), 0.1 );
	}

	public static <T extends RealType<T>> RandomAccessible<T> separableImage( final T t, final BiConsumer<Localizable,T>... funs1d )
	{
		BiConsumer<Localizable, T> f = new BiConsumer<Localizable,T>()
		{
			@Override
			public void accept( Localizable l, T t )
			{
				T tmp = t.createVariable();
				Point p = new Point( 1 );
				t.setOne();
				for( int i = 0; i < funs1d.length; i++ )
				{
					p.setPosition( l.getIntPosition( i ), 0 );
					funs1d[ i ].accept( p , tmp );
					t.mul( tmp );
				}
			}
		};

		Supplier<T> s = new Supplier<T>()
		{
			@Override
			public T get() {
				return t.createVariable();
			}
		};

		return new FunctionRandomAccessible<T>( 3, f, s );
	}

	public static RandomAccessible<DoubleType> polynomialImg1dC( final double[] coefs )
	{
		return Views.raster( polynomialReal1dC( coefs ));
	}

	public static RealRandomAccessible<DoubleType> polynomialReal1dC( final double[] coefs )
	{
		return new FunctionRealRandomAccessible<>( 1, polynomial1dC( coefs ), DoubleType::new );
	}

	public static BiConsumer< RealLocalizable, DoubleType > polynomial1dZ( final double[] zeros )
	{
		return new BiConsumer<RealLocalizable, DoubleType >()
			{
				@Override
				public void accept( RealLocalizable p, DoubleType v )
				{
					v.setZero();
					double total = 0;
					double term = 1;
					for( int i = 0; i < zeros.length; i++ )
					{
						term = 1;
						for( int j = 0; j < i; j++ )
						{
							term *= ( p.getDoublePosition( 0 ) - zeros[ i ]);
						}
						total += term;
					}
					v.set( total );
				}
			};
	}
	public static BiConsumer< RealLocalizable, DoubleType > polynomial1dC( final double[] coefs )
	{
		return new BiConsumer<RealLocalizable, DoubleType >()
			{
				@Override
				public void accept( RealLocalizable p, DoubleType v )
				{
					v.setZero();
					double total = 0;
					double term = 0;
					for( int i = 0; i < coefs.length; i++ )
					{
						term = coefs[ i ];
						for( int j = 0; j < i; j++ )
						{
							term *= p.getDoublePosition( 0 );
						}
						total += term;
					}
					v.set( total );
				}
			};
	}
}
