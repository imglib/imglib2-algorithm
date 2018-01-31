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

public class NumericalDerivatives extends GaussianSplinethirdorder {

	
public static double numdiffstart(double[] x, double[] a, int dim, double[] b) {
		
		double [] newa = new double[a.length];
		final int ndims = x.length;
		double epsilon = 1.0E-1;
		double f1 = 0;
		double f2 = 0;
		double diff = 0;
		
		
		do{
			
			
		for (int i = 0; i < a.length; ++i){
			newa[i] = a[i];
			if (i == dim)
			newa[i] = a[i] + epsilon;
		}
		f1 = (Estart(x, newa, b) - Estart(x, a, b))/ epsilon ;
		
		epsilon/=2;
		
		for (int i = 0; i < a.length; ++i){
			newa[i] = a[i];
			if (i == dim)
			newa[i] = a[i] + epsilon;
		}
		
		f2 = (Estart(x, newa, b) - Estart(x, a, b))/ epsilon ;
		
		diff =  Math.abs(f2 - f1) ;
		
		f1 = f2;
		}while(diff> 1.0E-7);
		
		return a[2 * ndims + 3] *f2;
		
	}

public static double numdiffend(double[] x, double[] a, int dim, double[] b) {
	
	double [] newa = new double[a.length];
	final int ndims = x.length;
	double epsilon = 1.0E-1;
	double f1 = 0;
	double f2 = 0;
	double diff = 0;
	
	
	do{
		
		
	for (int i = 0; i < a.length; ++i){
		newa[i] = a[i];
		if (i == dim)
		newa[i] = a[i] + epsilon;
	}
	f1 = (Eend(x, newa, b) - Eend(x, a, b))/ epsilon ;
	
	epsilon/=2;
	
	for (int i = 0; i < a.length; ++i){
		newa[i] = a[i];
		if (i == dim)
		newa[i] = a[i] + epsilon;
	}
	
	f2 = (Eend(x, newa, b) - Eend(x, a, b))/ epsilon ;
	
	diff =  Math.abs(f2 - f1) ;
	
	f1 = f2;
	}while(diff> 1.0E-7);
	
	return a[2 * ndims + 3] *f2;
	
}


public static double numdiff(double[] x, double[] a, int dim, double[] b) {
	
	double [] newa = new double[a.length];
	final int ndims = x.length;
	double epsilon = 0.001;
	double f1 = 0;
	double f2 = 0;
	double diff = 0;
	
	
	do{
		
		
	for (int i = 0; i < a.length; ++i){
		newa[i] = a[i];
		if (i == dim)
		newa[i] = a[i] + epsilon;
	}
	f1 = (Etotal(x, newa, b) - Etotal(x, a, b) )/ epsilon ;
	
	epsilon/=2;
	
	for (int i = 0; i < a.length; ++i){
		newa[i] = a[i];
		if (i == dim)
		newa[i] = a[i] + epsilon;
	}
	
	f2 =  (Etotal(x, newa, b) - Etotal(x, a, b) )/ epsilon ;
	
	diff =  Math.abs(f2 - f1) ;
	
	f1 = f2;
	}while(diff> 1.0E-2);
	
	return a[2 * ndims + 3] *f2;
	
}

	
}
