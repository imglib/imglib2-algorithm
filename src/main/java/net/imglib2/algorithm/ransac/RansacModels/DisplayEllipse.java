package net.imglib2.algorithm.ransac.RansacModels;

import ij.gui.EllipseRoi;

public class DisplayEllipse {

	
	
	/**
	 * 2D correlated Gaussian
	 * 
	 * @param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public static EllipseRoi create2DEllipse(final double[] mean, final double[] cov) {
		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);
		final double scale1 = Math.sqrt(0.5 * (a + c + d)) ;
		final double scale2 = Math.sqrt(0.5 * (a + c - d)) ;
		final double theta = 0.5 * Math.atan2((2 * b), (a - c));
		final double x = mean[0];
		final double y = mean[1];
		final double dx = scale1 * Math.cos(theta);
		final double dy = scale1 * Math.sin(theta);
		final EllipseRoi ellipse = new EllipseRoi(x - dx, y - dy, x + dx, y + dy, scale2 / scale1);
		
		System.out.println(ellipse.getLength() + " " + a + " " + b + " " + c + " " + mean[0] + " " + mean[1] + " " + mean[2]);
		return ellipse;
	}
	
	
}
