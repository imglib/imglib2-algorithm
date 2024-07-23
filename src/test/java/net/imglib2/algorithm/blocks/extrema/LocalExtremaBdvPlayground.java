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
package net.imglib2.algorithm.blocks.extrema;

import bdv.cache.SharedQueue;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.algorithm.blocks.convolve.DifferenceOfGaussian;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class LocalExtremaBdvPlayground
{
	public static void main( String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final String fn = "/Users/pietzsch/Desktop/dog.tif";
		ImagePlus imp = IJ.openImage( fn );
		final Img< FloatType > img = ImageJFunctions.wrapFloat( imp );

		final BdvSource bdv = BdvFunctions.show(
				img,
				"img",
				Bdv.options() );
		bdv.setColor( new ARGBType( 0xffffff ) );
		bdv.setDisplayRange( -10, 10 );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.SINGLE );
	}
}
