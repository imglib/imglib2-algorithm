package net.imglib2.algorithm.corner;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class HessianMatrix< T extends RealType< T > > implements RandomAccessibleInterval< DoubleType >
{
	private final double[] sigma;

	private final RandomAccessible< T > source;

	private final Interval interval;

	final OutOfBoundsFactory< DoubleType, ? super RandomAccessibleInterval< DoubleType > > outOfBounds;

	private final Img< DoubleType > gaussianConvolved;

	private final Img< DoubleType > gradient;

	private final Img< DoubleType > hessianMatrix;

	private final int nDim;

	private final int nTargetDim;

	private final long[] dimensions;

	private final long[] min;

	private final long[] max;

	public Img< DoubleType > getGaussianConvolved()
	{
		return this.gaussianConvolved;
	}

	public Img< DoubleType > getGradient()
	{
		return this.gradient;
	}

	private void update() throws IncompatibleTypeException
	{
		Gauss3.gauss( this.sigma, this.source, gaussianConvolved );
		for ( long d = 0; d < this.nDim; ++d )
		{
			PartialDerivative.gradientCentralDifference2( Views.extend( gaussianConvolved, this.outOfBounds ), Views.hyperSlice( gradient, this.nDim, d ), ( int ) d );
		}

		int count = 0;
		for ( long d1 = 0; d1 < this.nDim; ++d1 )
		{
			final IntervalView< DoubleType > hs1 = Views.hyperSlice( gradient, this.nDim, d1 );
			for ( long d2 = d1; d2 < this.nDim; ++d2 )
			{
				final IntervalView< DoubleType > hs2 = Views.hyperSlice( hessianMatrix, this.nDim, count );
				PartialDerivative.gradientCentralDifference2( Views.extend( hs1, this.outOfBounds ), hs2, ( int ) d2 );
				++count;
			}
		}
	}

	public void setSigma( final double[] sigma ) throws IncompatibleTypeException
	{
		boolean changed = false;
		for ( int d = 0; d < this.nDim; ++d )
		{
			if ( this.sigma[d] != sigma[d] )
			{
				changed = true;
				this.sigma[d] = sigma[d];
			}
		}
		if ( changed )
		{
			update();
		}
	}

	public void setSigma( final double sigma ) throws IncompatibleTypeException
	{
		boolean changed = false;
		for ( int d = 0; d < this.nDim; ++d )
		{
			if ( this.sigma[ d ] != sigma )
			{
				changed = true;
				this.sigma[ d ] = sigma;
			}
		}
		if ( changed )
		{
			update();
		}
	}

	public static double[] createSigmaArray( final int length, final double sigma )
	{
		final double[] sigmas = new double[ length ];
		Arrays.fill( sigmas, sigma );
		return sigmas;
	}

	public HessianMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double sigma,
			final OutOfBoundsFactory< DoubleType, ? super RandomAccessibleInterval< DoubleType > > outOfBounds,
					final ImgFactory< DoubleType > factory ) throws IncompatibleTypeException
	{
		this( source, interval, createSigmaArray( interval.numDimensions(), sigma ), outOfBounds, factory );
	}

	public HessianMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double[] sigma,
			final OutOfBoundsFactory< DoubleType, ? super RandomAccessibleInterval< DoubleType > > outOfBounds,
					final ImgFactory< DoubleType > factory ) throws IncompatibleTypeException
	{
		this.source = source;
		this.interval = interval;
		this.outOfBounds = outOfBounds;

		this.nDim = interval.numDimensions();
		this.sigma = new double[ nDim ];
		System.arraycopy( sigma, 0, this.sigma, 0, this.sigma.length );
		this.nTargetDim = this.nDim + 1;
		this.dimensions = new long[ this.nTargetDim ];
		this.min = new long[ this.nTargetDim ];
		this.max = new long[ this.nTargetDim ];

		for ( int d = 0; d < this.nDim; ++d )
		{
			this.dimensions[ d ] = this.interval.dimension( d );
			this.min[ d ] = this.interval.min( d );
			this.max[ d ] = this.interval.max( d );
		}
		this.dimensions[ this.nDim ] = this.nDim * ( this.nDim + 1 ) / 2;
		this.min[ this.nDim ] = 0;
		this.max[ this.nDim ] = this.dimensions[ this.nDim ] - 1;

		final long[] gradientDim = this.dimensions.clone();
		gradientDim[ this.nDim ] = this.nDim;

		this.gaussianConvolved = factory.create( interval, new DoubleType() );
		this.gradient = factory.create( gradientDim, new DoubleType() );
		this.hessianMatrix = factory.create( this.dimensions, new DoubleType() );

		update();

	}

	@Override
	public RandomAccess< DoubleType > randomAccess()
	{
		return this.hessianMatrix.randomAccess();
	}

	@Override
	public RandomAccess< DoubleType > randomAccess( final Interval interval )
	{
		return randomAccess();
	}

	@Override
	public int numDimensions()
	{
		return this.nTargetDim;
	}

	@Override
	public long min( final int d )
	{
		return this.min[ d ];
	}

	@Override
	public void min( final long[] min )
	{
		System.arraycopy( this.min, 0, min, 0, this.nTargetDim );
	}

	@Override
	public void min( final Positionable min )
	{
		min.setPosition( this.min );
	}

	@Override
	public long max( final int d )
	{
		return this.max[ d ];
	}

	@Override
	public void max( final long[] max )
	{
		System.arraycopy( this.max, 0, max, 0, this.nTargetDim );
	}

	@Override
	public void max( final Positionable max )
	{
		max.setPosition( this.max );
	}

	@Override
	public double realMin( final int d )
	{
		return min( d );
	}

	@Override
	public void realMin( final double[] min )
	{
		for ( int d = 0; d < this.nTargetDim; ++d )
		{
			min[ d ] = this.min[ d ];
		}
	}

	@Override
	public void realMin( final RealPositionable min )
	{
		min.setPosition( this.min );
	}

	@Override
	public double realMax( final int d )
	{
		return max( d );
	}

	@Override
	public void realMax( final double[] max )
	{
		for ( int d = 0; d < this.nTargetDim; ++d )
		{
			max[ d ] = this.max[ d ];
		}
	}

	@Override
	public void realMax( final RealPositionable max )
	{
		max.setPosition( this.max );
	}

	@Override
	public void dimensions( final long[] dimensions )
	{
		System.arraycopy( this.dimensions, 0, dimensions, 0, this.nTargetDim );

	}

	@Override
	public long dimension( final int d )
	{
		return this.dimensions[ d ];
	}

}
