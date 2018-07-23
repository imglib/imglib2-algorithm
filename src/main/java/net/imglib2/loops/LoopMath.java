package net.imglib2.loops;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.LinkedList;

import net.imglib2.Cursor;
import net.imglib2.IterableRealInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

/**
 * An easy yet relatively high performance way to perform pixel-wise math
 * on one or more {@link RandomAccessibleInterval} instances.
 * 
 * <pre>
 * {@code
 * RandomAccessibleInterval<A> img1 = ...
 * RandomAccessibleInterval<B> img2 = ...
 * RandomAccessibleInterval<C> img3 = ...
 * 
 * RandomAccessibleInterval<O> result = ...
 * 
 * LoopMath.compute( result, Div( Max( Max( img1, img2 ), img3 ), 3.0 ) );
 * }
 * </pre>
 * 
 * @author Albert Cardona
 *
 */
public class LoopMath
{

	private LoopMath() {}
	
	static public < I extends RealType< I >, O extends RealType< O > > void compute(
			final RandomAccessibleInterval< O > target,
			final IFunction< O > function
			) throws Exception 
	{
		compute( target, function, false );
	}
	
	static public < I extends RealType< I >, O extends RealType< O > > void compute(
			final RandomAccessibleInterval< O > target,
			final IFunction< O > function,
			final boolean in_doubles
			) throws Exception 
	{
		final Converter< I, O > converter = new Converter<I, O>()
		{
			@Override
			public final void convert( final I input, final O output) {
				output.setReal( input.getRealDouble() );
			}
		};

		compute( target, function, converter, in_doubles );
	}
	
	static public < I extends RealType< I >, O extends RealType< O > > void compute(
			final RandomAccessibleInterval< O > target,
			final IFunction< O > function,
			final Converter<I, O> converter
			) throws Exception 
	{	
		compute( target, function, converter, false );
	}
	
