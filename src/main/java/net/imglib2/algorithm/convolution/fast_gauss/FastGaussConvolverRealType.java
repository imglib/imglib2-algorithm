package net.imglib2.algorithm.convolution.fast_gauss;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.LineConvolverFactory;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

/**
 * Implementation of {@link LineConvolverFactory} that uses {@link FastGaussCalculator}
 * to calculate a fast gauss convolution.
 *
 * @author Vladimir Ulman
 * @author Matthias Arzt
 */
public class FastGaussConvolverRealType implements LineConvolverFactory< RealType< ? > >
{

	private final FastGaussCalculator.Parameters fc;

	private static final ClassCopyProvider< Runnable > provider = new ClassCopyProvider<>( MyConvolver.class, Runnable.class );

	public FastGaussConvolverRealType( double sigma )
	{
		this.fc = FastGaussCalculator.Parameters.exact( sigma );
	}

	@Override public long getBorderBefore()
	{
		return fc.N + 1;
	}

	@Override public long getBorderAfter()
	{
		return fc.N - 1;
	}

	@Override public Runnable getConvolver( RandomAccess< ? extends RealType< ? > > in, RandomAccess< ? extends RealType< ? > > out, int d, long lineLength )
	{
		Object key = Arrays.asList( in.getClass(), out.getClass(), in.get().getClass(), out.get().getClass() );
		return provider.newInstanceForKey( key, d, fc, in, out, lineLength );
	}

	public static class MyConvolver implements Runnable
	{
		private final int d;

		private final RandomAccess< ? extends RealType< ? > > in;

		private final RandomAccess< ? extends RealType< ? > > out;

		private final long lineLength;

		private final FastGaussCalculator fg;

		private final int offset;

		private final double[] tmpE;

		public MyConvolver( int d, FastGaussCalculator.Parameters fc, RandomAccess< ? extends RealType< ? > > in, RandomAccess< ? extends RealType< ? > > out, long lineLength )
		{
			if ( lineLength > Integer.MAX_VALUE )
				throw new UnsupportedOperationException();
			this.d = d;
			this.in = in;
			this.out = out;
			this.lineLength = lineLength;
			this.offset = 2 * fc.N;
			this.tmpE = new double[ ( int ) lineLength + offset ];
			this.fg = new FastGaussCalculator( fc );
		}

		@Override public void run()
		{
			//left boundary: x=-Nm1, such that tmp = I(0) + I(0),
			//but we say x=-N to simplify the following code block a bit
			for ( int i = 0; i < lineLength + offset; ++i )
			{
				tmpE[ i ] = in.get().getRealDouble(); //backward edge + forward edge
				in.fwd( d );
			}

			//cache...
			double boundaryValue = tmpE[ 0 ];

			fg.initialize( boundaryValue );
			for ( int i = -offset; i < 0; ++i )
			{
				fg.update( boundaryValue + tmpE[ i + offset ] );
				in.fwd( d );
			}

			for ( int i = 0; i < lineLength; ++i )
			{
				fg.update( tmpE[ i ] + tmpE[ i + offset ] );
				out.get().setReal( fg.getValue() );
				out.fwd( d );
			}
		}
	}
}
