package net.imglib2.algorithm.metrics.imagequality;

import java.util.Arrays;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

public class MSE
{
	public static < T extends RealType< T > > double computeMetrics( final RandomAccessibleInterval< T > reference, final RandomAccessibleInterval< T > processed )
	{
		if ( !Arrays.equals( reference.dimensionsAsLongArray(), processed.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// get image range
		final long nPixels = Arrays.stream( reference.dimensionsAsLongArray() ).reduce( 1, ( a, b ) -> a * b );

		// convert to float
		final RandomAccessibleInterval< DoubleType > refAsDouble = Converters.convert( reference, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );
		final RandomAccessibleInterval< DoubleType > procAsDouble = Converters.convert( processed, ( i, o ) -> o.set( i.getRealDouble() ), new DoubleType() );

		double mse = 0.;
		final Cursor< DoubleType > cu = Views.iterable( refAsDouble ).localizingCursor();
		final RandomAccess< DoubleType > ra = procAsDouble.randomAccess();
		while ( cu.hasNext() )
		{
			double dRef = cu.next().getRealDouble();

			ra.setPosition( cu );
			double dProc = ra.get().getRealDouble();

			mse += ( dRef - dProc ) * ( dRef - dProc );
		}

		return mse / nPixels;
	}
}