	static public < I extends RealType< I >, O extends RealType< O > > void compute(
			final RandomAccessibleInterval< O > target,
			final IFunction< O > function,
			final Converter<I, O> converter,
			final boolean in_doubles
			) throws Exception 
	{	
		// Recursive copy: initializes interval iterators
		final IFunction< O > f = function.copy();
		// Set temporary computation holders
		final O scrap = target.randomAccess().get().createVariable();
		f.setScrap( scrap );
		
		final LinkedList< RandomAccessibleInterval< ? > > images = findAndPrepareImages( f, converter );
		
		// Check compatible iteration order and dimensions
		if ( compatibleIterationOrder( images ) )
		{
			if ( in_doubles )
			{
				for ( final O output : Views.iterable( target ) )
					output.setReal( f.evalDouble() );
			} else {
				for ( final O output : Views.iterable( target ) )
					f.eval( output );
			}
		}
		else
		{
			// Incompatible iteration order
			final Cursor< O > cursor = Views.iterable( target ).cursor();
			
			if ( in_doubles )
			{
				while ( cursor.hasNext() )
				{
					cursor.fwd();
					cursor.get().setReal( f.evalDouble( cursor ) );
				}
			} else {
				while ( cursor.hasNext() )
				{
					cursor.fwd();
					f.eval( cursor.get(), cursor );
				}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public LinkedList< RandomAccessibleInterval< ? > > findAndPrepareImages( final IFunction< ? > f, final Converter< ?, ? > converter )
	{
		final LinkedList< Object > ops = new LinkedList<>();
		ops.add( f );
		
		final LinkedList< RandomAccessibleInterval< ? > > images = new LinkedList<>();
		
		// Find images
		while ( ! ops.isEmpty() )
		{
			final Object op = ops.removeFirst();
			
			if ( op instanceof IterableImgSource )
			{
				final IterableImgSource iis = ( IterableImgSource )op;
				// Side effect: set the converter from input to output types
				iis.setConverter( converter );
				images.addLast( iis.rai );
			}
			else if ( op instanceof BinaryFunction )
			{
				ops.addLast( ( ( BinaryFunction )op ).a );
				ops.addLast( ( ( BinaryFunction )op ).b );
			}
		}
		
		return images;
	}
	
	/**
	 * Returns true if images have the same dimensions and iterator order.
	 * Returns false when the iteration order is incompatible.
	 * 
	 * @param images
	 * @return
	 * @throws Exception When images have different dimensions.
	 */
	static public boolean compatibleIterationOrder( final LinkedList< RandomAccessibleInterval< ? > > images ) throws Exception
	{
		if ( images.isEmpty() )
		{
			// Purely numeric operations
			return true;
		}

		final Iterator< RandomAccessibleInterval< ? > > it = images.iterator();
		final RandomAccessibleInterval< ? > first = it.next();
		final Object order = Views.iterable( (RandomAccessibleInterval< ? >)first ).iterationOrder();
		
		boolean same_iteration_order = true;
		
		while ( it.hasNext() )
		{
			final RandomAccessibleInterval< ? > other = it.next();
			if ( other.numDimensions() != first.numDimensions() )
			{
				throw new Exception( "Images have different number of dimensions" );
			}
			
			for ( int d = 0; d < first.numDimensions(); ++d )
			{
				if ( first.realMin( d ) != other.realMin( d ) || first.realMax( d ) != other.realMax( d ) )
				{
					throw new Exception( "Images have different sizes" );
				}
			}
			
			if ( ! order.equals( ( (IterableRealInterval< ? >) other ).iterationOrder() ) )
			{
				// Images differ in their iteration order
				same_iteration_order = false;
			}
		}
		
		return same_iteration_order;
	}
	
	static public interface IFunction< O extends RealType< O > >
	{
		public void eval( O output );
		
		public void eval( O output, Localizable loc );
		
		public double evalDouble();
		
		public double evalDouble( Localizable loc );
		
		public IFunction< O > copy();
		
		public void setScrap( O output );
	}
	
	static protected class IterableImgSource< I extends RealType< I >, O extends RealType< O > > implements IFunction< O >
	{
		private final RandomAccessibleInterval< I > rai;
		private final Iterator< I > it;
		private final RandomAccess< I > ra;
		private Converter< RealType< ? >, O > converter;

		public IterableImgSource( final RandomAccessibleInterval< I > rai )
		{
			this.rai = rai;
			this.it = Views.iterable( rai ).iterator();
			this.ra = rai.randomAccess();
		}

		@Override
		public final void eval( final O output ) {
			this.converter.convert( this.it.next(), output );
		}

		@Override
		public final void eval( final O output, final Localizable loc ) {
			this.ra.setPosition( loc );
			this.converter.convert( this.ra.get(), output );
		}
		
		@Override
		public final double evalDouble() {
			return this.it.next().getRealDouble();
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			this.ra.setPosition( loc );
			return this.ra.get().getRealDouble();
		}

		@Override
		public IterableImgSource< I, O > copy()
		{
			return new IterableImgSource< I, O >( this.rai );
		}

		@Override
		public void setScrap(O output) {}
		
		public void setConverter( final Converter< RealType< ? >, O > converter ) {
			this.converter = converter;
		}
	}
	
	static protected class NumberSource< O extends RealType< O > > implements IFunction< O >
	{
		private final double number;
		
		public NumberSource( final Number number ) {
			this.number = number.doubleValue();
		}

		@Override
		public void eval( final O output ) {
			output.setReal( this.number );
		}

		@Override
		public void eval( final O output, final Localizable loc) {
			output.setReal( this.number );
		}
		
		@Override
		public final double evalDouble() {
			return this.number;
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return this.number;
		}

		@Override
		public NumberSource< O > copy()
		{
			return new NumberSource< O >( this.number );
		}

		@Override
		public void setScrap(O output) {}
	}
	
	static abstract public class Function< O extends RealType< O> >
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final public IFunction< O > wrap( final Object o )
		{
			if ( o instanceof RandomAccessibleInterval< ? > )
			{
				return new IterableImgSource( (RandomAccessibleInterval) o );
			}
			else if ( o instanceof Number )
			{
				return new NumberSource( ( (Number) o ).doubleValue() );
			}
			else if ( o instanceof IFunction )
			{
				return ( (IFunction) o ).copy();
			}
			
			// Make it fail
			return null;
		}
		
		@SuppressWarnings("unchecked")
		final public < F extends BinaryFunction< O > > Pair< IFunction< O >, IFunction< O > > wrapMap( final Object[] obs )
		{	
			try {
				final Constructor< ? > constructor = this.getClass().getConstructor( new Class[]{ Object.class, Object.class } );
				BinaryFunction< O > a = ( BinaryFunction< O > )constructor.newInstance( obs[0], obs[1] );
				BinaryFunction< O > b;

				for ( int i = 2; i < obs.length -1; ++i )
				{
					b = ( BinaryFunction< O > )constructor.newInstance( a, obs[i] );
					a = b;
				}
				
				final BinaryFunction< O > f = a;
				
				return new Pair< LoopMath.IFunction< O >, LoopMath.IFunction< O > >()
				{
					@Override
					public IFunction<O> getA() { return f; }

					@Override
					public IFunction<O> getB() { return f.wrap( obs[ obs.length - 1 ] ); }
				};
				
			} catch (Exception e)
			{
				throw new RuntimeException( "Error with the constructor for class " + this.getClass(), e );
			}
		}
	}

	static abstract public class BinaryFunction< O extends RealType< O > > extends Function< O > implements IFunction< O >
	{
		protected final IFunction< O > a, b;

		protected O scrap;
		
		public BinaryFunction( final Object o1, final Object o2 )
		{
			this.a = this.wrap( o1 );
			this.b = this.wrap( o2 );
		}
		
		public BinaryFunction( final Object... obs )
		{
			final Pair< IFunction< O >, IFunction< O > > p = this.wrapMap( obs );
			this.a = p.getA();
			this.b = p.getB();
		}
		
		public void setScrap( final O output )
		{
			if ( null == output ) return; 
			this.scrap = output.copy();
			this.a.setScrap( output );
			this.b.setScrap( output );
		}
	}
	
	static public class Mul< O extends RealType< O > > extends BinaryFunction< O >
	{

		public Mul( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Mul( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.mul( this.scrap );
		}

		@Override
		public final void eval( final O output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.mul( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return this.a.evalDouble() * this.b.evalDouble();
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return this.a.evalDouble( loc ) * this.b.evalDouble( loc );
		}

		@Override
		public Mul< O > copy() {
			final Mul< O > f = new Mul< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Div< O extends RealType< O > > extends BinaryFunction< O > implements IFunction< O >
	{

		public Div( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Div( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.div( this.scrap );
		}
		
		@Override
		public final void eval( final O output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.div( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return this.a.evalDouble() / this.b.evalDouble();
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return this.a.evalDouble( loc ) / this.b.evalDouble( loc );
		}

		@Override
		public Div< O > copy() {
			final Div< O > f = new Div< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Max< O extends RealType< O > > extends BinaryFunction< O > implements IFunction< O >
	{

		public Max( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Max( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			if ( -1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@Override
		public final void eval( final O output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			if ( -1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return Math.max( this.a.evalDouble(),  this.b.evalDouble() );
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return Math.max( this.a.evalDouble( loc ), this.b.evalDouble( loc ) );
		}

		@Override
		public Max< O > copy() {
			final Max< O > f = new Max< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Min< O extends RealType< O > > extends BinaryFunction< O >
	{

		public Min( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Min( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			if ( 1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@Override
		public final void eval( final O output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			if ( 1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return Math.min( this.a.evalDouble(),  this.b.evalDouble() );
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return Math.min( this.a.evalDouble( loc ),  this.b.evalDouble( loc ) );
		}

		@Override
		public Min< O > copy() {
			final Min< O > f = new Min< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Add< O extends RealType< O > > extends BinaryFunction< O >
	{

		public Add( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Add( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.add( this.scrap );
		}
		
		@Override
		public final void eval( final O output, final Localizable loc ) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.add( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return this.a.evalDouble() + this.b.evalDouble();
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return this.a.evalDouble( loc ) + this.b.evalDouble( loc );
		}

		@Override
		public Add< O > copy() {
			final Add< O > f = new Add< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Sub< O extends RealType< O > > extends BinaryFunction< O >
	{

		public Sub( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Sub( final Object... obs )
		{
			super( obs );
		}

		@Override
		public final void eval( final O output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.sub( this.scrap );
		}
		
		@Override
		public final void eval( final O output, final Localizable loc ) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.sub( this.scrap );
		}
		
		@Override
		public final double evalDouble() {
			return this.a.evalDouble() - this.b.evalDouble();
		}
		
		@Override
		public final double evalDouble( final Localizable loc ) {
			return this.a.evalDouble( loc ) - this.b.evalDouble( loc );
		}

		@Override
		public Sub< O > copy() {
			final Sub< O > f = new Sub< O >( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Neg< O extends RealType< O > > extends Sub< O >
	{
		public Neg( final Object o )
		{
			super( 0, o );
		}
	}
}
