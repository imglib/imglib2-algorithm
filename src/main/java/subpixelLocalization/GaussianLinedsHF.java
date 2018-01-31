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

public class GaussianLinedsHF implements MTFitFunction {

	@Override
	public double val(double[] x, double[] a, double[] b) {
		final int ndims = x.length;
		
		
		return  a[2 * ndims + 1] * Etotal(x, a, b) + a[2 * ndims + 2] ;
		
	}

	@Override
	public double grad(double[] x, double[] a, double[] b, int k) {
		final int ndims = x.length;

		if (k < ndims) {

			return 2 * b[k] * (x[k] - a[k])  * a[2 * ndims + 1] * Estart(x, a, b);

		}

		else if (k >= ndims && k <= ndims + 1) {
			int dim = k - ndims;
			return 2 * b[dim] * (x[dim] - a[k])  * a[2 * ndims + 1] * Eend(x, a, b);

		}

		else if (k == 2 * ndims)
			return  a[2 * ndims + 1] *Estartds(x, a, b);
		
		
		else if (k == 2 * ndims + 1)
			return Etotal(x, a, b);
		
		
		else if (k == 2 * ndims + 2)
			return 1.0;

		
		else
			return 0;

	}

	/*
	 * PRIVATE METHODS
	 */

	/*
	 * @ Define a line analytically as a sum of gaussians, the parameters to be
	 * determined are the start and the end points of the line
	 * 
	 */

	private static final double Estart(final double[] x, final double[] a, final double[] b) {

		double sum = 0;
		double di;
		for (int i = 0; i < x.length; i++) {
			di = x[i] - a[i];
			sum += b[i] * di * di;
		}

		return Math.exp(-sum);

	}

	private static final double Estartds(final double[] x, final double[] a, final double[] b) {

		
		double di;
		final int ndims = x.length;
		double[] minVal = new double[ndims];
		double[] maxVal = new double[ndims];

		for (int i = 0; i < x.length; i++) {
			minVal[i] = a[i];
			maxVal[i] = a[ndims + i];
		}
		double slope = b[x.length];
		
		
		double ds = Math.abs(a[2 * ndims]);

		double[] dxvector = { ds / Math.sqrt( 1 + slope * slope) , slope * ds/ Math.sqrt( 1 + slope * slope)  };
		double[] dxvectorderiv = { 1/ Math.sqrt( 1 + slope * slope) , slope/ Math.sqrt( 1 + slope * slope)  };

		
		
		double dsum = 0;
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			minVal[i] += dxvector[i];
			di = x[i] - minVal[i];
			sum += b[i] * di * di;
			dsum += 2 * b[i] * di * dxvectorderiv[i];
		}
		double sumofgaussians = dsum * Math.exp(-sum);
		
		double dsumend = 0;
		double sumend = 0;
		for (int i = 0; i < x.length; i++) {
			maxVal[i] -= dxvector[i];
			di = x[i] - maxVal[i];
			sumend += b[i] * di * di;
			dsumend += -2 * b[i] * di * dxvectorderiv[i];
		}
		sumofgaussians+= dsumend * Math.exp(-sumend);
		
		
		return    sumofgaussians ;

	}

	
	private static final double Eend(final double[] x, final double[] a, final double[] b) {

		double sum = 0;
		double di;
		int ndims = x.length;
		for (int i = 0; i < x.length; i++) {
			di = x[i] - a[i + ndims];
			sum += b[i] * di * di;
		}

		return Math.exp(-sum);

	}

	private static final double Etotal(final double[] x, final double[] a, final double[] b) {

		return Estart(x, a, b) + Esum(x, a, b) + Eend(x, a, b);

	}

	private static final double Esum(final double[] x, final double[] a, final double[] b) {

		final int ndims = x.length;
		double[] minVal = new double[ndims];
		double[] maxVal = new double[ndims];

		for (int i = 0; i < x.length; i++) {
			minVal[i] = a[i];
			maxVal[i] = a[ndims + i];
		}
		double slope = b[x.length];
		double sum = 0;
		double sumofgaussians = 0;
		double di;
		
		
		double ds = Math.abs(a[2 * ndims]);

		double[] dxvector = { ds/ Math.sqrt( 1 + slope * slope) , slope * ds/ Math.sqrt( 1 + slope * slope)  };

		while (true) {

			sum = 0;
			for (int i = 0; i < x.length; i++) {
				minVal[i] += dxvector[i];
				di = x[i] - minVal[i];
				sum += b[i] * di * di;
			}
			sumofgaussians += Math.exp(-sum);

			
			if (minVal[0] >= maxVal[0] || minVal[1] >= maxVal[1] && slope > 0)
				break;
			if (minVal[0] >= maxVal[0] || minVal[1] <= maxVal[1] && slope < 0)
				break;

		}
		
		
		

		return sumofgaussians;
	}

	public static double Distance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;
		final double ndims = cordone.length;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return Math.sqrt(distance);
	}

}
