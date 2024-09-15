package net.imglib2.algorithm.blocks.extrema;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractBlockProcessor;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Intervals;

public class LocalMaximaProcessor extends AbstractBlockProcessor< float[], byte[] >
{

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

	final int[] fromBufI;
	final int[] toBufI;
	final int[] fromBufM;
	final int[] toBufM;

	final TempArray< float[] > tempArrayAuxI0;
	final TempArray< float[] > tempArrayAuxI1;
	final TempArray< byte[] > tempArrayAuxM0;
	final TempArray< byte[] > tempArrayAuxM1;

	private final int[] ols;
	private final int[] ils;
	private final int[] ksteps;

	// TODO: make this configurable. provide good default values (probably dependent on data type)
	private final int bw = 2048;

	LocalMaximaProcessor( final int numDimensions )
	{
		super( PrimitiveType.FLOAT, numDimensions );
		n = numDimensions;
		destSize = new int[ n ];

		fromBufI = new int[ n ];
		toBufI = new int[ n ];
		fromBufM = new int[ n ];
		toBufM = new int[ n ];
		for ( int d = 0; d < n; ++d )
		{
			fromBufI[ d ] = ( d == 0 ) ? BUF_SRC : ( d + 1 ) % 2;
			toBufI[ d ] = d % 2;
			fromBufM[ d ] = ( d + 1 ) % 2;
			toBufM[ d ] = ( d == n - 1 ) ? BUF_DEST : d % 2;
		}

		final PrimitiveType primitiveType = PrimitiveType.FLOAT;
		tempArrayAuxI0 = TempArray.forPrimitiveType( primitiveType );
		tempArrayAuxI1 = TempArray.forPrimitiveType( primitiveType );
		tempArrayAuxM0 = TempArray.forPrimitiveType( PrimitiveType.BYTE );
		tempArrayAuxM1 = TempArray.forPrimitiveType( PrimitiveType.BYTE );

		ols = new int[n];
		ils = new int[n];
		ksteps = new int[n];
	}

