package net.imglib2.algorithm.morphology;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.morphology.neighborhoods.DiamondShape;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.Before;
import org.junit.Test;

public class ErosionTest
{

	private Img< UnsignedByteType > tpImg;

	private Shape diamondShape;

	private Interval interval;

	private List< Shape > diamondStrelDecomp;

	private List< Shape > diamondStrelStraight;

	private Img< UnsignedByteType > ranImg;

	@Before
	public void setUp() throws Exception
	{
		tpImg = ArrayImgs.unsignedBytes( 50l, 50l );
		for ( final UnsignedByteType pixel : tpImg )
		{
			pixel.set( 255 );
		}
		final RandomAccess< UnsignedByteType > randomAccess = tpImg.randomAccess();
		randomAccess.setPosition( new int[] { 0, 25 } );
		randomAccess.get().set( 0 );
		randomAccess.setPosition( new int[] { 35, 25 } );
		randomAccess.get().set( 0 );

		diamondShape = new DiamondShape( 8 );
		diamondStrelDecomp = StructuringElements.diamond( 8, 2, true );
		diamondStrelStraight = StructuringElements.diamond( 8, 2, false );

		interval = FinalInterval.createMinSize( 10, 10, 20, 20 );

		ranImg = ArrayImgs.unsignedBytes( 50l, 50l );
		final Random ran = new Random( 1l );
		for ( final UnsignedByteType pixel : ranImg )
		{
			pixel.set( ran.nextInt( 256 ) );
		}
	}

	@Test
	public void testErodeToTarget()
	{
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );

