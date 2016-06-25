/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.morphology;

import static org.junit.Assert.assertEquals;

import net.imglib2.algorithm.morphology.table2d.Branchpoints;
import net.imglib2.algorithm.morphology.table2d.Bridge;
import net.imglib2.algorithm.morphology.table2d.Clean;
import net.imglib2.algorithm.morphology.table2d.Endpoints;
import net.imglib2.algorithm.morphology.table2d.Fill;
import net.imglib2.algorithm.morphology.table2d.Hbreak;
import net.imglib2.algorithm.morphology.table2d.Life;
import net.imglib2.algorithm.morphology.table2d.Majority;
import net.imglib2.algorithm.morphology.table2d.Remove;
import net.imglib2.algorithm.morphology.table2d.Spur;
import net.imglib2.algorithm.morphology.table2d.Thicken;
import net.imglib2.algorithm.morphology.table2d.Thin;
import net.imglib2.algorithm.morphology.table2d.Vbreak;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;

import org.junit.Test;

/**
 * Tests 2d morphology operations backed by lookup tables.
 *
 * @author Leon Yang
 * @author Lee Kamentsky
 */
public class Table2dTest
{

	@Test
	public void testBranchpoints()
	{
		// 1 0 1 0 1
		// 0 1 0 0 0
		// 0 0 1 0 1
		// 0 1 0 1 0
		// 1 0 0 0 0
		final boolean[] data = new boolean[] {
				true, false, true, false, true,
				false, true, false, false, false,
				false, false, true, false, true,
				false, true, false, true, false,
				true, false, false, false, false,
		};
		// 0 0 0 0 0
		// 0 1 0 0 0
		// 0 0 1 0 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		final boolean[] expected = new boolean[] {
				false, false, false, false, false,
				false, true, false, false, false,
				false, false, true, false, false,
				false, false, false, false, false,
				false, false, false, false, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Branchpoints.branchpoints( in );

		assertImgEquals( expected, out );
	}

