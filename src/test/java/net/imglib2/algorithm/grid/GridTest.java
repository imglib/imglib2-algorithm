/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
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
package net.imglib2.algorithm.grid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Test for {@link Grid, Grids, Patch, GridRandomAccess}.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class GridTest {

	@Test
	public void gridTest() {
		// test parameters
		final long span = 5;
		final long gap = 4;
		final int[] whichDims = new int[] { 1, 0, 1 };
		final long[] pos = new long[] { 5, 2, 0 };

		// create test image, grid and patch
		ImgFactory<FloatType> imageFactory = new ArrayImgFactory<>();
		Img<FloatType> img = imageFactory.create(new int[] { 30, 20, 10 }, new FloatType());
		Grid<FloatType> grid = Grids.createGrid(img, gap, span, whichDims);
		RandomAccess<RandomAccessibleInterval<FloatType>> raGrid = grid.randomAccess();
		raGrid.setPosition(pos);
		Patch<FloatType> patch = (Patch<FloatType>) raGrid.get();

		// check patch dimensions
		assertEquals(patch.numDimensions(), whichDims.length);
		for (int i = 0; i < whichDims.length; i++) {
			assertEquals(patch.min(i), 0);
			if (whichDims[i] == 0)
				assertEquals(patch.dimension(i), 1);
			else
				assertEquals(patch.dimension(i), span * 2 + 1);
		}

		// check grid dimensions
		assertEquals(grid.numDimensions(), whichDims.length);
		assertEquals(grid.dimension(0), 6);
		for (int i = 0; i < whichDims.length; i++) {
			assertEquals(grid.dimension(i), (int) img.dimension(i) / gap - 1);
		}
	}
}
