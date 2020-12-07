package net.imglib2.algorithm.stats;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;

public class InformationMetricsTests
{
	
	private Img< IntType > imgZeros;
	private Img< IntType > img3;
	private Img< IntType > img3Shifted;
	private Img< IntType > img3SmallErr;
	private Img< IntType > imgTwo;
	
	// mutual info of a set with itself
	private double MI_id = 1.0986122886681096;
	private double log2 = Math.log( 2.0 );
	
	@Before
	public void setup()
	{
		int[] a = new int[]{ 0, 1, 2, 0, 1, 2, 0, 1, 2};
		img3 = ArrayImgs.ints( a, a.length );

		int[] b = new int[]{ 0, 1, 2, 1, 2, 0, 2, 0, 1};
		img3Shifted = ArrayImgs.ints( b, b.length );

		imgZeros = ArrayImgs.ints( new int[ 9 ], 9 );

		int[] c = new int[]{ 0, 1, 2, 0, 2, 1, 0, 1, 2 };
		img3SmallErr = ArrayImgs.ints( c, c.length );

		int[] twodat = new int[]{ 0, 1, 0, 1, 0, 1, 0, 1 };
		imgTwo = ArrayImgs.ints( twodat, twodat.length );
	}

	@Test
	public void testEntropy()
	{
		double entropyZeros = InformationMetrics.entropy( imgZeros, 0, 1, 2 );
		double entropyCoinFlip = InformationMetrics.entropy( imgTwo, 0, 1, 2 );

		assertEquals( "entropy zeros", 0.0, entropyZeros, 1e-6 );
		assertEquals( "entropy fair coin", log2, entropyCoinFlip, 1e-6 );
	}
	
	@Test
	public void testMutualInformation()
	{
		double miAA = InformationMetrics.mutualInformation( img3, img3, 0, 2, 3 );
		double nmiAA = InformationMetrics.normalizedMutualInformation( img3, img3, 0, 2, 3 );
		double nmiBB = InformationMetrics.normalizedMutualInformation( img3Shifted, img3Shifted, 0, 2, 3 );

		double miAB = InformationMetrics.mutualInformation( img3, img3Shifted, 0, 2, 3 );
		double nmiAB = InformationMetrics.normalizedMutualInformation( img3, img3Shifted, 0, 2, 3 );

		double miBA = InformationMetrics.mutualInformation( img3Shifted, img3, 0, 2, 3 );
		double nmiBA = InformationMetrics.normalizedMutualInformation( img3Shifted, img3, 0, 2, 3 );
		
		double miBB = InformationMetrics.mutualInformation( img3Shifted, img3Shifted, 0, 2, 3 );

		assertEquals( "self MI", MI_id, miAA, 1e-6 );
		assertEquals( "self NMI", 1.0, nmiAA, 1e-6 );

		assertEquals( "MI permutation", miAB, 0.0, 1e-6 );
		assertEquals( "NMI permutation", nmiAB, 0.0, 1e-6 );
		
	}

}
