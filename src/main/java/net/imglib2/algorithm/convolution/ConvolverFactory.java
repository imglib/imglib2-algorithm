package net.imglib2.algorithm.convolution;

import net.imglib2.RandomAccess;

public interface ConvolverFactory< S, T >
{
	long getBorderBefore();
	long getBorderAfter();
	Runnable getConvolver(RandomAccess<? extends S> in, RandomAccess<? extends T> out, int d, long lineLength);

	static <S, T> ConvolverFactory<S, T> wrapGauss3Convolver( double[] halfkernel, net.imglib2.algorithm.gauss3.ConvolverFactory< S, T > old ) {
		return new ConvolverFactory< S, T >()
		{
			@Override public long getBorderBefore()
			{
				return halfkernel.length - 1;
			}

			@Override public long getBorderAfter()
			{
				return halfkernel.length - 1;
			}

			@Override public Runnable getConvolver( RandomAccess< ? extends S > in, RandomAccess< ? extends T > out, int d, long lineLength )
			{
				@SuppressWarnings( "unchecked" )
				RandomAccess< S > in1 = ( RandomAccess< S > ) in;
				@SuppressWarnings( "unchecked" )
				RandomAccess<  T > out1 = ( RandomAccess< T > ) out;
				return old.create(halfkernel, in1, out1, d, lineLength);
			}
		};
	}
}
