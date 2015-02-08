package net.imglib2.algorithm.neighborhood;

import net.imglib2.RandomAccess;

public interface PairOfPointsNeighborhoodFactory< T >
{
	Neighborhood< T > create( long[] position, long[] offset, RandomAccess< T > sourceRandomAccess );
}
