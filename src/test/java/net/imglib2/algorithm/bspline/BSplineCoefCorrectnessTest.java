package net.imglib2.algorithm.bspline;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.bspline.BSplineCoefficientsInterpolator;
import net.imglib2.interpolation.randomaccess.BSplineCoefficientsInterpolatorFactory;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class BSplineCoefCorrectnessTest
{

	// 1d
	private Interval coefItvl1d;
	private Interval testItvl1d;;

	private RealRandomAccessible<DoubleType> const1d;
	private RealRandomAccessible<DoubleType> linear1d;
	private RealRandomAccessible<DoubleType> quadratic1d;
	private RealRandomAccessible<DoubleType> cubic1d;
	private RealRandomAccessible<DoubleType> quartic1d;

	// 2d
	private Interval coefItvl2d;
	private Interval testItvl2d;;

	private RealRandomAccessible<DoubleType> const2d;
	private RealRandomAccessible<DoubleType> linear2d;
	private RealRandomAccessible<DoubleType> quadratic2d;
	private RealRandomAccessible<DoubleType> cubic2d;
	private RealRandomAccessible<DoubleType> quartic2d;

	@SuppressWarnings("unchecked")
	@Before
	public void setup()
	{
		coefItvl1d = new FinalInterval( new long[]{ 64 });
		testItvl1d = new FinalInterval( new long[]{ 28 }, new long[]{ 36 });

		const1d = ConstantUtils.constantRealRandomAccessible( new DoubleType( 1 ), 1 );
		linear1d = polynomialReal1dC( new double[]{ 1, 2 } );
		quadratic1d = polynomialReal1dZ( 1, new double[]{ 31, 33 } );
		cubic1d = polynomialReal1dZ( 1, new double[]{ 31, 32, 33 } );


		coefItvl2d = new FinalInterval( new long[]{ 64, 64 });
		testItvl2d = new FinalInterval( new long[]{ 28, 28 }, new long[]{ 36, 36 });

		const2d = ConstantUtils.constantRealRandomAccessible( new DoubleType( 1 ), 2 );

		linear2d = separableImageReal( new DoubleType(), 
					polynomial1dC( new double[]{ 1, 2 }), 
					polynomial1dC( new double[]{ -10, -2 }));

		quadratic2d = separableImageReal( new DoubleType(), 
						polynomial1dZ( 1, new double[]{ 31, 33 } ),
						polynomial1dZ( -1, new double[]{ 31, 33 } ));

		cubic2d = separableImageReal( new DoubleType(), 
						polynomial1dZ( 1, new double[]{ 31, 32, 33 } ),
						polynomial1dZ( -1, new double[]{ 31, 32, 33 } ));
	}

	/**
	 * Test that splines of order N can correctly interpolate polynomials up to degree N 
	 * in one dimension.
	 */
	@Test
	public void testSplines1d()
	{
		final double delta = 1e-6;

		// order two bspline
		runTest( 2, const1d, "constant", coefItvl1d, testItvl1d, delta );
		runTest( 2, linear1d, "linear", coefItvl1d, testItvl1d, delta );
		runTest( 2, quadratic1d, "quadratic", coefItvl1d, testItvl1d, delta );

		// order three bspline
		runTest( 3, const1d, "constant", coefItvl1d, testItvl1d, delta );
		runTest( 3, linear1d, "linear", coefItvl1d, testItvl1d, delta );
		runTest( 3, quadratic1d, "quadratic", coefItvl1d, testItvl1d, delta );
		runTest( 3, cubic1d, "cubic", coefItvl1d, testItvl1d, delta );

		// order four bspline
		runTest( 4, const1d, "constant", coefItvl1d, testItvl1d, delta );
		runTest( 4, linear1d, "linear", coefItvl1d, testItvl1d, delta );
		runTest( 4, quadratic1d, "quadratic", coefItvl1d, testItvl1d, delta );
		runTest( 4, cubic1d, "cubic", coefItvl1d, testItvl1d, delta );

		// order five bspline
		runTest( 5, const1d, "constant", coefItvl1d, testItvl1d, delta );
		runTest( 5, linear1d, "linear", coefItvl1d, testItvl1d, delta );
		runTest( 5, quadratic1d, "quadratic", coefItvl1d, testItvl1d, delta );
		runTest( 5, cubic1d, "cubic", coefItvl1d, testItvl1d, delta );
	}

	/**
	 * Test that splines of order N can correctly interpolate polynomials up to degree N 
	 * in two dimensions.
	 */
	@Test
	public void testSplines2d()
	{
		final double delta = 1e-6;

		// order two bspline
		runTest( 2, const2d, "constant", coefItvl2d, testItvl2d, delta );
		runTest( 2, linear2d, "linear", coefItvl2d, testItvl2d, delta );
		runTest( 2, quadratic2d, "quadratic", coefItvl2d, testItvl2d, delta );

		// order three bspline
		runTest( 3, const2d, "constant", coefItvl2d, testItvl2d, delta );
		runTest( 3, linear2d, "linear", coefItvl2d, testItvl2d, delta );
		runTest( 3, quadratic2d, "quadratic", coefItvl2d, testItvl2d, delta );
		runTest( 3, cubic2d, "cubic", coefItvl2d, testItvl2d, delta );

		// order four bspline
		runTest( 4, const2d, "constant", coefItvl2d, testItvl2d, delta );
		runTest( 4, linear2d, "linear", coefItvl2d, testItvl2d, delta );
		runTest( 4, quadratic2d, "quadratic", coefItvl2d, testItvl2d, delta );
		runTest( 4, cubic2d, "cubic", coefItvl2d, testItvl2d, delta );

		// order five bspline
		runTest( 5, const2d, "constant", coefItvl2d, testItvl2d, delta );
		runTest( 5, linear2d, "linear", coefItvl2d, testItvl2d, delta );
		runTest( 5, quadratic2d, "quadratic", coefItvl2d, testItvl2d, delta );
		runTest( 5, cubic2d, "cubic", coefItvl2d, testItvl2d, 1e-3 ); // errors are a bit higher here
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends RealType<T>> void runTest( 
			final int order, 
			final RealRandomAccessible<T> realImg, 
			final String baseMessage,
			final Interval coefInterval, 
			final Interval testInterval, 
			final double delta )
	{
		assert( realImg.numDimensions() == coefInterval.numDimensions() && 
				coefInterval.numDimensions() == testInterval.numDimensions() );

		final int nd = realImg.numDimensions();
		RandomAccessible<T> img = Views.raster( realImg );
		BSplineCoefficientsInterpolator<DoubleType> est = new BSplineCoefficientsInterpolatorFactory( img, coefInterval, order ).create( img );

		RealRandomAccess<T> trueRa = realImg.realRandomAccess();
		IntervalIterator it = new IntervalIterator( testInterval );
		while( it.hasNext() )
		{
			it.fwd();
			for( int d = 0; d < nd; d++ )
			{
				trueRa.setPosition( it.getDoublePosition( d ) + 0.5, d );
				est.setPosition( it.getDoublePosition( d ) + 0.5, d );
			}

			double trueValue = trueRa.get().getRealDouble();
			Assert.assertEquals(String.format("%s : order %d spline at %f", baseMessage, order, it.getDoublePosition(0)),
					trueValue,
					est.get().getRealDouble(),
					delta );
		}
	}

	public static <T extends RealType<T>> RealRandomAccessible<T> separableImageReal( final T t, final BiConsumer<RealLocalizable,T>... funs1d )
	{
		BiConsumer<RealLocalizable, T> f = new BiConsumer<RealLocalizable,T>()
		{
			@Override
			public void accept( RealLocalizable l, T t )
			{
				T tmp = t.createVariable();
				t.setOne();
				final RealPoint p = new RealPoint( 1 );

				for( int i = 0; i < funs1d.length; i++ )
				{
					p.setPosition( l.getDoublePosition( i ) , 0 );
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

		return new FunctionRealRandomAccessible<T>( funs1d.length, f, s );
	}

	public static <T extends RealType<T>> RandomAccessible<T> separableImage( final T t, final BiConsumer<Localizable,T>... funs1d )
	{
		BiConsumer<Localizable, T> f = new BiConsumer<Localizable,T>()
		{
			@Override
			public void accept( Localizable l, T t )
			{
				T tmp = t.createVariable();
				t.setOne();
				final Point p = new Point( 1 );

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

		return new FunctionRandomAccessible<T>( funs1d.length, f, s );
	}

	public static RandomAccessible<DoubleType> polynomialImg1dC( final double[] coefs )
	{
		return Views.raster( polynomialReal1dC( coefs ));
	}

	public static RealRandomAccessible<DoubleType> polynomialReal1dC( final double[] coefs )
	{
		return new FunctionRealRandomAccessible<>( 1, polynomial1dC( coefs ), DoubleType::new );
	}

	public static RealRandomAccessible<DoubleType> polynomialReal1dZ( final double scale, final double[] zeros )
	{
		return new FunctionRealRandomAccessible<>( 1, polynomial1dZ( scale, zeros ), DoubleType::new );
	}

	/**
	 * Generate a polynomial with zeros at the specified locations, and scaled by the specified amount.
	 * 
	 * The polynomial is therefore:
	 * 	  s * (x - z[0]) * (x - z[1]) * ... * (x - z[N-1])
	 * 
	 * @param scale the scale (s)
	 * @param zeros the zeros (z)
	 * @return
	 */
	public static BiConsumer< RealLocalizable, DoubleType > polynomial1dZ( final double scale, final double[] zeros )
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
					v.set( scale * total );
				}
			};
	}

	/**
	 * Return a polynomial with the specified coefficients.  The coefficient at index i of the array
	 * is coefficient for order i.  
	 * 
	 * The polynomial is therefore:
	 * 	  c[0] + c[1]x + c[2]*x^2 + ... + c[N-1] * x^(N-1)
	 * 
	 * @param coefs the coefficients 
	 * @return the polynomial function
	 */
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
