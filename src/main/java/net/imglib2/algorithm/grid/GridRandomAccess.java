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

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;

/**
 * {@link RandomAccess} for a grid.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class GridRandomAccess<T> implements RandomAccess<RandomAccessibleInterval<T>> {

	private long[] position; // current position
	private Patch<T> patch; // current patch
	private long[] origin; // origin of the grid in the coordinates of the
							// source image
	private long[] gridDims; // dimensions of the grid
	// if parameters are added here, add them also in the copyRandomAccess()
	// method

	private GridRandomAccess(GridRandomAccess<T> access) {
		this.position = position.clone();
		this.patch = access.patch;
		this.origin = access.origin.clone();
		this.gridDims = access.gridDims.clone();
	}

	public GridRandomAccess(RandomAccessibleInterval<T> srcImage, long[] gap, long[] span, int[] whichDims,
			long[] origin, long[] gridDims) {
		this.position = new long[srcImage.numDimensions()];
		this.patch = new Patch<>(srcImage, gap, span, whichDims);
		this.origin = origin;
		this.gridDims = gridDims;
	}

	@Override
	public int getIntPosition(int arg0) {
		return (int) position[arg0];
	}

	@Override
	public long getLongPosition(int arg0) {
		return position[arg0];
	}

	@Override
	public void localize(int[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			arg0[i] = (int) position[i];
		}
	}

	@Override
	public void localize(long[] arg0) {
		arg0 = position;
	}

	@Override
	public double getDoublePosition(int arg0) {
		return position[arg0];
	}

	@Override
	public float getFloatPosition(int arg0) {
		return position[arg0];
	}

	@Override
	public void localize(float[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			arg0[i] = position[i];
		}
	}

	@Override
	public void localize(double[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			arg0[i] = position[i];
		}
	}

	@Override
	public int numDimensions() {
		return position.length;
	}

	@Override
	public void bck(int arg0) {
		position[arg0]--;

	}

	@Override
	public void fwd(int arg0) {
		position[arg0]++;

	}

	@Override
	public void move(Localizable arg0) {
		for (int i = 0; i < arg0.numDimensions(); i++) {
			position[i] += arg0.getIntPosition(i);
		}
	}

	@Override
	public void move(int[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			position[i] += arg0[i];
		}
	}

	@Override
	public void move(long[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			position[i] += arg0[i];
		}
	}

	@Override
	public void move(int arg0, int arg1) {
		position[arg1] += arg0;
	}

	@Override
	public void move(long arg0, int arg1) {
		position[arg1] += arg0;
	}

	@Override
	public void setPosition(Localizable arg0) {
		arg0.localize(position);
	}

	@Override
	public void setPosition(int[] arg0) {
		for (int i = 0; i < position.length; i++) {
			position[i] = arg0[i];
		}
	}

	@Override
	public void setPosition(long[] arg0) {
		position = arg0;
	}

	@Override
	public void setPosition(int arg0, int arg1) {
		position[arg1] = arg0;
	}

	@Override
	public void setPosition(long arg0, int arg1) {
		position[arg1] = arg0;
	}

	@Override
	public Sampler<RandomAccessibleInterval<T>> copy() {
		return this.copy();
	}

	@Override
	public Patch<T> get() {
		// check if position is inside of the interval
		for (int i = 0; i < position.length; i++) {
			if ((position[i] < 0) || (position[i] >= gridDims[i]))
				throw new IndexOutOfBoundsException("Position is out of bounds!");
		}
		patch.update(position, origin);
		return patch;
	}

	@Override
	public RandomAccess<RandomAccessibleInterval<T>> copyRandomAccess() {
		return new GridRandomAccess<>(this);
	}

}
