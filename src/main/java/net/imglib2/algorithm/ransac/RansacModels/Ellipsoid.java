package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;

import net.imglib2.util.LinAlgHelpers;

public class Ellipsoid extends HyperEllipsoid
	{
		/**
		 * Construct 3D ellipsoid. Some of the parameters may be null. The center
		 * parameter is always required. Moreover, either
		 * <ul>
		 * <li>covariance or</li>
		 * <li>precision or</li>
		 * <li>axes and radii</li>
		 * </ul>
		 * must be provided.
		 *
		 * @param center
		 *            coordinates of center. must not be {@code null}.
		 * @param covariance
		 * @param precision
		 * @param axes
		 * @param radii
		 */
		public Ellipsoid( final double[] center, final double[][] covariance, final double[][] precision, final double[][] axes, final double[] radii )
		{
			super( center, covariance, precision, axes, radii );
		}
		
		
		
		
		
		/**
		 * 
		 * In addition to the above ellipsoid, also include a representation of ellipsoid in a quadratic form
		 * The coefficient matrix contains these coefficients of such a form of the ellipsoid.
		 * 
		 * @param center
		 * @param covariance
		 * @param precision
		 * @param axes
		 * @param radii
		 * @param Coefficients
		 */
		
		public Ellipsoid(final double[] center, final double[][] covariance, final double[][] precision, final double[][] axes, final double[] radii,final double[] Coefficients) {
			
			
			super( center, covariance, precision, axes, radii, Coefficients );
		}
		

		@Override
		public String toString()
		{
			return "center = " +
					LinAlgHelpers.toString( getCenter() )
					+ "\nradii = " +
					LinAlgHelpers.toString( getRadii() )
					+ "\naxes = " +
					LinAlgHelpers.toString( getAxes() )
					+ "\nprecision = " +
					LinAlgHelpers.toString( getPrecision() )
			        +  "\nCoefficients = " +
					LinAlgHelpers.toString(getCoefficients());
		}

		
}
