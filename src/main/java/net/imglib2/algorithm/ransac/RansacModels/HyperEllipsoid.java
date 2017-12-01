package net.imglib2.algorithm.ransac.RansacModels;

import java.util.Collection;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import net.imglib2.AbstractRealLocalizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.AbstractEllipsoid;
import net.imglib2.roi.geom.real.Ellipsoid;
import net.imglib2.util.LinAlgHelpers;

/**
 * Hyperellipsoid in n dimensions.
 *
 * <p>
 * Points <em>x</em> on the ellipsoid are <em>(x - c)^T * M * (x - c) = 1</em>, where
 * <em>c = </em>{@link #getCenter()} and <em>M = </em>{@link #getPrecision()}.
 *
 * <p>
 * <em>M = R * D * R^T</em>, where <em>D</em> is diagonal with entries <em>1/e_i^2</em> and <em>e_i</em> are radii {@link #getRadii()}.
 * <em>R</em> is orthogonal matrix whose columns are the axis directions, that is, <em>R^T = </em>{@link #getAxes()}.
 *
 * <p>
 * To rotate a point <em>x</em> into ellipsoid coordinates (axis-aligned ellipsoid) compute <em>R^T * x</em>.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt; Modified to implement Ellipsoid of ImageJ-roi by V. Kapoor
 * */

public class HyperEllipsoid extends AbstractEllipsoid {

	
	private double[][] axes;

	private double[] radii;

	private double[][] covariance;

	private double[][] precision;
	
	private double[] Coefficients;
	

	/**
	 * Construct hyperellipsoid. Some of the parameters may be null. The center
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
	protected HyperEllipsoid( final double[] center, final double[][] covariance, final double[][] precision, final double[][] axes, final double[] radii )
	{
		super( center, radii );
		this.axes = axes;
		this.radii = radii;
		this.covariance = covariance;
		this.precision = precision;
	}
	
	
	protected HyperEllipsoid(final double[] center, final double[][] covariance, final double[][] precision, final double[][] axes, final double[] radii , final double[] Coefficients) {
		
		super(center, radii);
		this.axes = axes;
		this.radii = radii;
		this.covariance = covariance;
		this.precision = precision;
		this.Coefficients = Coefficients;
		
	}
	

	/**
	 * Get Coefficient matrix for ellipsoid in quadratic form.
	 *
	 * @return quadratic form coefficients.
	 */
	public double[] getCoefficients()
	{
		return Coefficients;
	}

	/**
	 * Get coordinates of center.
	 *
	 * @return center coordinates.
	 */
	public double[] getCenter()
	{
		return this.center;
	}

	/**
	 * Get axes as unit vectors.
	 * Indices are {@code axes[axisIndex][dimensionIndex]}.
	 *
	 * @return axes as array of unit vectors.
	 */
	public double[][] getAxes()
	{
		if ( axes == null )
		{
			if ( covariance != null )
				computeAxisAndRadiiFromCovariance();
			else
				computeAxisAndRadiiFromPrecision();
		}
		return axes;
	}

	
	/**
	 * Get array of radius along each {@link #getAxes() axis}.
	 *
	 * @return radii.
	 */
	public double[] getRadii()
	{
		if ( radii == null )
		{
			if ( covariance != null )
				computeAxisAndRadiiFromCovariance();
			else
				computeAxisAndRadiiFromPrecision();
		}
		return radii;
	}

	/**
	 * Get the covariance matrix.
	 *
	 * @return covariance matrix.
	 */
	public double[][] getCovariance()
	{
		if ( covariance == null )
		{
			if ( precision != null )
				computeCovarianceFromPrecision();
			else
				computeCovarianceFromAxesAndRadii();
		}
		return covariance;
	}

	/**
	 * Get the covariance matrix.
	 *
	 * @param m is set to covariance matrix.
	 */
	public void getCovariance( final double[][] m )
	{
		LinAlgHelpers.copy( getCovariance(), m );
	}

