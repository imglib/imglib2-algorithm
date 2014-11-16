package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;

public interface DiamondTipsNeighborhoodFactory< T >
{
	public Neighborhood< T > create( final long[] position, final long radius, final RandomAccess< T > sourceRandomAccess );
}
