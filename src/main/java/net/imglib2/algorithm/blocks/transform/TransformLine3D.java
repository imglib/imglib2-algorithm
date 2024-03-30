/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.transform;

import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Cast;


/**
 * Compute a destination X line for 3D.
 * <p>
 * An instance for a given input/output type (e.g. {@code float[]}) and {@link
 * Transform.Interpolation Interpolation} scheme can be obtained by {@link #of
 * TransformLine3D.of}.
 * <p>
 * A destination X line can then be computed by {@link #apply}, giving starting
 * position and X differential vector.
 *
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
@FunctionalInterface
interface TransformLine3D< P >
{

    /**
	 * Compute a destination X line.
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
     * @param d2
     * 		partial differential vector in X of the transform (Z component)
     * @param ss0
     * 		length of a source line (size_X)
     * @param ss1
     * 		length of a source plane (size_X * size_Y)
     * @param sf0
     * 		position of the first sample on the line (transformed into source)
     * @param sf1
     * 		position of the first sample on the line (transformed into source)
     * @param sf2
     * 		position of the first sample on the line (transformed into source)
     */
    void apply( P src, P dest, int offset, int length,
            float d0, float d1, float d2,
            int ss0, int ss1,
            float sf0, float sf1, float sf2 );

	static < P > TransformLine3D< P > of(
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		if ( interpolation == Transform.Interpolation.NLINEAR )
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
		else // if ( interpolation = Transform.Interpolation.NEARESTNEIGHBOR )
		{
			switch ( primitiveType )
			{
			case BYTE:
				return Cast.unchecked( NearestNeighbor_byte.INSTANCE );
			case SHORT:
				return Cast.unchecked( NearestNeighbor_short.INSTANCE );
			case INT:
				return Cast.unchecked( NearestNeighbor_int.INSTANCE );
			case LONG:
				return Cast.unchecked( NearestNeighbor_long.INSTANCE );
			case FLOAT:
				return Cast.unchecked( NearestNeighbor_float.INSTANCE );
			case DOUBLE:
				return Cast.unchecked( NearestNeighbor_double.INSTANCE );
			default:
				throw new IllegalArgumentException();
			}
		}
	}


	// ========== NLINEAR =====================================================


	class NLinear_float implements TransformLine3D< float[] >
	{
		private NLinear_float()
		{
		}

		static final NLinear_float INSTANCE = new NLinear_float();

		@Override
		public void apply( final float[] src, final float[] dest, int offset, final int length,
		final float d0, final float d1, final float d2,
		final int ss0, final int ss1,
		float sf0, float sf1, float sf2 )
		{
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				final float r0 = sf0 - s0;
				final float r1 = sf1 - s1;
				final float r2 = sf2 - s2;
				final int o = s2 * ss1 + s1 * ss0 + s0;
				final float a000 = src[ o ];
				final float a001 = src[ o + 1 ];
				final float a010 = src[ o + ss0 ];
				final float a011 = src[ o + ss0 + 1 ];
				final float a100 = src[ o + ss1 ];
				final float a101 = src[ o + ss1 + 1 ];
				final float a110 = src[ o + ss1 + ss0 ];
				final float a111 = src[ o + ss1 + ss0 + 1 ];
				dest[ offset++ ] = a000 +
						r0 * ( -a000 + a001 ) +
						r1 * ( ( -a000 + a010 ) +
								r0 * ( a000 - a001 - a010 + a011 ) ) +
						r2 * ( ( -a000 + a100 ) +
								r0 * ( a000 - a001 - a100 + a101 ) +
								r1 * ( ( a000 - a010 - a100 + a110 ) +
										r0 * ( -a000 + a001 + a010 - a011 + a100 - a101 - a110 + a111 ) ) );
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NLinear_double implements TransformLine3D< double[] >
	{
		private NLinear_double()
		{
		}

		static final NLinear_double INSTANCE = new NLinear_double();

		@Override
		public void apply( final double[] src, final double[] dest, int offset, final int length,
		final float d0, final float d1, final float d2,
		final int ss0, final int ss1,
		float sf0, float sf1, float sf2 )
		{
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				final float r0 = sf0 - s0;
				final float r1 = sf1 - s1;
				final float r2 = sf2 - s2;
				final int o = s2 * ss1 + s1 * ss0 + s0;
				final double a000 = src[ o ];
				final double a001 = src[ o + 1 ];
				final double a010 = src[ o + ss0 ];
				final double a011 = src[ o + ss0 + 1 ];
				final double a100 = src[ o + ss1 ];
				final double a101 = src[ o + ss1 + 1 ];
				final double a110 = src[ o + ss1 + ss0 ];
				final double a111 = src[ o + ss1 + ss0 + 1 ];
				dest[ offset++ ] = a000 +
						r0 * ( -a000 + a001 ) +
						r1 * ( ( -a000 + a010 ) +
								r0 * ( a000 - a001 - a010 + a011 ) ) +
						r2 * ( ( -a000 + a100 ) +
								r0 * ( a000 - a001 - a100 + a101 ) +
								r1 * ( ( a000 - a010 - a100 + a110 ) +
										r0 * ( -a000 + a001 + a010 - a011 + a100 - a101 - a110 + a111 ) ) );
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	// ========== NEARESTNEIGHBOR =============================================


	class NearestNeighbor_float implements TransformLine3D< float[] >
	{
		private NearestNeighbor_float()
		{
		}

		static final NearestNeighbor_float INSTANCE = new NearestNeighbor_float();

		@Override
		public void apply( final float[] src, final float[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NearestNeighbor_double implements TransformLine3D< double[] >
	{
		private NearestNeighbor_double()
		{
		}

		static final NearestNeighbor_double INSTANCE = new NearestNeighbor_double();

		@Override
		public void apply( final double[] src, final double[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NearestNeighbor_byte implements TransformLine3D< byte[] >
	{
		private NearestNeighbor_byte()
		{
		}

		static final NearestNeighbor_byte INSTANCE = new NearestNeighbor_byte();

		@Override
		public void apply( final byte[] src, final byte[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NearestNeighbor_short implements TransformLine3D< short[] >
	{
		private NearestNeighbor_short()
		{
		}

		static final NearestNeighbor_short INSTANCE = new NearestNeighbor_short();

		@Override
		public void apply( final short[] src, final short[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NearestNeighbor_int implements TransformLine3D< int[] >
	{
		private NearestNeighbor_int()
		{
		}

		static final NearestNeighbor_int INSTANCE = new NearestNeighbor_int();

		@Override
		public void apply( final int[] src, final int[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}


	class NearestNeighbor_long implements TransformLine3D< long[] >
	{
		private NearestNeighbor_long()
		{
		}

		static final NearestNeighbor_long INSTANCE = new NearestNeighbor_long();

		@Override
		public void apply( final long[] src, final long[] dest, int offset, final int length,
                final float d0, final float d1, final float d2,
                final int ss0, final int ss1,
                float sf0, float sf1, float sf2 )
		{
			sf0 += .5f;
			sf1 += .5f;
			sf2 += .5f;
			for ( int x = 0; x < length; ++x )
			{
				final int s0 = ( int ) sf0;
				final int s1 = ( int ) sf1;
				final int s2 = ( int ) sf2;
				dest[ offset++ ] = src[ s2 * ss1 + s1 * ss0 + s0 ];
				sf0 += d0;
				sf1 += d1;
				sf2 += d2;
			}
		}
	}
}
