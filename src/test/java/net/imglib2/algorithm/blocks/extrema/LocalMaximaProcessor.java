package net.imglib2.algorithm.blocks.extrema;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.util.Intervals;

public class LocalMaximaProcessor implements BlockProcessor< float[], byte[] >
{

	final int n;
	final int[] destSize;
	final long[] sourcePos;
	final int[] sourceSize;

	private final int ols[];
	private final int ils[];
	private final int ksteps[];

	private final BlockProcessorSourceInterval sourceInterval;

	// TODO: make this configurable. provide good default values (probably dependent on data type)
	private final int bw = 2048;

	LocalMaximaProcessor( final int numDimensions )
	{
		n = numDimensions;
		destSize = new int[ n ];
		sourceSize = new int[ n ];
		sourcePos = new long[ n ];

		ols = new int[n];
		ils = new int[n];
		ksteps = new int[n];

		sourceInterval = new BlockProcessorSourceInterval( this );
	}

	private LocalMaximaProcessor( final LocalMaximaProcessor convolve )
	{
		n = convolve.n;
		destSize = new int[ n ];
		sourceSize = new int[ n ];
		sourcePos = new long[ n ];
		ols = new int[ n ];
		ils = new int[ n ];
		ksteps = new int[ n ];
		sourceInterval = new BlockProcessorSourceInterval( this );
	}

	@Override
	public BlockProcessor< float[], byte[] > independentCopy()
	{
		return new LocalMaximaProcessor( this );
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		assert interval.numDimensions() == n;

		Arrays.setAll( destSize, d -> ( int ) interval.dimension( d ) );
		Arrays.setAll( sourcePos, d -> ( int ) interval.min( d ) - 1 ); // -1 == kerneloffset

		System.arraycopy( destSize, 0, sourceSize, 0, n );
		for ( int d = n - 1; d >= 0; --d )
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

			sourceSize[ d ] += 2; // 2 == kernelsize - 1
		}
	}

	// TODO
//	@Override
//	public void setTargetInterval( final long[] srcPos, final int[] size )
//	{
//	}

	@Override
	public long[] getSourcePos()
	{
		return sourcePos;
	}

	@Override
	public int[] getSourceSize()
	{
		return sourceSize;
	}

	@Override
	public Interval getSourceInterval()
	{
		return sourceInterval;
	}

	@Override
	public float[] getSourceBuffer()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void compute( final float[] src, final byte[] dest )
	{
		// TODO re-usable aux buffers etc, see ConvolveProcessors
		final int auxLength = ( int ) Intervals.numElements( sourceSize );
		float[] sourceI = null;
		byte[] sourceM = null;
		float[] targetI = null;
		byte[] targetM = null;
		for ( int d = 0; d < n; ++d )
		{
			if ( d == 0 )
			{
				sourceI = src;
				sourceM = new byte[ auxLength ];
				Arrays.fill( sourceM, ( byte ) 1 );
				targetI = new float[ auxLength ];
				targetM = new byte[ auxLength ];
			}
			else if ( d == n - 1 )
			{
				sourceI = targetI;
				sourceM = targetM;
				targetI = new float[ auxLength ];
				targetM = dest;
			}
			else
			{
				sourceI = targetI;
				sourceM = targetM;
				targetI = new float[ auxLength ];
				targetM = new byte[ auxLength ];
			}
			compute( sourceI, sourceM, targetI, targetM, ols[ d ], ils[ d ], ksteps[ d ], bw );
		}
	}

	private void compute(
			final float[] sourceI,
			final byte[] sourceM,
			final float[] targetI,
			final byte[] targetM,
			final int ol,
			final int til,
			final int kstep,
			final int bw )
	{
		final int sil = til + 2 * kstep;
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

				for ( int x = 0; x < bwb; ++x )
				{
					// read a,b,c from sourceI[ sob + x + {0, kstep, 2 * kstep}]
					float _a = sourceI[ sob + x ];
					float _b = sourceI[ sob + x + kstep ];
					byte _m = sourceM[ sob + x + kstep];
					float _c = sourceI[ sob + x + kstep + kstep ];

					float _e = Math.min( _a, _b );
					byte _f = 0;

					if ( _b < _e )
					{
						_e = _b;
						_f = _m;
					}

					targetI[ tob + x ] = _e;
					targetM[ tob + x ] = _f;
				}

				// TODO benchmark and optimize
				//      convolve has a loop over k outside of the loop over x
				//      equivalent here might be: (max over _a,_b,_c)
				//         copy _a line to lineBuf0
				//         copy _c line to lineBuf1
				//         max lineBuf0, lineBuf1 --> lineBuf0
				//         copy _b line to lineBuf0
				//         copy _mask line to lineM0
				//         max lineBuf0, lineBuf1 --> lineBuf0, and write lineM1
				//         copy lineBuf0, lineM1 to targetI and targetM
			}
		}
	}
}
