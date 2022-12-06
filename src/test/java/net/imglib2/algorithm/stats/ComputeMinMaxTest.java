package net.imglib2.algorithm.stats;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComputeMinMaxTest
{
	@Test
	public void testComputeMinMax() {
		Img<IntType> image = ArrayImgs.ints( new int[] { 100, 100, 50, 200 }, 2, 2 );
		IntType min = new IntType( 0 );
		IntType max = new IntType( 0 );
		ComputeMinMax.computeMinMax( image, min, max );
		assertEquals( new IntType(50), min );
		assertEquals( new IntType(200), max );
	}
}
