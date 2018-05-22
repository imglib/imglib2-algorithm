package net.imglib2.algorithm.convolution.kernel;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class Kernel1D {

	private final double[] fullKernel;
	private final int centralIndex;

	/**
	 * Creates a one-dimensional symmetric convolution kernel.
	 * @param halfKernel
	 *            the upper half (starting at the center pixel) of the symmetric
	 *            convolution kernel.
	 */
	public static Kernel1D symmetric(double[] halfKernel) {
		Objects.requireNonNull(halfKernel);
		return new Kernel1D(
				halfToFullKernel(halfKernel),
				halfKernel.length - 1
		);
	}

	/**
	 * Same as {@link this#symmetric(double[])} but creates an array of one-dimensional convolution kernels.
	 */
	public static Kernel1D[] symmetric(double[][] halfKernels) {
		return Stream.of(halfKernels).map(Kernel1D::symmetric).toArray(Kernel1D[]::new);
	}

	/**
	 * Creates a one-dimensional asymmetric convolution kernel.
	 * @param fullKernel
	 * 			an array containing the values of the kernel
	 * @param centralIndex
	 * 			the index of the array element which is the central element of the kernel
	 * @return
	 */
	public static Kernel1D asymmetric(double[] fullKernel, int centralIndex) {
		Objects.requireNonNull(fullKernel);
		return new Kernel1D(
				fullKernel,
				centralIndex
		);
	}

	/**
	 * Same as {@link this#asymmetric(double[], int)} but creates an array of one-dimensional convolution kernels.
	 */
	public static Kernel1D[] asymmetric(double[][] fullKernels, int[] centralIndices) {
		return IntStream.range(0, fullKernels.length).mapToObj(
				d -> asymmetric(fullKernels[d], centralIndices[d])
		).toArray(Kernel1D[]::new);
	}

	private Kernel1D(double[] fullKernel, int centralIndex) {
		this.fullKernel = fullKernel;
		this.centralIndex = centralIndex;
	}

	public double[] fullKernel() {
		return fullKernel;
	}

	public long min() {
		return - centralIndex;
	}

	public long max() {
		return size() - 1 - centralIndex;
	}

	public int size() {
		return fullKernel().length;
	}

	// --  Helper methods --

	public static double[] halfToFullKernel(double[] halfKernel) {
		int k = halfKernel.length;
		int k1 = k - 1;
		double[] kernel = new double[k1 + k];
		for(int i = 0; i < k; i++) {
			kernel[k1 - i] = halfKernel[i];
			kernel[k1 + i] = halfKernel[i];
		}
		return kernel;
	}
}
