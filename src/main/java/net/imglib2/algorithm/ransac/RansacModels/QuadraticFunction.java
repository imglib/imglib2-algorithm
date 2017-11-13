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

import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NoninvertibleModelException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;

/**
 * @author Stephan Preibisch and Varun Kapoor
 */
public class QuadraticFunction extends AbstractFunction2D< QuadraticFunction > implements Polynomial< QuadraticFunction, Point >
{
	private static final long serialVersionUID = 5289346951323596267L;

	// For initial guesses for Newton Raphson
	final Random rndx = new Random( 43583458 );

	final int minNumPoints = 3;
	double a, b, c; // a*x*x + b*x + c

	public QuadraticFunction() { this( 0, 0, 0 ); }
	public QuadraticFunction( final double a, final double b, final double c )
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public double getA(){ return a; }
	public double getB(){ return b; }
	public double getC(){ return c; }

	@Override
	public int degree() { return 2; }

	@Override
	public double getCoefficient( final int j )
	{
		if ( j == 0 )
			return c;
		else if ( j == 1 )
			return b;
		else if ( j == 2 )
			return a;
		else
			return 0;
	}

	@Override
	public double predict( final double x ) { return a*x*x + b*x + c; }

	@Override
	public int getMinNumPoints() { return minNumPoints; }

	@Override
	public void fitFunction( final Collection< Point > points ) throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		final int numPoints = points.size();

		if ( numPoints < minNumPoints )
			throw new NotEnoughDataPointsException( "Not enough points, at least " + minNumPoints + " are necessary and available are: " + numPoints );

		// compute matrices
		final double[] delta = new double[ 9 ];
		final double[] tetha = new double[ 3 ];
		
		for ( final Point p : points )
		{
			final double x = p.getW()[ 0 ];
			final double y = p.getW()[ 1 ];

			final double xx = x*x;
			final double xxx = xx*x;

			delta[ 0 ] += xx * xx;
			delta[ 1 ] += xxx;
			delta[ 2 ] += xx;

			delta[ 3 ] += xxx;
			delta[ 4 ] += xx;
			delta[ 5 ] += x;

			delta[ 6 ] += xx;
			delta[ 7 ] += x;
			delta[ 8 ] += 1;

			tetha[ 0 ] += xx * y;
			tetha[ 1 ] += x * y;
			tetha[ 2 ] += y;
		}

		// invert matrix
		try
		{
			MatrixFunctions.invert3x3( delta );
		}
		catch ( final NoninvertibleModelException e )
		{
			this.a = this.b = this.c = 0;
			throw new IllDefinedDataPointsException( "Cannot not invert Delta-Matrix, failed to fit function" );
		}

