/*
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

import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Cast;

/**
 * Compute a destination X line for 2D.
 * <p>
 * An instance for a given input/output type ({@code double[]} or {@code
 * float[]})can be obtained by {@link #of DispFieldAffine2D.of}.
 * <p>
 * A destination X line can then be computed by {@link #transformLine}, giving starting
 * position and X differential vector.
 *
 * @param <P>
 * 		input/output primitive array type (float[] or double[])
 */
interface DispFieldAffine2D< P >
{

	/**
	 * Compute a destination X line. Interpolate displacements and add sample
	 * positions (starting from {@code (sf0, sf1)} to produce (a line in) the
	 * {@code dest} position field.
	 * <p>
	 * All lengths are counted in full displacement vectors (not individual
	 * float components).
	 *
	 * @param src
	 * 		flattened source data
	 * @param dest
	 * 		flattened dest data
	 * @param offset
	 * 		offset (into {@code dest}) of the line to compute
	 * @param length
	 * 		length of the line to compute (in {@code dest})
	 * @param d0
	 * 		partial differential vector in X of the transform (X component)
	 * @param d1
	 * 		partial differential vector in X of the transform (Y component)
	 * @param ss0
	 * 		length of a source line (size_X)
	 * @param sf0
	 * 		position of the first sample on the line (transformed into source)
	 * @param sf1
	 * 		position of the first sample on the line (transformed into source)
	 */
	void transformLine( P src, P dest, int offset, int length,
			float d0, float d1,
			int ss0,
			float sf0, float sf1 );

	/**
	 * Scale position vectors by the given scale {@code s0, s1}.
	 * <p>
	 * {@code length} is counted in full displacement vectors (not individual
	 * float components).
	 *
	 * @param dest
	 * 		flattened dest data
	 * @param offset
	 * 		offset (into {@code dest}) of the line to compute
	 * @param length
	 * 		length of the line to compute (in {@code dest})
	 * @param s0
	 *      scale factor for X to apply
	 * @param s1
	 *      scale factor for Y to apply
	 */
	void scale( P dest, int offset, int length, double s0, double s1 );

	/**
	 * Compute source bounds: Which image region will be needed to render with
	 * the position vectors in {@code dest}.
	 * <p>
	 * {@code length} is counted in full displacement vectors (not individual
	 * float components).
	 *
	 * @param dest
	 * 		flattened dest data
	 * @param length
	 * 		length of the line to compute (in {@code dest})
	 * @param o0
	 * 		X offset to add to each position vector
	 * @param o1
	 * 		Y offset to add to each position vector
	 * @param interpolation
	 * 		to determine appropriate padding
	 * @param bounds
	 * 		source bounds will be written here
	 */
	void sourceBounds( P dest, int length, double o0, double o1, Interpolation interpolation, final BlockInterval bounds );

	static < P > DispFieldAffine2D< P > of( final PrimitiveType primitiveType )
	{
		switch ( primitiveType )
		{
		case FLOAT:
			return Cast.unchecked( NLinear_float.INSTANCE );
		case DOUBLE:
			return Cast.unchecked( NLinear_double.INSTANCE );
		default:
			throw new IllegalArgumentException();
		}
	}


	class NLinear_float implements DispFieldAffine2D< float[] >
	{
		private NLinear_float()
		{
		}

		static final NLinear_float INSTANCE = new NLinear_float();

		@Override
		public void transformLine( final float[] src, final float[] dest, int offset, final int length,
				final float d0, final float d1,
				final int ss0,
				float sf0, float sf1 )
		{
			final int n = 2;
			final int nss0 = n * ss0;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final float r0 = sf0 - s0;
				final float r1 = sf1 - s1;
				final int o = s1 * nss0 + n * s0;
				// TODO: create benchmark and play with loop unrolling, inlining n, etc...
				{
					final int k = 0;
					final float a00 = src[ k + o ];
					final float a01 = src[ k + o + n ];
					final float a10 = src[ k + o + nss0 ];
					final float a11 = src[ k + o + nss0 + n ];
					final float v0 = a00 + r0 * ( a01 - a00 ) + r1 * ( a10 - a00 + r0 * ( a00 - a10 - a01 + a11 ) );
					dest[ offset++ ] = v0 + sf0;
				}
				{
					final int k = 1;
					final float a00 = src[ k + o ];
					final float a01 = src[ k + o + n ];
					final float a10 = src[ k + o + nss0 ];
					final float a11 = src[ k + o + nss0 + n ];
					final float v1 = a00 + r0 * ( a01 - a00 ) + r1 * ( a10 - a00 + r0 * ( a00 - a10 - a01 + a11 ) );
					dest[ offset++ ] = v1 + sf1;
				}
				sf0 += d0;
				sf1 += d1;
			}
		}

		@Override
		public void scale( final float[] dest, int offset, final int length, final double s0, final double s1 )
		{
			for ( int x = 0; x < length; ++x ) {
				dest[ offset++ ] *= s0;
				dest[ offset++ ] *= s1;
			}
		}

