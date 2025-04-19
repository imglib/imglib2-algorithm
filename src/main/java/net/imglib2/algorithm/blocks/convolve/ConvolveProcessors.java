/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractBlockProcessor;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;

/**
 * Separable convolution
 */
class ConvolveProcessors
{
	abstract static class AbstractConvolve< T extends AbstractConvolve< T, P >, P > extends AbstractBlockProcessor< P, P >
	{
		final P[] kernels;
		final int[] kernelOffsets;
		final int[] kernelSizes;

		final int n;
		final int[] destSize;

		// buf indices:
		//   0 intermediate 0
		//   1 intermediate 1
		//   2 src
		//   3 dest
		private static final int BUF_AUX0 = 0;
		private static final int BUF_AUX1 = 1;
		private static final int BUF_SRC = 2;
		private static final int BUF_DEST = 3;

		final int[] fromBuf;
		final int[] toBuf;

		final TempArray< P > tempArrayAux0;
		final TempArray< P > tempArrayAux1;

		private final int[] ols;
		private final int[] ils;
		private final int[] ksteps;

		private int aux0Length;
		private int aux1Length;

		// TODO: make this configurable. provide good default values (probably dependent on data type)
		private final int bw = 2048;

		/**
		 * If {@code kernel1Ds[d]==null}, the convolution for dimension {@code d} is
		 * skipped (equivalent to convolution with the kernel {@code {1}}).
		 *
		 * @param kernel1Ds
		 * 		convolution kernel for each dimension.
		 */
		AbstractConvolve( final Kernel1D[] kernel1Ds, final Function< Kernel1D, P > extractKernel, final PrimitiveType primitiveType )
		{
			super( primitiveType, kernel1Ds.length );
			n = kernel1Ds.length;

			kernels = Cast.unchecked( new Object[ n ] );
			kernelOffsets = new int[ n ];
			kernelSizes = new int[ n ];
			for ( int d = 0; d < n; ++d )
			{
				final Kernel1D kernel1D = kernel1Ds[ d ];
				if ( kernel1D != null )
				{
					kernels[ d ] = extractKernel.apply( kernel1D );
					kernelOffsets[ d ] = safeInt( kernel1D.min() );
					kernelSizes[ d ] = safeInt( kernel1D.size() );
				}
			}

			destSize = new int[ n ];
			fromBuf = new int[ n ];
			toBuf = new int[ n ];

			// find total number of passes to do.
			final int numPasses = ( int ) Arrays.stream( kernels ).filter( Objects::nonNull ).count();
			if ( numPasses == 0 )
				throw new IllegalArgumentException();

			// if numPasses < 2, we need no intermediate buffer
			// if numPasses == 2, we need 1 intermediate buffer
			// if numPasses > 2, we need 2 intermediate buffers

			// pass 0 reads from src
			// pass i>0 reads from intermediate (i+1)%2

			// that is for 4 passes:
			//   pass 0 reads from src
			//   pass 1 reads from intermediate 0
			//   pass 2 reads from intermediate 1
			//   pass 3 reads from intermediate 0

			// pass i<(numPasses-1) writes to intermediate i%2
			// pass (numPasses-1) writes to dest

			// that is for 4 passes:
			//   pass 0 writes to intermediate 0
			//   pass 1 writes to intermediate 1
			//   pass 2 writes to intermediate 0
			//   pass 3 writes to dest

			// combined, for 4 passes:
			//   pass 0 reads from src, writes to intermediate 0
			//   pass 1 reads from intermediate 0, writes to intermediate 1
			//   pass 2 reads from intermediate 1, writes to intermediate 0
			//   pass 3 reads from intermediate 0, writes to dest

			int pass = 0;
			for ( int d = 0; d < n; ++d )
			{
				if ( kernels[ d ] != null )
				{
					fromBuf[ d ] = ( pass == 0 ) ? BUF_SRC : ( pass + 1 ) % 2;
					toBuf[ d ] = ( pass == numPasses - 1 ) ? BUF_DEST : pass % 2;
					++pass;
				}
			}

			tempArrayAux0 = TempArray.forPrimitiveType( primitiveType );
			tempArrayAux1 = TempArray.forPrimitiveType( primitiveType );

			ols = new int[n];
			ils = new int[n];
			ksteps = new int[n];
		}

