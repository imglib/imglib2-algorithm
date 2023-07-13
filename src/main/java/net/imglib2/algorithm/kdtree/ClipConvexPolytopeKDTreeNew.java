/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.util.ArrayList;
import java.util.Arrays;
import net.imglib2.kdtree.KDTree;
import net.imglib2.kdtree.KDTreeNode;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * inside and outside a given convex polytope, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #clip(ConvexPolytope)} to
 * partition with respect to a {@link ConvexPolytope}. Then call
 * {@link #getInsideNodes()} and {@link #getOutsideNodes()} to get the sets of
 * nodes inside and outside the polytope, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="http://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @param <T>
 *            type of values stored in the tree.
 *
 * @author Tobias Pietzsch
 */
public class ClipConvexPolytopeKDTreeNew< T >
{
	private final ClipConvexPolytopeKDTreeImpl< Object > impl;
	private final KDTreeNodeIterableNew< T > insideNodes;
	private final KDTreeNodeIterableNew< T > outsideNodes;

	public ClipConvexPolytopeKDTreeNew( final KDTree< T > tree )
	{
		impl = new ClipConvexPolytopeKDTreeImpl<>( tree.impl() );
		insideNodes = new KDTreeNodeIterableNew<>( impl.getInsideNodes(), tree );
		outsideNodes = new KDTreeNodeIterableNew<>( impl.getOutsideNodes(), tree );
	}

	public int numDimensions()
	{
		return impl.numDimensions();
	}

	public void clip( final ConvexPolytope polytope )
	{
		impl.clip( polytope );
	}

	public void clip( final double[][] planes )
	{
		impl.clip( planes );
	}

	public Iterable< KDTreeNode< T > > getInsideNodes()
	{
		return insideNodes;
	}

	public Iterable< KDTreeNode< T > > getOutsideNodes()
	{
		return outsideNodes;
	}
}
