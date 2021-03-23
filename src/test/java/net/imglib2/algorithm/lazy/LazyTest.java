package net.imglib2.algorithm.lazy;

import java.util.Iterator;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class LazyTest
{
	@Test
	public final void test()
	{
		final FunctionRandomAccessible< LongType > x = new FunctionRandomAccessible<>( 3, ( a, b ) -> b.set( a.getLongPosition( 0 ) ), LongType::new );
		final FunctionRandomAccessible< LongType > y = new FunctionRandomAccessible<>( 3, ( a, b ) -> b.set( a.getLongPosition( 1 ) ), LongType::new );
		final FunctionRandomAccessible< LongType > z = new FunctionRandomAccessible<>( 3, ( a, b ) -> b.set( a.getLongPosition( 2 ) ), LongType::new );

		final Consumer< RandomAccessibleInterval< LongType > > xPositionLoader = cell -> {
			final Cursor< LongType > c = Views.iterable( cell ).cursor();
			while ( c.hasNext() )
			{
				c.fwd();
				c.get().set( c.getLongPosition( 0 ) );
			}
		};

		final Consumer< RandomAccessibleInterval< LongType > > yPositionLoader = cell -> {
			final Cursor< LongType > c = Views.iterable( cell ).cursor();
			while ( c.hasNext() )
			{
				c.fwd();
				c.get().set( c.getLongPosition( 1 ) );
			}
		};

		final Consumer< RandomAccessibleInterval< LongType > > zPositionLoader = cell -> {
			final Cursor< LongType > c = Views.iterable( cell ).cursor();
			while ( c.hasNext() )
			{
				c.fwd();
				c.get().set( c.getLongPosition( 2 ) );
			}
		};

		final FinalInterval[] intervals =
				new FinalInterval[] {
						Intervals.createMinSize( 0, 0, 0, 11, 20, 33 ),
						Intervals.createMinSize( 22, 11, 3, 11, 20, 33 ) };

		final int[] blockSize = { 3, 4, 5 };

		for ( final FinalInterval interval : intervals)
		{

			final CachedCellImg< LongType, ? > lazyX = Lazy.generate( interval, blockSize, new LongType(), AccessFlags.setOf(), xPositionLoader );

			final CachedCellImg< LongType, ? > lazyY = Lazy.generate( interval, blockSize, new LongType(), AccessFlags.setOf(), yPositionLoader );

			final CachedCellImg< LongType, ? > lazyZ = Lazy.generate( interval, blockSize, new LongType(), AccessFlags.setOf(), zPositionLoader );

			Assert.assertTrue( equals( Views.flatIterable( lazyX ), Views.flatIterable( Views.interval( x, interval ) ) ) );

			Assert.assertTrue( equals( Views.flatIterable( lazyY ), Views.flatIterable( Views.interval( y, interval ) ) ) );

			Assert.assertTrue( equals( Views.flatIterable( lazyZ ), Views.flatIterable( Views.interval( z, interval ) ) ) );
		}
	}

	private static < T extends Type< T > > boolean equals( final Iterable< ? extends T > a, final Iterable< ? extends T > b )
	{
		final Iterator< ? extends T > itA = a.iterator();
		final Iterator< ? extends T > itB = b.iterator();

		while ( itA.hasNext() )
		{
			if ( !itA.next().valueEquals( itB.next() ) )
				return false;
		}
		return true;
	}

}
