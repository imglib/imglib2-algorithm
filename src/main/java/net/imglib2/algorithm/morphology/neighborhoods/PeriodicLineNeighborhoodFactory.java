package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public interface PeriodicLineNeighborhoodFactory< T >
{
	public Neighborhood< T > create( final long[] position, final long span, final int[] increments, final RandomAccess< T > sourceRandomAccess );
}
