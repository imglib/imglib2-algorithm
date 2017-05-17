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

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;

/**
 * Create a grid of patches of a {@link RandomAccessibleInterval}.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class Grids {
	/**
	 * Create a grid of patches. It is placed in the middle of the source image.
	 * Its origin is at the left top and has span as minimal distance to the
	 * borders.
	 * 
	 * @param srcImage
	 *            input/source image
	 * @param gap
	 *            distance/gap between two patches
	 * @param span
	 *            span of each patch: size = 2 * span + 1
	 * @param whichDims
	 *            array contains zeros and ones and defines which dimensions the
	 *            patches have
	 */
	public static <T> Grid<T> createGrid(RandomAccessibleInterval<T> srcImage, long gap, long span, int[] whichDims) {
		long[] gapA = new long[srcImage.numDimensions()];
		long[] spanA = new long[srcImage.numDimensions()];
		Arrays.fill(gapA, gap);
		Arrays.fill(spanA, span);
		return createGrid(srcImage, gapA, spanA, whichDims);
	}

	/**
	 * whichDims is filled with ones
	 */
	public static <T> Grid<T> createGrid(RandomAccessibleInterval<T> srcImage, long gap, long span) {
		return createGrid(srcImage, gap, span, new int[] { -1 });
	}

	/**
	 * gap and span have different values for each dimension
	 */
	public static <T> Grid<T> createGrid(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span,
			int[] whichDims) {
		if (whichDims[0] == -1) {
			whichDims = new int[srcImage.numDimensions()];
			Arrays.fill(whichDims, 1);
		}
		if ((whichDims.length != srcImage.numDimensions()) || (gap.length != srcImage.numDimensions())
				|| (span.length != srcImage.numDimensions())) {
			throw new IllegalArgumentException("Wrong number of dimensions!");
		}
		long[] gridDims = new long[srcImage.numDimensions()];
		long[] origin = new long[srcImage.numDimensions()];
		for (int i = 0; i < whichDims.length; i++) {
			gridDims[i] = srcImage.dimension(i) / gap[i] - 1;
			origin[i] = (srcImage.dimension(i) - gap[i] * (gridDims[i] - 1)) / 2;
		}
		return new Grid<>(srcImage, gap, span, origin, gridDims, whichDims);
	}

	/**
	 * whichDims is filled with ones
	 */
	public static <T> Grid<T> createGrid(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span) {
		return createGrid(srcImage, gap, span, new int[] { -1 });
	}
}
