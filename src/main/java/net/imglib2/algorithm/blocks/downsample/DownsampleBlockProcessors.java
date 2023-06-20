/*-
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
package net.imglib2.algorithm.blocks.downsample;

import static net.imglib2.type.PrimitiveType.DOUBLE;
import static net.imglib2.type.PrimitiveType.FLOAT;

class DownsampleBlockProcessors
{
	static class CenterFloat extends AbstractDownsample< CenterFloat, float[] >
	{
		public CenterFloat( final boolean[] downsampleInDim )
		{
			super( downsampleInDim, FLOAT );
		}

		private CenterFloat( CenterFloat downsample )
		{
			super( downsample );
		}

		@Override
		CenterFloat newInstance()
		{
			return new CenterFloat( this );
		}

		@Override
		void downsample( final float[] source, final int[] destSize, final float[] dest, final int dim )
		{
			downsample_float( source, destSize, dest, dim );
		}
	}

	static class CenterDouble extends AbstractDownsample< CenterDouble, double[] >
	{
		public CenterDouble( final boolean[] downsampleInDim )
		{
			super( downsampleInDim, DOUBLE );
		}

		private CenterDouble( CenterDouble downsample )
		{
			super( downsample );
		}

		@Override
		CenterDouble newInstance()
		{
			return new CenterDouble( this );
		}

		@Override
		void downsample( final double[] source, final int[] destSize, final double[] dest, final int dim )
		{
			downsample_double( source, destSize, dest, dim );
		}
	}

	static class HalfPixelFloat extends AbstractDownsampleHalfPixel< HalfPixelFloat, float[] >
	{
		public HalfPixelFloat( final boolean[] downsampleInDim )
		{
			super( downsampleInDim, FLOAT );
		}

		private HalfPixelFloat( HalfPixelFloat downsample )
		{
			super( downsample );
		}

		@Override
		HalfPixelFloat newInstance()
		{
			return new HalfPixelFloat( this );
		}

		@Override
		void downsample( final float[] source, final int[] destSize, final float[] dest, final int dim )
		{
			downsample_halfpixel_float( source, destSize, dest, dim );
		}
	}

	static class HalfPixelDouble extends AbstractDownsampleHalfPixel< HalfPixelDouble, double[] >
	{
		public HalfPixelDouble( final boolean[] downsampleInDim )
		{
			super( downsampleInDim, DOUBLE );
		}

		private HalfPixelDouble( HalfPixelDouble downsample )
		{
			super( downsample );
		}

		@Override
		HalfPixelDouble newInstance()
		{
			return new HalfPixelDouble( this );
		}

		@Override
		void downsample( final double[] source, final int[] destSize, final double[] dest, final int dim )
		{
			downsample_halfpixel_double( source, destSize, dest, dim );
		}
	}

	private static void downsample_float( final float[] source, final int[] destSize, final float[] dest, final int dim )
	{
		if ( dim == 0 )
			downsampleX_float( source, destSize, dest );
		else
			downsampleN_float( source, destSize, dest, dim );
	}

	private static void downsampleX_float( final float[] source, final int[] destSize, final float[] dest )
	{
		final int len1 = destSize[ 0 ];
		final int len2 = mulDims( destSize, 1, destSize.length );
		for ( int z = 0; z < len2; ++z )
		{
			final int destOffsetZ = z * len1;
			final int srcOffsetZ = z * ( 2 * len1 + 1 );
			for ( int x = 0; x < len1; ++x )
			{
				dest[ destOffsetZ + x ] = wavg_float(
						source[ srcOffsetZ + 2 * x ],
						source[ srcOffsetZ + 2 * x + 1 ],
						source[ srcOffsetZ + 2 * x + 2 ] );
			}
		}
	}

	private static void downsampleN_float( final float[] source, final int[] destSize, final float[] dest, final int dim )
	{
		final int len0 = mulDims( destSize, 0, dim );
		final int len1 = destSize[ dim ];
		final int len2 = mulDims( destSize, dim + 1, destSize.length );
		for ( int z = 0; z < len2; ++z )
		{
			final int destOffsetZ = z * len1 * len0;
			final int srcOffsetZ = z * ( 2 * len1 + 1 ) * len0;
			for ( int y = 0; y < len1; ++y )
			{
				final int destOffset = destOffsetZ + y * len0;
				final int srcOffset = srcOffsetZ + 2 * y * len0;
				for ( int x = 0; x < len0; ++x )
				{
					dest[ destOffset + x ] = wavg_float(
							source[ srcOffset + x ],
							source[ srcOffset + x + len0 ],
							source[ srcOffset + x + 2 * len0 ] );
				}
			}
		}
	}

	private static float wavg_float( final float a, final float b, final float c )
	{
		return 0.25f * ( a + 2 * b + c );
	}


	private static void downsample_double( final double[] source, final int[] destSize, final double[] dest, final int dim )
	{
		if ( dim == 0 )
			downsampleX_double( source, destSize, dest );
		else
			downsampleN_double( source, destSize, dest, dim );
	}

	private static void downsampleX_double( final double[] source, final int[] destSize, final double[] dest )
	{
		final int len1 = destSize[ 0 ];
		final int len2 = mulDims( destSize, 1, destSize.length );
		for ( int z = 0; z < len2; ++z )
		{
			final int destOffsetZ = z * len1;
			final int srcOffsetZ = z * ( 2 * len1 + 1 );
			for ( int x = 0; x < len1; ++x )
			{
				dest[ destOffsetZ + x ] = wavg_double(
						source[ srcOffsetZ + 2 * x ],
						source[ srcOffsetZ + 2 * x + 1 ],
						source[ srcOffsetZ + 2 * x + 2 ] );
			}
		}
	}

	private static void downsampleN_double( final double[] source, final int[] destSize, final double[] dest, final int dim )
	{
		final int len0 = mulDims( destSize, 0, dim );
		final int len1 = destSize[ dim ];
		final int len2 = mulDims( destSize, dim + 1, destSize.length );
		for ( int z = 0; z < len2; ++z )
		{
			final int destOffsetZ = z * len1 * len0;
			final int srcOffsetZ = z * ( 2 * len1 + 1 ) * len0;
			for ( int y = 0; y < len1; ++y )
			{
				final int destOffset = destOffsetZ + y * len0;
				final int srcOffset = srcOffsetZ + 2 * y * len0;
				for ( int x = 0; x < len0; ++x )
				{
					dest[ destOffset + x ] = wavg_double(
							source[ srcOffset + x ],
							source[ srcOffset + x + len0 ],
							source[ srcOffset + x + 2 * len0 ] );
				}
			}
		}
	}

	private static double wavg_double( final double a, final double b, final double c )
	{
		return 0.25 * ( a + 2 * b + c );
	}

	private static void downsample_halfpixel_float( final float[] source, final int[] destSize, final float[] dest, final int dim )
	{
		if ( dim == 0 )
			downsampleX_halfpixel_float( source, destSize, dest );
		else
			downsampleN_halfpixel_float( source, destSize, dest, dim );
	}

	private static void downsampleX_halfpixel_float( final float[] source, final int[] destSize, final float[] dest )
	{
		final int len = mulDims( destSize, 0, destSize.length );
		for ( int x = 0; x < len; ++x )
			dest[ x ] = avg_float(
					source[ 2 * x ],
					source[ 2 * x + 1 ] );
	}

	static void downsampleN_halfpixel_float( final float[] source, final int[] destSize, final float[] dest, final int dim )
	{
		int len0 = mulDims( destSize, 0, dim );
		int len1 = mulDims( destSize, dim, destSize.length );

		for ( int y = 0; y < len1; ++y )
		{
			final int destOffset = y * len0;
			final int srcOffset = 2 * destOffset;
			for ( int x = 0; x < len0; ++x )
				dest[ destOffset + x ] = avg_float(
						source[ srcOffset + x ],
						source[ srcOffset + x + len0 ] );
		}
	}

	private static float avg_float( final float a, final float b )
	{
		return 0.5f * ( a + b );
	}

	private static void downsample_halfpixel_double( final double[] source, final int[] destSize, final double[] dest, final int dim )
	{
		if ( dim == 0 )
			downsampleX_halfpixel_double( source, destSize, dest );
		else
			downsampleN_halfpixel_double( source, destSize, dest, dim );
	}

	private static void downsampleX_halfpixel_double( final double[] source, final int[] destSize, final double[] dest )
	{
		final int len = mulDims( destSize, 0, destSize.length );
		for ( int x = 0; x < len; ++x )
			dest[ x ] = avg_double(
					source[ 2 * x ],
					source[ 2 * x + 1 ] );
	}

	static void downsampleN_halfpixel_double( final double[] source, final int[] destSize, final double[] dest, final int dim )
	{
		int len0 = mulDims( destSize, 0, dim );
		int len1 = mulDims( destSize, dim, destSize.length );

		for ( int y = 0; y < len1; ++y )
		{
			final int destOffset = y * len0;
			final int srcOffset = 2 * destOffset;
			for ( int x = 0; x < len0; ++x )
				dest[ destOffset + x ] = avg_double(
						source[ srcOffset + x ],
						source[ srcOffset + x + len0 ] );
		}
	}

	private static double avg_double( final double a, final double b )
	{
		return 0.5 * ( a + b );
	}

	private static int mulDims( int[] dims, int from, int to )
	{
		int product = 1;
		for ( int d = from; d < to; ++d )
			product *= dims[ d ];
		return product;
	}
}
