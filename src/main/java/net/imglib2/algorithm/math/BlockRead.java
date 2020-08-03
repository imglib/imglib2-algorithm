package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.BlockReading;
import net.imglib2.algorithm.math.execution.BlockReadingDirect;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

/**
 * Intended for reading cuboid blocks out of an integral image.
 * 
 * @author Albert Cardona
 *
 * @param <I> The {@code Type} of the {@code RandomAccessible} from which to read blocks.
 */
public final class BlockRead< I extends RealType< I > > extends ViewableFunction implements IFunction, RandomAccessOnly
{
	final private RandomAccessible< I > src;
	final private long[][] corners;
	final private byte[] signs;
	
	/**
	 * A block centered on a particular pixel.
	 * 
	 * @param src A {@code RandomAccessible} such as an @{code IntegralImg}, presumably a {@code RandomAccessibleInterval} that was extended with an {@code OutOfBounds} strategy.
	 * @param blockRadius Array of half of the length of each side of the block.
	 */
	public BlockRead( final RandomAccessible< I > src, final long[] blockRadius )
	{
		this.src = src;
		this.corners = new long[ ( int )Math.pow( 2,  blockRadius.length ) ][ blockRadius.length ];

		// All possible combinations to define all corners, sorted with lower dimensions being the slower-moving,
		// following Gray code (see https://en.wikipedia.org/wiki/Gray_code )
		for (int d = 0; d < src.numDimensions(); ++d )
		{
			final int cycle = corners.length / ( int )Math.pow( 2, d + 1 );
			long inc = blockRadius[ d ];
			for (int i = 0; i < corners.length; ++i )
			{
				if ( 0 == i % cycle) inc *= -1;
				corners[ i ][ d ] = inc;
				System.out.println("corners[" + i + "][" + d + "] = " + corners[i][d]);
			}
		}
		this.signs = BlockRead.signsArray( src );
		for (int i=0; i<signs.length; ++i)
			System.out.println("signs[" + i + "] = " + signs[i]);
	}
	
	static public byte[] signsArray( final RandomAccessible< ? > src )
	{
		switch ( src.numDimensions() )
		{
		case 1:
			return new byte[]{ -1, 1 };
		case 2:
			// 2D: S( (x1,y1), (x2,y2) = C( x1,y1 )
			//                          -C( x1,y2 )
			//                          -C( x2,y1 )
			//                          +C( x2,y2 )
			// Corners as: (x1, y1), (x1, y2), (x2, y1), (x2, y2)
			return new byte[]{ 1, -1, -1, 1 };
		case 3:
			// 3D: S( (x1,y1,z1) to (x2,y2,z2) ) =  - C( x1, y1, z1 )
			//                                      + C( x1, y1, z2 )
			//                                      + C( x1, y2, z1 )
			//                                      - C( x1, y2, z2 )
			//                                      + C( x2, y1, z1 )
			//                                      - C( x2, y1, z2 )
			//                                      - C( x2, y2, z1 )
			//                                      + C( x2, y2, z2 )
			return new byte[]{ -1, 1, 1, -1, 1, -1, -1, 1 };
		default:
			// There's a clear pattern, but I can't find the time now to break through it
			// Must re-read Tapias 2011 doi:10.1016/j.patrec.2010.10.007
			// for the use of the Mobius function for determining the sign
			throw new UnsupportedOperationException( "Sorry, numDimensions " + src.numDimensions() + " not supported yet." );
		}
	}
	
	/**
	 * 
	 * @param src
	 * @param corners In coordinate moves relative to a pixel's location.
	 */
	public BlockRead( final RandomAccessible< I > src, final long[][] corners )
	{
		this.src = src;
		this.corners = corners;
		this.signs = BlockRead.signsArray( src );
	}

	@SuppressWarnings("unchecked")
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		if ( tmp.getClass() == src.randomAccess().get().getClass() )
			return new BlockReadingDirect< O >( tmp.copy(), ( RandomAccessible< O > )this.src, this.corners, this.signs );
		return new BlockReading< I, O >(
				tmp.copy(),
				converter,
				this.src,
				this.corners,
				this.signs );
	}
	
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.src;
	}
}