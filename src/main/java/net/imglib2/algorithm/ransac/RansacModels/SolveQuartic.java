package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

public class SolveQuartic {

	
	/**
	 * 
	 * Quartic solvers needed to find interesection of ellipses, adopted from David Eberlz SolveQuartics.h code
	 */
	
	
	public ArrayList<Pair<Integer, Double>> SolveQuartic(double[] p){
		
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();
		
		int rat2 = 2, rat3 = 3, rat4 = 4, rat6 = 6;
		double q0 = p[0] / p[4];
		double q1 = p[1] / p[4];
		double q2 = p[2] / p[4];
		double q3 = p[3] / p[4];
		double q3fourth = q3 / rat4;
		double q3fourthSqr = q3fourth * q3fourth;
		double c0 = q0 - q3fourth * (q1 - q3fourth * ( q2 - q3fourthSqr * rat3));
		double c1 = q1 - rat2 * q3fourth * (q2 - rat4 * q3fourthSqr);
		double c2 = q2 -rat6 * q3fourthSqr;
		
		ArrayList<Pair<Integer, Double>> RootmapLocal = SolveDepressedQuartic(c0, c1, c2);
		for (Pair<Integer, Double> rm : RootmapLocal) {
			
			double root = rm.getB() - q3fourth;
			Rootmap.add(new ValuePair<Integer, Double>(rm.getA(), root));
			
			
		}
		
		
		
		return Rootmap;
	}
	
	
	public ArrayList<Pair<Integer, Double>> SolveDepressedQuadratic(double c0){
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();

		double zero = 0;
		if (c0 < zero )
		{
			//Two simple roots
			
			double root1 = Math.sqrt(-c0);
			double root0 = - root1;
			Rootmap.add(new ValuePair<Integer, Double>(1, root0));
			Rootmap.add(new ValuePair<Integer, Double>(1, root1));

		}

		else if (c0 == zero) {
			
			// One double root
			Rootmap.add(new ValuePair<Integer, Double>(2, zero));

			
		}
		
		else
		{
			
			
			//Roots are complex
		}
		
		return Rootmap;
		
	}
	
	
	public ArrayList<Pair<Integer, Double>> SolveDepressedCubic(double c0, double c1){
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();

		double zero = 0;
		if (c0 == zero)
		{
			ArrayList<Pair<Integer, Double>> RootmapLocal =	SolveDepressedQuadratic(c1);
			
			for (Pair<Integer, Double> rm: RootmapLocal) {
				
				if(rm.getB()!=0) {
					
					//The quadratic does not have a root of zero, Insert one for cubic
					
					Rootmap.add(new ValuePair<Integer, Double>(1, zero));
				}
			}
			
			return Rootmap;
		}
		
		double oneThird = 1.0 / 3.0;
		
		if (c1 ==zero)
		{
			
			double root0;
			if (c0 > zero)
			{
				
				root0 = -Math.pow(c0, oneThird);
				
			}
			
			else
			{
				
				root0 = Math.pow(-c0, oneThird);
				
			}
			
			Rootmap.add(new ValuePair<Integer, Double>(1, root0));
			
			return Rootmap;
			
			
			
		}
		
		double rat2 = 2, rat3 = 3, rat4 = 4, rat27 = 27, rat108 = 108;
		double delta = -(rat4 * c1 * c1 * c1 + rat27 * c0 * c0);
		if (delta > zero)
		{
			
			//Three simple roots
			double deltaDiv108 = delta / rat108;
			double betaRe = -c0 / rat2;
			double betaIM = Math.sqrt(deltaDiv108);
			double theta = Math.atan2(betaIM, betaRe);
			double thetaDiv3 = theta / rat3;
			double angle = thetaDiv3;
			double cs = Math.cos(angle);
			double sn = Math.sin(angle);
			double rhoSqr = betaRe * betaRe + betaIM * betaIM;
			double rhoPowThird = Math.pow(rhoSqr, 1.0 / 6.0);
			double temp0 = rhoPowThird * cs;
			double temp1 = rhoPowThird * sn * Math.sqrt(3);
			double root0 = rat2 * temp0;
			double root1 = -temp0 - temp1;
			double root2 = -temp0 + temp1;
			Rootmap.add(new ValuePair<Integer, Double>(1, root0));
			Rootmap.add(new ValuePair<Integer, Double>(1, root1));
			Rootmap.add(new ValuePair<Integer, Double>(1, root2));
		}

		else if (delta < zero)
		{
			
			// One Simple root
			double deltaDiv108 = delta / rat108;
			double temp0 = -c0 / rat2;
			double temp1 = Math.sqrt(-deltaDiv108);
			double temp2 = temp0 - temp1;
			double temp3 = temp0 + temp1;
			
			if (temp2 >=zero)
			{
				temp2 = Math.pow(temp2, oneThird);
				
			}
			else
			{
				
				temp2 = -Math.pow(-temp2, oneThird);
				
			}
			if (temp3 >= zero)
			{
				
				temp3 = Math.pow(temp3, oneThird);
				
			}
			
			else
			{
				
				temp3 = -Math.pow(-temp3, oneThird);
			}
			
			double root0 = temp2 + temp3;
			Rootmap.add(new ValuePair<Integer, Double>(1, root0));
			
			
		}
		
		else
		{
			
			//One simple root and one double root.
			double root0 = -rat3 * c0 / (rat2 * c1);
			double root1 = -rat2 * root0;
			Rootmap.add(new ValuePair<Integer, Double>(2, root0));

			Rootmap.add(new ValuePair<Integer, Double>(1, root1));

			
			
		}
		
		
		return Rootmap;
		
		
	}
	
	
	public ArrayList<Pair<Integer, Double>> SolveBiquadratic(double c0, double c2)
	{
		
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();

		double zero = 0, rat2 = 2, rat256 = 256;
		double c2Half = c2 / rat2;
		double a1 = c0 - c2Half * c2Half;
		double delta = rat256 * c0 * a1 * a1;
		if (delta > zero)
		{
			if (c2 < zero)
			{
				
				if (a1 < zero)
				{
					
					// Four simple roots
					
					double temp0 = Math.sqrt(-a1);
					double temp1 = -c2Half - temp0;
					double temp2 = -c2Half + temp0;
					double root1 = Math.sqrt(temp1);
					double root0 = -root1;
					double root2 = Math.sqrt(temp2);
					double root3 = -root2;
					
					Rootmap.add(new ValuePair<Integer, Double>(1, root0));
					Rootmap.add(new ValuePair<Integer, Double>(1, root1));
					Rootmap.add(new ValuePair<Integer, Double>(1, root2));
					Rootmap.add(new ValuePair<Integer, Double>(1, root3));
					
				}

				else
				{
					
					
					//Roots are complex
					
					
				}
				
			}
			
			else
			{
				
				//c2 > 0
				
				//Roots are complex
				
				
				
			}
			
		return Rootmap;	
			
		}
		
		else if (delta < zero)
		{
			
			// Two simple real roots
			
			double root1 = Math.sqrt(-c2Half);
			double root0 = -root1;
			Rootmap.add(new ValuePair<Integer, Double>(1, root0));
			Rootmap.add(new ValuePair<Integer, Double>(1, root1));

			return Rootmap;
			
			
			
		}
		
		else // delta = 0
		{
			
			if (c2 < zero)
			{
				
				// Two double real roots
				double root1 = Math.sqrt(-c2Half);
				double root0 = -root1;
				
				Rootmap.add(new ValuePair<Integer, Double>(2, root0));
				Rootmap.add(new ValuePair<Integer, Double>(2, root1));
				
				
			}
			else // c2 > 0
			{
				
				// Roots are complex
				
			}
			
			return Rootmap;
		}
		
		
		
		
		
		
	}
	
