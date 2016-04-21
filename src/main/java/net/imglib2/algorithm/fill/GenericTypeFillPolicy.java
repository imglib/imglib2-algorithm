package net.imglib2.algorithm.fill;

import net.imglib2.type.Type;

/**
 * @author Philipp Hanslovsky &lt;hanslovskyp@janelia.hhmi.org&gt;
 *
 * Implementation of {@link FillPolicy} for generic imglib {@link Type}
 * Fills neighbor with {@link this#newType} if neighbor is equal to {@link this#seedType} and different from {@link this#newType}.
 */
public class GenericTypeFillPolicy<T extends Type<T>> implements FillPolicy<T> {

    private final T seedType;
    private final T newType;

    public GenericTypeFillPolicy(T seedType, T newType) {
        this.seedType = seedType;
        this.newType = newType;
    }

    @Override
    public void fill(T t) {
        t.set( newType );
    }

    @Override
    public boolean isValidNeighbor(T t) {
        return t.equals( this.seedType ) && !t.equals( this.newType );
    }
}
