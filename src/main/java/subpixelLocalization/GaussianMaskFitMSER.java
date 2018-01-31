/*-
 * #%L
 * Microtubule tracker.
 * %%
 * Copyright (C) 2017 MTrack developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package subpixelLocalization;

import ij.IJ;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class GaussianMaskFitMSER {
	public static enum EndfitMSER {
		StartfitMSER, EndfitMSER
	}

	protected EndfitMSER EndfitMSER;

	public static double[] sumofgaussianMaskFit(final RandomAccessibleInterval<FloatType> signalInterval,
			final double[] location, final double[] sigma, final int numgaussians, final int iterations,
			final double[] dxvector, final double slope, final double intercept, final double maxintensityline,
			final boolean halfgaussian, final EndfitMSER startorend, int label, double noiselevel, double R)
			throws Exception {
		final int n = signalInterval.numDimensions();

		// pre-compute sigma^2
		final double[] sq_sigma = new double[n];
		for (int d = 0; d < n; ++d)
			sq_sigma[d] = sigma[d] * sigma[d];

		// make the interval we fit on iterable
		final IterableInterval<FloatType> signalIterable = Views.iterable(signalInterval);

		final double[] newlocation = new double[n];
		for (int d = 0; d < n; ++d)
			newlocation[d] = location[d];
		// create the mask image

		final Img<FloatType> gaussianMask = new ArrayImgFactory<FloatType>().create(signalInterval,
				signalIterable.firstElement());

		// set the mask image to the same location as the interval we fit on and
		// make it iterable
		final long[] translation = new long[n];
		for (int d = 0; d < n; ++d)
			translation[d] = signalInterval.min(d);

		final RandomAccessibleInterval<FloatType> translatedMask = Views.translate(gaussianMask, translation);
		final IterableInterval<FloatType> translatedIterableMask = Views.iterable(translatedMask);
		// remove background in the input
		final double bg = removeBackground(signalIterable);

		double N = 0;
		double Numphotons = 0;
		double Nold = 0;
		int i = 0;
		do {

			switch (startorend) {

			case StartfitMSER:
				beststartfitsumofGaussian(translatedIterableMask, newlocation, numgaussians, sigma, dxvector, slope,
						intercept, maxintensityline, noiselevel, halfgaussian);
				break;

			case EndfitMSER:
				bestendfitsumofGaussian(translatedIterableMask, newlocation, numgaussians, sigma, dxvector, slope,
						intercept, maxintensityline, noiselevel, halfgaussian);
				break;

			}

			// compute the sums
			final Cursor<FloatType> cMask = gaussianMask.cursor();
			final Cursor<FloatType> cImg = signalIterable.localizingCursor();
			double sumLocSN[] = new double[n]; // int_{all_px} d * S[ d ] * N[ d ]
												// ]
			double sumSN = 0; // int_{all_px} S[ d ] * N[ d ]

			double sumSS = 0; // int_{all_px} S[ d ] * S[ d ]
			double sumNN = 0; // int_{all_px} N[ d ] * N[ d ]
			double sumN = 0; // int_{all_px} N[ d ]

			while (cMask.hasNext()) {
				cMask.fwd();
				cImg.fwd();

				final double signal = cImg.get().getRealDouble();
				final double mask = cMask.get().getRealDouble();

				final double weight = 8;

				final double signalmask = signal * mask * weight;

				sumSN += signalmask;
				sumSS += signal * signal * weight;
				sumNN += mask * mask * weight;
				sumN += mask * weight;

				for (int d = 0; d < n; ++d) {
					final double l = cImg.getDoublePosition(d);
					sumLocSN[d] += l * signalmask;
				}

			}
			for (int d = 0; d < n; ++d)
				newlocation[d] = sumLocSN[d] / sumSN;

			Nold = N;
			N = sumSN / sumSS;

			Numphotons = (sumN * sumSN) / (sumNN);

			++i;
			if (i >= iterations)
				break;

		} while (Math.abs(N - Nold) > 1.0E-2);
		restoreBackground(signalIterable, bg);


		System.out.println("Number of photons on this pixel (Relative): " + Numphotons);

		return newlocation;

	}

	public static double removeBackground(final IterableInterval<FloatType> iterable) {
		double i = 0;

		for (final FloatType t : iterable)
			i += t.getRealDouble();

		i /= (double) iterable.size();

		for (final FloatType t : iterable)
			t.setReal(t.get() - i);

		return i;
	}

	public static void restoreBackground(final IterableInterval<FloatType> iterable, final double value) {
		for (final FloatType t : iterable)
			t.setReal(t.get() + value);
	}

	final public static void beststartfitsumofGaussian(final IterableInterval<FloatType> image, final double[] location,
			final int numgaussians, final double[] sigma, final double[] dxvector, final double slope,
			final double intercept, final double maxintensityline, final double noiselevel, boolean halfgaussian) {
		final int ndims = image.numDimensions();

		double[] secondlocation = new double[ndims];

		for (int n = 0; n < numgaussians; ++n) {

			for (int d = 0; d < ndims; ++d) {

				secondlocation[d] = location[d] - (n) * dxvector[d];
			}

			AddGaussian.addGaussian(image, maxintensityline, secondlocation, sigma);

		}

	}

	final public static void bestendfitsumofGaussian(final IterableInterval<FloatType> image, final double[] location,
			final int numgaussians, final double[] sigma, final double[] dxvector, final double slope,
			final double intercept, final double maxintensityline, final double noiselevel, boolean halfgaussian) {
		final int ndims = image.numDimensions();

		double[] secondlocation = new double[ndims];

		for (int n = 0; n < numgaussians; ++n) {

			for (int d = 0; d < ndims; ++d) {

				secondlocation[d] = location[d] + (n) * dxvector[d];
			}

			AddGaussian.addGaussian(image, maxintensityline, secondlocation, sigma);

		}

	}

}
