package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;

public interface PairNeighborhoodFactory< T >
{
	Neighborhood< T > create( long[] position, long[] offset, RandomAccess< T > sourceRandomAccess );
}
