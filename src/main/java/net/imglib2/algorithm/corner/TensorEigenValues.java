package net.imglib2.algorithm.corner;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

public class TensorEigenValues
{

	public interface EigenValues
	{
		default public < T extends RealType< T >, U extends RealType< U > > void compute( final RealComposite< T > matrix, final RealComposite< U > evs )
		{
			throw new UnsupportedOperationException( "EigenValues not implemented yet!" );
		}
	}

	public static class EigenValuesScalar implements EigenValues
	{
		@Override
		public < T extends RealType< T >, U extends RealType< U > > void compute( final RealComposite< T > tensor, final RealComposite< U > evs )
		{
			evs.get( 0 ).setReal( tensor.get( 0 ).getRealDouble() );
		}
	}

	public static class EigenValuesSquare2D implements EigenValues
	{
		@Override
		public < K extends RealType< K >, L extends RealType< L > > void compute( final RealComposite< K > tensor, final RealComposite< L > evs )
		{
			final double x11 = tensor.get( 0 ).getRealDouble();
			final double x12 = tensor.get( 1 ).getRealDouble();
			final double x21 = tensor.get( 2 ).getRealDouble();
			final double x22 = tensor.get( 3 ).getRealDouble();
			final double sum = x11 + x22;
			final double diff = x11 - x22;
			final double sqrt = Math.sqrt( 4 * x12 * x21 + diff * diff );
			evs.get( 0 ).setReal( 0.5 * ( sum + sqrt ) );
			evs.get( 1 ).setReal( 0.5 * ( sum - sqrt ) );
		}
	}

	public static class EigenValuesSymmetric2D implements EigenValues
	{
		@Override
		public < K extends RealType< K >, L extends RealType< L > > void compute( final RealComposite< K > tensor, final RealComposite< L > evs )
		{
			final double x11 = tensor.get( 0 ).getRealDouble();
			final double x12 = tensor.get( 1 ).getRealDouble();
			final double x22 = tensor.get( 2 ).getRealDouble();
			final double sum = x11 + x22;
			final double diff = x11 - x22;
			final double sqrt = Math.sqrt( 4 * x12 * x12 + diff * diff );
			evs.get( 0 ).setReal( 0.5 * ( sum + sqrt ) );
			evs.get( 1 ).setReal( 0.5 * ( sum - sqrt ) );
		}
	}

	public static class EigenValuesSquare implements EigenValues
	{
		private final int nDim;

		public EigenValuesSquare( final int nDim )
		{
			super();
			this.nDim = nDim;
		}

		@Override
		public < K extends RealType< K >, L extends RealType< L > > void compute( final RealComposite< K > tensor, final RealComposite< L > evs )
		{
			final int nImageDim = nDim - 1;
			final RealCompositeSquareMatrix< K > m = new RealCompositeSquareMatrix<>( tensor, nImageDim );
			final EigenDecomposition ed = new EigenDecomposition( m );
			final double[] evArray = ed.getRealEigenvalues();
			for ( int z = 0; z < evArray.length; ++z )
			{
				evs.get( z ).setReal( evArray[ z ] );
			}
		}
	}

	public static class EigenValuesSymmetric implements EigenValues
	{
		private final int nDim;

		public EigenValuesSymmetric( final int nDim )
		{
			super();
			this.nDim = nDim;
		}


		@Override
		public < K extends RealType< K >, L extends RealType< L > > void compute( final RealComposite< K > tensor, final RealComposite< L > evs )
		{
			final int nImageDim = nDim - 1;
			final RealCompositeSymmetricMatrix< K > m = new RealCompositeSymmetricMatrix<>( tensor, nImageDim );
			final EigenDecomposition ed = new EigenDecomposition( m );
			final double[] evArray = ed.getRealEigenvalues();
			for ( int z = 0; z < evArray.length; ++z )
			{
				evs.get( z ).setReal( evArray[ z ] );
			}
		}
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateEigenValuesSymmetric(
			final RandomAccessibleInterval< T > tensor,
			final ImgFactory< U > factory,
			final U u )
	{

		final Img< U > eigenvalues = createAppropriateResultImg( tensor, factory, u );

		calculateEigenValuesSymmetric( tensor, eigenvalues );

		return eigenvalues;

	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateEigenValuesSquare(
			final RandomAccessibleInterval< T > tensor,
			final ImgFactory< U > factory,
			final U u )
	{

		final Img< U > eigenvalues = createAppropriateResultImg( tensor, factory, u );

		calculateEigenValuesSquare( tensor, eigenvalues );

		return eigenvalues;

	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateEigenValues(
			final RandomAccessibleInterval< T > tensor,
			final ImgFactory< U > factory,
			final EigenValues ev,
			final U u )
	{
		final Img< U > eigenvalues = createAppropriateResultImg( tensor, factory, u );

		calculateEigenValues( tensor, eigenvalues, ev );

		return eigenvalues;
	}

	public static < T extends RealType< T >, U extends RealType< U > > void calculateEigenValuesSymmetric(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues )
	{

		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * ( eigenvalues.dimension( nDim - 1 ) + 1 ) / 2 == tensor.dimension( nDim - 1 );

		final EigenValues ev;
		if ( nDim == 2 )
		{
			ev = new EigenValuesScalar();
		}
		else if ( nDim == 3 )
		{
			ev = new EigenValuesSymmetric2D();
		}
		else if ( nDim > 3 )
		{
			ev = new EigenValuesSymmetric( nDim );
		}
		else
		{
			ev = new EigenValues()
			{};
		}
		calculateEigenValues( tensor, eigenvalues, ev );
	}

	public static < T extends RealType< T >, U extends RealType< U > > void calculateEigenValuesSquare(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues )
	{
		final int nDim = tensor.numDimensions();
		assert eigenvalues.dimension( nDim - 1 ) * eigenvalues.dimension( nDim - 1 ) == tensor.dimension( nDim - 1 );

		final EigenValues ev;
		if ( nDim == 2 )
		{
			ev = new EigenValuesScalar();
		}
		else if ( nDim == 3 )
		{
			ev = new EigenValuesSquare2D();
		}
		else if ( nDim > 3 )
		{
			ev = new EigenValuesSquare( nDim );
		}
		else
		{
			ev = new EigenValues()
			{};
		}
		calculateEigenValues( tensor, eigenvalues, ev );
	}

	public static < T extends RealType< T >, U extends RealType< U > > void calculateEigenValues(
			final RandomAccessibleInterval< T > tensor,
			final RandomAccessibleInterval< U > eigenvalues,
			final EigenValues ev )
	{
		final Cursor< RealComposite< T > > m = Views.iterable( Views.collapseReal( tensor ) ).cursor();
		final Cursor< RealComposite< U > > e = Views.iterable( Views.collapseReal( eigenvalues ) ).cursor();
		while ( m.hasNext() )
		{
			ev.compute( m.next(), e.next() );
		}
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > createAppropriateResultImg(
			final RandomAccessibleInterval< T > tensor,
			final ImgFactory< U > factory,
			final U u )
	{
		final int nDim = tensor.numDimensions();
		final long[] dimensions = new long[ nDim ];
		tensor.dimensions( dimensions );
		dimensions[ nDim - 1 ] = nDim - 1;
		return factory.create( dimensions, u );
	}


}
