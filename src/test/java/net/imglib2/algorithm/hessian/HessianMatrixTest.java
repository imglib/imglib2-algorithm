package net.imglib2.algorithm.hessian;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.HessianMatrix;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

public class HessianMatrixTest
{

	public void test(
			final RandomAccessibleInterval< DoubleType > img,
			final RandomAccessibleInterval< DoubleType > hessian,
			final double[][] refs,
			final int size )
	{
		Assert.assertEquals( img.numDimensions() + 1, hessian.numDimensions() );
		for ( int d = 0; d < img.numDimensions(); ++d )
			Assert.assertEquals( img.dimension( d ), hessian.dimension( d ) );

		Assert.assertEquals( img.numDimensions() * ( img.numDimensions() + 1 ) / 2, hessian.dimension( img.numDimensions() ) );

		for ( int i = 0; i < hessian.dimension( img.numDimensions() ); ++i )
			for ( Cursor< DoubleType > r = ArrayImgs.doubles( refs[ i ], size, size ).cursor(), c = Views.hyperSlice( hessian, img.numDimensions(), i ).cursor(); r.hasNext(); )
				Assert.assertEquals( r.next().get(), c.next().get(), 1e-20 );
	}

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

		final double[] dxxRef = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				1.0, 0.0, -2.0, 0.0, 1.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0
		};

		final double[] dxyRef = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, -1.0, 0.0, 1.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0
		};

		final double[] dyyRef = new double[] {
				0.0, 0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, -2.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0, 0.0
		};

		final double[][] refs = {
				dxxRef,
				dxyRef,
				dyyRef
		};

		final ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( imgArray, size, size );

		final double sigma = 0.1;

		final ArrayImg< DoubleType, DoubleArray > gaussian = ArrayImgs.doubles( size, size );
		Gauss3.gauss( sigma, Views.extendBorder( img ), gaussian );

		final RandomAccessibleInterval< DoubleType > hessian =
				HessianMatrix.calculateMatrix( Views.extendBorder( gaussian ), ArrayImgs.doubles( size, size, 2 ), ArrayImgs.doubles( size, size, 3 ), new OutOfBoundsBorderFactory<>() );

		test( img, hessian, refs, size );

	}

	@Test
	public void test3D() throws IncompatibleTypeException
	{
		final int size = 3;

		final double[] imgArray = new double[] {
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 4.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0
		};

		final double[] dxxRef = new double[] {
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				-1.0, -2.0, -1.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0
		};

		final double[] dxyRef = new double[] {
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				1.0, 0.0, -1.0,
				0.0, 0.0, 0.0,
				-1.0, 0.0, 1.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0
		};

		final double[] dxzRef = new double[] {
				0.0, 0.0, 0.0,
				1.0, 0.0, -1.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				-1.0, 0.0, 1.0,
				0.0, 0.0, 0.0
		};

		final double[] dyyRef = new double[] {
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, -1.0, 0.0,
				0.0, -2.0, 0.0,
				0.0, -1.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0
		};

		final double[] dyzRef = new double[] {
				0.0, 1.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, -1.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, -1.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 1.0, 0.0
		};

		final double[] dzzRef = new double[] {
				0.0, 0.0, 0.0,
				0.0, -1.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, -2.0, 0.0,
				0.0, 0.0, 0.0,

				0.0, 0.0, 0.0,
				0.0, -1.0, 0.0,
				0.0, 0.0, 0.0
		};

		final double[][] refs = {
				dxxRef,
				dxyRef,
				dxzRef,
				dyyRef,
				dyzRef,
				dzzRef
		};

		final ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( imgArray, size, size, size );

		final double sigma = 0.1;

		final ArrayImg< DoubleType, DoubleArray > gaussian = ArrayImgs.doubles( size, size, size );
		Gauss3.gauss( sigma, Views.extendBorder( img ), gaussian );

		final RandomAccessibleInterval< DoubleType > hessian =
				HessianMatrix.calculateMatrix( Views.extendBorder( gaussian ), ArrayImgs.doubles( size, size, size, 3 ), ArrayImgs.doubles( size, size, size, 6 ), new OutOfBoundsBorderFactory<>() );

		test( img, hessian, refs, size );

	}

}
