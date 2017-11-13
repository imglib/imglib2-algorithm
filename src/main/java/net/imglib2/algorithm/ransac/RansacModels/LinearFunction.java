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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import mpicbg.models.AbstractModel;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;

/**
 * @author Stephan Preibisch (stephan.preibisch@gmx.de) & Timothee Lionnet
 */
public class LinearFunction extends AbstractFunction2D< LinearFunction > implements Polynomial< LinearFunction, Point >
{
	private static final long serialVersionUID = 5289346951323596267L;

	final int minNumPoints = 2;
	
	double m, n; // m*x + n

	public LinearFunction() { this( 0,0 ); }
	public LinearFunction( final double m, final double n )
	{
		this.m = m;
		this.n = n;
	}

	/**
	 * @return - the center of the circle in x
	 */
	public double getN() { return n; }

	/**
	 * @return - the center of the circle in y
	 */
	public double getM() { return m; }

	@Override
	public int degree() { return 1; }

	@Override
	public double getCoefficient( final int j )
	{
		if ( j == 0 )
			return n;
		else if ( j == 1 )
			return m;
		else
			return 0;
	}

	@Override
	public int getMinNumPoints() { return minNumPoints; }

	public void fitFunction( final Collection<Point> points ) throws NotEnoughDataPointsException
	{
		final int numPoints = points.size();

		if ( numPoints < minNumPoints )
			throw new NotEnoughDataPointsException( "Not enough points, at least " + minNumPoints + " are necessary and available are: " + numPoints );

		// compute matrices
		final double[] delta = new double[ 4 ];
		final double[] tetha = new double[ 2 ];

		for ( final Point p : points )
		{
			final double x = p.getW()[ 0 ]; 
			final double y = p.getW()[ 1 ]; 

			final double xx = x*x;
			final double xy = x*y;

			delta[ 0 ] += xx;
			delta[ 1 ] += x;
			delta[ 2 ] += x;
			delta[ 3 ] += 1;

			tetha[ 0 ] += xy;
			tetha[ 1 ] += y;
		}

		// invert matrix
		MatrixFunctions.invert2x2( delta );

		this.m = delta[ 0 ] * tetha[ 0 ] + delta[ 1 ] * tetha[ 1 ];
		this.n = delta[ 2 ] * tetha[ 0 ] + delta[ 3 ] * tetha[ 1 ];
	}

	@Override
	public double distanceTo( final Point point )
	{
		final double x1 = point.getW()[ 0 ]; 
		final double y1 = point.getW()[ 1 ];

		return Math.abs( y1 - m*x1 - n ) / ( Math.sqrt( m*m + 1 ) );
	}

	@Override
	public void set( final LinearFunction m )
	{
		this.n = m.getN();
		this.m = m.getM();
		this.setCost( m.getCost() );
	}

	@Override
	public double predict( final double x ) { return m*x + n; }

	@Override
	public LinearFunction copy()
	{
		LinearFunction c = new LinearFunction();

		c.n = getN();
		c.m = getM();
		c.setCost( getCost() );

		return c;
	}

	@Override
	public String toString() { return "f(x)=" + getM() + "*x + " + getN(); }