		AbstractConvolve( final T convolve )
		{
			super( convolve );
			n = convolve.n;
			kernels = convolve.kernels;
			kernelOffsets = convolve.kernelOffsets;
			kernelSizes = convolve.kernelSizes;
			destSize = new int[ n ];
			fromBuf = convolve.fromBuf;
			toBuf = convolve.toBuf;
			tempArrayAux0 = convolve.tempArrayAux0.newInstance();
			tempArrayAux1 = convolve.tempArrayAux1.newInstance();
			ols = new int[ n ];
			ils = new int[ n ];
			ksteps = new int[ n ];
		}

		@Override
		public void setTargetInterval( final Interval interval )
		{
			assert interval.numDimensions() == n;

			Arrays.setAll( destSize, d -> ( int ) interval.dimension( d ) );
			Arrays.setAll( sourcePos, d -> interval.min( d ) + kernelOffsets[ d ] );

			aux0Length = 0;
			aux1Length = 0;
			System.arraycopy( destSize, 0, sourceSize, 0, n );
			for ( int d = n - 1; d >= 0; --d )
			{
				if ( kernels[ d ] != null )
				{
					ils[ d ] = 1;
					for ( int dd = 0; dd < d + 1; ++dd )
						ils[ d ] *= sourceSize[ dd ];

					ols[ d ] = 1;
					for ( int dd = d + 1; dd < n; ++dd )
						ols[ d ] *= sourceSize[ dd ];

					ksteps[ d ] = 1;
					for ( int dd = 0; dd < d; ++dd )
						ksteps[ d ] *= sourceSize[ dd ];

					if ( toBuf[ d ] == BUF_AUX0 )
						aux0Length = Math.max( aux0Length, safeInt( Intervals.numElements( sourceSize ) ) );
					else if ( toBuf[ d ] == BUF_AUX1 )
						aux1Length = Math.max( aux1Length, safeInt( Intervals.numElements( sourceSize ) ) );

					sourceSize[ d ] += kernelSizes[ d ] - 1;
				}
			}
		}

		@Override
		public void compute( final P src, final P dest )
		{
			final P aux0 = tempArrayAux0.get( aux0Length );
			final P aux1 = tempArrayAux1.get( aux1Length );
			for ( int d = 0; d < n; ++d )
			{
				if ( kernels[ d ] != null )
				{
					final P source = selectBuf( fromBuf[ d ], src, dest, aux0, aux1 );
					final P target = selectBuf( toBuf[ d ], src, dest, aux0, aux1 );
					convolve( source, target, kernels[ d ], ols[ d ], ils[ d ], ksteps[ d ], bw );
				}
			}
		}

		private P selectBuf( final int bufId, final P src, final P dest, final P aux0, final P aux1 )
		{
			switch ( bufId )
			{
			case BUF_AUX0:
				return aux0;
			case BUF_AUX1:
				return aux1;
			case BUF_SRC:
				return src;
			case BUF_DEST:
			default:
				return dest;
			}
		}


		// convolve in one dimension (d)

		// ol:    outer loop length
		// til:   inner loop length.
		//        how many target elements we can process in a row until we have to
		//        adjust source/target offset because we skip a few elements to move
		//        to the next line/plane/hyperplane (depending on dimension d).
		// kstep: when moving to the next kernel element how much to move in the source.
		//        (this depends on the dimension d. TODO: == source stride?)
		// bw:    block width.
		//        Roughly: how many elements to process "in parallel".
		//        The length of the accumulation buffer:
		//        We loop over the kernel once and accumulate kernel[i] * source[?+a] into accumulator[a]
		//        This is NOT the same as the target block width! It will usually be
		//        larger than that because we just let the accumulation loop run across
		//        line/plane/cube boundaries,

		// TODO: For tuning, we should support different bw for each dimension

		abstract void convolve(
				final P source,
				final P target,
				final P kernel,
				final int ol,
				final int til,
				final int kstep,
				final int bw );
	}

	private static float[] extractKernelFloat( final Kernel1D k )
	{
		final double[] doubles = k.fullKernel();
		final int length = doubles.length;
		final float[] result = new float[ length ];
		for ( int i = 0; i < length; i++ )
			result[ i ] = ( float ) doubles[ length - 1 - i ];
		return result;
	}

	private static double[] extractKernelDouble( final Kernel1D k )
	{
		final double[] doubles = k.fullKernel();
		final int length = doubles.length;
		final double[] result = new double[ length ];
		for ( int i = 0; i < length; i++ )
			result[ i ] = doubles[ length - 1 - i ];
		return result;
	}

