package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.Interval;
import net.imglib2.type.PrimitiveType;

class BlendingFunction3D< F > extends AbstractLookupFunction< F, float[] >
{
	private final Blend3D< F > blend3D;

	/**
	 * Blending weights are {@code 0 <= w <= 1}.
	 * <p>
	 * Weights are {@code w=0} for the outermost {@code border} pixels of {@code interval} (and outside of {@code interval}).
	 * Then weights transition from {@code 0<=w<=1} over {@code blending} pixels.
	 * Weights are {@code w=1} inside {@code border+blending} from the {@code interval} bounds.
	 *
	 * @param dfieldPrimitiveType
	 * @param interval
	 * @param border
	 * @param blending
	 */
	BlendingFunction3D(
			final PrimitiveType dfieldPrimitiveType,
			final Interval interval,
			final float[] border,
			final float[] blending )
	{
		super( 3 );

		if ( dfieldPrimitiveType == PrimitiveType.FLOAT )
			blend3D = ( Blend3D< F > ) new Float_( interval, border, blending );
		else if ( dfieldPrimitiveType == PrimitiveType.DOUBLE )
			blend3D = ( Blend3D< F > ) new Double_( interval, border, blending );
		else
			throw new IllegalArgumentException();
	}

	private BlendingFunction3D( BlendingFunction3D< F > processor )
	{
		super( processor );
		blend3D = processor.blend3D;
	}

	@Override
	public BlendingFunction3D< F > independentCopy()
	{
		return new BlendingFunction3D<>( this );
	}

	@Override
	public void compute( final float[] dest )
	{
		// TODO: We only need destLength directly. We don't care about destSize[], because we get vectors to look up anyway/
		final int length = destSize[ 0 ] * destSize[ 1 ] * destSize[ 2 ];
		blend3D.apply( positionField, dest, length, positionOffset );
	}

	private static abstract class Blend3D< F >
	{
		/**
		 * min border distance.
		 * for {@code x<b0: w(x)=0}.
		 */
		final float b0d0;
		final float b0d1;
		final float b0d2;

		/**
		 * min border+blend distance.
		 * for {@code b0<x<b1: w(x)=fn(x-b0)}.
		 */
		final float b1d0;
		final float b1d1;
		final float b1d2;

		/**
		 * max border+blend distance.
		 * for {@code b1<x<b2: w(x)=1}.
		 */
		final float b2d0;
		final float b2d1;
		final float b2d2;

		/**
		 * max border distance.
		 * for {@code b2<x<b3: w(x)=fn(b3-x)}.
		 * for {@code b3<x: w(x)=0}.
		 */
		final float b3d0;
		final float b3d1;
		final float b3d2;

		/**
		 * 1 / blend distance
		 */
		final float bs0;
		final float bs1;
		final float bs2;

		Blend3D(
				final Interval interval,
				final float[] border,
				final float[] blending )
		{
			final int n = 3;
			final float[] b0 = new float[ n ];
			final float[] b1 = new float[ n ];
			final float[] b2 = new float[ n ];
			final float[] b3 = new float[ n ];
			final float[] blendScale = new float[ n ];
			for ( int d = 0; d < n; ++d )
			{
				final int dim = ( int ) interval.dimension( d );
				b0[ d ] = border[ d ];
				b1[ d ] = border[ d ] + blending[ d ];
				b2[ d ] = dim - 1 - border[ d ] - blending[ d ];
				b3[ d ] = dim - 1 - border[ d ];
				blendScale[ d ] = 1f / blending[ d ];

				if ( b1[ d ] > b2[ d ] ) // there is no "inside region" where w=1
				{
					b1[ d ] = ( b1[ d ] + b2[ d ] ) / 2;
					b2[ d ] = b1[ d ];
				}
				// TODO handle the case where border is so big that w=0 everywhere
			}

			b0d0 = b0[ 0 ];
			b0d1 = b0[ 1 ];
			b0d2 = b0[ 2 ];
			b1d0 = b1[ 0 ];
			b1d1 = b1[ 1 ];
			b1d2 = b1[ 2 ];
			b2d0 = b2[ 0 ];
			b2d1 = b2[ 1 ];
			b2d2 = b2[ 2 ];
			b3d0 = b3[ 0 ];
			b3d1 = b3[ 1 ];
			b3d2 = b3[ 2 ];
			bs0 = blendScale[ 0 ];
			bs1 = blendScale[ 1 ];
			bs2 = blendScale[ 2 ];
		}

