/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2018 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.loop;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.loops.LoopBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FastCursorRandomAccessLoops
{
	private FastCursorRandomAccessLoops()
	{
		// prevent from instantiation
	}

	static void loop( final Object action, long n, final Cursor< ? > cursor, final List< ? extends RandomAccess< ? > > randomAccesses )
	{
		createLoop( action, cursor, randomAccesses ).accept( n );
	}

	private static final List< ClassCopyProvider< LongConsumer > > factories = Arrays.asList(
			new ClassCopyProvider<>( OneCursorLoop.class, LongConsumer.class ),
			new ClassCopyProvider<>( TwoCursorLoop.class, LongConsumer.class ),
			new ClassCopyProvider<>( ThreeCursorLoop.class, LongConsumer.class ) );

	private static LongConsumer createLoop( final Object action, final Cursor< ? > cursor, final List< ? extends RandomAccess< ? > > randomAccesses )
	{
		final Object[] arguments = Stream.concat( Stream.of( action, cursor ), randomAccesses.stream() ).toArray();
		ClassCopyProvider< LongConsumer > factory = factories.get( randomAccesses.size() );
		final List< Class< ? > > key = Stream.of( arguments ).map( Object::getClass ).collect( Collectors.toList() );
		return factory.newInstanceForKey( key, arguments );
	}

	public static class OneCursorLoop< A > implements LongConsumer
	{

		private final Consumer< A > action;

		private final Cursor< A > cursorA;

		public OneCursorLoop( final Consumer< A > action, final Cursor< A > cursorA )
		{
			this.action = action;
			this.cursorA = cursorA;
		}

		@Override
		public void accept( long n )
		{
			while ( --n >= 0 )
				action.accept( cursorA.next() );
		}
	}

	public static class TwoCursorLoop< A, B > implements LongConsumer
	{

		private final BiConsumer< A, B > action;

		private final Cursor< A > cursorA;

		private final RandomAccess< B > randomAccessB;

		public TwoCursorLoop( final BiConsumer< A, B > action, final Cursor< A > cursorA, final RandomAccess< B > randomAccessB )
		{
			this.action = action;
			this.cursorA = cursorA;
			this.randomAccessB = randomAccessB;
		}

		@Override
		public void accept( long n )
		{
			while ( --n >= 0 )
			{
				A a = cursorA.next();
				randomAccessB.setPosition( cursorA );
				action.accept( a, randomAccessB.get() );
			}
		}
	}

	public static class ThreeCursorLoop< A, B, C > implements LongConsumer
	{

		private final LoopBuilder.TriConsumer< A, B, C > action;

		private final Cursor< A > cursorA;

		private final RandomAccess< B > randomAccessB;

		private final RandomAccess< C > randomAccessC;

		public ThreeCursorLoop( final LoopBuilder.TriConsumer< A, B, C > action, final Cursor< A > cursorA, final RandomAccess< B > randomAccessB, final RandomAccess< C > randomAccessC )
		{
			this.action = action;
			this.cursorA = cursorA;
			this.randomAccessB = randomAccessB;
			this.randomAccessC = randomAccessC;
		}

		@Override
		public void accept( long n )
		{
			while ( --n >= 0 )
			{
				A a = cursorA.next();
				randomAccessB.setPosition( cursorA );
				randomAccessC.setPosition( cursorA );
				action.accept( a, randomAccessB.get(), randomAccessC.get() );
			}
		}
	}

}
