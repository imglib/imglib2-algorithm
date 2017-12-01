package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import net.imglib2.RealLocalizable;
import net.imglib2.util.Pair;

public class Intersections {

	/**
	 * Takes in the GeneralEllipsoid of the form a x^2 + b y^2 + c z^2 + 2 d xy + 2
	 * e xz + 2 f yz + 2 gx + 2 hy + 2 iz = 1
	 * 
	 * In 2D a x^2 + b y^2 + 2 d xy + 2 gx + 2 hy = 1
	 * 
	 * For a chosen Z plane, determine the intersection to the ellipse to another
	 * ellipse by Dividing the form above with the coefficient of y^2 (b) to reduce
	 * the ellipse to the form
	 * 
	 * A(x,y) = a0 + a1 x + a2 y + a3 x^2 + a4 xy + y^2
	 * 
	 * 
	 * a0 = -1/b, a1 = 2 g / b,a2 = 2 h / b, a3 = a/b, a4 = 2 d / b
	 * 
	 * @ V Kapoor
	 */

	public static ArrayList<double[]> PointsofIntersection(final Pair<Ellipsoid, Ellipsoid> Ellipsepair) {

		

		
		
		Ellipsoid EllipseA = Ellipsepair.getA();
		Ellipsoid EllipseB = Ellipsepair.getB();
		
		final double[] coefficients = EllipseA.getCoefficients();

		final double a = coefficients[0];
		final double b = coefficients[1];
		final double d = coefficients[2];
		final double g = coefficients[3];
		final double h = coefficients[4];

		final double a0 = -1.0 / b;
		final double a1 = 2.0 * g / b;
		final double a2 = 2.0 * h / b;
		final double a3 = a / b;
		final double a4 = 2 * d / b;
		final double[] coefficientsSec = EllipseB.getCoefficients();

		final double aSec = coefficientsSec[0];
		final double bSec = coefficientsSec[1];
		final double dSec = coefficientsSec[2];
		final double gSec = coefficientsSec[3];
		final double hSec = coefficientsSec[4];

		final double a0Sec = -1.0 / bSec;
		final double a1Sec = 2.0 * gSec / bSec;
		final double a2Sec = 2.0 * hSec / bSec;
		final double a3Sec = aSec / bSec;
		final double a4Sec = 2 * dSec / bSec;

		final double d0 = a0 - a0Sec;
		final double d1 = a1 - a1Sec;
		final double d2 = a2 - a2Sec;
		final double d3 = a3 - a3Sec;
		final double d4 = a4 - a4Sec;
		final double[] dVector = { d0, d1, d2, d3, d4 };

		final double e0 = d0 - a2 * d2 / 2;
		final double e1 = d1 - (a2 * d4 + a4 * d2) / 2;
		final double e2 = d3 - a4 * d4 / 2;

		final double c0 = a0 - a2 * a2 / 4;
		final double c1 = a1 - a2 * a4 / 2;
		final double c2 = a3 - a4 * a4 / 2;

		for (int i = 0; i < dVector.length; ++i) {

			if (dVector[i] == 0) {

				System.out.println("Ellipses are identical");
				return null;

			}

		}

		// Finding points of intersection in different cases
		double xbar = -d2 / d4;
		double e2xbar = e0 + e1 * xbar + e2 * xbar * xbar;
		double veto = 1.0E-1;
		ArrayList<double[]> intersection = new ArrayList<>();
		if (c2 > 0 && c1 * c1 > 4 * c0 * c2) {
			
		
		if (Math.abs(d4) > veto && Math.abs(e2xbar) >= 1.0E-3) {

			// Listing 1 and 2 David Eberly, intersection of ellipses text

			System.out.println("Intersection: Listing 1 Solution" + d4);
		
			double f0 = c0 * d2 * d2 + e0 * e0;
			double f1 = c1 * d2 * d2 + 2 * (c0 * d2 * d4 + e0 * e1);
			double f2 = c2 * d2 * d2 + c0 * d4 * d4 + e1 * e1 + 2 * (c1 * d2 * d4 + e0 * e2);
			double f3 = c1 * d4 * d4 + 2 * (c2 * d2 * d4 + e1 * e2);
			double f4 = c2 * d4 * d4 + e2 * e2;
	
			ArrayList<Pair<Integer, Double>> RootMap = Solvers.SolveQuartic(new double[] { f0, f1, f2, f3, f4 });

			for (Pair<Integer, Double> rm : RootMap) {

				double x = rm.getB();
				double w = -(e0 + x * (e1 + x * e2)) / (d2 + d4 * x);
				double y = w - (a2 + x * a4) / 2;
				intersection.add(new double[] { x, y });

			}


		}

		else	 if (Math.abs(d4) > veto && Math.abs(e2xbar) <= 1.0E-3) {

			// Listing 2 David Eberly text, intersection of ellipses
			System.out.println("Intersection: Listing 2 Solution");
			double translate, w, y;

			// Compute intersections of x = xbar with ellipse
			double ncbar = -(c0 + xbar * (c1 + xbar * c2));

			if (ncbar >= 0) {

				translate = (a2 + xbar * a4) / 2;
				w = Math.sqrt(ncbar);
				y = w - translate;
				intersection.add(new double[] { xbar, y });
				if (w > 0) {

					w = -w;
					y = w - translate;
					intersection.add(new double[] { xbar, y });

				}

			}
			// Compute intersections of w = -h(x)with ellipse

			double h1 = e2 / d4;
			double h0 = (e1 - d2 * h1) / d4;
			double f0 = c0 + h0 * h0;
			double f1 = c1 + 2 * h0 * h1;
			double f2 = c2 + h1 * h1;

			ArrayList<Pair<Integer, Double>> RootMap = Solvers.SolveQuartic(new double[] { f0, f1, f2 });

			for (Pair<Integer, Double> rm : RootMap) {

				double x = rm.getB();
				translate = (a2 + xbar * a4) / 2;
				w = -(h0 + x * h1);
				y = w - translate;
				intersection.add(new double[] { x, y });
			}


		}

		if (Math.abs(d4) <= veto && d2 != 0 && e2 != 0) {
//			if (d2 != 0 && e2 != 0) {
			// Listing 3 David Eberly, interesection of ellipses text
			System.out.println("Intersection: Listing 3 Solution");
			double f0 = c0 * d2 * d2 + e0 * e0;
			double f1 = c1 * d2 * d2 + 2 * e0 * e1;
			double f2 = c2 * d2 * d2 + e1 * e1 + 2 * e0 * e2;
			double f3 = 2 * e1 * e2;
			double f4 = e2 * e2;
			ArrayList<Pair<Integer, Double>> RootMap = Solvers.SolveQuartic(new double[] { f0, f1, f2, f3, f4 });

			for (Pair<Integer, Double> rm : RootMap) {

				double x = rm.getB();
				double w = -(e0 + x * (e1 + x * e2)) / d2;
				double y = w - (a2 + x * a4) / 2;
				intersection.add(new double[] { x, y });

			}

		}

		else if (Math.abs(d4) <= veto && d2 != 0 && Math.abs(e2) <= 1.0E-3) {
	//		else if (d2 != 0 && Math.abs(e2) <= 1.0E-3) {
			
			// Listing 4 David Eberly, interesection of ellipses text
			System.out.println("Intersection: Listing 4 Solution");
			double f0 = c0 * d2 * d2 + e0 * e0;
			double f1 = c1 * d2 * d2 + 2 * e0 * e1;
			double f2 = c2 * d2 * d2 + e1 * e1;

			ArrayList<Pair<Integer, Double>> RootMap = Solvers.SolveQuartic(new double[] { f0, f1, f2 });

			for (Pair<Integer, Double> rm : RootMap) {

				double x = rm.getB();
				double w = -(e0 + x * e1) / d2;
				double y = w - (a2 + x * a4) / 2;
				intersection.add(new double[] { x, y });

			}


		}

		else 	if (Math.abs(d4) <= veto && Math.abs(d2) <= 1.0E-3 && Math.abs(e2) <= 1.0E-3) {
		//		else 	if (Math.abs(d2) <= 1.0E-3 && Math.abs(e2) <= 1.0E-3) {

			// Listing 5 David Eberly, intersection of ellipses text
			System.out.println("Intersection: Listing 5 Solution");
			double w, y;

			double xhat = -e0 / e1;
			double nchat = -(c0 + xhat * (c1 + xhat * c2));

			if (nchat > 0) {

				double translate = (a2 + xhat * a4) / 2;

				w = Math.sqrt(nchat);
				y = w - translate;
				intersection.add(new double[] { xhat, y });

				w = -w;
				y = w - translate;
				intersection.add(new double[] { xhat, y });

			}

			else if (nchat == 0) {

				y = -(a2 + xhat * a4) / 2;
				intersection.add(new double[] { xhat, y });

			}


		}

		else if (Math.abs(d4) <= veto && Math.abs(d2) <= 1.0E-3 && e2 != 0) {
		//		else if (Math.abs(d2) <= 1.0E-3 && e2 != 0) {

			
			// Listing 6 David Eberly intersection of ellipses
			System.out.println("Intersection: Listing 6 Solution");
			
			ResultRoot result = null;
			ResultRoot resultA;
			ResultRoot resultB;
			double f0 = e0 / e2, f1 = e1 / e2;
			double mid = -f1 / 2;
			double discr = mid * mid - f0;
			if (discr > 0) {

				// The roots are xhat = mid + s* sqrtDiscr for s in {-1, 1}
				double sqrtDiscr = Math.sqrt(discr);
				double g0 = c0 - c2 * f0, g1 = c1 - c2 * f1;

				if (g1 > 0) {

					// We need s * sqrt(discr) < = -g0 / g1 + fi/2
					double r = -g0 / g1 - mid;

					// s =+1;

					if (r >= 0) {

						double rsqr = r * r;
						if (discr < rsqr) {

							result = SpecialIntersection(mid + sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
						}

						else if (discr == rsqr) {

							result = SpecialIntersection(mid + sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);
						}

					}

					// s = -1;

					if (r > 0) {

						result = SpecialIntersection(mid - sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
					}

					else {

						double rsqr = r * r;
						if (discr > rsqr) {

							result = SpecialIntersection(mid - sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);

						}

						else if (discr == rsqr) {

							result = SpecialIntersection(mid - sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);
						}

					}

				}

				else if (g1 < 0) {

					// We need s* sqrt(discr) >= -g0/g1 + f1/2

					double r = -g0 / g1 - mid;

					// s = -1
					if (r <= 0) {

						double rsqr = r * r;
						if (discr < rsqr) {

							result = SpecialIntersection(mid - sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);

						} else {

							result = SpecialIntersection(mid - sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);
						}

					}
					// s = +1;
					if (r < 0) {

						result = SpecialIntersection(mid + sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
					} else {

						double rsqr = r * r;
						if (discr > rsqr) {
							result = SpecialIntersection(mid + sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
						}

						else if (discr == rsqr) {
							result = SpecialIntersection(mid + sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);

						}

					}

				}

				else // g1 = 0
				{

					if (g0 < 0) {
						resultA = SpecialIntersection(mid - sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
						resultB = SpecialIntersection(mid + sqrtDiscr, true, a2Sec, a4Sec, c0, c1, c2);
						Vector<double[]> vec = resultA.intersection;
						vec.addAll(resultB.intersection);
						result = new ResultRoot(vec);
					} else if (g0 == 0) {

						resultA = SpecialIntersection(mid - sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);
						resultB = SpecialIntersection(mid + sqrtDiscr, false, a2Sec, a4Sec, c0, c1, c2);

						Vector<double[]> vec = resultA.intersection;
						vec.addAll(resultB.intersection);
						result = new ResultRoot(vec);

					}

				}

			}

			else if (discr == 0) {

				double nchat = -(c0 + mid * (c1 + mid * c2));
				if (nchat > 0) {
					result = SpecialIntersection(mid, true, a2Sec, a4Sec, c0, c1, c2);

				} else if (nchat == 0) {

					result = SpecialIntersection(mid, false, a2Sec, a4Sec, c0, c1, c2);
				}

			}

			intersection.addAll(result.intersection);
		}
		
	
		}
		
		return intersection;
		

	}

	public static ResultRoot SpecialIntersection(double x, boolean transverse, double a2, double a4, double c0, double c1,
			double c2) {

		Vector<double[]> intersection = new Vector<double[]>();
		if (transverse) {

			double translate = (a2 + x * a4) / 2;
			double nc = -(c0 + x * (c1 + x * c2));

			if (nc < 0) {

				// Clamp to eliminate the rounding error, but duplicate the point because we
				// know that it is a transverse interesection

				nc = 0;
			}

			double w = Math.sqrt(nc);
			double y = w - translate;
			intersection.add(new double[] { x, y });
			w = -w;
			y = w - translate;
			intersection.add(new double[] { x, y });

		}

		else {

			// The vertical line at the root is tangent to the ellipse

			double y = -(a2 + x * a4) / 2;
			intersection.add(new double[] { x, y });

		}

		ResultRoot finalroot = new ResultRoot(intersection);

		return finalroot;

	}

}