	static class ConvolveDouble extends AbstractConvolve< ConvolveDouble, double[] >
	{
		ConvolveDouble( final Kernel1D[] kernel1Ds )
		{
			super( kernel1Ds, ConvolveProcessors::extractKernelDouble, PrimitiveType.DOUBLE );
		}

		ConvolveDouble( final ConvolveDouble convolve )
		{
			super( convolve );
		}

		@Override
		public BlockProcessor< double[], double[] > independentCopy()
		{
			return new ConvolveDouble( this );
		}

		@Override
		void convolve(
				final double[] source,
				final double[] target,
				final double[] kernel,
				final int ol,
				final int til,
				final int kstep,
				final int bw )
		{
			final int kl = kernel.length;
			final int sil = til + ( kl - 1 ) * kstep;

			final double[] sourceCopy = new double[ bw ];
			final double[] targetCopy = new double[ bw ];
			final int nBlocks = ( til - 1 ) / bw + 1;
			final int trailing = til - ( nBlocks - 1 ) * bw;

			for ( int o = 0; o < ol; ++o )
			{
				final int to = o * til;
				final int so = o * sil;
				for ( int b = 0; b < nBlocks; ++b )
				{
					final int tob = to + b * bw;
					final int sob = so + b * bw;
					final int bwb = ( b == nBlocks - 1 ) ? trailing : bw;

					Arrays.fill( targetCopy, 0 );
					for ( int k = 0; k < kl; ++k )
					{
						// NB: Copy data to make auto-vectorization happen.
						// See https://richardstartin.github.io/posts/multiplying-matrices-fast-and-slow
						System.arraycopy( source, sob + k * kstep, sourceCopy, 0, bwb );
						line( sourceCopy, targetCopy, bwb, kernel[ k ] );
					}
					System.arraycopy( targetCopy, 0, target, tob, bwb );
				}
			}
		}

		private static void line( final double[] source, final double[] target, final int txl, final double v )
		{
			for ( int x = 0; x < txl; ++x )
	//			target[ x ] = Math.fma( v, source[ x ], target[ x ] );// TODO: Use Math.fma when moving to Java9+
				target[ x ] += v * source[ x ];
		}
	}

	/**
	 * Separable convolution
	 */
	static class ConvolveFloat extends AbstractConvolve< ConvolveFloat, float[] >
	{
		ConvolveFloat( final Kernel1D[] kernel1Ds )
		{
			super( kernel1Ds, ConvolveProcessors::extractKernelFloat, PrimitiveType.FLOAT );
		}

		ConvolveFloat( final ConvolveFloat convolve )
		{
			super( convolve );
		}

		@Override
		public BlockProcessor< float[], float[] > independentCopy()
		{
			return new ConvolveFloat( this );
		}

		@Override
		void convolve(
				final float[] source,
				final float[] target,
				final float[] kernel,
				final int ol,
				final int til,
				final int kstep,
				final int bw )
		{
			final int kl = kernel.length;
			final int sil = til + ( kl - 1 ) * kstep;

			final float[] sourceCopy = new float[ bw ];
			final float[] targetCopy = new float[ bw ];
			final int nBlocks = ( til - 1 ) / bw + 1;
			final int trailing = til - ( nBlocks - 1 ) * bw;

			for ( int o = 0; o < ol; ++o )
			{
				final int to = o * til;
				final int so = o * sil;
				for ( int b = 0; b < nBlocks; ++b )
				{
					final int tob = to + b * bw;
					final int sob = so + b * bw;
					final int bwb = ( b == nBlocks - 1 ) ? trailing : bw;

					Arrays.fill( targetCopy, 0 );
					for ( int k = 0; k < kl; ++k )
					{
						// NB: Copy data to make auto-vectorization happen.
						// See https://richardstartin.github.io/posts/multiplying-matrices-fast-and-slow
						System.arraycopy( source, sob + k * kstep, sourceCopy, 0, bwb );
						line( sourceCopy, targetCopy, bwb, kernel[ k ] );
					}
					System.arraycopy( targetCopy, 0, target, tob, bwb );
				}
			}
		}

		private static void line( final float[] source, final float[] target, final int txl, final float v )
		{
			for ( int x = 0; x < txl; ++x )
	//			target[ x ] = Math.fma( v, source[ x ], target[ x ] );// TODO: Use Math.fma when moving to Java9+
				target[ x ] += v * source[ x ];
		}
	}
}
