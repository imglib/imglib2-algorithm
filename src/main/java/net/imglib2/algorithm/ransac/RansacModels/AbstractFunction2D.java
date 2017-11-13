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
import mpicbg.models.PointMatch;

/**
 * Adds a RANSAC with a specifc max gap between points in 2d on the x-axis
 * 
 * @author Stephan Saalfeld and Stephan Preibisch
 *
 * @param <M>
 */
public abstract class AbstractFunction2D< M extends AbstractFunction2D< M > > extends AbstractFunction< M >
{
	private static final long serialVersionUID = 9102425001575237374L;

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
			final double maxGapDim0 )
		throws NotEnoughDataPointsException
	{
		if ( candidates.size() < getMinNumMatches() )
			throw new NotEnoughDataPointsException( candidates.size() + " data points are not enough to solve the Model, at least " + getMinNumMatches() + " data points required." );

		cost = Double.MAX_VALUE;

		final M copy = copy();
		final M m = copy();

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
			boolean isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio, minNumInliers, maxGapDim0 );
			while ( isGood && numInliers < tempInliers.size() )
			{
				numInliers = tempInliers.size();
				try { m.fit( tempInliers ); }
				catch ( final IllDefinedDataPointsException e )
				{
					++i;
					continue A;
				}
				isGood = m.test( candidates, tempInliers, epsilon, minInlierRatio, minNumInliers, maxGapDim0 );
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
			final double maxGapDim0 )
	{
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
					if ( tmpInliers.size() > maxInliers.size() )
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
			if ( tmpInliers.size() > maxInliers.size() )
				inliers.addAll( tmpInliers );
			else
				inliers.addAll( maxInliers );
		}

		final double ir = ( double )inliers.size() / ( double )candidates.size();
		setCost( Math.max( 0.0, Math.min( 1.0, 1.0 - ir ) ) );

		return ( inliers.size() >= minNumInliers && ir > minInlierRatio );
	}
}
