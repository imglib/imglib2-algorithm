/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2022 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.list.ListImg;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.RealComposite;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Matthias Arzt
 */
public class KernelConvolverFactoryTest
{

	@Test
	public void test()
	{
		testFactoryTypeMatching( DoubleConvolverRealType.class, ArrayImgs.doubles( 1 ) );
		testFactoryTypeMatching( FloatConvolverRealType.class, ArrayImgs.bytes( 1 ) );
		testFactoryTypeMatching( ConvolverNativeType.class, ArrayImgs.argbs( 1 ) );
		testFactoryTypeMatching( ConvolverNumericType.class, createImageOfNumericType() );
	}

	private ListImg< ? extends NumericType< ? > > createImageOfNumericType()
	{
		// NB: The returned pixel type is not even NativeType.
		NumericType< ? > type = new RealComposite<>( ArrayImgs.bytes( 1 ).randomAccess(), 1 );
		return new ListImg<>( Collections.singleton( type ), 1 );
	}

	private void testFactoryTypeMatching( Class< ? > expectedConvolver, Img< ? extends NumericType< ? > > image )
	{
		KernelConvolverFactory factory = new KernelConvolverFactory( Kernel1D.symmetric( new double[] { 1 } ) );
		Runnable convolver = factory.getConvolver( image.randomAccess(), image.randomAccess(), 0, image.dimension( 0 ) );
		// NB: The classes are different because ClassCopyProvider is used, but the names are still equal.
		assertEquals( expectedConvolver.getName(), convolver.getClass().getName() );
	}
}