	/**
	 * Get the precision (inverse covariance) matrix.
	 *
	 * @return precision matrix.
	 */
	public double[][] getPrecision()
	{
		if ( precision == null )
		{
			if ( covariance != null )
				computePrecisionFromCovariance();
			else
				computePrecisionFromAxesAndRadii();
		}
		return precision;
	}
	
	public boolean test( final double[] point )
	{
		final double[] x = new double[ n ];
		final double[] y = new double[ n ];
		LinAlgHelpers.subtract( point, getCenter(), x );
		LinAlgHelpers.mult( getPrecision(), x, y );
		return LinAlgHelpers.dot( x, y ) <= 1;
	}
	@Override
	public boolean test( final RealLocalizable point )
	{
		final double[] p = new double[ n ];
		point.localize( p );
		return test( p );
	}

	private void computeCovarianceFromAxesAndRadii()
	{
		final double[][] tmp = new double[ n ][];
		covariance = new double[ n ][];
		for ( int d = 0; d < n; ++d )
		{
			tmp[ d ] = new double[ n ];
			covariance[ d ] = new double[ n ];
			LinAlgHelpers.scale( axes[ d ], radii[ d ] * radii[ d ], tmp[ d ] );
		}
		LinAlgHelpers.multATB( axes, tmp, covariance );
	}

	private void computeCovarianceFromPrecision()
	{
		covariance = new Matrix( precision ).inverse().getArray();
	}
	
	private void computePrecisionFromAxesAndRadii()
	{
		final double[][] tmp = new double[ n ][];
		precision = new double[ n ][];
		for ( int d = 0; d < n; ++d )
		{
			tmp[ d ] = new double[ n ];
			precision[ d ] = new double[ n ];
			LinAlgHelpers.scale( axes[ d ], 1.0 / ( radii[ d ] * radii[ d ] ), tmp[ d ] );
		}
		LinAlgHelpers.multATB( axes, tmp, precision );
	}

	private void computePrecisionFromCovariance()
	{
		precision = new Matrix( covariance ).inverse().getArray();
	}

	private void computeAxisAndRadiiFromPrecision()
	{
		final EigenvalueDecomposition eig = new Matrix( precision ).eig();
		axes = eig.getV().transpose().getArray();
		final Matrix ev = eig.getD();
		radii = new double[ n ];
		for ( int d = 0; d < n; ++d )
			radii[ d ] = Math.sqrt( 1 / ev.get( d, d ) );
	}

	private void computeAxisAndRadiiFromCovariance()
	{
		final EigenvalueDecomposition eig = new Matrix( covariance ).eig();
		axes = eig.getV().transpose().getArray();
		final Matrix ev = eig.getD();
		radii = new double[ n ];
		for ( int d = 0; d < n; ++d )
			radii[ d ] = Math.sqrt( ev.get( d, d ) );
	}

	@Override
	public double exponent() {

		return 2;
	}

	@Override
	public double semiAxisLength(int d) {

		
		return radii[d];
	}

	@Override
	public RealPoint center() {
		
		
		
		return this.center();
	}

	@Override
	public void setSemiAxisLength(int d, double length) {
		radii[d] = length;
		
	}

	



	// -- Helper methods --

	/**
	 * Computes the unit distance squared between a given location and the
	 * center of the ellipsoid.
	 *
	 * @param l
	 *            location to check
	 * @return squared unit distance
	 */
	protected double distancePowered( final RealLocalizable l )
	{
		assert ( l.numDimensions() >= n ): "l must have no less than " + n + " dimensions";

		double distancePowered = 0;
		for ( int d = 0; d < n; d++ )
			distancePowered += ( ( l.getDoublePosition( d ) - getCenter()[ d ] ) / radii[ d ] ) * ( ( l.getDoublePosition( d ) - getCenter()[ d ] ) / radii[ d ] );

		return distancePowered;
	}

	@Override
	public double realMin(int d) {
		
		return getCenter()[d] - radii[d];
	}

	@Override
	public double realMax(int d) {

		return getCenter()[d] + radii[d];
	}
}
