package net.imglib2.algorithm.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import org.junit.Test;

public class RandomAccessiblePairWithNeighborhoodTest {

	@Test
	public void test() {
		// Extend input with Views.extend()
		Img<ByteType> in = new ArrayImgFactory< ByteType >().create( new FinalDimensions(3, 3), new ByteType() );
		OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>> outOfBoundsFactory =
			new OutOfBoundsBorderFactory<>();
		RandomAccessibleInterval<ByteType> extendedIn = Views.interval(Views.extend(in, outOfBoundsFactory), in); // FAILS
//		RandomAccessible<ByteType> extendedIn = Views.extend(in, outOfBoundsFactory); // WORKS

		// Create RandomAccessiblePair
		final RandomAccessible<Neighborhood<ByteType>> safe = new RectangleShape(1,
			false).neighborhoodsRandomAccessibleSafe(extendedIn);
		RandomAccessible<Pair<Neighborhood<ByteType>, ByteType>> pair = Views.pair(safe, extendedIn);

		// Set position out of bounds
		RandomAccess<Pair<Neighborhood<ByteType>, ByteType>> randomAccess = pair
			.randomAccess();
		randomAccess.setPosition(new int[] { 0, 0 });

		// Get value from Neighborhood via RandomAccessiblePair
		Pair<Neighborhood<ByteType>, ByteType> pair2 = randomAccess.get();
		Neighborhood<ByteType> neighborhood = pair2.getA();
		Cursor<ByteType> cursor = neighborhood.cursor();
		
		// Tries to access (-1, -1) of the source
		cursor.next().getRealDouble();
	}

}