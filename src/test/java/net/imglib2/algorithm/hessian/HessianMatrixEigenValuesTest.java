/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.hessian;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.HessianMatrix;
import net.imglib2.algorithm.linalg.eigen.TensorEigenValues;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

public class HessianMatrixEigenValuesTest
{

	@Test
	public void test2D() throws IncompatibleTypeException
	{
		final int size = 5;

		final double[] imgArray = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 4.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0
		};

		final double[] eigenvaluesRef1 = new double[] {
				0.0, 0.0, 1.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 1.0, 0.0,
				1.0, 0.0, -2.0, 0.0, 1.0,
				0.0, 1.0, 0.0, 1.0, 0.0,
				0.0, 0.0, 1.0, 0.0, 0.0
		};

		final double[] eigenvaluesRef2 = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, -1.0, 0.0, -1.0, 0.0,
				0.0, 0.0, -2.0, 0.0, 0.0,
				0.0, -1.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0
		};

		final ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( imgArray, size, size );

		final double sigma = 0.1;

		final ArrayImg< DoubleType, DoubleArray > gaussian = ArrayImgs.doubles( size, size );
		Gauss3.gauss( sigma, Views.extendBorder( img ), gaussian );

		final RandomAccessibleInterval< DoubleType > hessian =
				HessianMatrix.calculateMatrix( Views.extendBorder( gaussian ), ArrayImgs.doubles( size, size, 2 ), ArrayImgs.doubles( size, size, 3 ), new OutOfBoundsBorderFactory<>() );

		final RandomAccessibleInterval< DoubleType > eigenvalues = TensorEigenValues.calculateEigenValuesSymmetric(
				hessian,
				TensorEigenValues.createAppropriateResultImg( hessian, new ArrayImgFactory<>(), new DoubleType() ) );

		Assert.assertEquals( img.numDimensions() + 1, eigenvalues.numDimensions() );
		for ( int d = 0; d < img.numDimensions(); ++d )
			Assert.assertEquals( img.dimension( d ), eigenvalues.dimension( d ) );

		Assert.assertEquals( img.numDimensions(), eigenvalues.dimension( img.numDimensions() ) );


		for ( Cursor< DoubleType > r = ArrayImgs.doubles( eigenvaluesRef1, size, size ).cursor(), c = Views.hyperSlice( eigenvalues, img.numDimensions(), 0 ).cursor(); r.hasNext(); )
			Assert.assertEquals( r.next().get(), c.next().get(), 1.0e-20 );

		for ( Cursor< DoubleType > r = ArrayImgs.doubles( eigenvaluesRef2, size, size ).cursor(), c = Views.hyperSlice( eigenvalues, img.numDimensions(), 1 ).cursor(); r.hasNext(); )
			Assert.assertEquals( r.next().get(), c.next().get(), 1.0e-20 );


	}

}
