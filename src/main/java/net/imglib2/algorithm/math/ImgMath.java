package net.imglib2.algorithm.math;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * An easy yet relatively high performance way to perform pixel-wise math
 * on one or more {@link RandomAccessibleInterval} instances.
 * 
 * An example in java:
 * 
 * <pre>
 * {@code
 * RandomAccessibleInterval<A> img1 = ...
 * RandomAccessibleInterval<B> img2 = ...
 * RandomAccessibleInterval<C> img3 = ...
 * 
 * RandomAccessibleInterval<O> result = ...
 * 
 * new ImgMath<A, O>( Div<O>( Max<O>( img1, img2, img3 ), 3.0 ) ).into( result );
 * }
 * </pre>
 * 
 * While java compilation cares about exact types, the type erasure that happens
 * at compile time means that input types can be mixed, as long as all of them
 * extend RealType.
 * 
 * @author Albert Cardona
 *
 */
public class ImgMath
{
	private final IFunction operation;
	private final Converter< RealType< ? >, RealType< ? > > converter;
	
	public ImgMath(
			final IFunction operation
			) throws Exception 
	{
		this( operation,
			  new Converter< RealType< ? >, RealType< ? > >()
		{
			@Override
			public final void convert( final RealType< ? > input, final RealType< ? > output) {
				output.setReal( input.getRealDouble() );
			}
		});
	}
	
	public ImgMath(
			final IFunction operation,
			final Converter< RealType< ? >, RealType< ? > > converter
			) throws Exception 
	{
		this.operation = operation;
		this.converter = converter;
	}
	
	public < O extends RealType< O > > RandomAccessibleInterval< O > into( final RandomAccessibleInterval< O > target )
	{
		// Recursive copy: initializes interval iterators
		final IFunction f = this.operation.copy();
		// Set temporary computation holders
		final O scrap = target.randomAccess().get().createVariable();
		f.setScrap( scrap );
		
		final boolean compatible_iteration_order = setup( f, converter );
		
		// Check compatible iteration order and dimensions
		if ( compatible_iteration_order )
		{
			// Evaluate function for every pixel
			for ( final O output : Views.iterable( target ) )
				f.eval( output );
		}
		else
		{
			// Incompatible iteration order
			final Cursor< O > cursor = Views.iterable( target ).cursor();
			
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				f.eval( cursor.get(), cursor );
			}
		}
		
		return target;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public boolean setup( final IFunction f, final Converter< ?, ? > converter )
	{	
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// child-parent map
		final HashMap< IFunction, IFunction > cp = new HashMap<>();
		
		// Collect images to later check their iteration order
		final LinkedList< RandomAccessibleInterval< ? > > images = new LinkedList<>();
		
		// Collect Var instances to check that each corresponds to an upstream Let
		final ArrayList< Var > vars = new ArrayList<>();
		
		IFunction parent = null;
		
		// Iterate into the nested operations
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			cp.put( op, parent );
			parent = op;
			
			if ( op instanceof IterableImgSource )
			{
				final IterableImgSource iis = ( IterableImgSource )op;
				// Side effect: set the converter from input to output types
				iis.setConverter( converter );
				images.addLast( iis.rai );
			}
			else if ( op instanceof IUnaryFunction )
			{
				ops.addLast( ( ( IUnaryFunction )op ).getFirst() );
				
				if ( op instanceof IBinaryFunction )
				{
					ops.addLast( ( ( IBinaryFunction )op ).getSecond() );
				}
			}
			else if ( op instanceof Var )
			{
				final Var var = ( Var )op;
				vars.add( var );
			}
		}
		
		// Check Vars: are they all using names declared in upstream Lets
		all: for ( final Var var : vars )
		{
			parent = var;
			while ( null != ( parent = cp.get( parent ) ) )
			{
				if ( parent instanceof Let )
				{
					Let let = ( Let )parent;
					if ( let.varName != var.name )
						continue;
					// Else, found: Var is in use
					continue all;
				}
			}
			// No upstream Let found
			throw new RuntimeException( "The Var(\"" + var.name + "\") does not read from any upstream Let. " );
		}		
		
