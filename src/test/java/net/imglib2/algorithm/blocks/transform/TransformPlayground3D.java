package net.imglib2.algorithm.blocks.transform;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class TransformPlayground3D
{
	public static void main( String[] args )
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// -- open 2D image -----------

		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
//		final String fn = "/Users/pietzsch/workspace/data/DrosophilaWing.tif";
//		final String fn = "/Users/pietzsch/workspace/data/leafcrop.tif";
		final ImagePlus imp = IJ.openImage( fn );
		final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );


		// -- show image -----------

		final BdvSource bdv = BdvFunctions.show( img, "input" );
		bdv.setColor( new ARGBType( 0xffffff ) );
		bdv.setDisplayRange( 0, 255 );


		final AffineTransform3D affine = new AffineTransform3D();
		affine.rotate( 2,0.3 );
		affine.rotate( 1,0.1 );
		affine.rotate( 0,1.5 );
		affine.scale( 1.4 );

		final RealRandomAccessible< UnsignedByteType > interpolated = Views.interpolate( Views.extendZero( img ), new ClampingNLinearInterpolatorFactory<>() );
		final RandomAccessible< UnsignedByteType > transformed = RealViews.affine( interpolated, affine );
		final BdvSource sourceTransformed = BdvFunctions.show(
				transformed,
				img,
				"transformed",
				Bdv.options().addTo( bdv ) );
		sourceTransformed.setColor( new ARGBType( 0xffffff ) );
		sourceTransformed.setDisplayRange( 0, 255 );



		final long[] min = { 200, -330, 120 };
		final int[] size = { 64, 64, 64 };
		final RandomAccessibleInterval< UnsignedByteType > copy = copy( transformed, new UnsignedByteType(), min, size );


		final PrimitiveBlocks< UnsignedByteType > blocks = PrimitiveBlocks.of( Views.extendZero( img ) );
		BlockProcessor< byte[], byte[] > processor = Transform.affine( new UnsignedByteType(), affine, Interpolation.NLINEAR ).blockProcessor();
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		processor.setTargetInterval( FinalInterval.wrap( min, max ) );
		blocks.copy( processor.getSourcePos(), processor.getSourceBuffer(), processor.getSourceSize() );
		final byte[] dest = new byte[ ( int ) Intervals.numElements( size ) ];
		processor.compute( processor.getSourceBuffer(), dest );
		final RandomAccessibleInterval< UnsignedByteType > destImg = ArrayImgs.unsignedBytes( dest, size[ 0 ], size[ 1 ], size[ 2 ] );



		// ----------------------------------------------

		final BdvSource bdv2 = BdvFunctions.show(
				copy,
				"copy");
		bdv2.setColor( new ARGBType( 0xffffff ) );
		bdv2.setDisplayRange( 0, 255 );
		final BdvSource sourceDest = BdvFunctions.show(
				destImg,
				"dest",
				Bdv.options().addTo( bdv2 ) );
		sourceDest.setColor( new ARGBType( 0xffffff ) );
		sourceDest.setDisplayRange( 0, 255 );
	}


	private static < T extends NativeType< T > > RandomAccessibleInterval< T > copy(
			final RandomAccessible< T > ra,
			final T type,
			final long[] min,
			final int[] size )
	{
		final ArrayImg< T, ? > img = new ArrayImgFactory<>( type ).create( size );
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		final Cursor< T > cin = Views.flatIterable( Views.interval( ra, min, max ) ).cursor();
		final Cursor< T > cout = img.cursor();
		while ( cout.hasNext() )
			cout.next().set( cin.next() );
		return img;
	}
}
