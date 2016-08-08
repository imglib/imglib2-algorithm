package net.imglib2.algorithm.corner;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
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

		final Img< DoubleType > hessian =
				HessianMatrix.calculateMatrix( Views.extendBorder( img ), img, sigma, new OutOfBoundsBorderFactory<>(), new ArrayImgFactory<>(), new DoubleType(), 1 );

		Assert.assertEquals( img.numDimensions() + 1, hessian.numDimensions() );
		for ( int d = 0; d < img.numDimensions(); ++d )
		{
			Assert.assertEquals( img.dimension( d ), hessian.dimension( d ) );
		}

		Assert.assertEquals( img.numDimensions() * ( img.numDimensions() + 1 ) / 2, hessian.dimension( img.numDimensions() ) );

		final Img< DoubleType > eigenvalues = TensorEigenValues.calculateEigenValuesSymmetric( hessian, new ArrayImgFactory<>(), new DoubleType(), 1 );

		Assert.assertEquals( img.numDimensions() + 1, eigenvalues.numDimensions() );
		for ( int d = 0; d < img.numDimensions(); ++d )
		{
			Assert.assertEquals( img.dimension( d ), eigenvalues.dimension( d ) );
		}

		Assert.assertEquals( img.numDimensions(), eigenvalues.dimension( img.numDimensions() ) );


		for ( Cursor< DoubleType > r = ArrayImgs.doubles( eigenvaluesRef1, size, size ).cursor(), c = Views.hyperSlice( eigenvalues, img.numDimensions(), 0 ).cursor(); r.hasNext(); )
		{
			Assert.assertEquals( r.next().get(), c.next().get(), 1.0e-20 );
		}

		for ( Cursor< DoubleType > r = ArrayImgs.doubles( eigenvaluesRef2, size, size ).cursor(), c = Views.hyperSlice( eigenvalues, img.numDimensions(), 1 ).cursor(); r.hasNext(); )
		{
			Assert.assertEquals( r.next().get(), c.next().get(), 1.0e-20 );
		}


	}

}