		return compatibleIterationOrder( images );
	}
	
	/**
	 * Returns true if images have the same dimensions and iterator order.
	 * Returns false when the iteration order is incompatible.
	 * 
	 * @param images
	 * @return
	 * @throws Exception When images have different dimensions.
	 */
	static public boolean compatibleIterationOrder( final LinkedList< RandomAccessibleInterval< ? > > images )
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
				throw new RuntimeException( "Images have different number of dimensions" );
			}
			
			for ( int d = 0; d < first.numDimensions(); ++d )
			{
				if ( first.realMin( d ) != other.realMin( d ) || first.realMax( d ) != other.realMax( d ) )
				{
					throw new RuntimeException( "Images have different sizes" );
				}
			}
			
			if ( ! order.equals( ( Views.iterable( other ) ).iterationOrder() ) )
			{
				// Images differ in their iteration order
				same_iteration_order = false;
			}
		}
		
		return same_iteration_order;
	}
	
	static public interface IFunction
	{
		public void eval( RealType< ? > output );
		
		public void eval( RealType< ? > output, Localizable loc );
		
		public IFunction copy();
		
		public void setScrap( RealType< ? > output );
	}
	
	static public interface IUnaryFunction extends IFunction
	{
		public IFunction getFirst();
	}
	
	static public interface IBinaryFunction extends IUnaryFunction
	{
		public IFunction getSecond();
	}
	
	static protected final class IterableImgSource< I extends RealType< I > > implements IFunction
	{
		private final RandomAccessibleInterval< I > rai;
		private final Iterator< I > it;
		private final RandomAccess< I > ra;
		private Converter< RealType< ? >, RealType< ? > > converter;

		public IterableImgSource( final RandomAccessibleInterval< I > rai )
		{
			this.rai = rai;
			this.it = Views.iterable( rai ).iterator();
			this.ra = rai.randomAccess();
		}

		@Override
		public final void eval( final RealType< ? > output ) {
			this.converter.convert( this.it.next(), output );
		}

		@Override
		public final void eval( final RealType< ? > output, final Localizable loc ) {
			this.ra.setPosition( loc );
			this.converter.convert( this.ra.get(), output );
		}

		@Override
		public IterableImgSource< I > copy()
		{
			return new IterableImgSource< I >( this.rai );
		}

		@Override
		public void setScrap( final RealType< ? > output) {}
		
		public void setConverter( final Converter< RealType< ? >, RealType< ? > > converter ) {
			this.converter = converter;
		}
	}
	
	static protected final class NumberSource implements IFunction
	{
		private final double number;
		
		public NumberSource( final Number number ) {
			this.number = number.doubleValue();
		}

		@Override
		public void eval( final RealType< ? > output ) {
			output.setReal( this.number );
		}

		@Override
		public void eval( final RealType< ? > output, final Localizable loc) {
			output.setReal( this.number );
		}

		@Override
		public NumberSource copy()
		{
			return new NumberSource( this.number );
		}

		@Override
		public void setScrap( final RealType< ? > output) {}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static final private IFunction wrap( final Object o )
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
			return ( (IFunction) o );
		}
		
		// Make it fail
		return null;
	}
	
	static abstract public class Function
	{	
		final public IFunction[] wrapMap( final Object[] obs )
		{	
			try {
				final Constructor< ? > constructor = this.getClass().getConstructor( new Class[]{ Object.class, Object.class } );
				BinaryFunction a = ( BinaryFunction )constructor.newInstance( obs[0], obs[1] );
				BinaryFunction b;

				for ( int i = 2; i < obs.length -1; ++i )
				{
					b = ( BinaryFunction )constructor.newInstance( a, obs[i] );
					a = b;
				}
				
				return new IFunction[]{ a, wrap( obs[ obs.length - 1 ] ) };
			} catch (Exception e)
			{
				throw new RuntimeException( "Error with the constructor for class " + this.getClass(), e );
			}
		}
	}
	
	
	static abstract public class UnaryFunction extends Function implements IUnaryFunction
	{
		protected final IFunction a;

		protected RealType< ? > scrap;
		
		public UnaryFunction( final Object o1 )
		{
			this.a = wrap( o1 );
		}
		
		public IFunction getFirst()
		{
			return this.a;
		}
		
		public void setScrap( final RealType< ? > output )
		{
			if ( null == output ) return; 
			this.scrap = output.copy();
			this.a.setScrap( output );
		}
	}

	static abstract public class BinaryFunction extends Function implements IBinaryFunction
	{
		protected final IFunction a, b;

		protected RealType< ? > scrap;
		
		public BinaryFunction( final Object o1, final Object o2 )
		{
			this.a = wrap( o1 );
			this.b = wrap( o2 );
		}
		
		public BinaryFunction( final Object... obs )
		{
			final IFunction[] p = this.wrapMap( obs );
			this.a = p[ 0 ];
			this.b = p[ 1 ];
		}
		
		public final IFunction getFirst()
		{
			return this.a;
		}
		
		public final IFunction getSecond()
		{
			return this.b;
		}
		
		public void setScrap( final RealType< ? > output )
		{
			if ( null == output ) return; 
			this.scrap = output.copy();
			this.a.setScrap( output );
			this.b.setScrap( output );
		}
	}
	
	static public class Mul extends BinaryFunction
	{
		public Mul( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Mul( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.mul( this.scrap );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.mul( this.scrap );
		}

		@Override
		public Mul copy() {
			final Mul f = new Mul( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Div extends BinaryFunction implements IFunction
	{

		public Div( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Div( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.div( this.scrap );
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.div( this.scrap );
		}

		@Override
		public Div copy() {
			final Div f = new Div( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Max extends BinaryFunction implements IFunction
	{

		public Max( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Max( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			if ( -1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			if ( -1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}

		@Override
		public Max copy() {
			final Max f = new Max( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Min extends BinaryFunction
	{

		public Min( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Min( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			if ( 1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			if ( 1 == output.compareTo( this.scrap ) )
				output.set( this.scrap );
		}

		@Override
		public Min copy() {
			final Min f = new Min( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Add extends BinaryFunction
	{

		public Add( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Add( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.add( this.scrap );
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc ) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.add( this.scrap );
		}

		@Override
		public Add copy() {
			final Add f = new Add( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Sub extends BinaryFunction
	{

		public Sub( final Object o1, final Object o2 )
		{
			super( o1, o2 );
		}
		
		public Sub( final Object... obs )
		{
			super( obs );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output ) {
			this.a.eval( output );
			this.b.eval( this.scrap );
			output.sub( this.scrap );
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public final void eval( final RealType output, final Localizable loc ) {
			this.a.eval( output, loc );
			this.b.eval( this.scrap, loc );
			output.sub( this.scrap );
		}

		@Override
		public Sub copy() {
			final Sub f = new Sub( this.a.copy(), this.b.copy() );
			f.setScrap( this.scrap );
			return f;
		}
	}
	
	static public class Neg extends Sub
	{
		public Neg( final Object o )
		{
			super( 0, o );
		}
	}
	
	static public final class Let implements IFunction, IBinaryFunction
	{
		private final String varName;
		private final IFunction varValue;
		private final IFunction body;
		private RealType< ? > scrap;
		
		public Let( final String varName, final Object varValue, final Object body )
		{
			this.varName = varName;
			this.varValue = wrap( varValue );
			this.body = wrap( body );
		}
		
		public Let( final Object[] pairs, final Object body )
		{
			if ( pairs.length < 2 || 0 != pairs.length % 2 )
				throw new RuntimeException( "Let: need an even number of var-value pairs." );
			
			this.varName = ( String )pairs[0];
			this.varValue = wrap( pairs[1] );
			
			if ( 2 == pairs.length )
			{
				this.body = wrap( body );
			} else
			{
				final Object[] pairs2 = new Object[ pairs.length - 2 ];
				System.arraycopy( pairs, 2, pairs2, 0, pairs2.length );
				this.body = new Let( pairs2, body );
			}
		}
		
		public Let( final Object... obs )
		{
			this( fixAndValidate( obs ), obs[ obs.length - 1] );
		}
		
		static private final Object[] fixAndValidate( final Object[] obs )
		{
			if ( obs.length < 3 || 0 == obs.length % 2 )
				throw new RuntimeException( "Let: need an even number of var-value pairs plus the body at the end." );
			final Object[] obs2 = new Object[ obs.length - 1];
			System.arraycopy( obs, 0, obs2, 0, obs2.length );
			return obs2;
		}
		
		/**
		 * Recursive search for Var instances of this.varName
		 * 
		 * @param o
		 */
		private final void setupVars( final IFunction o, final boolean[] used )
		{
			if ( o instanceof IUnaryFunction )
			{
				final IUnaryFunction uf = ( IUnaryFunction )o;
				
				if ( uf.getFirst() instanceof Var )
				{
					final Var var = ( Var )uf.getFirst();
					if ( var.name == this.varName )
					{
						var.setScrap( this.scrap );
						used[0] = true;
					}
				} else
				{
					setupVars( uf.getFirst(), used );
				}
				
				if ( o instanceof IBinaryFunction )
				{
					final IBinaryFunction bf = ( IBinaryFunction )o;
					
					if ( bf.getSecond() instanceof Var )
					{
						final Var var = ( Var )bf.getSecond();
						if ( var.name == this.varName )
						{
							var.setScrap( this.scrap );
							used[0] = true;
						}
					} else
					{
						setupVars( bf.getSecond(), used );
					}
				}
			}
		}
		

		@Override
		public void eval( final RealType< ? > output ) {
			// Evaluate the varValue into this.scrap, which is shared with all Vars of varName
			this.varValue.eval( this.scrap );
			// The body may contain Vars that will use this.varValue via this.scrap
			this.body.eval( output );
		}

		@Override
		public void eval( final RealType< ? > output, final Localizable loc) {
			this.varValue.eval( this.scrap, loc );
			this.body.eval( output, loc );
		}

		@Override
		public Let copy() {
			final Let copy = new Let( this.varName, this.varValue.copy(), this.body.copy() );
			copy.setScrap( this.scrap );
			return copy;
		}

		@Override
		public void setScrap( final RealType< ? > output ) {
			if ( null == output ) return;
			this.scrap = output.copy();
			this.varValue.setScrap( output );
			this.body.setScrap( output );
			
			// Setup Var instances that read this varName's value
			// and ensure that it is read at least once
			final boolean[] used = new boolean[]{ false };
			setupVars( this.body, used );
			if ( ! used[0] )
				throw new RuntimeException( "Let-declared variable \"" + this.varName + "\" is never read by a Var(\"" + this.varName + "\")." );
		}

		@Override
		public IFunction getFirst() {
			return this.varValue;
		}

		@Override
		public IFunction getSecond() {
			return this.body;
		}
	}

	static public final class Var implements IFunction
	{
		private final String name;
		private RealType< ? > scrap;

		public Var( final String name ) {
			this.name = name;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void eval( final RealType output ) {
			output.set( this.scrap );
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void eval( final RealType output, final Localizable loc) {
			output.set( this.scrap );
		}

		@Override
		public Var copy() {
			return new Var( this.name );
		}

		@Override
		public void setScrap( final RealType< ? > output ) {
			this.scrap = output;
		}
	}
	
	/*
	static public interface BooleanFunction< O extends RealType< O > >
	{
		public boolean eval( final O output );
		
		public boolean eval( final O output, final Localizable loc );
	}
	
	static public class Equals< O extends RealType O > extends BooleanFunction< O >
	{
		
		
		... TODO  Equals, LessThan, GreaterThan
	}
	*/
}
