package net.imglib2.algorithm.fill;

import gnu.trove.list.array.TLongArrayList;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;

/**
 * @author Philipp Hanslovsky &lt;hanslovskyp@janelia.hhmi.org&gt;
 * Flood fill {@link RandomAccessible} of arbitrary dimension.
 */
public class FloodFill {

    /**
     *
     * convenience method to automatically determine filler policy from value at seed point
     *
     * @param randomAccessible input (also written into)
     * @param seed flood fill starting at this location
     * @param shape neighborhood that is checked for filling
     * @param fillerFactory rule for creating {@link FillPolicy} from pixel located at seed
     * @param <T> no restrictions on T as long as appropriate {@link FillPolicy} is provided
     */
    public static < T > void fill(
            final RandomAccessible< T > randomAccessible,
            final Localizable seed,
            final Shape shape,
            final FillPolicyFactory< T > fillerFactory )
    {
        RandomAccess<T> access = randomAccessible.randomAccess();
        access.setPosition( seed );
        fill( randomAccessible, seed, shape, fillerFactory.call( access.get() ) );
    }


    /**
     *
     * @param randomAccessible input (also written into)
     * @param seed flood fill starting at this location
     * @param shape neighborhood that is checked for filling
     * @param filler policy for deciding whether or not neighbor is part of connected component and instructions on how to fill a pixel
     * @param <T> no restrictions on T as long as appropriate {@link FillPolicy} is provided
     */
    public static < T > void fill(
            final RandomAccessible< T > randomAccessible,
            final Localizable seed,
            final Shape shape,
            final FillPolicy< T > filler )
    {
        final int n = randomAccessible.numDimensions();

        final TLongArrayList[] coordinates = new TLongArrayList[ n ];
        for ( int d = 0; d < n; ++d )
        {
            coordinates[ d ] = new TLongArrayList();
            coordinates[ d ].add( seed.getLongPosition( d ) );
        }

        RandomAccessible<Neighborhood<T>> neighborhood = shape.neighborhoodsRandomAccessible(randomAccessible);
        final RandomAccess<Neighborhood<T>> neighborhoodAccess = neighborhood.randomAccess();

        final RandomAccess< T > canvasAccess = randomAccessible.randomAccess();
        canvasAccess.setPosition( seed );
        filler.fill( canvasAccess.get() );

        for ( int i = 0; i < coordinates[ 0 ].size(); ++i )
        {
            for ( int d = 0; d < n; ++d )
                neighborhoodAccess.setPosition( coordinates[ d ].get( i ), d );

            final Cursor<T> neighborhoodCursor = neighborhoodAccess.get().cursor();

            while ( neighborhoodCursor.hasNext() )
            {
                final T t = neighborhoodCursor.next();
                if ( filler.isValidNeighbor( t ) )
                {
                    filler.fill( t );
                    for ( int d = 0; d < n; ++d )
                        coordinates[ d ].add( neighborhoodCursor.getLongPosition( d ) );
                }
            }
        }
    }

}
