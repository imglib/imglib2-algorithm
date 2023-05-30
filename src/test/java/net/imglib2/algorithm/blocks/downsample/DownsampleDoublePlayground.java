package net.imglib2.algorithm.blocks.downsample;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.CenterDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.HalfPixelDouble;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

public class DownsampleDoublePlayground
{
	public static void main( String[] args )
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
		final ImagePlus imp = IJ.openImage( fn );
		final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );

		final BdvSource bdv = BdvFunctions.show(
				img,
				"img",
				Bdv.options() );
		bdv.setColor( new ARGBType( 0xffffff ) );
		bdv.setDisplayRange( 0, 255 );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.SINGLE );

		final PrimitiveBlocks< DoubleType > blocks = PrimitiveBlocks.of(
				Converters.convert( Views.extendBorder( img ), new RealDoubleConverter<>(), new DoubleType() )
		).threadSafe();

		final boolean[] downsampleInDim = { true, true, true };
		final long[] downsampledDimensions = Downsample.getDownsampledDimensions( img.dimensionsAsLongArray(), downsampleInDim );
		final int[] cellDimensions = { 64, 64, 64 };
		final CachedCellImg< DoubleType, ? > downsampled = BlockAlgoUtils.cellImg(
				blocks, new CenterDouble( downsampleInDim ), new DoubleType(), downsampledDimensions, cellDimensions );

		final double[] calib = new double[ 3 ];
		Arrays.setAll(calib, d -> downsampleInDim[ d ] ? 2 : 1 );
		final BdvSource out = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( downsampled ),
				"downsampled",
				Bdv.options()
						.addTo( bdv )
						.sourceTransform( calib ) );
		out.setDisplayRange( 0, 255 );
//		out.setColor( new ARGBType( 0xff0000 ) );

		final CachedCellImg< DoubleType, ? > downsampled2 = BlockAlgoUtils.cellImg(
				blocks, new HalfPixelDouble( downsampleInDim ), new DoubleType(), downsampledDimensions, cellDimensions );
		final BdvSource out2 = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( downsampled2 ),
				"downsampled half-pixel",
				Bdv.options()
						.addTo( bdv )
						.sourceTransform( calib ) );
		out2.setDisplayRange( 0, 255 );
//		out2.setColor( new ARGBType( 0x00ff00 ) );

	}
}