		abstract void apply( F pfield, float[] dest, int length, final double[] positionOffset );
	}

	private static class Float_ extends Blend3D< float[] >
	{
		Float_( final Interval interval, final float[] border, final float[] blending )
		{
			super( interval, border, blending );
		}

		@Override
		public void apply( final float[] pfield, final float[] dest, final int length, final double[] positionOffset )
		{
			final float d0 = ( float ) positionOffset[ 0 ];
			final float d1 = ( float ) positionOffset[ 1 ];
			final float d2 = ( float ) positionOffset[ 2 ];
			for ( int x = 0; x < length; ++x )
			{
				final float sf0 = pfield[ 3 * x ] + d0;
				final float sf1 = pfield[ 3 * x + 1 ] + d1;
				final float sf2 = pfield[ 3 * x + 2 ] + d2;
				final float w0 = computeWeight( sf0, bs0, b0d0, b1d0, b2d0, b3d0 );
				final float w1 = computeWeight( sf1, bs1, b0d1, b1d1, b2d1, b3d1 );
				final float w2 = computeWeight( sf2, bs2, b0d2, b1d2, b2d2, b3d2 );
				dest[ x ] = w0 * w1 * w2;
			}
		}
	}

	private static class Double_ extends Blend3D< double[] >
	{
		Double_( final Interval interval, final float[] border, final float[] blending )
		{
			super( interval, border, blending );
		}

		@Override
		public void apply( final double[] pfield, final float[] dest, final int length, final double[] positionOffset  )
		{
			final double d0 = positionOffset[ 0 ];
			final double d1 = positionOffset[ 1 ];
			final double d2 = positionOffset[ 2 ];
			for ( int x = 0; x < length; ++x )
			{
				final float sf0 = ( float ) ( pfield[ 3 * x ] + d0 );
				final float sf1 = ( float ) ( pfield[ 3 * x + 1 ] + d1 );
				final float sf2 = ( float ) ( pfield[ 3 * x + 2 ] + d2 );
				final float w0 = computeWeight( sf0, bs0, b0d0, b1d0, b2d0, b3d0 );
				final float w1 = computeWeight( sf1, bs1, b0d1, b1d1, b2d1, b3d1 );
				final float w2 = computeWeight( sf2, bs2, b0d2, b1d2, b2d2, b3d2 );
				dest[ x ] = w0 * w1 * w2;
			}
		}
	}

	private static float computeWeight(
			final float l,
			final float blendScale,
			final float b0,
			final float b1,
			final float b2,
			final float b3 )
	{
		if ( l < b0 )
			return 0;
		else if ( l < b1 )
			return Lookup.get( ( l - b0 ) * blendScale );
		else if ( l < b2 )
			return 1;
		else if ( l < b3 )
			return Lookup.get( ( b3 - l ) * blendScale );
		else
			return 0;
	}

	/**
	 * Lookup table for blending weight function
	 * {@code fn(x) = (Math.cos((1 - x) * Math.PI) + 1) / 2}
	 */
	private static final class Lookup
	{
		private static final int n = 30;

		// static lookup table for the blending function
		// size of the array is n + 2
		private static final float[] lookUp = createLookup( n );

		private static float[] createLookup( final int n )
		{
			final float[] lookup = new float[ n + 2 ];
			for ( int i = 0; i <= n; i++ )
			{
				final double d = ( double ) i / n;
				lookup[ i ] = ( float ) ( ( Math.cos( ( 1 - d ) * Math.PI ) + 1 ) / 2 );
			}
			lookup[ n + 1 ] = lookup[ n ];
			return lookup;
		}

		static float get( final float d )
		{
			final int i = ( int ) ( d * n );
			final float s = ( d * n ) - i;
			return lookUp[ i ] * (1.0f - s) + lookUp[ i + 1 ] * s;
		}
	}
}