	@Test
	public void testBridge()
	{
		// 1 0 1 1 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		// 0 0 1 0 1
		// 1 0 0 0 0
		final boolean[] data = new boolean[] {
				true, false, true, true, false,
				false, false, false, false, false,
				false, false, false, false, false,
				false, false, true, false, true,
				true, false, false, false, false,
		};
		// 1 1 1 1 0
		// 0 1 0 0 0
		// 0 0 0 1 0
		// 0 1 1 1 1
		// 1 1 0 1 0
		final boolean[] expected = new boolean[] {
				true, true, true, true, false,
				false, true, false, false, false,
				false, false, false, true, false,
				false, true, true, true, true,
				true, true, false, true, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Bridge.bridge( in );

		assertImgEquals( expected, out );
	}

	@Test
	public void testClean()
	{
		// 1 0 0 0 0
		// 0 0 1 0 1
		// 0 0 0 0 0
		// 0 0 1 0 1
		// 0 1 0 0 1
		final boolean[] data = new boolean[] {
				true, false, false, false, false,
				false, false, true, false, true,
				false, false, false, false, false,
				false, false, true, false, true,
				false, true, false, false, true,
		};
		// 0 0 0 0 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		// 0 0 1 0 1
		// 0 1 0 0 1
		final boolean[] expected = new boolean[] {
				false, false, false, false, false,
				false, false, false, false, false,
				false, false, false, false, false,
				false, false, true, false, true,
				false, true, false, false, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Clean.clean( in );

		assertImgEquals( expected, out );
	}

	@Test
	public void testEndpoints()
	{
		// 1 0 1 0 1
		// 0 1 0 0 0
		// 0 0 1 0 1
		// 0 1 0 1 0
		// 1 0 0 0 0
		final boolean[] data = new boolean[] {
				true, false, true, false, true,
				false, true, false, false, false,
				false, false, true, false, true,
				false, true, false, true, false,
				true, false, false, false, false,
		};
		// 1 0 1 0 1
		// 0 0 0 0 0
		// 0 0 0 0 1
		// 0 0 0 0 0
		// 1 0 0 0 0
		final boolean[] expected = new boolean[] {
				true, false, true, false, true,
				false, false, false, false, false,
				false, false, false, false, true,
				false, false, false, false, false,
				true, false, false, false, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Endpoints.endpoints( in );

		assertImgEquals( expected, out );
	}

	@Test
	public void testFill()
	{
		// 1 0 1 1 1
		// 1 1 1 0 0
		// 1 0 1 1 1
		// 1 1 1 0 1
		// 0 1 0 1 1
		final boolean[] data = new boolean[] {
				true, false, true, true, true,
				true, true, true, false, false,
				true, false, true, true, true,
				true, true, true, false, true,
				false, true, false, true, true,
		};
		// 1 1 1 1 1
		// 1 1 1 0 0
		// 1 1 1 1 1
		// 1 1 1 0 1
		// 1 1 0 1 1
		final boolean[] expected = new boolean[] {
				true, true, true, true, true,
				true, true, true, false, false,
				true, true, true, true, true,
				true, true, true, false, true,
				true, true, false, true, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Fill.fill( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testHbreak()
	{
		// 1 1 1 0 1
		// 0 1 0 1 0
		// 1 1 1 1 1
		// 0 1 0 0 1
		// 1 0 1 1 1
		final boolean[] data = new boolean[] {
				true, true, true, false, true,
				false, true, false, true, false,
				true, true, true, true, true,
				false, true, false, false, true,
				true, false, true, true, true,
		};
		// 1 1 1 0 1
		// 0 0 0 1 0
		// 1 1 1 1 1
		// 0 1 0 0 1
		// 1 0 1 1 1
		final boolean[] expected = new boolean[] {
				true, true, true, false, true,
				false, false, false, true, false,
				true, true, true, true, true,
				false, true, false, false, true,
				true, false, true, true, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Hbreak.hbreak( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testLife()
	{
		// 1 0 1 1 1
		// 0 0 0 0 0
		// 1 1 0 1 0
		// 0 1 1 1 1
		// 1 1 0 1 0
		final boolean[] data = new boolean[] {
				true, false, true, true, true,
				false, false, false, false, false,
				true, true, false, true, false,
				false, true, true, true, true,
				true, true, false, true, false,
		};
		// 0 0 0 1 0
		// 1 0 0 0 1
		// 1 1 0 1 1
		// 0 0 0 0 1
		// 1 1 0 1 1
		final boolean[] expected = new boolean[] {
				false, false, false, true, false,
				true, false, false, false, true,
				true, true, false, true, true,
				false, false, false, false, true,
				true, true, false, true, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Life.life( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testMajority()
	{
		// 1 1 1 1 1
		// 1 0 1 1 0
		// 0 0 0 0 0
		// 1 0 0 1 0
		// 1 1 1 0 0
		final boolean[] data = new boolean[] {
				true, true, true, true, true,
				true, false, true, true, false,
				false, false, false, false, false,
				true, false, false, true, false,
				true, true, true, false, false,
		};
		// 0 1 1 1 0
		// 0 1 1 1 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		final boolean[] expected = new boolean[] {
				false, true, true, true, false,
				false, true, true, true, false,
				false, false, false, false, false,
				false, false, false, false, false,
				false, false, false, false, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Majority.majority( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testRemove()
	{
		// 0 1 1 1 0
		// 1 1 1 1 0
		// 0 1 0 1 1
		// 1 1 1 1 1
		// 1 1 1 1 0
		final boolean[] data = new boolean[] {
				false, true, true, true, false,
				true, true, true, true, false,
				false, true, false, true, true,
				true, true, true, true, true,
				true, true, true, true, false,
		};
		// 0 1 1 1 0
		// 1 0 1 1 0
		// 0 1 0 1 1
		// 1 0 1 0 1
		// 1 1 1 1 0
		final boolean[] expected = new boolean[] {
				false, true, true, true, false,
				true, false, true, true, false,
				false, true, false, true, true,
				true, false, true, false, true,
				true, true, true, true, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Remove.remove( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testSpur()
	{
		// 1 1 1 0 1
		// 0 1 0 1 0
		// 0 0 0 0 0
		// 0 1 0 1 0
		// 0 1 0 0 1
		final boolean[] data = new boolean[] {
				true, true, true, false, true,
				false, true, false, true, false,
				false, false, false, false, false,
				false, true, false, true, false,
				false, true, false, false, true,
		};
		// 1 1 1 0 1
		// 0 1 0 1 0
		// 0 0 0 0 0
		// 0 0 0 0 0
		// 0 1 0 0 1
		final boolean[] expected = new boolean[] {
				true, true, true, false, true,
				false, true, false, true, false,
				false, false, false, false, false,
				false, false, false, false, false,
				false, true, false, false, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Spur.spur( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testThicken()
	{
		// 1 1 1 0 1
		// 1 0 1 1 0
		// 1 0 1 0 0
		// 1 0 1 0 1
		// 1 1 1 0 0
		final boolean[] data = new boolean[] {
				true, true, true, false, true,
				true, false, true, true, false,
				true, false, true, false, false,
				true, false, true, false, true,
				true, true, true, false, false,
		};
		// 1 1 1 1 1
		// 1 1 1 1 1
		// 1 0 1 0 0
		// 1 1 1 0 1
		// 1 1 1 0 1
		final boolean[] expected = new boolean[] {
				true, true, true, true, true,
				true, true, true, true, true,
				true, false, true, false, false,
				true, true, true, false, true,
				true, true, true, false, true,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Thicken.thicken( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testThin()
	{
		// Erode a pixel only if you're sure that it won't
		// change the Euler # by creating a new component or
		// an addtional hole.
		//
		// 1 1 1 1 1
		// 1 1 0 1 0
		// 1 1 1 1 0
		// 1 1 0 1 0
		// 1 0 0 0 0
		final boolean[] data = new boolean[] {
				true, true, true, true, true,
				true, true, false, true, false,
				true, true, true, true, false,
				true, true, false, true, false,
				true, false, false, false, false
		};
		// What we get -- what might be if thin2 and thin1 were reversed
		//
		// 1 1 1 1 1 -- 1 1 1 1 1
		// 1 1 0 1 0 -- 1 1 0 1 0
		// 1 1 1 0 0 -- 1 1 1 1 0
		// 1 0 0 1 0 -- 1 0 0 0 0
		// 1 0 0 0 0 -- 1 0 0 0 0
		final boolean[] expected = new boolean[] {
				true, true, true, true, true,
				true, true, false, true, false,
				true, true, true, false, false,
				true, false, false, true, false,
				true, false, false, false, false

		};
		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Thin.thin( in );
		assertImgEquals( expected, out );
	}

	@Test
	public void testVbreak()
	{
		// 1 0 1 0 1
		// 0 1 1 1 1
		// 1 0 1 0 1
		// 1 0 1 1 1
		// 1 1 1 0 0
		final boolean[] data = new boolean[] {
				true, false, true, false, true,
				false, true, true, true, true,
				true, false, true, false, true,
				true, false, true, true, true,
				true, true, true, false, false,
		};
		// 1 0 1 0 1
		// 0 1 1 0 1
		// 1 0 1 0 1
		// 1 0 1 1 1
		// 1 1 1 0 0
		final boolean[] expected = new boolean[] {
				true, false, true, false, true,
				false, true, true, false, true,
				true, false, true, false, true,
				true, false, true, true, true,
				true, true, true, false, false,
		};

		final Img< BitType > in = initImg( data, 5, 5 );
		final Img< BitType > out = Vbreak.vbreak( in );
		assertImgEquals( expected, out );
	}

	private Img< BitType > initImg( final boolean[] data, final long... dim )
	{
		final Img< BitType > img = ArrayImgs.bits( dim );
		int i = 0;
		for ( final BitType px : img )
			px.set( data[ i++ ] );
		return img;
	}

	private void assertImgEquals( final boolean[] expected, final Img< BitType > img )
	{
		int i = 0;
		for ( final BitType px : img )
			assertEquals( px.get(), expected[ i++ ] );
	}
}
