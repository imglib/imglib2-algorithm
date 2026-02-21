/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;

import java.util.Arrays;

/**
 * Utility methods for creating displacement fields from {@code RealTransform}.
 */
public final class DisplacementFields
{
	/**
	 * Sample the given transform into a {@link DisplacementField} that can be
	 * used for interpolated lookup into a source image {@code BlockSupplier}
	 * with the {@link DisplacementFieldTransform#displacementFieldAffine
	 * displacementFieldAffine} block operator.
	 * <p>
	 * The {@code transform} is from target to source image coordinates.
	 * <p>
	 * The {@code transform} is sampled in the given {@code interval} with the
	 * given {@code spacing} between sample points.
	 *
	 * @param transform
	 * 		the transform to sample
	 * @param interval
	 * 		the interval in which to sample coordinates
	 * @param spacing
	 * 		the spacing between samples
	 *
	 * @return the (zero-min) {@code DisplacementField} and the {@code
	 * transformFromField} that turns displacement field coordinates into target
	 * coordinates (scaling by {@code spacing} and shifting to the min of {@code
	 * interval}).
	 */
	public static TransformedDisplacementField< DoubleType > sample(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing )
	{
		return sample( transform, interval, spacing, new DoubleType() );
	}

	/**
	 * Sample the given transform into a {@link DisplacementField} that can be
	 * used for interpolated lookup into a source image {@code BlockSupplier}
	 * with the {@link DisplacementFieldTransform#displacementFieldAffine
	 * displacementFieldAffine} block operator.
	 * <p>
	 * The {@code transform} is from target to source image coordinates.
	 * <p>
	 * The {@code transform} is sampled in the given {@code interval} with the
	 * given {@code spacing} between sample points.
	 *
	 * @param transform
	 * 		the transform to sample
	 * @param interval
	 * 		the interval in which to sample coordinates
	 * @param spacing
	 * 		the spacing between samples
	 * @param type
	 * 		displacement component type. should be {@code DoubleType} or {@code FloatType}.
	 * @param <D>
	 * 		displacement component type. should be {@code DoubleType} or {@code FloatType}.
	 *
	 * @return the (zero-min) {@code DisplacementField} and the {@code
	 * transformFromField} that turns displacement field coordinates into target
	 * coordinates (scaling by {@code spacing} and shifting to the min of {@code
	 * interval}).
	 */
	public static < D extends NativeType< D > & RealType< D > > TransformedDisplacementField< D > sample(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final D type )
	{
		final int n = transform.numSourceDimensions();
		if ( transform.numTargetDimensions() != n || interval.numDimensions() != n || spacing.length != n )
			throw new IllegalArgumentException( "Dimension do not match" );
		if ( n < 2 || n > 3 )
			throw new IllegalArgumentException( "Only 2D and 3D transforms are supported currently" );

		// size of sample grid over which to rasterize the TPS
		final long[] gridSize = new long[ n ];
		Arrays.setAll( gridSize, d -> ( long ) Math.ceil( interval.dimension( d ) / spacing[ d ] ) );

		final double[] offset = interval.minAsDoubleArray();
		final RandomAccessibleInterval< D > dfieldImg = createNormalized( transform, FinalDimensions.wrap( gridSize ), spacing, offset, type );

		final AffineTransform3D transformFromField = new AffineTransform3D();
		transformFromField.set(
				spacing[ 0 ], 0, 0, offset[ 0 ],
				0, spacing[ 1 ], 0, offset[ 1 ],
				0, 0, spacing[ 2 ], offset[ 2 ] );

		final DisplacementField< D > dfield = new DisplacementField<>( BlockSupplier.of( dfieldImg ), spacing, offset );

		return new TransformedDisplacementField<>( transformFromField, dfield );
	}

	/**
	 * A (zero-min) {@code DisplacementField} and the affine transform that
	 * turns displacement field coordinates into target coordinates.
	 *
	 * @param <D>
	 * 		displacement field type
	 */
	public static class TransformedDisplacementField< D extends NativeType< D > & RealType< D > >
	{
		/**
		 * A 2D or 3D affine transform from {@code dfield} coordinates to target coordinates.
		 */
		private final AffineGet transformFromField;

		/**
		 * Zero-min (normalized) displacement field.
		 */
		private final DisplacementField< D > dfield;

		public TransformedDisplacementField(
				final AffineGet transformFromField,
				final DisplacementField< D > dfield )
		{
			this.transformFromField = transformFromField;
			this.dfield = dfield;
		}

