package net.imglib2.algorithm.morphology;

import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

public class MorphologyUtilsTest
{

	Img<IntType> input = ArrayImgs.ints(new int[] { //
			1, 2, 3, 4, //
			5, 6, 7, 8, //
			9, 10, 11, 12, //
			13, 14, 15, 16 //
	}, 4, 4);

	Img<IntType> expected = ArrayImgs.ints(new int[] { //
			0, 0, 0, 0, //
			0, 6, 7, 0, //
			0, 10, 11, 0, //
			0, 0, 0, 0 //
	}, 4, 4);

	FinalInterval interval = Intervals.createMinSize( 1, 1, 2, 2 );

	@Test
	public void testCopy() {
		Img<IntType> output = ArrayImgs.ints( 4, 4 );
		IterableInterval<IntType> crop = Views.interval( input, interval );
		MorphologyUtils.copy( crop, Views.extendZero( output ), 4 );
		ImgLib2Assert.assertImageEquals( expected, output );
	}

	@Test
	public void testCopy2() {
		Img<IntType> output = ArrayImgs.ints( 4, 4 );
		IterableInterval<IntType> crop = Views.interval( output, interval );
		MorphologyUtils.copy2( Views.extendZero( input ), crop, 4 );
		ImgLib2Assert.assertImageEquals( expected, output );
	}

	@Test
	public void testCopyCropped() {
		Img<IntType> output = MorphologyUtils.copyCropped( input, new FinalInterval( 2, 2 ), 4 );
		ImgLib2Assert.assertImageEquals( Views.zeroMin( Views.interval( expected, interval ) ), output );
	}

	@Test
	public void testSubAAB() {
		Img<IntType> a = ArrayImgs.ints( new int[] { 10, 30 }, 2 );
		Img<IntType> b = ArrayImgs.ints( new int[] { 4, 5 }, 2 );
		Img<IntType> expected = ArrayImgs.ints( new int[] { 10, 25 }, 2 );
		MorphologyUtils.subAAB( Views.extendZero(a), Views.interval( b, Intervals.createMinSize( 1, 1 ) ), 4 );
		ImgLib2Assert.assertImageEquals( expected, a );
	}

	@Test
	public void testSubAAB2() {
		Img<IntType> a = ArrayImgs.ints( new int[] { 10, 30 }, 2 );
		Img<IntType> b = ArrayImgs.ints( new int[] { 4, 5 }, 2 );
		Img<IntType> expected = ArrayImgs.ints( new int[] { 10, 25 }, 2 );
		MorphologyUtils.subAAB2( Views.interval(a, Intervals.createMinSize( 1, 1 )), Views.extendZero( b ), 4 );
		ImgLib2Assert.assertImageEquals( expected, a );
	}

	@Test
	public void testSubABA() {
		Img<IntType> a = ArrayImgs.ints( new int[] { 10, 30 }, 2 );
		Img<IntType> b = ArrayImgs.ints( new int[] { 4, 5 }, 2 );
		Img<IntType> expected = ArrayImgs.ints( new int[] { 10, -25 }, 2 );
		MorphologyUtils.subABA( Views.extendZero(a), Views.interval( b, Intervals.createMinSize( 1, 1 ) ), 4 );
		ImgLib2Assert.assertImageEquals( expected, a );
	}

	@Test
	public void testSubABA2() {
		Img<IntType> a = ArrayImgs.ints( new int[] { 10, 30 }, 2 );
		Img<IntType> b = ArrayImgs.ints( new int[] { 4, 5 }, 2 );
		Img<IntType> expected = ArrayImgs.ints( new int[] { 4, -25 }, 2 );
		MorphologyUtils.subABA2( Views.interval(a, Intervals.createMinSize( 1, 1 )), Views.extendZero( b ), 4 );
		ImgLib2Assert.assertImageEquals( expected, b );
	}

	@Test
	public void testSubABA3() {
		Img<IntType> a = ArrayImgs.ints( new int[] { 10, 30 }, 2 );
		Img<IntType> b = ArrayImgs.ints( new int[] { 4, 5 }, 2 );
		Img<IntType> expected = ArrayImgs.ints( new int[] { 4, 25 }, 2 );
		MorphologyUtils.subBAB( Views.extendZero( a ), Views.interval(b, Intervals.createMinSize( 1, 1 )), 4 );
		ImgLib2Assert.assertImageEquals( expected, b );
	}
}
