/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.kdtree;

import net.imglib2.KDTree;
import net.imglib2.KDTreeNode;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * above and below a given hyperplane, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #split(HyperPlane)} to
 * partition with respect to a {@link HyperPlane}. Then call
 * {@link #getAboveNodes()} and {@link #getBelowNodes()} to get the sets of
 * nodes above and below the hyperplane, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="https://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @param <T>
 *            type of values stored in the tree.
 *
 * @author Tobias Pietzsch
 */
public class SplitHyperPlaneKDTree< T >
{
	private final SplitHyperPlaneKDTreeImpl impl;
	private final KDTreeNodeIterable< T > aboveNodes;
	private final KDTreeNodeIterable< T > belowNodes;

	public SplitHyperPlaneKDTree( final KDTree< T > tree )
	{
		impl = new SplitHyperPlaneKDTreeImpl( tree.impl() );
		aboveNodes = new KDTreeNodeIterable<>( impl.getAboveNodes(), tree );
		belowNodes = new KDTreeNodeIterable<>( impl.getBelowNodes(), tree );
	}

	public int numDimensions()
	{
		return impl.numDimensions();
	}

	public void split( final HyperPlane plane )
	{
		impl.split( plane );
	}

	public void split( final double[] plane )
	{
		impl.split( plane );
	}

	public Iterable< KDTreeNode< T > > getAboveNodes()
	{
		return aboveNodes;
	}

	public Iterable< KDTreeNode< T > > getBelowNodes()
	{
		return belowNodes;
	}

}