		final Img< UnsignedByteType > result1 = tpImg.factory().create( interval, tpImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target1 = Views.translate( result1, min );
		Erosion.erode( tpImg, target1, diamondStrelDecomp, 1 );

		final Img< UnsignedByteType > result2 = tpImg.factory().create( interval, tpImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target2 = Views.translate( result2, min );
		Erosion.erode( tpImg, target2, diamondStrelStraight, 1 );

		final Img< UnsignedByteType > result3 = tpImg.factory().create( interval, tpImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target3 = Views.translate( result3, min );
		Erosion.erode( tpImg, target3, diamondShape, 1 );

		final Cursor< UnsignedByteType > cursor1 = result1.cursor();
		final RandomAccess< UnsignedByteType > randomAccess2 = result2.randomAccess( result2 );
		final RandomAccess< UnsignedByteType > randomAccess3 = result3.randomAccess( result3 );

		while ( cursor1.hasNext() )
		{
			cursor1.fwd();
			randomAccess2.setPosition( cursor1 );
			randomAccess3.setPosition( cursor1 );

			assertEquals( "Mismatch between single shape erosion and optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
			assertEquals( "Mismatch between single shape erosion and un-optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
		}
	}

	@Test
	public void testErodeToTargetMultithreaded()
	{
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );

		final Img< UnsignedByteType > result1 = ranImg.factory().create( interval, ranImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target1 = Views.translate( result1, min );
		Erosion.erode( ranImg, target1, diamondStrelDecomp, 2 );

		final Img< UnsignedByteType > result2 = ranImg.factory().create( interval, ranImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target2 = Views.translate( result2, min );
		Erosion.erode( ranImg, target2, diamondStrelStraight, 2 );

		final Img< UnsignedByteType > result3 = ranImg.factory().create( interval, ranImg.firstElement().copy() );
		final IntervalView< UnsignedByteType > target3 = Views.translate( result3, min );
		Erosion.erode( ranImg, target3, diamondShape, 1 );

		final Cursor< UnsignedByteType > cursor1 = result1.cursor();
		final RandomAccess< UnsignedByteType > randomAccess2 = result2.randomAccess( result2 );
		final RandomAccess< UnsignedByteType > randomAccess3 = result3.randomAccess( result3 );

		while ( cursor1.hasNext() )
		{
			cursor1.fwd();
			randomAccess2.setPosition( cursor1 );
			randomAccess3.setPosition( cursor1 );

			assertEquals( "Mismatch between single shape erosion and multithreaded optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
			assertEquals( "Mismatch between single shape erosion and multithreaded un-optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
		}
	}
	
	@Test
	public void testErodeToNew()
	{
		final Img< UnsignedByteType > result1 = Erosion.erode( tpImg, diamondStrelDecomp, 1 );
		final Img< UnsignedByteType > result2 = Erosion.erode( tpImg, diamondStrelStraight, 1 );
		final Img< UnsignedByteType > result3 = Erosion.erode( tpImg, diamondShape, 1 );
		
		assertEquals( "Target 1 and source number of dimensions do not match.", tpImg.numDimensions(), result1.numDimensions() );
		assertEquals( "Target 2 and source number of dimensions do not match.", tpImg.numDimensions(), result2.numDimensions() );
		assertEquals( "Target 3 and source number of dimensions do not match.", tpImg.numDimensions(), result3.numDimensions() );

		for ( int d = 0; d < tpImg.numDimensions(); d++ )
		{
			assertEquals( "Target 1 and source size do not match. Expected " + Util.printInterval( tpImg ) + ", got " + Util.printInterval( result1 ), tpImg.dimension( d ), result1.dimension( d ) );
			assertEquals( "Target 2 and source size do not match. Expected " + Util.printInterval( tpImg ) + ", got " + Util.printInterval( result1 ), tpImg.dimension( d ), result2.dimension( d ) );
			assertEquals( "Target 3 and source size do not match. Expected " + Util.printInterval( tpImg ) + ", got " + Util.printInterval( result1 ), tpImg.dimension( d ), result3.dimension( d ) );
		}

		final Cursor< UnsignedByteType > cursor1 = result1.cursor();
		final RandomAccess< UnsignedByteType > randomAccess2 = result2.randomAccess( result2 );
		final RandomAccess< UnsignedByteType > randomAccess3 = result3.randomAccess( result3 );
		while ( cursor1.hasNext() )
		{
			cursor1.fwd();
			randomAccess2.setPosition( cursor1 );
			randomAccess3.setPosition( cursor1 );

			assertEquals( "Mismatch between single shape erosion and optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
			assertEquals( "Mismatch between single shape erosion and un-optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
		}
	}

	@Test
	public void testErodeToFull()
	{
		final Img< UnsignedByteType > result1 = Erosion.erodeFull( ranImg, diamondStrelDecomp, 1 );
		final Img< UnsignedByteType > result2 = Erosion.erodeFull( ranImg, diamondStrelStraight, 1 );
		final Img< UnsignedByteType > result3 = Erosion.erodeFull( ranImg, diamondShape, 1 );


		assertEquals( "Target 1 and source number of dimensions do not match.", ranImg.numDimensions(), result1.numDimensions() );
		assertEquals( "Target 2 and source number of dimensions do not match.", ranImg.numDimensions(), result2.numDimensions() );
		assertEquals( "Target 3 and source number of dimensions do not match.", ranImg.numDimensions(), result3.numDimensions() );

		final Neighborhood< BitType > neighborhood = MorphologyUtils.getNeighborhood( diamondShape, ranImg );
		
		for ( int d = 0; d < ranImg.numDimensions(); d++ )
		{
			assertEquals( "Target 1 and inflated source size do not match. Expected " + Util.printInterval( ranImg ) + "+" + Util.printInterval( neighborhood ) + ", got " + Util.printInterval( result1 ), ranImg.dimension( d ) + neighborhood.dimension( d ) - 1, result1.dimension( d ) );
			assertEquals( "Target 2 and inflated source size do not match. Expected " + Util.printInterval( ranImg ) + "+" + Util.printInterval( neighborhood ) + ", got " + Util.printInterval( result2 ), ranImg.dimension( d ) + neighborhood.dimension( d ) - 1, result1.dimension( d ) );
			assertEquals( "Target 3 and inflated source size do not match. Expected " + Util.printInterval( ranImg ) + "+" + Util.printInterval( neighborhood ) + ", got " + Util.printInterval( result3 ), ranImg.dimension( d ) + neighborhood.dimension( d ) - 1, result1.dimension( d ) );
		}

		final Cursor< UnsignedByteType > cursor1 = result1.cursor();
		final RandomAccess< UnsignedByteType > randomAccess2 = result2.randomAccess( result2 );
		final RandomAccess< UnsignedByteType > randomAccess3 = result3.randomAccess( result3 );
		while ( cursor1.hasNext() )
		{
			cursor1.fwd();
			randomAccess2.setPosition( cursor1 );
			randomAccess3.setPosition( cursor1 );

			assertEquals( "Mismatch between single shape erosion and optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
			assertEquals( "Mismatch between single shape erosion and un-optimized strel erosion at " + Util.printCoordinates( cursor1 ) + ".",
					randomAccess3.get().get(), cursor1.get().get() );
		}
	}

}
