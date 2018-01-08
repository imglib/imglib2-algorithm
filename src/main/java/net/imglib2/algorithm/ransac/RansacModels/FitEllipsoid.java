package net.imglib2.algorithm.ransac.RansacModels;


import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.apache.commons.math3.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import Jama.CholeskyDecomposition;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Adapted from BoneJ's FitEllipsoid.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt Modified by Varun Kapoor;
 */
public class FitEllipsoid
{
	/**
	 * <p>
	 * Ellipsoid fitting method by Yury Petrov.<br>
	 * Fits an ellipsoid in the form <i>Ax</i><sup>2</sup> +
	 * <i>By</i><sup>2</sup> + <i>Cz</i><sup>2</sup> + 2<i>Dxy</i> + 2<i>Exz</i>
	 * + 2<i>Fyz</i> + 2<i>Gx</i> + 2<i>Hy</i> + 2<i>Iz</i> = 1 <br>
	 * To an n * 3 array of coordinates.
	 * </p>
	 *
	 * @see <p>
	 *      <a href=
	 *      "http://www.mathworks.com/matlabcentral/fileexchange/24693-ellipsoid-fit"
	 *      >MATLAB script</a>
	 *      </p>
	 */
	
	public static Ellipsoid yuryPetrov( final double[][] points, final int ndims )
	{
		final int nPoints = points.length;
		
	
		
		if ( nPoints < 9 ) 
			throw new IllegalArgumentException( "Too few points; need at least 9 to calculate a unique ellipsoid" );

	
		RealMatrix MatrixD = new Array2DRowRealMatrix(nPoints, 9);

		
		if (ndims > 2) {
		for (int i = 0; i < nPoints; i++) {
			final double x = points[i][0];
			final double y = points[i][1];
			final double z = points[i][2];
			
			double xx = x*x;
			double yy =y*y;
			double zz = z*z;
			double xy = 2 * x *y;
			double xz = 2 * x * z;
			double yz = 2 * y * z;
		

			

			MatrixD.setEntry(i, 0, xx);
			MatrixD.setEntry(i, 1, yy);
			MatrixD.setEntry(i, 2, zz);
			MatrixD.setEntry(i, 3, xy);
			MatrixD.setEntry(i, 4, xz);
			MatrixD.setEntry(i, 5, yz);
			MatrixD.setEntry(i, 6, 2 * x);
			MatrixD.setEntry(i, 7, 2 * y);
			MatrixD.setEntry(i, 8, 2 * z);
			
			
			
		}
		
		RealMatrix dtd = MatrixD.transpose().multiply(MatrixD);
		
		
		// Create a vector of ones.
				RealVector ones = new ArrayRealVector(nPoints);
				ones.mapAddToSelf(1);

				// Multiply: d' * ones.mapAddToSelf(1)
				RealVector dtOnes = MatrixD.transpose().operate(ones);

				// Find ( d' * d )^-1
				DecompositionSolver solver = new SingularValueDecomposition(dtd)
						.getSolver();
				RealMatrix dtdi = solver.getInverse();

				// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
				RealVector v = dtdi.operate(dtOnes);
		
		
		
		return ellipsoidFromEquation( v );
		
		}
		
		else
		{
			
			for (int i = 0; i < nPoints; i++) {
				final double x = points[i][0];
				final double y = points[i][1];
				
				double xx = x*x;
				double yy =y*y;
				double xy = 2 * x *y;
			

				

				MatrixD.setEntry(i, 0, xx);
				MatrixD.setEntry(i, 1, yy);
				MatrixD.setEntry(i, 2, xy);
				MatrixD.setEntry(i, 3, 2 * x);
				MatrixD.setEntry(i, 4, 2 * y);
				
				
				
			}
			
			RealMatrix dtd = MatrixD.transpose().multiply(MatrixD);
			
			
			// Create a vector of ones.
					RealVector ones = new ArrayRealVector(nPoints);
					ones.mapAddToSelf(1);

					// Multiply: d' * ones.mapAddToSelf(1)
					RealVector dtOnes = MatrixD.transpose().operate(ones);

					// Find ( d' * d )^-1
					DecompositionSolver solver = new SingularValueDecomposition(dtd)
							.getSolver();
					RealMatrix dtdi = solver.getInverse();

					// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
					RealVector v = dtdi.operate(dtOnes);
			
			
			
			return ellipsoidFromEquation2D( v );
			
			
			
		}
		
	}

