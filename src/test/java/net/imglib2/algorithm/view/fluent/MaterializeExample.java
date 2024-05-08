package net.imglib2.algorithm.view.fluent;

import static net.imglib2.algorithm.view.fluent.MaterializeExample.Transformers.toArrayImg;
import static net.imglib2.algorithm.view.fluent.MaterializeExample.Transformers.toCachedCellImg;
import static net.imglib2.view.fluent.RaiView.Extension.mirrorSingle;

import java.util.function.Function;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.list.ListImg;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.ImgUtil;

public class MaterializeExample
{
	public static void main( String[] args )
	{
		// We can think about using apply() as a "terminal operation", similar to collect() on a Stream.
		// The Transformers prototype below is a bit like the Collectors class.
		// Transformers.toArrayImg() is similar to Collectors.toList().


		// To generate some data, let's create a RandomAccessible with I(x,y) = x + y
		FunctionRandomAccessible< UnsignedByteType > function = new FunctionRandomAccessible<>(
				2,
				( pos, val ) -> val.set(
						pos.getIntPosition( 0 ) + pos.getIntPosition( 1 ) ),
				UnsignedByteType::new );


		// Apply some view operations and then copy the result into an ArrayImg
		ArrayImg< UnsignedByteType, ? > img = function.view()
				.interval( new FinalInterval( 4, 4 ) )
				.expand( mirrorSingle(), 2 )
				.zeroMin()
				.apply( toArrayImg() );

		print( img );


		// Apply some view operations and then (lazy-)copy the result into a CachedCellImg
		CachedCellImg< UnsignedByteType, ? > lazy = function.view()
				.interval( new FinalInterval( 4, 4 ) )
				.expand( mirrorSingle(), 2 )
				.zeroMin()
				.apply( toCachedCellImg( 3 ) );

		print( lazy );


		// Note that the Transformers methods are type-safe.
		// For example, we cannot materialize a RAI<String> into an ArrayImg,
		// because String is not a NativeType!

		Img< String > strings = new ListImg<>( new long[] { 1 }, "" );
		// strings.view().apply( toArrayImg() );
		// compile erro: no instance(s) of type variable(s) T exist so that String conforms to NativeType<T>
	}


	public static class Transformers
	{
		public static < T extends NativeType< T > > Function< RandomAccessibleInterval< T >, ArrayImg< T, ? > > toArrayImg()
		{
			return Transformers::copyToArrayImg;
		}

		public static < T extends NativeType< T > > Function< RandomAccessibleInterval< T >, CachedCellImg< T, ? > > toCachedCellImg( final int... cellDimensions )
		{
			return s -> createCachedCellImg( s, cellDimensions );
		}

		/**
		 * Copy {@code source} into new {@code ArrayImg}.
		 */
		// TODO: What to do if source is not zeroMin? Make zeroMin, or throw exception?
		private static < T extends NativeType< T > > ArrayImg< T, ? > copyToArrayImg( final RandomAccessibleInterval< T > source )
		{
			final ArrayImg< T, ? > img = new ArrayImgFactory<>( source.getType() ).create( source );
			ImgUtil.copy( source, img );
			return img;
		}

		private static < T extends NativeType< T > > CachedCellImg< T, ? > createCachedCellImg( final RandomAccessibleInterval< T > source, final int... cellDimensions )
		{
			final CachedCellImg< T, ? > img = new ReadOnlyCachedCellImgFactory().create(
					source.dimensionsAsLongArray(),
					source.getType(),
					cell -> {
						ImgUtil.copy( source.view().interval( cell ), cell );
					},
					ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions )
			);
			return img;
		}
	}


	private static void print( RandomAccessibleInterval< UnsignedByteType > rai )
	{
		final long h = rai.dimension( 1 );
		final long w = rai.dimension( 0 );
		for ( int y = 0; y < h; y++ )
		{
			for ( int x = 0; x < w; x++ )
				System.out.print( rai.getAt( x, y ).get() + " " );
			System.out.println();
		}
	}
}
