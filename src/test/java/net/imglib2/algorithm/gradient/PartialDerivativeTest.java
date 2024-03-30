/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.gradient;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link PartialDerivative}.
 *
 * @author Matthias Arzt
 */
public class PartialDerivativeTest
{

	private final double DELTA = 0.0001;

	private final RandomAccessibleInterval< DoubleType > testImage =
			createImage2dMinSizeValues( /* min: */ 0, 0, /* size: */ 5, 3, /* values: */
					0.0, 0.0, 0.0, 0.0, 0.0,
					0.0, 2.0, 0.0, -1.0, 0.0,
					0.0, 0.0, 5.0, 0.0, 0.0
			);

	private final RandomAccessibleInterval< DoubleType > centralDifferenceExpected =
			createImage2dMinSizeValues( /* min: */ 1, 0, /* size: */ 3, 3, /* values: */
					0.0, 0.0, 0.0,
					0.0, -1.5, 0.0,
					2.5, 0.0, -2.5
			);

	private final RandomAccessibleInterval< DoubleType > backwardDifferenceExpected =
			createImage2dMinSizeValues( /* min: */ 1, 0, /* size: */ 4, 3, /* values: */
					0.0, 0.0, 0.0, 0.0,
					2.0, -2.0, -1.0, 1.0,
					0.0, 5.0, -5.0, 0.0
			);

	public static RandomAccessibleInterval< DoubleType > createImage2dMinSizeValues( long minX, long minY, long sizeX, long sizeY, double... values )
	{
		Interval interval = Intervals.createMinSize( minX, minY, sizeX, sizeY );
		return Views.translate( ArrayImgs.doubles( values, Intervals.dimensionsAsLongArray( interval ) ),
				Intervals.minAsLongArray( interval ) );
	}

	@Test
	public void testGradientCentralDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( centralDifferenceExpected );
		PartialDerivative.gradientCentralDifference( data, result, 0 );
		assertImagesEqual( centralDifferenceExpected, result );
	}

	@Test
	public void testGradientBackwardDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( backwardDifferenceExpected );
		PartialDerivative.gradientBackwardDifference( data, result, 0 );
		assertImagesEqual( backwardDifferenceExpected, result );
	}

	@Test
	public void testForwardDifferenceX()
	{
		RandomAccessibleInterval< DoubleType > data = testImage;
		RandomAccessibleInterval< DoubleType > expected = Views.translate( backwardDifferenceExpected, -1, 0 );
		RandomAccessibleInterval< DoubleType > result = emptyArrayImg( expected );
		PartialDerivative.gradientForwardDifference( data, result, 0 );
		assertImagesEqual( expected, result );
	}

	private void assertImagesEqual( RandomAccessibleInterval< DoubleType > expected, RandomAccessibleInterval< DoubleType > actual )
	{
		assertTrue( Intervals.equals( expected, actual ) );
		assertArrayEquals( imageAsArray( expected ), imageAsArray( actual ), DELTA );
	}

	private double[] imageAsArray( RandomAccessibleInterval< DoubleType > image )
	{
		List< Double > values = new ArrayList<>();
		Views.iterable( image ).forEach( x -> values.add( x.getRealDouble() ) );
		return values.stream().mapToDouble( x -> x ).toArray();
	}

	private RandomAccessibleInterval< DoubleType > emptyArrayImg( Interval interval )
	{
		return Views.translate( ArrayImgs.doubles( Intervals.dimensionsAsLongArray( interval ) ), Intervals.minAsLongArray( interval ) );
	}
}
