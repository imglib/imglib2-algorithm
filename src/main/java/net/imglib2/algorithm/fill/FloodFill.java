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


//    public static void main(String[] args) {
//        long[] dim = {120, 90};
//
//        long[] circle = { 40, 50, 30 }; // x, y, r
//
//        ArrayImg<LongType, LongArray> img = ArrayImgs.longs(dim);
//        ArrayCursor<LongType> c;
//        for(c = img.cursor(); c.hasNext(); )
//        {
//            c.fwd();
//            long x = c.getLongPosition(0) - circle[0];
//            long y = c.getLongPosition(1) - circle[1];
//            if ( x*x + y*y <= circle[2]*circle[2] && ( y < 10 || y > 12 ) )
//                c.get().set( 5 );
//
//        }
//
//
//        new ImageJ();
//        ImageJFunctions.show( img.copy() );
//
//        LongType seedType = new LongType(5);
//        LongType newType = new LongType(8);
//        GenericTypeFillPolicy<LongType> filler = new GenericTypeFillPolicy<LongType>(seedType, newType);
//        fill( img, new Point( circle[0], circle[1] ), new DiamondShape(1), filler );
//        ImageJFunctions.show( img );
//    }

}
