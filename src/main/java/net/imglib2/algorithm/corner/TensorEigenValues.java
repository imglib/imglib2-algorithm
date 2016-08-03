package net.imglib2.algorithm.corner;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

public class TensorEigenValues< T extends RealType< T > > implements RandomAccessibleInterval< DoubleType >
{

	private interface EigenValues
	{
		default public void compute( final RealComposite< DoubleType > matrix, final RealComposite< DoubleType > evs )
		{
			throw new UnsupportedOperationException( "EigenValues not implemented yet!" );
		}
	}

	private final HessianMatrix< T > matrix;

	private final int nDim;

	private final long[] dimensions;

	private final Img< DoubleType > eigenvalues;

	private final EigenValues ev;

	private final void update()
	{
		final Cursor< RealComposite< DoubleType > > m = Views.iterable( Views.collapseReal( matrix ) ).cursor();
		final Cursor< RealComposite< DoubleType > > e = Views.iterable( Views.collapseReal( eigenvalues ) ).cursor();
		while ( m.hasNext() )
		{
			ev.compute( m.next(), e.next() );
		}
	}

	public TensorEigenValues( final HessianMatrix< T > matrix, final ImgFactory< DoubleType > factory )
	{
		super();
		this.matrix = matrix;
		this.nDim = matrix.numDimensions();
		this.dimensions = new long[ this.nDim ];
		matrix.dimensions( this.dimensions );
		this.dimensions[ this.nDim - 1 ] = this.nDim - 1;
		this.eigenvalues = factory.create( this.dimensions, new DoubleType() );
		if ( this.nDim == 3 )
		{
			ev = new EigenValues()
			{
				@Override
				public void compute( final RealComposite< DoubleType > matrix, final RealComposite< DoubleType > evs )
				{
					final double x11 = matrix.get( 0 ).get();
					final double x12 = matrix.get( 1 ).get();
					final double x22 = matrix.get( 2 ).get();
					final double sum = x11 + x22;
					final double diff = x11 - x22;
					final double sqrt = Math.sqrt( 4 * x12 * x12 + diff * diff );
					evs.get( 0 ).set( 0.5 * ( sum + sqrt ) );
					evs.get( 1 ).set( 0.5 * ( sum - sqrt ) );
				}
			};
		}
		else if ( this.nDim > 3 )
		{
			ev = new EigenValues()
			{
				@Override
				public void compute( final RealComposite< DoubleType > matrix, final RealComposite< DoubleType > evs )
				{
					final int nImageDim = nDim - 1;
					final RealCompositeSymmetricMatrix< DoubleType > m = new RealCompositeSymmetricMatrix<>( matrix, nImageDim );
					final EigenDecomposition ed = new EigenDecomposition( m );
					final double[] evArray = ed.getRealEigenvalues();
					for ( int z = 0; z < evArray.length; ++z )
					{
						evs.get( z ).set( evArray[z]  );
					}
				}
			};
		}
		else
		{
			ev = new EigenValues()
			{};
		}
		update();
	}

	@Override
	public RandomAccess< DoubleType > randomAccess()
	{
		return this.eigenvalues.randomAccess();
	}

	@Override
	public RandomAccess< DoubleType > randomAccess( final Interval interval )
	{
		return randomAccess();
	}

	@Override
	public int numDimensions()
	{
		return this.nDim;
	}

	@Override
	public long min( final int d )
	{
		return eigenvalues.min( d );
	}

	@Override
	public void min( final long[] min )
	{
		eigenvalues.min( min );
	}

	@Override
	public void min( final Positionable min )
	{
		eigenvalues.min( min );
	}

	@Override
	public long max( final int d )
	{
		return eigenvalues.max( d );
	}

	@Override
	public void max( final long[] max )
	{
		eigenvalues.max( max );
	}

	@Override
	public void max( final Positionable max )
	{
		eigenvalues.max( max );
	}

	@Override
	public double realMin( final int d )
	{
		return eigenvalues.realMax( d );
	}

	@Override
	public void realMin( final double[] min )
	{
		eigenvalues.realMin( min );
	}

	@Override
	public void realMin( final RealPositionable min )
	{
		eigenvalues.realMin( min );
	}

	@Override
	public double realMax( final int d )
	{
		return eigenvalues.realMax( d );
	}

	@Override
	public void realMax( final double[] max )
	{
		eigenvalues.realMax( max );
	}

	@Override
	public void realMax( final RealPositionable max )
	{
		eigenvalues.realMax( max );
	}

	@Override
	public void dimensions( final long[] dimensions )
	{
		System.arraycopy( this.dimensions, 0, dimensions, 0, this.nDim );

	}

	@Override
	public long dimension( final int d )
	{
		return this.dimensions[ d ];
	}

}
