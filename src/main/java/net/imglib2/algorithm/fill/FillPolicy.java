package net.imglib2.algorithm.fill;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Shape;

/**
 * @author Philipp Hanslovsky &lt;hanslovskyp@janelia.hhmi.org&gt;
 *
 * Policy for filling and selecting neighbors in {@link FloodFill#fill(RandomAccessible, Localizable, Shape, FillPolicy)}.
 *
 */
public interface FillPolicy< T > {
    void fill( T t );

    boolean isValidNeighbor( T t );
}
