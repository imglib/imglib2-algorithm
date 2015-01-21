package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public interface HorizontalLineNeighborhoodFactory< T >
{
	public Neighborhood< T > create( final long[] position, final long span, final int dim, final boolean skipCenter, final RandomAccess< T > sourceRandomAccess );
}