		this.a = delta[ 0 ] * tetha[ 0 ] + delta[ 1 ] * tetha[ 1 ] + delta[ 2 ] * tetha[ 2 ];
		this.b = delta[ 3 ] * tetha[ 0 ] + delta[ 4 ] * tetha[ 1 ] + delta[ 5 ] * tetha[ 2 ];
		this.c = delta[ 6 ] * tetha[ 0 ] + delta[ 7 ] * tetha[ 1 ] + delta[ 8 ] * tetha[ 2 ];
	}

	@Override
	public double distanceTo( final Point point )
	{
		final double x1 = point.getW()[0];
		final double y1 = point.getW()[1];

	//	return new NewtonRaphson( rndx, 2 ).run( x1, y1, new double[]{ c, b, a } );

		final double a3 = 2 * this.a * this.a ;
		final double a2 = 3 * this.b * this.a  / a3 ;
		final double a1 = (2 * this.c * this.a - 2 * this.a * y1 + 1 + this.b * this.b ) / a3;
		final double a0 = (this.c * this.b - y1 * this.b - x1) / a3 ;

		final double p = (3 * a1 - a2 * a2) / 3;
		final double q = (-9 * a1 * a2  + 27 * a0  + 2 * a2 * a2 * a2) / 27 ;

		final double tmp1 = Math.sqrt( -p / 3 );
		final double tmp2 = (q * q / 4 + p * p * p / 27);

		final double xc1, xc2, xc3;

		
		if ( tmp2 > 0 )
		{
			final double aBar = Math.cbrt(-q/2 + Math.sqrt( q * q / 4 + p * p * p / 27));
			final double bBar = Math.cbrt(-q/2 - Math.sqrt( q * q / 4 + p * p * p / 27));

			xc1 = xc2 = xc3 = aBar + bBar -  a2 / 3;
		}
		else if ( tmp2 == 0 )
		{
			if ( q > 0 )
			{
				xc1 = -2 * tmp1;
				xc2 = tmp1;
				xc3 = xc2;
			}
			else if ( q < 0 )
			{
				xc1 = 2 * tmp1;
				xc2 = -tmp1;
				xc3 = xc2;
			}
			else
			{
				xc1 = 0;
				xc2 = 0;
				xc3 = 0;
			}
		}
		else
		{
			final double phi;

			if ( q >= 0 )
				phi = Math.acos(-Math.sqrt( q * q * 0.25 / (-p * p * p / 27)));
			else
				phi = Math.acos(Math.sqrt(  q * q * 0.25 / (-p * p * p / 27)));

			xc1 = 2 * tmp1 * Math.cos(phi / 3) - a2 / 3;
			xc2 = 2 * tmp1 * Math.cos((phi + 2 * Math.PI) / 3) - a2 / 3;
			xc3 = 2 * tmp1 * Math.cos((phi + 4 * Math.PI) / 3) - a2 / 3;
		}

		final double returndistA = NewtonRaphsonPolynomial.distance( x1, y1, xc1, c + b*xc1 + a*xc1*xc1 );
		final double returndistB = NewtonRaphsonPolynomial.distance( x1, y1, xc2, c + b*xc2 + a*xc2*xc2 );
		final double returndistC = NewtonRaphsonPolynomial.distance( x1, y1, xc3, c + b*xc3 + a*xc3*xc3 );

		return Math.min( returndistA, Math.min( returndistB, returndistC ) );
		
	}

	@Override
	public void set( final QuadraticFunction m )
	{
		this.a = m.getA();
		this.b = m.getB();
		this.c = m.getC();
		this.setCost( m.getCost() );
	}

	@Override
	public QuadraticFunction copy()
	{
		final QuadraticFunction c = new QuadraticFunction();

		c.a = getA();
		c.b = getB();
		c.c = getC();
		c.setCost( getCost() );

		return c;
	}

	@Override
	public String toString() { return "f(x)=" + getA() + "*x*x + " + getB() + "*x + " + getC(); }

	@SuppressWarnings("deprecation")
	public static void main( String[] args ) throws NotEnoughDataPointsException, IllDefinedDataPointsException
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

		final ArrayList<PointFunctionMatch> candidates = new ArrayList<PointFunctionMatch>();
		final ArrayList<PointFunctionMatch> inliersPoly = new ArrayList<PointFunctionMatch>();
		long startTime = System.nanoTime();
		for (final Point p : points)
			candidates.add(new PointFunctionMatch(p));

		final int degree = 2;
		// Using the polynomial model to do the fitting
		final QuadraticFunction regression = new QuadraticFunction();

		regression.ransac( candidates, inliersPoly, 100, 0.1, 0.5 );

		System.out.println("inliers: " + inliersPoly.size());
		for ( final PointFunctionMatch p : inliersPoly )
			System.out.println( regression.distanceTo( p.getP1() ) );
		regression.fit(inliersPoly);
		System.out.println(" y = "  );
		for (int i = degree; i >= 0; --i)
			System.out.println(regression.getCoefficient(i) + "*" + "x" + "^"  + i );
		long totalTime = (System.nanoTime()- startTime)/1000;
		System.out.println("Time: " + totalTime);

	}
}
