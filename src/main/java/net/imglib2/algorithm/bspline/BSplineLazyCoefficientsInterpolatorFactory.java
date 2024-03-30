/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.algorithm.lazy.Lazy;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValue;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class BSplineLazyCoefficientsInterpolatorFactory<T extends RealType<T>, S extends RealType<S> & NativeType<S>> implements InterpolatorFactory< S, RandomAccessible< T > >
{
	protected final int order;

	protected final boolean clipping;

	protected final Interval interval;

	protected RandomAccessibleInterval< S > coefficientStorage;

	protected RandomAccessible< S > coefficientAccess;

	protected S coefficientType;

	protected final OutOfBoundsFactory< ?, ? > oobFactory;

	protected int[] blockSize;

	/**
	 * 
	 * Creates a new {@link BSplineLazyCoefficientsInterpolatorFactory}.
	 * 
	 * @param img
	 *            the image to interpoloate
	 * @param interval
	 *            the interval over which to interpolate
	 * @param order
	 *            the order of the bspline
	 * @param clipping
	 *            clip to the range of the {@link Type} if wanted
	 * @param coefficientType
	 *            the data type used to store coefficiceints
	 * @param blockSize
	 *            size of blocks
	 * @param oobFactory
	 *            out of bounds factory
	 */
	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, 
			final int order, final boolean clipping, S coefficientType,
			final int[] blockSize, final OutOfBoundsFactory<? extends RealType<?>,?> oobFactory )
	{
		this.order = order;
		this.clipping = clipping;
		this.interval = interval;
		this.blockSize = blockSize;
		this.coefficientType = coefficientType;
		this.oobFactory = oobFactory;

		ExtendedRandomAccessibleInterval<T, IntervalView<T>> extendedImg = Views.extend( 
				Views.interval( img, interval ), 
				((OutOfBoundsFactory<T,RandomAccessibleInterval<T>>)oobFactory));

		long[] min = Intervals.minAsLongArray( interval );
		BSplineDecomposition<T,S> decomp = new BSplineDecomposition<>( Views.translateInverse( extendedImg, min ));
		LazyCellImgFactory<T,S> factory = new LazyCellImgFactory<T,S>( decomp, interval, blockSize, coefficientType );

		if( Arrays.stream( min ).allMatch( x -> x == 0 ) )
		{
			coefficientStorage = factory.create( interval );
		}
		else
		{
			Img<S> coefficientsBase = factory.create( interval );
			coefficientStorage = Views.translate( coefficientsBase, min );
		}

		coefficientAccess = Views.extend( coefficientStorage,
				(OutOfBoundsFactory<S,RandomAccessibleInterval<S>>)oobFactory);
	}

	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval,
			final int order, final boolean clipping, S coefficientType,
			final int[] blockSize )
	{
		this( img, interval, order, clipping, coefficientType, blockSize, new OutOfBoundsConstantValueFactory<>( new DoubleType() ));
	}

	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img,
			final int order, final boolean clipping, S coefficientType,
			final int[] blockSize,  final OutOfBoundsFactory<? extends RealType<?>,?> oobFactory )
	{
		this.order = order;
		this.clipping = clipping;
		this.blockSize = blockSize;
		this.coefficientType = coefficientType;
		this.oobFactory = oobFactory;

		// Cache and compute coefficients over a "nearly infinite" range
		// how big can we make this?
		long[] min = new long[ img.numDimensions() ];
		Arrays.fill( min, -Integer.MAX_VALUE / 1000 );

		long[] max = new long[ img.numDimensions() ];
		Arrays.fill( max, Integer.MAX_VALUE / 1000 );

		this.interval = new FinalInterval( min, max );

		BSplineDecomposition<T,S> decomp = new BSplineDecomposition<>( Views.translateInverse( img, min ));
		LazyCellImgFactory<T,S> factory = new LazyCellImgFactory<T,S>( decomp, interval, blockSize, coefficientType );

		Img<S> coefficientsBase = factory.create( interval );
		coefficientStorage = Views.translate( coefficientsBase, min );

		coefficientAccess = Views.extendZero( coefficientStorage );
	}

	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img,
			final int order, final boolean clipping, S coefficientType,
			final int[] blockSize )
	{
		this( img, order, clipping, coefficientType, blockSize, new OutOfBoundsConstantValueFactory<>( new DoubleType() ));
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int order, final boolean clipping,
			final int[] blockSize )
	{
		this( img, interval, order, clipping, (S)new DoubleType(), blockSize );
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int order,
			final int[] blockSize )
	{
		this( img, interval, order, true, (S)new DoubleType(), blockSize );
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessible<T> img, final Interval interval, final int[] blockSize )
	{
		this( img, interval, 3, true, (S)new DoubleType(), blockSize );
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img, final int[] blockSize )
	{
		this( img, img, 3, true, (S)new DoubleType(), blockSize );
	}

	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img,  final int order, final boolean clipping,
			S coefficientType, final int[] blockSize )
	{
		this( img, img, order, clipping, blockSize );
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessibleInterval<T> img, final int order, final boolean clipping,
			final int[] blockSize )
	{
		this( img, img, order, clipping, (S)new DoubleType(), blockSize );
	}

	@SuppressWarnings("unchecked")
	public BSplineLazyCoefficientsInterpolatorFactory( final RandomAccessibleInterval< T > img, final int order, final int[] blockSize )
	{
		this( img, img, order, true, ( S ) new DoubleType(), blockSize );
	}

	@Override
	public RealRandomAccess< S > create( RandomAccessible< T > f )
	{
		return BSplineCoefficientsInterpolator.build( order, coefficientAccess, coefficientType.copy() );
	}

	@Override
	public RealRandomAccess< S > create( RandomAccessible< T > f, RealInterval interval )
	{
		return create( f );
	}

	/**
	 * Returns an image of coefficients that is lazily evaluated and cached.
	 */
	public static class LazyCellImgFactory< T extends RealType< T >, S extends RealType< S > & NativeType< S > > extends ImgFactory< S >
	{
		private int[] blockSize;

		private S type;

		private Interval interval;

		private BSplineDecomposition< T, S > decomposition;

		public LazyCellImgFactory( final int order, final RandomAccessibleInterval< T > img, final int[] blockSize, final S type )
		{
			super( type );
			this.blockSize = blockSize;
			this.type = type;
			this.interval = img;
			this.decomposition = new BSplineDecomposition< T, S >( order, img );
		}

		public LazyCellImgFactory( final BSplineDecomposition< T, S > decomposition, final Interval interval, final int[] blockSize, final S type )
		{
			super( type );
			this.blockSize = blockSize;
			this.type = type;
			this.interval = interval;
			this.decomposition = decomposition;
		}

		@Override
		public Img< S > create( long... dimensions )
		{
			return create( dimensions, type );
		}

		@SuppressWarnings( { "rawtypes", "unchecked" } )
		@Override
		public < R > ImgFactory< R > imgFactory( R type ) throws IncompatibleTypeException
		{
			if ( type instanceof RealType && type instanceof NativeType )
				return new LazyCellImgFactory( decomposition, interval, blockSize, ( RealType ) type );
			else
				throw new IncompatibleTypeException( this, "type must be RealType and NativeType" );
		}

		@Override
		public Img< S > create( long[] dim, S type )
		{
			return Lazy.generate( interval, blockSize, type, AccessFlags.setOf( AccessFlags.VOLATILE ), decomposition );
		}
	}

}
