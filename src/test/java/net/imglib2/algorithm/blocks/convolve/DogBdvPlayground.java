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
package net.imglib2.algorithm.blocks.convolve;

import java.util.function.Function;

import bdv.cache.SharedQueue;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class DogBdvPlayground
{
	public static void main( String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
		final ImagePlus imp = IJ.openImage( fn );
		final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );


		final int sensitivity = 4;
		final double sigmaSmaller = 2;
		final double sigmaLarger = computeSigma2( sigmaSmaller, sensitivity );

		System.out.println( "sigmaSmaller = " + sigmaSmaller );
		System.out.println( "sigmaLarger = " + sigmaLarger );


		final BlockSupplier< FloatType > blocks = BlockSupplier.of( Views.extendMirrorDouble( img ) ).andThen( Convert.convert( new FloatType() ) );

		final Function< BlockSupplier< FloatType >, UnaryBlockOperator< FloatType, FloatType > >
				g1 = Convolve.gauss( sigmaSmaller );
		final Function< BlockSupplier< FloatType >, UnaryBlockOperator< FloatType, FloatType > >
				g2 = Convolve.gauss( sigmaLarger );

		final UnaryBlockOperator< FloatType, FloatType > gOp1 = g1.apply( blocks );
		final UnaryBlockOperator< FloatType, FloatType > gOp2 = g2.apply( blocks );

		final BlockProcessor< ?, ? > gProc1 = gOp1.blockProcessor();
		final BlockProcessor< ?, ? > gProc2 = gOp2.blockProcessor();

		final FinalInterval targetInterval = Intervals.createMinSize( 0, 0, 0, 10, 10, 10 );
		System.out.println( "targetInterval = " + Intervals.toString( targetInterval ) );

		gProc1.setTargetInterval( targetInterval );
		System.out.println( "gProc1.getSourceInterval() = " + Intervals.toString( gProc1.getSourceInterval() ) );
		;

		gProc2.setTargetInterval( targetInterval );
		System.out.println( "gProc2.getSourceInterval() = " + Intervals.toString( gProc2.getSourceInterval() ) );
		;

		final DoGProcessor jp = new DoGProcessor(
				gOp1.getSourceType(), gOp1.getTargetType(), gProc1, gProc2 );
		jp.setTargetInterval( targetInterval );
		System.out.println( "jp.getSourceInterval() = " + Intervals.toString( jp.getSourceInterval() ) );


		final FloatType type = new FloatType();
		final UnaryBlockOperator< FloatType, FloatType > joined = new DefaultUnaryBlockOperator<>( type, type, 3, 3, jp );


		final long[] dimensions = img.dimensionsAsLongArray();
		final int[] cellDimensions = { 64, 64, 64 };
		final Img< FloatType > convolved = BlockAlgoUtils.cellImg(
				blocks.andThen( joined ),
				dimensions,
				cellDimensions );

		final BdvSource bdv = BdvFunctions.show(
				img,
				"img",
				Bdv.options() );
		bdv.setColor( new ARGBType( 0xffffff ) );
		bdv.setDisplayRange( 0, 255 );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.SINGLE );

		final BdvSource out = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( convolved, new SharedQueue( 8, 1 ) ),
				"DoG",
				Bdv.options().addTo( bdv )
		);
		out.setDisplayRange( -10, 10 );
//		out.setColor( new ARGBType( 0x00ff00 ) );
		out.setColor( new ARGBType( 0xffffff ) );

	}

	private static double computeSigma2( final double sigma1, final int stepsPerOctave )
	{
		final double k = Math.pow( 2f, 1f / stepsPerOctave );
		return sigma1 * k;
	}

}
