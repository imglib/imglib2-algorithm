package net.imglib2.algorithm.convolution.kernel;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Kernel for a one dimensional convolution. Multiple kernels could be used to
 * specify a separable convolution.
 *
 * @author Matthias Arzt
 */
public class Kernel1D
{

	private final double[] fullKernel;

	private final int centralIndex;

	/**
	 * Creates a one-dimensional symmetric convolution kernel.
	 *
	 * @param halfKernel
	 *            the upper half (starting at the center pixel) of the symmetric
	 *            convolution kernel.
	 */
	public static Kernel1D symmetric( final double... halfKernel )
	{
		Objects.requireNonNull( halfKernel );
		return new Kernel1D(
				halfToFullKernel( halfKernel ),
				halfKernel.length - 1 );
	}

	/**
	 * Similar to {@link #symmetric(double[])} but creates an array of
	 * one-dimensional convolution kernels.
	 */
	public static Kernel1D[] symmetric( final double[][] halfKernels )
	{
		return Stream.of( halfKernels ).map( Kernel1D::symmetric ).toArray( Kernel1D[]::new );
	}

	/**
	 * Creates a one-dimensional asymmetric convolution kernel.
	 *
	 * @param fullKernel
	 *            an array containing the values of the kernel
	 * @param originIndex
	 *            the index of the array element which is the origin of the
	 *            kernel
	 */
	public static Kernel1D asymmetric( final double[] fullKernel, final int originIndex )
	{
		Objects.requireNonNull( fullKernel );
		return new Kernel1D(
				fullKernel,
				originIndex );
	}

	/**
	 * Creates a one-dimensional asymmetric convolution kernel, where the origin
	 * of the kernel is in the middle.
	 */
	public static Kernel1D centralAsymmetric( final double... kernel )
	{
		return asymmetric( kernel, ( kernel.length - 1 ) / 2 );
	}

	/**
	 * Similar to {@link #asymmetric(double[], int)} but creates an array of
	 * one-dimensional convolution kernels.
	 */
	public static Kernel1D[] asymmetric( final double[][] fullKernels, final int[] originIndices )
	{
		return IntStream.range( 0, fullKernels.length ).mapToObj(
				d -> asymmetric( fullKernels[ d ], originIndices[ d ] ) ).toArray( Kernel1D[]::new );
	}

	/**
	 * Similar to {@link #centralAsymmetric(double...)} but creates an array of
	 * one-dimensional convolution kernels.
	 */
	public static Kernel1D[] centralAsymmetric( final double[][] kernels )
	{
		return Stream.of( kernels ).map( Kernel1D::centralAsymmetric ).toArray( Kernel1D[]::new );
	}

	private Kernel1D( final double[] fullKernel, final int centralIndex )
	{
		this.fullKernel = fullKernel;
		this.centralIndex = centralIndex;
	}

	public double[] fullKernel()
	{
		return fullKernel;
	}

	public long min()
	{
		return -centralIndex;
	}

	public long max()
	{
		return size() - 1 - centralIndex;
	}

	public int size()
	{
		return fullKernel().length;
	}

	// -- Helper methods --

	public static double[] halfToFullKernel( final double[] halfKernel )
	{
		final int k = halfKernel.length;
		final int k1 = k - 1;
		final double[] kernel = new double[ k1 + k ];
		for ( int i = 0; i < k; i++ )
		{
			kernel[ k1 - i ] = halfKernel[ i ];
			kernel[ k1 + i ] = halfKernel[ i ];
		}
		return kernel;
	}
}