	private LocalMaximaProcessor( final LocalMaximaProcessor convolve ) // TODO rename argument
	{
		super( convolve );
		n = convolve.n;
		destSize = new int[ n ];
		fromBufI = convolve.fromBufI;
		toBufI = convolve.toBufI;
		fromBufM = convolve.fromBufM;
		toBufM = convolve.toBufM;
		tempArrayAuxI0 = convolve.tempArrayAuxI0.newInstance();
		tempArrayAuxI1 = convolve.tempArrayAuxI1.newInstance();
		tempArrayAuxM0 = convolve.tempArrayAuxM0.newInstance();
		tempArrayAuxM1 = convolve.tempArrayAuxM1.newInstance();
		ols = new int[ n ];
		ils = new int[ n ];
		ksteps = new int[ n ];
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

	@Override
	public void compute( final float[] src, final byte[] dest )
	{
		// TODO re-usable aux buffers etc, see ConvolveProcessors
		final int auxLength = ( int ) Intervals.numElements( sourceSize );

		final float[] auxI0 = tempArrayAuxI0.get( auxLength );
		final float[] auxI1 = tempArrayAuxI1.get( auxLength );
		final byte[] auxM0 = tempArrayAuxM0.get( auxLength );
		final byte[] auxM1 = tempArrayAuxM1.get( auxLength );
		for ( int d = 0; d < n; ++d )
		{
			final float[] sourceI = selectBuf( fromBufI[ d ], src, null, auxI0, auxI1 );
			final float[] targetI = selectBuf( toBufI[ d ], src, null, auxI0, auxI1 );
			final byte[] sourceM = selectBuf( fromBufM[ d ], null, dest, auxM0, auxM1 );
			final byte[] targetM = selectBuf( toBufM[ d ], null, dest, auxM0, auxM1 );
			if ( d == 0 )
				compute0( sourceI, targetI, targetM, ols[ d ], ils[ d ], bw );
			else if ( d == n - 1 )
				compute2( sourceI, sourceM, targetM, ols[ d ], ils[ d ], ksteps[ d ], bw );
			else
				compute1( sourceI, sourceM, targetI, targetM, ols[ d ], ils[ d ], ksteps[ d ], bw );
		}
	}

	private < P > P selectBuf( final int bufId, final P src, final P dest, final P aux0, final P aux1 )
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

	private void compute0(
			final float[] sourceI,
			final float[] targetI,
			final byte[] targetM,
			final int ol,
			final int til,
			final int bw )
	{
		final int sil = til + 2;
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
					final float ab = Math.max( sourceI[ x + sob ], sourceI[ x + sob + 2 ] );
					targetI[ x + tob ] = Math.max( sourceI[ x + sob + 1 ], ab );
					targetM[ x + tob ] = sourceI[ x + sob + 1 ] > ab ? ( byte ) 1 : ( byte ) 0;
				}
			}
		}
	}

	private void compute1(
			final float[] sourceI,
			final byte[] sourceM,
			final float[] targetI,
			final byte[] targetM,
			final int ol,
			final int til,
			final int kstep,
			final int bw )
	{
		final float[] lineI0 = new float[ bw ];
		final float[] lineI1 = new float[ bw ];
		final byte[] lineM0 = new byte[ bw ];
		final byte[] lineM1 = new byte[ bw ];

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

				System.arraycopy( sourceI, sob, lineI0, 0, bwb );
				System.arraycopy( sourceI, sob + 2 * kstep, lineI1, 0, bwb );
				lineMax( lineI0, lineI1, bwb );
				System.arraycopy( sourceI, sob + kstep, lineI0, 0, bwb );
				System.arraycopy( sourceM, sob + kstep, lineM0, 0, bwb );
				lineMax1( lineI0, lineI1, lineM0, lineM1, bwb );
				System.arraycopy( lineI1, 0, targetI, tob, bwb );
				System.arraycopy( lineM1, 0, targetM, tob, bwb );
			}
		}
	}

	private static void lineMax( final float[] s0, final float[] s1, final int l )
	{
		for ( int x = 0; x < l; ++x )
			s1[ x ] = Math.max( s0[ x ], s1[ x ] );
	}

	private static void lineMax1( final float[] s0, final float[] s1, final byte[] m0, final byte[] m1, final int l )
	{
		for ( int x = 0; x < l; ++x )
		{
			m1[ x ] = s0[ x ] > s1[ x ] ? m0[ x ] : ( byte ) 0;
			s1[ x ] = Math.max( s0[ x ], s1[ x ] );
		}
	}

	private void compute2(
			final float[] sourceI,
			final byte[] sourceM,
			final byte[] targetM,
			final int ol,
			final int til,
			final int kstep,
			final int bw )
	{
		final float[] lineI0 = new float[ bw ];
		final float[] lineI1 = new float[ bw ];
		final byte[] lineM0 = new byte[ bw ];
		final byte[] lineM1 = new byte[ bw ];

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

				System.arraycopy( sourceI, sob, lineI0, 0, bwb );
				System.arraycopy( sourceI, sob + 2 * kstep, lineI1, 0, bwb );
				lineMax( lineI0, lineI1, bwb );
				System.arraycopy( sourceI, sob + kstep, lineI0, 0, bwb );
				System.arraycopy( sourceM, sob + kstep, lineM0, 0, bwb );
				lineMax2( lineI0, lineI1, lineM0, lineM1, bwb );
				System.arraycopy( lineM1, 0, targetM, tob, bwb );
			}
		}
	}

	private static void lineMax2( final float[] s0, final float[] s1, final byte[] m0, final byte[] m1, final int l )
	{
		for ( int x = 0; x < l; ++x )
			m1[ x ] = s0[ x ] > s1[ x ] ? m0[ x ] : ( byte ) 0;
	}
}
