package net.imglib2.algorithm.morphology;

import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public class TopHat
{

	public static < T extends RealType< T >> Img< T > whiteTopHat( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > opened = Opening.open( source, strel, numThreads );
		MorphologyUtils.minusSubtractInPlace( opened, source, numThreads );
		return opened;
	}

	public static < T extends RealType< T >> Img< T > blackTopHat( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > closed = Closing.close( source, strel, numThreads );
		MorphologyUtils.subtractInPlace( closed, source, numThreads );
		return closed;
	}

	/**
	 * Private default constructor.
	 */
	private TopHat()
	{}

}