	/**
	 * Find the {@link AbstractModel} of a set of {@link PointMatch} candidates
	 * containing a high number of outliers using
	 * {@link #ransac(List, Collection, int, double, double, int) RANSAC}
	 * \citet[{FischlerB81}.
	 *
	 * @param modelClass class of the model to be estimated
	 * @param candidates candidate data points inluding (many) outliers
	 * @param inliers remaining candidates after RANSAC
	 * @param iterations number of iterations
	 * @param epsilon maximal allowed transfer error
	 * @param minInlierRatio minimal number of inliers to number of
	 *   candidates
	 * @param minNumInliers minimally required absolute number of inliers
	 * @param maxGapDim0 max distance between points on the x-axis (will keep the larger set of points)
	 *
	 * @return true if {@link AbstractModel} could be estimated and inliers is not
	 *   empty, false otherwise.  If false, {@link AbstractModel} remains unchanged.
	 */
	@SuppressWarnings("deprecation")
	final public < P extends PointFunctionMatch >boolean ransac(
			final List< P > candidates,
			final Collection< P > inliers,
			final int iterations,
			final double epsilon,
			final double minInlierRatio,
			final int minNumInliers,
			final double maxGapDim0,
			final double minSlope,
			final double maxSlope )
		throws NotEnoughDataPointsException
	{
		if ( candidates.size() < getMinNumMatches() )
			throw new NotEnoughDataPointsException( candidates.size() + " data points are not enough to solve the Model, at least " + getMinNumMatches() + " data points required." );

		cost = Double.MAX_VALUE;

		final LinearFunction copy = copy();
		final LinearFunction m = copy();

		inliers.clear();

		int i = 0;
		final HashSet< P > minMatches = new HashSet< P >();

A:		while ( i < iterations )
		{
			// choose model.MIN_SET_SIZE disjunctive matches randomly
			minMatches.clear();
			for ( int j = 0; j < getMinNumMatches(); ++j )
			{
				P p;
				do
				{
					p = candidates.get( ( int )( rnd.nextDouble() * candidates.size() ) );
				}
				while ( minMatches.contains( p ) );
				minMatches.add( p );
			}
			try { m.fit( minMatches ); }
			catch ( final IllDefinedDataPointsException e )
			{
				++i;
				continue;
			}

			final ArrayList< P > tempInliers = new ArrayList< P >();

			int numInliers = 0;
			boolean isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio, minNumInliers, maxGapDim0, minSlope, maxSlope );
			while ( isGood && numInliers < tempInliers.size() )
			{
				numInliers = tempInliers.size();
				try { m.fit( tempInliers ); }
				catch ( final IllDefinedDataPointsException e )
				{
					++i;
					continue A;
				}
				isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio, minNumInliers, maxGapDim0, minSlope, maxSlope );
			}
			if (
					isGood &&
					m.betterThan( copy ) &&
					tempInliers.size() >= minNumInliers )
			{
				copy.set( m );
				inliers.clear();
				inliers.addAll( tempInliers );
			}
			++i;
		}
		if ( inliers.size() == 0 )
			return false;

		set( copy );
		return true;
	}

	/**
	 * Test the {@link AbstractModel} for a set of {@link PointMatch} candidates.
	 * Return true if the number of inliers / number of candidates is larger
	 * than or equal to min_inlier_ratio, otherwise false.
	 *
	 * Clears inliers and fills it with the fitting subset of candidates.
	 *
	 * Sets {@link #getCost() cost} = 1.0 - |inliers| / |candidates|.
	 *
	 * @param candidates set of point correspondence candidates
	 * @param inliers set of point correspondences that fit the model
	 * @param epsilon maximal allowed transfer error
	 * @param minInlierRatio minimal ratio |inliers| / |candidates| (0.0 => 0%, 1.0 => 100%)
	 * @param minNumInliers minimally required absolute number of inliers
	 */
	public < P extends PointFunctionMatch > boolean test(
			final Collection< P > candidates,
			final List< P > inliers,
			final double epsilon,
			final double minInlierRatio,
			final int minNumInliers,
			final double maxGapDim0,
			final double minSlope,
			final double maxSlope )
	{
		final LinearFunction tmp = new LinearFunction();
		inliers.clear();

		for ( final P m : candidates )
		{
			m.apply( this );
			if ( m.getDistance() < epsilon ) inliers.add( m );
		}

		if ( inliers.size() > 1 )
		{
			Collections.sort( inliers, new Comparator< P >()
			{
				@Override
				public int compare( final P o1, final P o2 )
				{
					if ( o1.getP1().getW()[ 0 ] < o2.getP1().getW()[ 0 ] )
						return -1;
					else if ( o1.getP1().getW()[ 0 ] == o2.getP1().getW()[ 0 ] )
						return 0;
					else
						return 1;
				}
			} );

			final ArrayList< P > maxInliers = new ArrayList< P >();
			final ArrayList< P > tmpInliers = new ArrayList< P >();

			tmpInliers.add( inliers.get( 0 ) );

			for ( int i = 1; i < inliers.size(); ++i )
			{
				final P current = inliers.get( i );

				if ( Math.abs( current.getP1().getW()[ 0 ] - inliers.get( i - 1 ).getP1().getW()[ 0 ] ) <= maxGapDim0 )
				{
					// distance between the points <= maxGapDim0, then just keep adding the points
					tmpInliers.add( inliers.get( i ) );
				}
				else
				{
					// distance between two points on the x > maxGapDim0

					// if this was the largest chunk of data so far, keep it
					if ( tmpInliers.size() > maxInliers.size() && slopeFits( tmpInliers, tmp, minSlope, maxSlope ) )
					{
						maxInliers.clear();
						maxInliers.addAll( tmpInliers );
					}

					// clear tmpInliers, add the current one for a new start
					tmpInliers.clear();
					tmpInliers.add( current );
				}
			}

			inliers.clear();

			// is the latest set of points larger than the biggest set so far?
			if ( tmpInliers.size() > maxInliers.size() && slopeFits( tmpInliers, tmp, minSlope, maxSlope ) )
				inliers.addAll( tmpInliers );
			else
				inliers.addAll( maxInliers );
		}

		final double ir = ( double )inliers.size() / ( double )candidates.size();
		setCost( Math.max( 0.0, Math.min( 1.0, 1.0 - ir ) ) );

		return ( inliers.size() >= minNumInliers && ir > minInlierRatio );
	}

	@SuppressWarnings("deprecation")
	public static < P extends PointFunctionMatch > boolean slopeFits( final List< P > inliers, final LinearFunction function, final double minSlope, final double maxSlope )
	{
		try
		{
			function.fit( inliers );

			if ( function.getM() >= minSlope && function.getM() <= maxSlope )
				return true;
		}
		catch ( Exception e ) {}

		return false;
	}
	
	

	@SuppressWarnings("deprecation")
	public static void main( String[] args ) throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		final ArrayList< Point > points = new ArrayList<Point>();

		points.add( new Point( new double[]{ 1f, -3.95132f } ) );
		points.add( new Point( new double[]{ 2f, 6.51205f } ) );
		points.add( new Point( new double[]{ 3f, 18.03612f } ) );
		points.add( new Point( new double[]{ 4f, 28.65245f } ) );
		points.add( new Point( new double[]{ 5f, 42.05581f } ) );
		points.add( new Point( new double[]{ 6f, 54.01327f } ) );
		points.add( new Point( new double[]{ 7f, 64.58747f } ) );
		points.add( new Point( new double[]{ 8f, 76.48754f } ) );
		points.add( new Point( new double[]{ 9f, 89.00033f } ) );
		
		final ArrayList< PointFunctionMatch > candidates = new ArrayList<PointFunctionMatch>();
		final ArrayList< PointFunctionMatch > inliers = new ArrayList<PointFunctionMatch>();
		
		for ( final Point p : points )
			candidates.add( new PointFunctionMatch( p ) );
		
		final LinearFunction l = new LinearFunction();
		
		l.ransac( candidates, inliers, 100, 0.1, 0.5 );
		
		System.out.println( inliers.size() );
		
		l.fit( inliers );
		
		System.out.println( "y = " + l.m + " x + " + l.n );
		for ( final PointFunctionMatch p : inliers )
			System.out.println( l.distanceTo( p.getP1() ) );
		
		//System.out.println( l.distanceTo( new Point( new float[]{ 1f, 0f } ) ) );
	}
}
