package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public interface PairOfPointsNeighborhoodFactory< T >
{
	Neighborhood< T > create( long[] position, long[] offset, RandomAccess< T > sourceRandomAccess );
}
