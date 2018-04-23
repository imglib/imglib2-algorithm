package net.imglib2.algorithm.ransac.RansacModels.CircleFits;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import net.imglib2.algorithm.ransac.RansacModels.Ellipsoid;
import net.imglib2.util.LinAlgHelpers;

public class FitCircle {

	
	public static Ellipsoid yuryPetrov( final double[][] points, final int ndims )
	{
		final int nPoints = points.length;
		
	
		
		if ( nPoints < 6 ) 
			throw new IllegalArgumentException( "Too few points; need at least 6 to calculate a unique sphere" );

	
		RealMatrix MatrixD = new Array2DRowRealMatrix(nPoints, 6);

		
		if (ndims > 2) {
		for (int i = 0; i < nPoints; i++) {
			final double x = points[i][0];
			final double y = points[i][1];
			final double z = points[i][2];
			
			double xx = x*x;
			
		

			

			MatrixD.setEntry(i, 0, xx);
			MatrixD.setEntry(i, 1, 2 * x);
			MatrixD.setEntry(i, 2, 2 * y);
			MatrixD.setEntry(i, 3, 2 * z);
			
			
			
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
				
			

				

				MatrixD.setEntry(i, 0, xx);
				MatrixD.setEntry(i, 1, 2 * x);
				MatrixD.setEntry(i, 2, 2 * y);
				
				
				
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
		final double b = V.getEntry(0);
		final double c = V.getEntry(0);
		final double d = 0;
		final double e = 0;
		final double f = 0;
		final double g = V.getEntry(1);
		final double h = V.getEntry(2);
		final double i = V.getEntry(3);

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
		final double b = V.getEntry(0);
		final double c = 0;
		final double d = V.getEntry(1);
		final double e = V.getEntry(2);
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
