/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.blocks.downsample;

import bdv.export.DownsampleBlock;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.downsample.Downsample.ComputationType;
import net.imglib2.algorithm.blocks.downsample.Downsample.Offset;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public class DownsampleBdvPlayground
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

		final boolean[] downsampleInDim = { true, true, true };
		final long[] downsampledDimensions = Downsample.getDownsampledDimensions( img.dimensionsAsLongArray(), downsampleInDim );
		final int[] cellDimensions = { 64, 64, 64 };

		final ExtendedRandomAccessibleInterval< UnsignedByteType, Img< UnsignedByteType > > extended = Views.extendBorder( img );
		final UnsignedByteType type = new UnsignedByteType();

		final double[] calib = new double[ 3 ];
		Arrays.setAll(calib, d -> downsampleInDim[ d ] ? 2 : 1 );



		int[] downsamplingFactors = new int[ 3 ];
		Arrays.setAll(downsamplingFactors, d -> downsampleInDim[ d ] ? 2 : 1 );
		final DownsampleBlock< UnsignedByteType > downsampleBlock = DownsampleBlock.create( cellDimensions, downsamplingFactors, UnsignedByteType.class, RandomAccess.class );
		final RandomAccess< UnsignedByteType > in = extended.randomAccess();
		final long[] currentCellMin = new long[ 3 ];
		final int[] currentCellDim = new int[ 3 ];
		CellLoader< UnsignedByteType > downsampleBlockLoader = cell -> {
			Arrays.setAll( currentCellMin, d -> cell.min( d ) * downsamplingFactors[ d ] );
			Arrays.setAll( currentCellDim, d -> ( int ) cell.dimension( d ) );
			in.setPosition( currentCellMin );
			downsampleBlock.downsampleBlock( in, cell.cursor(), currentCellDim );
		};
		final CachedCellImg< UnsignedByteType, ? > downsampled = new ReadOnlyCachedCellImgFactory().create(
				downsampledDimensions,
				type,
				downsampleBlockLoader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions) );



		final BdvSource out = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( downsampled ),
				"downsampled bdv",
				Bdv.options()
						.addTo( bdv )
						.sourceTransform( calib ) );
		out.setDisplayRange( 0, 255 );
		out.setColor( new ARGBType( 0xff0000 ) );


		final BlockSupplier< UnsignedByteType > blocks = BlockSupplier
				.of( extended )
				.andThen( Downsample.downsample( ComputationType.AUTO, Offset.HALF_PIXEL, downsampleInDim ) );
		final CachedCellImg< UnsignedByteType, ? > downsampled2 = BlockAlgoUtils.cellImg( blocks, downsampledDimensions, cellDimensions );
		final BdvSource out2 = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( downsampled2 ),
				"downsampled half-pixel",
				Bdv.options()
						.addTo( bdv )
						.sourceTransform( calib ) );
		out2.setDisplayRange( 0, 255 );
		out2.setColor( new ARGBType( 0x00ff00 ) );
	}
}
