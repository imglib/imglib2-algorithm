package net.imglib2.algorithm.astar;

import net.imglib2.Localizable;
import net.imglib2.util.Util;

public interface AStarHeuristics
{

	public static final int COST_ORTHOGONAL = 5; // 1 * 5

	public static final int COST_DIAGONAL = 7; // 1.4 * 5

	public static AStarHeuristics EUCLIDEAN = ( c, t ) -> ( int ) Util.distance( c, t ) * COST_ORTHOGONAL;

	public static AStarHeuristics CHEBYSHEV = ( c, t ) -> {
		double maxDist = 0.;
		for ( int d = 0; d < c.numDimensions(); d++ )
			maxDist = Math.max( maxDist, Math.abs( c.getDoublePosition( d ) - t.getDoublePosition( d ) ) );
		return ( int ) ( maxDist * COST_ORTHOGONAL );
	};

	/**
	 * Returns the cost to reach the specified target position from the current
	 * position.
	 * 
	 * @param current
	 *            the current position.
	 * @param target
	 *            the target position.
	 * @return the cost as a positive double.
	 */
	public int cost( Localizable current, Localizable target );
}
