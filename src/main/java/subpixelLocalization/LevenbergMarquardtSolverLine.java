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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import Jama.Matrix;
import ij.IJ;
public class LevenbergMarquardtSolverLine {
	
		/**
		 * Calculate the current sum-squared-error
		 */
		public static final double chiSquared(
				final double[][] x, 
				final double[] a, 
				final double[] b,
				final double[] y, 
				final MTFitFunction f)  {
			
			int npts = y.length;
			double sum = 0.;

			for( int i = 0; i < npts; i++ ) {
				double d = y[i] - f.val(x[i], a, b);
				sum = sum + (d*d);
			}

			return sum;
		} //chiSquared
	
		/**
		 * Minimize E = sum {(y[k] - f(x[k],a)) }^2
		 * Note that function implements the value and gradient of f(x,a),
		 * NOT the value and gradient of E with respect to a!
		 * 
		 * @param x array of domain points, each may be multidimensional
		 * @param y corresponding array of values
		 * @param a the parameters/state of the model
		 * @param lambda blend between steepest descent (lambda high) and
		 *	jump to bottom of quadratic (lambda zero). Start with 0.001.
		 * @param termepsilon termination accuracy (0.01)
		 * @param maxiter	stop and return after this many iterations if not done
		 *
		 * @return the number of iteration used by minimization
		 */
		public static final int solve(
				double[][] x, 
				double[] a,
				double[] b,
				double[] y, 
				MTFitFunction f,
				double lambda, 
				double termepsilon, 
				int maxiter) throws Exception  {
			int npts = y.length;
			int nparm = a.length;
		
			
			double e0 = chiSquared(x, a, b, y, f);
			boolean done = false;

			// g = gradient, H = hessian, d = step to minimum
			// H d = -g, solve for d
			double[][] H = new double[nparm][nparm];
			double[] g = new double[nparm];

			int iter = 0;
			int term = 0;	// termination count test
		
			do {
				++iter;
				// hessian approximation
				for( int r = 0; r < nparm; r++ ) {
					for( int c = 0; c < nparm; c++ ) {
						H[r][c] = 0.;
						for( int i = 0; i < npts; i++ ) {
							double[] xi = x[i];
							H[r][c] += f.grad(xi, a, b, r) * f.grad(xi, a, b, c);
						}  //npts
					} //c
				} //r

				// boost diagonal towards gradient descent
				for( int r = 0; r < nparm; r++ )
					H[r][r] *= (1.0 + lambda);

				// gradient
				for( int r = 0; r < nparm; r++ ) {
					g[r] = 0.;
					for( int i = 0; i < npts; i++ ) {
						double[] xi = x[i];
						g[r] += (y[i]-f.val(xi,a, b)) * f.grad(xi, a, b, r);
					}
					
				} //npts
			
				// solve H d = -g, evaluate error at new location
				//double[] d = DoubleMatrix.solve(H, g);
				double[] d = null;
				try {
					d = (new Matrix(H)).lu().solve(new Matrix(g, nparm)).getRowPackedCopy();
				} catch (RuntimeException re) {
					// Matrix is singular
					lambda *= 10.;
					continue;
				}
				double[] na = (new Matrix(a, nparm)).plus(new Matrix(d, nparm)).getRowPackedCopy();
				double e1 = chiSquared(x, na, b, y, f);

				// termination test (slightly different than NR)
				if (Math.abs(e1-e0) > termepsilon) {
					term = 0;
				}
				else {
					
					term++;
					if (term == 3)
						done = true;
					
				}
				if (iter > maxiter -1)
				System.out.println("LM solver unable to find extrema after" + iter + " iterations");
				if (iter >= maxiter) done = true;


				//System.out.println(iter+ " " + lambda+ " "+ Math.abs(e1-e0));
				// in the C++ version, found that changing this to e1 >= e0
				// was not a good idea.  See comment there.
				//
				if (e1 > e0 || Double.isNaN(e1) ) { // new location worse than before
					lambda *= 10;
					
				}
				else {		// new location better, accept new parameters
					lambda *= 0.1;
				
					e0 = e1;
					// simply assigning a = na will not get results copied back to caller
					for( int i = 0; i < nparm; i++ ) {
						a[i] = na[i];
						
					}
				}
				
				// New truncation criteria to fasten the solver when it moves slowly during an iteration step @Varun
				for( int i = 0; i < nparm; i++ ) {
					if (Math.abs(a[i] - na[i]) < 1.0E-10   ){
						lambda *= 50;
					
						
					}
					    
				}
				
				if (lambda >= 1.0E50)
					done = true;
				
				
				

			} while(!done);
			return iter;
		} //solve
	}


