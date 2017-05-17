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

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.type.Type;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * A patch of a {@link RandomAccessibleInterval}.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class Patch<T> implements RandomAccessibleInterval<T>, Type<Patch<T>> {

	private RandomAccessibleInterval<T> srcImage;
	private long[] pos;
	private RandomAccessibleInterval<T> patch;
	private int[] whichDims;
	private long[] span;
	private long[] gap;
	// if parameters are added here, add them also in the set() method and
	// copy() method

	private Patch(Patch<T> access) {
		this.srcImage = access.srcImage;
		this.gap = access.gap.clone();
		this.span = access.span.clone();
		this.whichDims = access.whichDims.clone();
		this.pos = access.pos.clone();
		this.patch = access.patch;
	}

	/**
	 * Create a patch placed at the origin of the grid.
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
	public Patch(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span, int[] whichDims) {
		this.srcImage = srcImage;
		this.gap = gap;
		this.span = span;
		if (whichDims[0] == -1) {
			whichDims = new int[srcImage.numDimensions()];
			Arrays.fill(whichDims, 1);
		}
		this.whichDims = whichDims;

		update(new long[srcImage.numDimensions()], gap);
	}

	public Patch(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span) {
		new Patch<>(srcImage, gap, span, new int[] { -1 });
	}

	public Patch(RandomAccessibleInterval<T> srcImage, long gap, long span, int[] whichDims) {
		long[] gapA = new long[srcImage.numDimensions()];
		long[] spanA = new long[srcImage.numDimensions()];
		Arrays.fill(gapA, gap);
		Arrays.fill(spanA, span);
		new Patch<>(srcImage, gapA, spanA, whichDims);
	}

	public Patch(RandomAccessibleInterval<T> srcImage, long gap, long span) {
		new Patch<>(srcImage, gap, span, new int[] { -1 });
	}

	public long[] getPosition() {
		return pos;
	}

	public RandomAccessibleInterval<T> getPatch() {
		return patch;
	}

	@Override
	public RandomAccess<T> randomAccess() {
		return patch.randomAccess();
	}

	@Override
	public RandomAccess<T> randomAccess(Interval interval) {
		return patch.randomAccess(interval);
	}

	/**
	 * 
	 * @param gridPos
	 *            position of the grid where the patch should be placed
	 * @param origin
	 *            origin of the gird (left top)
	 */
	public void update(final long[] gridPos, long[] origin) {
		// compute the actual position of the patch in the coordinates of the
		// source image
		long[] patchPos = new long[gridPos.length];
		for (int i = 0; i < gridPos.length; i++) {
			patchPos[i] = origin[i] + gridPos[i] * gap[i];
		}
		// compute min and max of the interval of the patch
		long[] min = new long[patchPos.length];
		long[] max = new long[patchPos.length];
		for (int i = 0; i < patchPos.length; i++) {
			if (whichDims[i] == 1) {
				min[i] = patchPos[i] - span[i];
				max[i] = patchPos[i] + span[i];
			} else {
				min[i] = patchPos[i];
				max[i] = patchPos[i];
			}
		}
		this.pos = patchPos;

		// check if the center of the patch is inside the interval of the source
		// image
		RandomAccessibleInterval<T> raiPos = Views.interval(srcImage, patchPos, patchPos);
		if (!Intervals.contains(srcImage, raiPos)) {
			throw new IndexOutOfBoundsException("IndexOutOfBoundsException");
		}
		RandomAccessibleInterval<T> raiPatch = Views.interval(srcImage, min, max);
		// extend the source image if the patch and borders overlap
		if (!Intervals.contains(srcImage, raiPatch)) {
			raiPatch = Views.interval(Views.extendMirrorSingle(srcImage), min, max);
		}

		this.patch = Views.zeroMin(raiPatch);
	}

	@Override
	public int numDimensions() {
		return patch.numDimensions();
	}

	@Override
	public long min(int d) {
		return patch.min(d);
	}

	@Override
	public void min(long[] min) {
		patch.min(min);
	}

	@Override
	public void min(Positionable min) {
		patch.min(min);
	}

	@Override
	public long max(int d) {
		return patch.max(d);
	}

	@Override
	public void max(long[] max) {
		patch.max(max);
	}

	@Override
	public void max(Positionable max) {
		patch.max(max);
	}

	@Override
	public double realMin(int d) {
		return patch.realMin(d);
	}

	@Override
	public void realMin(double[] min) {
		patch.realMin(min);
	}

	@Override
	public void realMin(RealPositionable min) {
		patch.realMin(min);
	}

	@Override
	public double realMax(int d) {
		return patch.realMax(d);
	}

	@Override
	public void realMax(double[] max) {
		patch.realMax(max);
	}

	@Override
	public void realMax(RealPositionable max) {
		patch.realMax(max);
	}

	@Override
	public void dimensions(long[] dimensions) {
		patch.dimensions(dimensions);
	}

	@Override
	public long dimension(int d) {
		return patch.dimension(d);
	}

	@Override
	public Patch<T> createVariable() {
		// create new patch located at 0,0
		return new Patch<>(srcImage, gap, span, whichDims);
	}

	@Override
	public Patch<T> copy() {
		// create copy of patch which is fixed to the current coordinates
		return new Patch<>(this);
	}

	@Override
	public void set(Patch<T> c) {
		// check compatibility and then set values
		if ((c.pos.length == c.gap.length) && (c.pos.length == c.gap.length) && (c.whichDims.length == c.gap.length)
				&& (c.span.length == c.gap.length)) {
			this.gap = c.gap.clone();
			this.pos = c.pos;
			this.whichDims = c.whichDims;
			this.span = c.span;
			this.srcImage = c.srcImage;

			update(new long[c.srcImage.numDimensions()], c.gap);
		} else {
			throw new IllegalArgumentException("Array lengths do not match.");
		}
	}
}
