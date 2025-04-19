/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Compute the mean squared error (MSE) between a reference and a processed image. The
 * metrics runs on the whole image, whether 2D or 3D. In order to get individual slice MSE, run
 * the metrics on each slice independently.
 *
 * @author Joran Deschamps
 */
public class MSE
{
	/**
	 * Compute the mean squared error (MSE) score between reference and processed images. The metrics
	 * run on the whole image (regardless of the dimensions).
	 *
	 * @param reference
	 * 		Reference image
	 * @param processed
	 * 		Processed image
	 * @param <T>
	 * 		Type of the image pixels
	 *
	 * @return Metrics score
	 */
	public static < T extends RealType< T > > double computeMetrics( final RandomAccessibleInterval< T > reference, final RandomAccessibleInterval< T > processed )
	{
		if ( !Intervals.equalDimensions( reference, processed ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// get image size
		final long nPixels = Intervals.numElements( reference );

		double mse = 0.;
		final Cursor< T > cu = Views.iterable( reference ).localizingCursor();
		final RandomAccess< T > ra = processed.randomAccess();
		while ( cu.hasNext() )
		{
			double dRef = cu.next().getRealDouble();

			ra.setPosition( cu );
			double dProc = ra.get().getRealDouble();

			mse += ( dRef - dProc ) * ( dRef - dProc );
		}

		return nPixels > 0 ? mse / nPixels : Double.NaN;
	}
}
