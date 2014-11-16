package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;

public interface DiamondNeighborhoodFactory< T >
{

	Neighborhood< T > create( long[] position, long radius, RandomAccess< T > sourceRandomAccess );

}
