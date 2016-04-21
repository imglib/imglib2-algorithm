package net.imglib2.algorithm.fill;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.LongType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hanslovskyp on 4/21/16.
 */
public class FloodFillTest {

    private static final long START_LABEL = 1;
    private static final long FILL_LABEL = 2;
    private static final int[] N_DIMS = { 1, 2, 3, 4 };
    private static final int SIZE_OF_EACH_DIM = 30;


    private static < T extends IntegerType < T > > void runTest(
            int nDim,
            int sizeOfEachDim,
            ImgFactory< T > imageFactory,
            FillPolicyFactory< T > fillPolicyFactory,
            T t )
    {
        long[] dim = new long[nDim];
        long[] c = new long[nDim];
        long r = sizeOfEachDim / 4;
        for ( int d = 0; d < nDim; ++d ) {
            dim[d] = sizeOfEachDim;
            c[d] = sizeOfEachDim / 3;
        }

        long divisionLine = r / 3;

        Img<T> img = imageFactory.create(dim, t.copy() );
        Img<T> refImg = imageFactory.create(dim, t.copy());

        for (Cursor<T> i = img.cursor(), ref = refImg.cursor(); i.hasNext(); )
        {
            i.fwd();
            ref.fwd();
            long diffSum = 0;
            for ( int d = 0; d < nDim; ++d )
            {
                long diff = i.getLongPosition( d ) - c[d];
                diffSum += diff * diff;

            }

            if ( ( diffSum < r * r ) ) {
                if ((i.getLongPosition(0) - c[0] < divisionLine)) {
                    i.get().setInteger(START_LABEL);
                    ref.get().setInteger( FILL_LABEL );
                } else if (i.getLongPosition(0) - c[0] > divisionLine) {
                    i.get().setInteger( START_LABEL );
                    ref.get().setInteger( START_LABEL );
                }
            }

        }

        FloodFill.fill( img, new Point( c ), new DiamondShape( 1 ), fillPolicyFactory );

        for ( Cursor< T > imgCursor = img.cursor(), refCursor = refImg.cursor(); imgCursor.hasNext(); )
        {
            Assert.assertEquals( refCursor.next(), imgCursor.next() );
        }


    }

    @Test
    public void runTests()
    {
        for ( int nDim : N_DIMS )
        {
            runTest(
                    nDim,
                    SIZE_OF_EACH_DIM,
                    new ArrayImgFactory<LongType>(),
                    new GenericTypeFillPolicy.Factory<LongType>( new LongType( FILL_LABEL ) ),
                    new LongType() );
        }
    }

}