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

public interface MTFitFunction {
	
        // Defines the Microtubule fit function as a line
		
		/**
		 * Evaluate this function at point <code>x</code>. The function is
		 * otherwise defined over an array of parameters <code>a</code>, that
		 * is the target of the fitting procedure.
		 * @param x  the multidimensional to evaluate the fonction at
		 * @param a  the set of parameters that defines the function
		 * @param b the set of parameters which are fixed
		 * @return  a double value, the function evaluated at <code>x</code>
		 *  
		 */
		public double val(double[] x, double[] a, double [] b);
		

		/**
		 * Evaluate the gradient value of the function, taken with respect to the 
		 * <code>ak</code><sup>th</sup> parameter, evaluated at point <code>x</code>.
		 * @param x  the point to evaluate the gradient at
		 * @param a  the set of parameters that defines the function
		 *  @param b the set of parameters which are fixed
		 * @param ak the index of the parameter to compute the gradient 
		 * @return the kth component of the gradient <code>df(x,a)/da_k</code>
		 * @see #val(double[], double[])
		 */
		public double grad(double[] x, double[] a, double[] b, int ak);
	}


