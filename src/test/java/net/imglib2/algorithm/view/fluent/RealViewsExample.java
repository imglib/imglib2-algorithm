package net.imglib2.algorithm.view.fluent;

import static net.imglib2.algorithm.view.fluent.RealViewsExample.Affine2D.affine2D;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.fluent.RaView;
import net.imglib2.view.fluent.RaView.Interpolation;
import net.imglib2.view.fluent.RaiView.Extension;
import net.imglib2.view.fluent.RraView;

public class RealViewsExample
{

	public static void main( String[] args )
	{
		RandomAccessibleInterval< IntType > img = ArrayImgs.ints( 100, 100 );

		// Arbitrary functions can be passed to RraView.apply().
		//
		// Here we create a Function that transforms a RealRandomAccessible with
		// a AffineTransform2D (using RealViews.affine) and returns a RaView of the result.
		AffineTransform2D affineTransform2D = new AffineTransform2D();
		affineTransform2D.scale( 1.1 );
		affineTransform2D.translate( 2.1, 1.3 );
		Function< RealRandomAccessible< IntType >, RaView< IntType, ? > > transform = rra ->
				RaView.wrap( RealViews.affine( rra, affineTransform2D ) );

		// We can use that in RraView.apply(). Our function returns a RaView, so
		// we can keep chaining more methods on the result.
		RandomAccessibleInterval< IntType > affine1 = img.view()
				.extend( Extension.border() )
				.interpolate( Interpolation.nLinear() )
				.apply( transform )
				.interval( img );


		// --------------------------------------------------------------------


		// We can think about creating "function factories" like the Affine2D
		// prototype below.
		//
		// The above can then be written like this:
		RandomAccessibleInterval< IntType > affine2 = img.view()
				.extend( Extension.border() )
				.interpolate( Interpolation.nLinear() )
				.apply( affine2D().scale( 1.1 ).translate( 2, 1.3 ).transform() )
				.interval( img );
		// Here affine2D creates a builder.
		// scale() and translate() concatenate transformations.
		// Finally, transform() creates a function to be used in apply().


		// --------------------------------------------------------------------


		// transformReal() creates another function to be used in apply() This
		// one uses RealViews.affineReal, so the result is a RraView instead of
		// a RaView.
		RealRandomAccessible< IntType > affine3 = img.view()
				.extend( Extension.border() )
				.interpolate( Interpolation.nLinear() )
				.apply( affine2D().scale( 1.1 ).translate( 2, 1.3 ).transformReal() );
	}


	public static class Affine2D
	{
		private final AffineTransform2D transform;

		public Affine2D( AffineTransform2D transform )
		{
			this.transform = transform;
		}

		public static Affine2D affine2D()
		{
			return new Affine2D( new AffineTransform2D() );
		}

		public Affine2D translate( double... txy )
		{
			final AffineTransform2D t = transform.copy();
			t.translate( txy );
			return new Affine2D( t );
		}

		public Affine2D rotate( double d )
		{
			final AffineTransform2D t = transform.copy();
			t.rotate( d );
			return new Affine2D( t );
		}

		public Affine2D scale( double s )
		{
			final AffineTransform2D t = transform.copy();
			t.scale( s );
			return new Affine2D( t );
		}

		public < T >  Function< RealRandomAccessible< T >, RaView< T, ? > > transform()
		{
			return rra -> RaView.wrap( RealViews.affine( rra, transform ) );
		}

		public < T >  Function< RealRandomAccessible< T >, RraView< T > > transformReal()
		{
			return rra -> RraView.wrap( RealViews.affineReal( rra, transform ) );
		}
	}
}
