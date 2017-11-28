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

	public static Vector<double[]> PointsofIntersection(Ellipsoid EllipseA, Ellipsoid EllipseB) {

		
		
		
		double[] centerA = EllipseA.getCenter();
		
		RealVector VxA = new ArrayRealVector(EllipseA.getAxes().length);
		RealVector VyA = new ArrayRealVector(EllipseA.getAxes().length);
		for (int i = 0; i < EllipseA.getAxes().length ; ++i) {
			
			
			
			VxA.setEntry(i,EllipseA.getAxes()[i][0]);
			VyA.setEntry(i,EllipseA.getAxes()[i][1]);
			
		}
		
		double[] LA = EllipseA.getRadii();
		RealMatrix UUA = VxA.outerProduct(VxA).scalarMultiply(1.0/(LA[0] * LA[0])).add(VxA.outerProduct(VxA).scalarMultiply(1.0/(LA[1] * LA[1]))) ;
		double mA00 = UUA.getEntry(0, 0);
		double mA11 = UUA.getEntry(1, 1);
		double mA01 = UUA.getEntry(0, 1);
		double mA10 = UUA.getEntry(1, 0);
		
		double[] centerB = EllipseB.getCenter();
		
		RealVector VxB = new ArrayRealVector(EllipseB.getAxes().length);
		RealVector VyB = new ArrayRealVector(EllipseB.getAxes().length);
		
	    for (int i = 0; i < EllipseB.getAxes().length ; ++i) {
			
			VxB.setEntry(i,EllipseB.getAxes()[i][0]);
			VyB.setEntry(i,EllipseB.getAxes()[i][1]);
			
		}
		
		
		
		double[] LB = EllipseB.getRadii();
		
		RealMatrix UUB = VxB.outerProduct(VxB).scalarMultiply(1.0/(LB[0] * LB[0])).add(VxB.outerProduct(VxB).scalarMultiply(1.0/(LB[1] * LB[1]))) ;
		double mB00 = UUB.getEntry(0, 0);
		double mB11 = UUB.getEntry(1, 1);
		double mB01 = UUB.getEntry(0, 1);
		double mB10 = UUB.getEntry(1, 0);
		
		
		
		

		double a0 = ( mA00 * centerA[0] * centerA[0] + mA11 * centerA[1] * centerA[1] + (mA01 + mA10) * ( centerA[0] * centerA[1]) );
		double a1 = (-2 * centerA[0] * mA00 - centerA[1] * (mA01 + mA10)  );
		double a2 = (-2 * centerA[1] -centerA[0] * (mA01 + mA10));
		double a3 = mA00;
		double a4 = mA01 + mA10;
		double a5 = mA11;
		
		a0 = a0 / a5;
		a1 = a1 / a5;
		a2 = a2 / a5;
		a3 = a3 / a5;
		a4 = a4 / a5;
		
		
	

		double a0Sec = ( mB00 * centerB[0] * centerB[0] + mA11 * centerB[1] * centerB[1] + (mB01 + mB10) * ( centerB[0] * centerB[1]) );
		double a1Sec = (-2 * centerB[0] * mB00 - centerB[1] * (mB01 + mB10)  );
		double a2Sec = (-2 * centerB[1] -centerB[0] * (mB01 + mB10));
		double a3Sec =  mB00;
		double a4Sec = mB01 + mB10;
		double a5Sec = mB11;

		a0Sec = a0Sec / a5Sec;
		a1Sec = a1Sec / a5Sec;
		a2Sec = a2Sec / a5Sec;
		a3Sec = a3Sec / a5Sec;
		a4Sec = a4Sec / a5Sec;
		
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
		System.out.println(centerA[0]);
		Vector<double[]> intersection = new Vector<>();
		if (d4 != 0 && e2xbar != 0) {

			// Listing 1 and 2 David Eberly, intersection of ellipses text

			System.out.println("Intersection: Listing 1 Solution");
			double f0 = c0 * d2 * d2 + e0 * e0;
			double f1 = c1 * d2 * d2 + 2 * (c0 * d2 * d4 + e0 * e1);
			double f2 = c2 * d2 * d2 + c0 * d4 * d4 + e1 + 2 * (c1 * d2 * d4 + e0 * e2);
			double f3 = c1 * d4 * d4 + 2 * (c2 * d2 * d4 + e1 * e2);
			double f4 = c2 * d4 * d4 + e2 * e2;

			ArrayList<Pair<Integer, Double>> RootMap = Solvers.SolveQuartic(new double[] { f0, f1, f2, f3, f4 });

			for (Pair<Integer, Double> rm : RootMap) {

				double x = rm.getB();
				double e2x = -(e0 + x * (e1 + x * e2));

				double w = e2x / (d2 + d4 * x);
				double y = w - (a2 + x * a4) / 2;

				intersection.add(new double[] { x, y });

			}

			return intersection;

		}

		if (d4 != 0 && e2xbar == 0) {

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

			return intersection;

		}

		if (d4 == 0 && d2 != 0 && e2 != 0) {

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

			return intersection;
		}

		if (d4 == 0 && d2 != 0 && e2 == 0) {

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

			return intersection;

		}

		if (d4 == 0 && d2 == 0 && e2 == 0) {

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

			return intersection;

		}

		if (d4 == 0 && d2 == 0 && e2 != 0) {
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

			return result.intersection;
		}

		else

			return null;

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