		@Override
		public void sourceBounds( final float[] dest, final int length,
				final double o0, final double o1,
				final Interpolation interpolation, final BlockInterval bounds )
		{
			float min0 = dest[ 0 ], max0 = min0;
			float min1 = dest[ 1 ], max1 = min1;
			for ( int i = 1; i < length; ++i )
			{
				final float v0 = dest[ 2 * i ];
				if ( v0 < min0 )
					min0 = v0;
				else if ( v0 > max0 )
					max0 = v0;
				final float v1 = dest[ 2 * i + 1 ];
				if ( v1 < min1 )
					min1 = v1;
				else if ( v1 > max1 )
					max1 = v1;
			}
			min0 += (float) o0;
			max0 += (float) o0;
			min1 += (float) o1;
			max1 += (float) o1;

			final long[] boundsMin = bounds.min();
			final int[] boundsSize = bounds.size();
			switch ( interpolation )
			{
			case NEARESTNEIGHBOR:
				boundsMin[ 0 ] = Math.round( min0 - 0.5f );
				boundsMin[ 1 ] = Math.round( min1 - 0.5f );
				boundsSize[ 0 ] = ( int ) ( Math.round( max0 + 0.5f ) - boundsMin[ 0 ] ) + 1;
				boundsSize[ 1 ] = ( int ) ( Math.round( max1 + 0.5f ) - boundsMin[ 1 ] ) + 1;
				break;
			case NLINEAR:
				boundsMin[ 0 ] = ( long ) Math.floor( min0 - 0.5f );
				boundsMin[ 1 ] = ( long ) Math.floor( min1 - 0.5f );
				boundsSize[ 0 ] = ( int ) ( ( long ) Math.floor( max0 + 0.5f ) - boundsMin[ 0 ] ) + 2;
				boundsSize[ 1 ] = ( int ) ( ( long ) Math.floor( max1 + 0.5f ) - boundsMin[ 1 ] ) + 2;
				break;
			}
		}
	}

	class NLinear_double implements DispFieldAffine2D< double[] >
	{
		private NLinear_double()
		{
		}

		static final NLinear_double INSTANCE = new NLinear_double();

		@Override
		public void transformLine( final double[] src, final double[] dest, int offset, final int length,
				final float d0, final float d1,
				final int ss0,
				float sf0, float sf1 )
		{
			final int n = 2;
			final int nss0 = n * ss0;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final float r0 = sf0 - s0;
				final float r1 = sf1 - s1;
				final int o = s1 * nss0 + n * s0;
				// TODO: create benchmark and play with loop unrolling, inlining n, etc...
				{
					final int k = 0;
					final double a00 = src[ k + o ];
					final double a01 = src[ k + o + n ];
					final double a10 = src[ k + o + nss0 ];
					final double a11 = src[ k + o + nss0 + n ];
					final double v0 = a00 + r0 * ( a01 - a00 ) + r1 * ( a10 - a00 + r0 * ( a00 - a10 - a01 + a11 ) );
					dest[ offset++ ] = v0 + sf0;
				}
				{
					final int k = 1;
					final double a00 = src[ k + o ];
					final double a01 = src[ k + o + n ];
					final double a10 = src[ k + o + nss0 ];
					final double a11 = src[ k + o + nss0 + n ];
					final double v1 = a00 + r0 * ( a01 - a00 ) + r1 * ( a10 - a00 + r0 * ( a00 - a10 - a01 + a11 ) );
					dest[ offset++ ] = v1 + sf1;
				}
				sf0 += d0;
				sf1 += d1;
			}
		}

		@Override
		public void scale( final double[] dest, int offset, final int length, final double s0, final double s1 )
		{
			for ( int x = 0; x < length; ++x ) {
				dest[ offset++ ] *= s0;
				dest[ offset++ ] *= s1;
			}
		}

		@Override
		public void sourceBounds( final double[] dest, final int length,
				final double o0, final double o1,
				final Interpolation interpolation, final BlockInterval bounds )
		{
			double min0 = dest[ 0 ], max0 = min0;
			double min1 = dest[ 1 ], max1 = min1;
			for ( int i = 1; i < length; ++i )
			{
				final double v0 = dest[ 2 * i ];
				if ( v0 < min0 )
					min0 = v0;
				else if ( v0 > max0 )
					max0 = v0;
				final double v1 = dest[ 2 * i + 1 ];
				if ( v1 < min1 )
					min1 = v1;
				else if ( v1 > max1 )
					max1 = v1;
			}
			min0 += (double) o0;
			max0 += (double) o0;
			min1 += (double) o1;
			max1 += (double) o1;

			final long[] boundsMin = bounds.min();
			final int[] boundsSize = bounds.size();
			switch ( interpolation )
			{
			case NEARESTNEIGHBOR:
				boundsMin[ 0 ] = Math.round( min0 - 0.5f );
				boundsMin[ 1 ] = Math.round( min1 - 0.5f );
				boundsSize[ 0 ] = ( int ) ( Math.round( max0 + 0.5f ) - boundsMin[ 0 ] ) + 1;
				boundsSize[ 1 ] = ( int ) ( Math.round( max1 + 0.5f ) - boundsMin[ 1 ] ) + 1;
				break;
			case NLINEAR:
				boundsMin[ 0 ] = ( long ) Math.floor( min0 - 0.5f );
				boundsMin[ 1 ] = ( long ) Math.floor( min1 - 0.5f );
				boundsSize[ 0 ] = ( int ) ( ( long ) Math.floor( max0 + 0.5f ) - boundsMin[ 0 ] ) + 2;
				boundsSize[ 1 ] = ( int ) ( ( long ) Math.floor( max1 + 0.5f ) - boundsMin[ 1 ] ) + 2;
				break;
			}
		}
	}
}
