package net.imglib2.algorithm.fitting.ellipsoid;

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
				LinAlgHelpers.toString( getPrecision() );
	}
}
