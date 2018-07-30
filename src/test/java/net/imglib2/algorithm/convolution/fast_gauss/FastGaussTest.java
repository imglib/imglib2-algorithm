package net.imglib2.algorithm.convolution.fast_gauss;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FastGaussTest
{

	public static < S extends ComplexType< S >, T extends ComplexType< T > > double psnr(
			RandomAccessibleInterval< S > expected, RandomAccessibleInterval< T > actual )
	{
		double meanSquareError = meanSquareError( expected, actual );
		if ( meanSquareError == 0.0 )
			return Float.POSITIVE_INFINITY;
		return ( 20 * Math.log10( max( expected ) ) - 10 * Math.log10( meanSquareError ) );
	}

	private static < S extends ComplexType< S >, T extends ComplexType< T > > double meanSquareError(
			RandomAccessibleInterval< S > a, RandomAccessibleInterval< T > b )
	{
		DoubleType sum = new DoubleType( 0.0f );
		Views.interval( Views.pair( a, b ), a ).forEach( x -> sum.set( sum.get() + sqr( x.getA().getRealDouble() - x.getB().getRealDouble() ) ) );
		return sum.get() / Intervals.numElements( a );
	}

	private static < T extends ComplexType< T > > double max( RandomAccessibleInterval< T > a )
	{
		IntervalView< T > interval = Views.interval( a, a );
		T result = interval.firstElement().createVariable();
		interval.forEach( x -> result.setReal( Math.max( result.getRealDouble(), x.getRealDouble() ) ) );
		return result.getRealDouble();
	}

	private static double sqr( double v )
	{
		return v * v;
	}

	@Test
	public void test()
	{
		long[] dimensions = { 100, 100, 100 };
		long[] peak = new long[] { 50, 50, 50 };
		double sigma = 5.0;
		Img< DoubleType > img = dirac( peak, dimensions );
		FastGauss.convolve( sigma, Views.extendBorder( img ), img );
		RandomAccessibleInterval< DoubleType > expected = gaussDistribution( sigma, peak, dimensions );
		assertTrue( psnr( expected, img ) > 70 );
	}

	private static Img< DoubleType > dirac( long[] peak, long[] dimensions )
	{
		ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( dimensions );
		final RandomAccess< ? extends RealType< ? > > ra = img.randomAccess();
		ra.setPosition( peak );
		ra.get().setReal( 1 );
		return img;
	}

	private static RandomAccessibleInterval< DoubleType > gaussDistribution( double sigma, long[] peak, long[] dimensions )
	{
		FunctionRandomAccessible< DoubleType > ra = new FunctionRandomAccessible< DoubleType >( 3, ( position, out ) -> {
			long x = position.getIntPosition( 0 ) - peak[ 0 ];
			long y = position.getIntPosition( 1 ) - peak[ 1 ];
			long z = position.getIntPosition( 2 ) - peak[ 2 ];
			out.setReal( Math.exp( -0.5 / sigma / sigma * ( x * x + y * y + z * z ) ) / Math.pow( 2 * Math.PI * sigma * sigma, 3 * 0.5 ) );
		}, DoubleType::new );
		return Views.interval( ra, new FinalInterval( dimensions ) );
	}

}
