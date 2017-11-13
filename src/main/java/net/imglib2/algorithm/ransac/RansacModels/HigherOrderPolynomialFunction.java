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
package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import Jama.Matrix;
import Jama.QRDecomposition;

import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;

/**
 * @author Varun Kapoor, Stephan Preibisch
 */
public class HigherOrderPolynomialFunction extends AbstractFunction2D< HigherOrderPolynomialFunction > implements Polynomial< HigherOrderPolynomialFunction, Point >
{
	private static final long serialVersionUID = 5010369758205651325L;

	// For initial guesses for Newton Raphson
	final Random rndx = new Random( 43583458 );

	final int minNumPoints;
	final int degree;

	public final double[] coeff;

	public HigherOrderPolynomialFunction(final int degree)
	{
		this.degree = degree;
		this.minNumPoints = degree + 1;
		this.coeff = new double[degree + 1];
	}

	/**
	 * @return - the coefficients of the polynomial in x
	 */
	@Override
	public double getCoefficient( final int j ) { return coeff[j]; }

	@Override
	public int getMinNumPoints() { return minNumPoints; }

	/*
	 * This is a fit function for the polynomial of user chosen degree
	 */
	public void fitFunction( final Collection< Point > points ) throws NotEnoughDataPointsException
	{
		final int nPoints = points.size();

		if ( nPoints < minNumPoints )
			throw new NotEnoughDataPointsException("Not enough points, at least " + minNumPoints + " are necessary, available are " + nPoints );

		// Vandermonde matrix
		final double[][] vandermonde = new double[ nPoints ][ degree + 1 ];
		final double[] y = new double[ nPoints ];

		int i = 0;
		for ( final Point p : points )
		{
			final double x = p.getW()[ 0 ];

			for ( int j = 0; j <= degree; ++j )
				vandermonde[ i ][ j ] = NewtonRaphsonPolynomial.pow( x, j );

			y[ i++ ] = p.getW()[ 1 ];
		}

		final Matrix X = new Matrix(vandermonde);

		// create matrix from vector
		final Matrix Y = new Matrix(y, nPoints);

		// find least squares solution
		final QRDecomposition qr = new QRDecomposition(X);
		final Matrix coefficients = qr.solve(Y);

		for ( int j = degree; j >= 0; --j )
			this.coeff[ j ] = coefficients.get( j, 0 );
	}

	public void fitFunction2(final Collection< Point > points ) throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		final int numPoints = points.size();

		if ( numPoints < minNumPoints )
			throw new NotEnoughDataPointsException( "Not enough points, at least " + minNumPoints + " are necessary and available are: " + numPoints );

		// compute matrices
		final double[][] delta = new double[ degree + 1 ][ degree + 1 ];
		final double[] tetha = new double[ degree + 1 ];

		final double[] powCache = new double[ degree * 2 + 1 ];
		powCache[ powCache.length - 1 ] = 1;

		for ( final Point p : points )
		{
			final double x = p.getW()[ 0 ];
			final double y = p.getW()[ 1 ];

			double power = 1;
			for ( int d = 1; d < powCache.length; ++d )
			{
				power *= x;
				powCache[ powCache.length - 1 - d ] = power;
			}

			for ( int r = 0; r <= degree; ++r )
				for ( int c = 0; c <= degree; ++c )
					delta[ r ][ c ] += powCache[ r + c ];

			double mulY = y;

			for ( int d = 0; d <= degree; ++d )
			{
				tetha[ degree - d ] += mulY;
				mulY *= x;
			}
		}

		// invert matrix
		final Matrix deltaInv = MatrixFunctions.computePseudoInverseMatrix( new Matrix( delta ), 0.00001 );

		for ( int d = degree; d >= 0; --d )
		{
			this.coeff[ d ] = 0;

			for ( int i = 0; i <= degree; ++i )
				this.coeff[ d ] += deltaInv.get( degree - d, i ) * tetha[ i ];
		}
	}

	// Distance of a point from a polynomial
	@Override
	public double distanceTo( final Point point )
	{
		final double x1 = point.getW()[0];
		final double y1 = point.getW()[1];

		return new NewtonRaphsonPolynomial( rndx, degree ).run( x1, y1, coeff );
	}

	@Override
	public void set( final HigherOrderPolynomialFunction p )
	{
		for (int j = degree; j >= 0; j--)
			this.coeff[j] = p.getCoefficient(j);

		this.setCost(p.getCost());
	}

	@Override
	public HigherOrderPolynomialFunction copy()
	{
		final HigherOrderPolynomialFunction c = new HigherOrderPolynomialFunction( degree );

		for (int j = degree; j >= 0; j--)
			c.coeff[j] = getCoefficient(j);

		c.setCost( getCost() );

		return c;
	}

	@Override
	public int degree() { return degree; }

	// Horner's method to get y values correspoing to x
	@Override
	public double predict( final double x )
	{
		// horner's method
		double y = 0.0;
		for (int j = degree; j >= 0; j--)
			y = getCoefficient(j) + (x * y);
		return y;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		final ArrayList<Point> points = new ArrayList<Point>();

		points.add(new Point(new double[] { 1f, -3.95132f }));
		points.add(new Point(new double[] { 2f, 6.51205f }));
		points.add(new Point(new double[] { 3f, 18.03612f }));
		points.add(new Point(new double[] { 4f, 28.65245f }));
		points.add(new Point(new double[] { 5f, 42.05581f }));
		points.add(new Point(new double[] { 6f, 54.01327f }));
		points.add(new Point(new double[] { 7f, 64.58747f }));
		points.add(new Point(new double[] { 8f, 76.48754f }));
		points.add(new Point(new double[] { 9f, 89.00033f }));

		//for ( int n = 0; n < 100000; ++n )
		//	points.add(new Point(new double[] { 9f+n, 89.00033f }));
		
		final ArrayList<PointFunctionMatch> candidates = new ArrayList<PointFunctionMatch>();
		final ArrayList<PointFunctionMatch> inliersPoly = new ArrayList<PointFunctionMatch>();
		long startTime = System.nanoTime();
		for (final Point p : points)
			candidates.add(new PointFunctionMatch(p));

		final int degree = 2;
		// Using the polynomial model to do the fitting
		final HigherOrderPolynomialFunction regression = new HigherOrderPolynomialFunction(degree);

		regression.ransac( candidates, inliersPoly, 100, 0.1, 0.5 );

		System.out.println("inliers: " + inliersPoly.size());
		for ( final PointFunctionMatch p : inliersPoly )
			System.out.println( regression.distanceTo( p.getP1() ) );
		regression.fit(inliersPoly);
		System.out.println(" y = "  );
		for (int i = degree; i >= 0; --i)
			System.out.println(regression.getCoefficient(i) + "  " + "x" + "^"  + i );
		long totalTime = (System.nanoTime()- startTime)/1000;
		System.out.println("Time: " + totalTime);

	}

}