	/**
	 * Calculate the matrix representation of the ellipsoid from the equation variables
	 * <i>ax</i><sup>2</sup> + <i>by</i><sup>2</sup> + <i>cz</i><sup>2</sup> +
	 * 2<i>dxy</i> + 2<i>exz</i> + 2<i>fyz</i> + 2<i>gx</i> + 2<i>hy</i> +
	 * 2<i>iz</i> = 1 <br />
	 *
	 * @param V vector (a,b,c,d,e,f,g,h,i)
	 * @return the ellipsoid.
	 */
	private static Ellipsoid ellipsoidFromEquation( final RealVector V )
	{
		final double a = V.getEntry(0);
		final double b = V.getEntry( 1);
		final double c = V.getEntry( 2);
		final double d = V.getEntry( 3);
		final double e = V.getEntry( 4);
		final double f = V.getEntry( 5);
		final double g = V.getEntry( 6);
		final double h = V.getEntry( 7);
		final double i = V.getEntry( 8);

		double[] Coefficents = V.toArray();
		
		
		final double[][] aa = new double[][] {
				{ a, d, e },
				{ d, b, f },
				{ e, f, c } };
		final double[] bb = new double[] { g, h, i };
		final double[] cc = new Matrix( aa ).solve( new Matrix( bb, 3 ) ).getRowPackedCopy();
		LinAlgHelpers.scale( cc, -1, cc );

		final double[] At = new double[ 3 ];
		LinAlgHelpers.mult( aa, cc, At );
		final double r33 = LinAlgHelpers.dot( cc, At ) + 2 * LinAlgHelpers.dot( bb, cc ) - 1;
		LinAlgHelpers.scale( aa, -1 / r33, aa );
		int n = cc.length;
		double[][] covariance = new Matrix(aa).inverse().getArray();	
		return (new Ellipsoid( cc, covariance , aa, null, computeAxisAndRadiiFromCovariance(covariance, n), Coefficents ));
	}
	private static Ellipsoid ellipsoidFromEquation2D( final RealVector V )
	{
		final double a = V.getEntry(0);
		final double b = V.getEntry( 1);
		final double c = V.getEntry( 2);
		final double d = V.getEntry( 3);
		final double e = V.getEntry( 4);
		double[] Coefficents = V.toArray();

		
		final double[][] aa = new double[][] {
				{ a, c },
				{ c, b } };
		final double[] bb = new double[] { d, e };
		final double[] cc = new Matrix( aa ).solve( new Matrix( bb, 2 ) ).getRowPackedCopy();
		LinAlgHelpers.scale( cc, -1, cc );
		final double[] At = new double[ 2 ];
		LinAlgHelpers.mult( aa, cc, At );
		final double r33 = LinAlgHelpers.dot( cc, At ) + 2 * LinAlgHelpers.dot( bb, cc ) - 1;
		LinAlgHelpers.scale( aa, -1 / r33, aa );
		int n = cc.length;
		double[][] covariance = new Matrix(aa).inverse().getArray();	
		return (new Ellipsoid( cc, covariance , aa, null, computeAxisAndRadiiFromCovariance(covariance, n), Coefficents ));
	}
	
	
	private static double[] computeAxisAndRadiiFromCovariance(double[][] covariance, int n)
	{
		final EigenvalueDecomposition eig = new Matrix( covariance ).eig();
		final Matrix ev = eig.getD();
		double[] radii = new double[ n ];
		for ( int d = 0; d < n; ++d )
			radii[ d ] = Math.sqrt( ev.get( d, d ) );
		return radii;
	}

	
	
}
	
