/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.util.Arrays;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.LineConvolverFactory;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Used by {@link SeparableKernelConvolution} as {@link LineConvolverFactory}
 * for convolution. {@link ClassCopyProvider} is used to create copies of the
 * byte code of the actual convolvers. This improves their performance, as the
 * JIT can optimize the byte code to the individual use cases.
 * <p>
 * The actual convolvers that are used (depending on the pixel type) are:
 * {@link DoubleConvolverRealType}, {@link FloatConvolverRealType},
 * {@link ConvolverNativeType}, {@link ConvolverNumericType}.
 *
 * @author Matthias Arzt
 */
public class KernelConvolverFactory implements LineConvolverFactory< NumericType< ? > >
{

	private final Kernel1D kernel;

	public KernelConvolverFactory( final Kernel1D kernel )
	{
		this.kernel = kernel;
	}

	@Override
	public long getBorderBefore()
	{
		return +kernel.max();
	}

	@Override
	public long getBorderAfter()
	{
		return -kernel.min();
	}

	@Override
	public Runnable getConvolver( final RandomAccess< ? extends NumericType< ? > > in, final RandomAccess< ? extends NumericType< ? > > out, final int d, final long lineLength )
	{
		final NumericType< ? > targetType = out.get();
		final NumericType< ? > sourceType = in.get();
		final ClassCopyProvider< Runnable > provider = getProvider( sourceType, targetType );
		final List< Class< ? > > key = Arrays.asList( in.getClass(), out.getClass(), sourceType.getClass(), targetType.getClass() );
		return provider.newInstanceForKey( key, kernel, in, out, d, lineLength );
	}

	@Override
	public NumericType< ? > preferredSourceType( NumericType< ? > targetType )
	{
		if (targetType instanceof DoubleType)
			return targetType;
		if (targetType instanceof RealType)
			return new FloatType();
		return targetType;
	}

	private ClassCopyProvider< Runnable > getProvider( final NumericType< ? > sourceType, final NumericType< ? > targetType )
	{
		for ( final Entry entry : factories )
			if ( entry.supported( sourceType, targetType ) )
				return entry.provider;
		throw new IllegalArgumentException( "Convolution is not supported for the given source and target type," +
				" source: " + sourceType.getClass().getSimpleName() +
				" target: " + targetType.getClass().getSimpleName() );
	}

	private static final List< Entry > factories = Arrays.asList(
			new Entry( DoubleConvolverRealType.class, RealType.class, DoubleType.class ),
			new Entry( FloatConvolverRealType.class, RealType.class, RealType.class ),
			new Entry( ConvolverNativeType.class, null, NativeType.class ),
			new Entry( ConvolverNumericType.class, null, NumericType.class ) );

	private static class Entry
	{

		private final ClassCopyProvider< Runnable > provider;

		private final Class< ? extends Type > sourceClass;

		private final Class< ? extends Type > targetClass;

		private Entry( final Class< ? extends Runnable > convolverClass, final Class< ? extends Type > sourceClass, final Class< ? extends Type > targetClass )
		{
			this.provider = new ClassCopyProvider<>( convolverClass, Runnable.class );
			this.sourceClass = sourceClass;
			this.targetClass = targetClass;
		}

		private boolean supported( final Object sourceType, final Type< ? > targetType )
		{
			if ( !targetClass.isInstance( targetType ) )
				return false;
			final Class< ? > sourceClass = this.sourceClass == null ? targetType.getClass() : this.sourceClass;
			return sourceClass.isInstance( sourceType );
		}
	}
}