	public ArrayList<Pair<Integer, Double>> SolveCubic(double[] p)
	{
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();

		int rat2 = 2, rat3 = 3;
		double q0 = p[0] / p[3];
		double q1 = p[1] / p[3];
		double q2 = p[2] / p[3];
		double q2third = q2 / rat3;
		double c0 = q0 - q2third * (q1 - rat2 * q2third * q2third);
		double c1 = q1 - q2 * q2third;
		ArrayList<Pair<Integer, Double>> RootmapLocal = SolveDepressedCubic(c0, c1);
		
		for (Pair<Integer, Double> rm: RootmapLocal) {
			
			double root = rm.getB() - q2third;
			Rootmap.add(new ValuePair<Integer, Double>(rm.getA(), root));
			
		}
		
		return Rootmap;
		
	}
	
	public ArrayList<Pair<Integer, Double>> SolveDepressedQuartic(double c0, double c1, double c2){
		ArrayList<Pair<Integer, Double>> Rootmap = new ArrayList<Pair<Integer, Double>>();
		
		
		double zero = 0;
		if (c0 == zero)
		{
			ArrayList<Pair<Integer, Double>> RootmapLocal =	SolveDepressedCubic(c1, c2);
			
	         for (Pair<Integer, Double> rm: RootmapLocal) {
				
				if(rm.getB()!=0) {
					
					//The quadratic does not have a root of zero, Insert one for cubic
					
					Rootmap.add(new ValuePair<Integer, Double>(1, zero));
				}
			}
			
			return Rootmap;
			
			
			
		}
		
		if (c1 == zero)
		{
			Rootmap =	SolveBiquadratic(c0, c2);

			return Rootmap;
			
		}
		
		double rat2 =2 , rat4 = 4, rat8 = 8, rat12 = 12, rat16 = 16, rat27 = 27, rat36 = 36;
		double c0sqr = c0 * c0, c1sqr = c1 * c1, c2sqr = c2 * c2;
		double delta = c1sqr * (-rat27 * c1sqr + rat4 * c2 * (rat36 * c0 -c2sqr)) + rat16 * c0 * (c2sqr * (c2sqr -rat8 * c0) + rat16 * c0sqr);
		double a0 = rat12 * c0 + c2sqr;
		double a1 = rat4 * c0 - c2sqr;
		
		if (delta > zero)
		{
			
			
			if (c2 < zero && a1 < zero)
			{
				
				// Four simple roots
				ArrayList<Pair<Integer, Double>> RootmapLocal = SolveCubic(new double[] {c1sqr - rat4 * c0 *c2, rat8 * c0, rat4 * c2, -rat8});
				
				double t = RootmapLocal.get(RootmapLocal.size() - 1).getB();
				double alphaSqr = rat2 * t - c2;
				double alpha = Math.sqrt(alphaSqr);
				double sgnC1;
				
				if (c1 > zero)
				{
					
					sgnC1 = 1.0;
					
				}
				
				else
				{
					
					sgnC1 = -1.0;
					
				}
				
				
				double arg = t * t - c0;
				double beta = (sgnC1 * Math.sqrt(Math.max(arg, 0)));
				double D0 = alphaSqr - rat4 * (t + beta);
				double sqrtD0 =  Math.sqrt(Math.max(D0, 0));
				double D1 = alphaSqr - rat4 * (t - beta);
				double sqrtD1 = Math.sqrt(Math.max(D1, 0));
				double root0 = (alpha - sqrtD0) / rat2;
				double root1 = (alpha + sqrtD0) / rat2;
				double root2 = (- alpha - sqrtD1) / rat2;
				double root3 = (- alpha + sqrtD1) / rat2;
				
				Rootmap.add(new ValuePair<Integer, Double>( 1, root0));
				Rootmap.add(new ValuePair<Integer, Double>( 1, root1));
				Rootmap.add(new ValuePair<Integer, Double>( 1, root2));
				Rootmap.add(new ValuePair<Integer, Double>( 1, root3));
				
				
				
				
			}
			
			else {
				
				// c2>=0 or a1 >=0
				// Roots are complex
				
			}
			return Rootmap;
			
		}
		
		else if (delta < zero)
		{
			
			
			
			// Two simple real roots, one complex conjugate pair
			ArrayList<Pair<Integer, Double>> RootmapLocal = SolveCubic(new double[] {c1sqr - rat4 * c0 * c2, rat8 * c0, rat4 * c2, -rat8});
			double t = RootmapLocal.get(RootmapLocal.size() - 1).getB();
			double alphaSqr = rat2* t - c2;
			double alpha = Math.sqrt(Math.max(alphaSqr, 0));
			double sgnC1;
			if (c1 > zero)
			{
				sgnC1 = 1.0;
				
			}
			
			else
			{
				
				sgnC1 = -1.0;
				
			}
			
			double arg = t * t - c0;
			double beta = (sgnC1 * Math.sqrt(Math.max(arg, 0)));
			double root0, root1;
			if (sgnC1 > 0)
			{
				
				double D1 = alphaSqr - rat4 * (t - beta);
					double sqrtD1 = Math.sqrt(Math.max(D1, 0));
					root0 = (-alpha -sqrtD1) / rat2;
					root1 = (-alpha + sqrtD1) / rat2;
				
			}
			else 
			{
			
				double D0 = alphaSqr - rat4 * (t + beta);
				double sqrtD0 = Math.sqrt(Math.max(D0, 0));
				root0 = (alpha - sqrtD0) / rat2;
				root1 = (alpha + sqrtD0) / rat2;
				
				
			}
			Rootmap.add(new ValuePair<Integer, Double> (1, root0));
			Rootmap.add(new ValuePair<Integer, Double> (1, root1));

		return Rootmap;	
			
		}
		else { // delta = 0
			
			
			if (a1 > zero || (c2 > zero && (a1!=zero || c1!=zero)))
			{
				
				// One double real root
				int rat9 = 9;
				double root0 = -c1 * a0 / (rat9 * c1sqr -rat2 * c2 * a1);
				Rootmap.add(new ValuePair<Integer, Double> (2, root0));

				
			}
			else
			{
				
				int rat3 = 3;
				if (a0!= zero)
				{
					
					// One double real root, two simple real roots
					int rat9 = 9;
					double root0 = -c1 * a0 / (rat9 * c1sqr - rat2 * c2 * a1);
					double alpha = rat2 * root0;
					double beta = c2 + rat3 * root0 * root0;
					double discr = alpha * alpha - rat4 * beta;
					double temp1 = Math.sqrt(discr);
					double root1 = (-alpha - temp1) / rat2;
					double root2 = (-alpha + temp1) / rat2;
					Rootmap.add(new ValuePair<Integer, Double> (2, root0));

					Rootmap.add(new ValuePair<Integer, Double> (1, root1));

					Rootmap.add(new ValuePair<Integer, Double> (1, root2));

					
				}
				else
				{
					
					// One triple real root, one simple real root
					
					double root0 = -rat3 * c1 / (rat4 * c2);
					double root1 = -rat3 * root0;
					
					Rootmap.add(new ValuePair<Integer, Double> (3, root0));

					Rootmap.add(new ValuePair<Integer, Double> (1, root1));
					
					
				}
				
				
			}
			
			
		return Rootmap;	
			
		}
		
		
		
		
	}
	
	
}
