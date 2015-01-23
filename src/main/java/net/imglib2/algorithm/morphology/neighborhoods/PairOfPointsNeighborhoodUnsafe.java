package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public class PairOfPointsNeighborhoodUnsafe< T > extends PairOfPointsNeighborhood< T >
{

	public static < T > PairOfPointsNeighborhoodFactory< T > factory()
	{
		return new PairOfPointsNeighborhoodFactory< T >()
		{
			@Override
			public Neighborhood< T > create( final long[] position, final long[] offset, final RandomAccess< T > sourceRandomAccess )
			{
				return new PairOfPointsNeighborhoodUnsafe< T >( position, offset, sourceRandomAccess );
			}
		};
	}

	private final LocalCursor theCursor;

	private final LocalCursor firstElementCursor;

	PairOfPointsNeighborhoodUnsafe( final long[] position, final long[] offset, final RandomAccess< T > sourceRandomAccess )
	{
		super( position, offset, sourceRandomAccess );
		theCursor = super.cursor();
		firstElementCursor = super.cursor();
	}

	@Override
	public T firstElement()
	{
		firstElementCursor.reset();
		return firstElementCursor.next();
	}

	@Override
	public LocalCursor cursor()
	{
		theCursor.reset();
		return theCursor;
	}

}
