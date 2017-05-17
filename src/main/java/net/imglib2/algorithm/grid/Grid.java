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

import net.imglib2.AbstractInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;

/**
 * A grid of patches. Its patches can be accessed by a {@link RandomAccess}.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class Grid<T> extends AbstractInterval implements RandomAccessibleInterval<RandomAccessibleInterval<T>> {

	private long[] gap;
	private long[] span;
	final private RandomAccessibleInterval<T> srcImage;
	private int[] whichDims;
	private long[] origin;

	/**
	 * @param srcImage
	 *            input/source image
	 * @param gap
	 *            distance/gap between two patches
	 * @param span
	 *            span of each patch: size = 2 * span + 1
	 * @param origin
	 *            origin of the grid is at the left top
	 * @param gridDims
	 *            dimensions of the grid
	 * @param whichDims
	 *            array contains zeros and ones and defines which dimensions the
	 *            patches have
	 */
	public Grid(RandomAccessibleInterval<T> srcImage, long gap, long span, long[] origin, long[] gridDims,
			int[] whichDims) {
		super(gridDims);
		this.srcImage = srcImage;
		this.origin = origin;
		this.gap = new long[srcImage.numDimensions()];
		this.span = new long[srcImage.numDimensions()];
		Arrays.fill(this.gap, gap);
		Arrays.fill(this.span, span);
		if (whichDims[0] == -1) {
			whichDims = new int[srcImage.numDimensions()];
			Arrays.fill(whichDims, 1);
		}
		this.whichDims = whichDims;
	}

	/**
	 * whichDims is filled with ones
	 */
	public Grid(RandomAccessibleInterval<T> srcImage, long gap, long span, long[] origin, long[] gridDims) {
		this(srcImage, gap, span, origin, gridDims, new int[] { -1 });
	}

	/**
	 * gap and span have different values for each dimension
	 */
	public Grid(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span, long[] origin, long[] gridDims,
			int[] whichDims) {
		super(gridDims);
		this.srcImage = srcImage;
		this.origin = origin;
		this.gap = gap;
		this.span = span;
		if (whichDims[0] == -1) {
			whichDims = new int[srcImage.numDimensions()];
			Arrays.fill(whichDims, 1);
		}
		this.whichDims = whichDims;
	}

	/**
	 * whichDims is filled with ones
	 */
	public Grid(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span, long[] origin, long[] gridDims) {
		this(srcImage, gap, span, origin, gridDims, new int[] { -1 });
	}

	@Override
	public RandomAccess<RandomAccessibleInterval<T>> randomAccess() {
		long[] gridDims = new long[srcImage.numDimensions()];
		this.dimensions(gridDims);
		return new GridRandomAccess<>(srcImage, gap, span, whichDims, origin, gridDims);
	}

	@Override
	public RandomAccess<RandomAccessibleInterval<T>> randomAccess(Interval arg0) {
		return randomAccess();
	}

	public long[] getGap() {
		return gap;
	}

	public long[] getSpan() {
		return span;
	}

}