		public AffineGet transformFromField()
		{
			return transformFromField;
		}

		public DisplacementField< D > displacementField()
		{
			return dfield;
		}
	}

	/**
	 * Sample the given {@link RealTransform} to create a normalized
	 * displacement field.
	 * <p>
	 * A "normalized" field expresses displacements in units relative to the
	 * displacement grid's own pixel spacing. Consequently, downsampling the
	 * grid by a factor of N requires scaling the displacement vectors by 1/N to
	 * maintain normalization.
	 * <p>
	 * Components of the displacements are in the 0th dimension, the extents of
	 * the field are given by {@code dimensions}. The output interval will
	 * therefore be of size: <br> [ transform.numTargetDimensions(),
	 * interval.dimension(0), ..., interval.dimension( N-1 )]
	 *
	 * @param transform
	 * 		the transform to sample
	 * @param dimensions
	 * 		dimensions of the displacement field to create (dimension 0 for the vector components will be prepended)
	 * @param spacing
	 * 		spacing of the samples
	 * @param offset
	 * 		offset of the samples
	 * @param type
	 * 		displacement component type. should be {@code DoubleType} or {@code FloatType}.
	 * @param <D>
	 * 		displacement component type. should be {@code DoubleType} or {@code FloatType}.
	 *
	 * @return the displacement field
	 */
	public static < D extends NativeType< D > & RealType< D > > RandomAccessibleInterval< D > createNormalized(
			final RealTransform transform,
			final Dimensions dimensions,
			final double[] spacing,
			final double[] offset,
			final D type )
	{
		final int n = dimensions.numDimensions();
		final long dataSize = n * Intervals.numElements( dimensions );
		if ( dataSize > Integer.MAX_VALUE - 8 )
			throw new IllegalArgumentException( "requested interval is too large to sample into an ArrayImg" );

		final int[] gridSize = new int[ n ];
		final long[] dfieldSize = new long[ n + 1 ];
		for ( int d = 0; d < n; d++ )
			dfieldSize[ d + 1 ] = gridSize[ d ] = ( int ) dimensions.dimension( d );
		dfieldSize[ 0 ] = n;
		final double[] data = new Sample( transform, spacing, offset, gridSize ).data;

		if ( type instanceof DoubleType )
			return Cast.unchecked( ArrayImgs.doubles( data, dfieldSize ) );
		else if ( type instanceof FloatType )
		{
			final float[] fdata = new float[ data.length ];
			for ( int i = 0; i < data.length; ++i )
				fdata[ i ] = ( float ) data[ i ];
			return Cast.unchecked( ArrayImgs.floats( fdata, dfieldSize ) );
		}
		else
			throw new IllegalArgumentException();
	}

	private static final class Sample
	{
		private final RealTransform transform;
		private final double[] spacing;
		private final double[] offset;
		private final int[] gridSize;
		private final int[] gridStride;
		private final int n;
		private final double[] data;
		private final double[] p;
		private final double[] q;

		private Sample(
				final RealTransform transform,
				final double[] spacing,
				final double[] offset,
				final int[] gridSize )
		{
			this.transform = transform;
			this.spacing = spacing;
			this.offset = offset;
			this.gridSize = gridSize;
			n = gridSize.length;
			gridStride = IntervalIndexer.createAllocationSteps( gridSize );
			Arrays.setAll( gridStride, d -> n * gridStride[ d ] );
			data = new double[ gridStride[ n - 1 ] * gridSize[ n - 1 ] ];
			p = new double[ n ];
			q = new double[ n ];
			sample( 0, n - 1 );
		}

		private void sample( final int data_offset, final int d )
		{
			final double o = offset[ d ];
			final double s = spacing[ d ];
			final int l = gridSize[ d ];
			final int stride = gridStride[ d ];
			if ( d == 0 )
			{
				for ( int x = 0; x < l; ++x )
				{
					p[ d ] = o + s * x;
					transform.apply( p, q );
					for ( int i = 0; i < n; ++i )
						data[ data_offset + x * stride + i ] = ( q[ i ] - p[ i ] ) / spacing[ i ];
				}
			}
			else
			{
				for ( int x = 0; x < l; ++x )
				{
					p[ d ] = o + s * x;
					sample( data_offset + x * stride, d - 1 );
				}
			}
		}
	}

	private DisplacementFields()
	{
		// utility class, don't instantiate
	}
}
