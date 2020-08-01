package net.imglib2.algorithm.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.AbstractWrappedPositionableLocalizable;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IBinaryFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.ITrinaryFunction;
import net.imglib2.algorithm.math.abstractions.IUnaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.algorithm.math.execution.FunctionCursor;
import net.imglib2.algorithm.math.execution.FunctionCursorDouble;
import net.imglib2.algorithm.math.execution.FunctionCursorDoubleIncompatibleOrder;
import net.imglib2.algorithm.math.execution.FunctionCursorIncompatibleOrder;
import net.imglib2.algorithm.math.execution.FunctionRandomAccess;
import net.imglib2.algorithm.math.execution.FunctionRandomAccessDouble;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Compute
{
	private final IFunction operation;
	private final Parameters params;
		
	/**
	 * Validate the {code operation}.
	 * 
	 * @param operation
	 */
	public Compute( final IFunction operation )
	{
		this.operation = operation;
		
		// Throw RuntimeException as needed to indicate incorrect construction
		this.params = Compute.validate( this.operation );
	}
	
	public < O extends RealType< O > & NativeType< O > > RandomAccessibleInterval< O >
	intoArrayImg()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > rai = ( RandomAccessibleInterval< O > )Util.findImg( operation ).iterator().next();
		final ArrayImg< O, ? > target = new ArrayImgFactory< O >( rai.randomAccess().get().createVariable() ).create( rai );
		this.params.compatible_iteration_order = Util.compatibleIterationOrder( Arrays.asList( rai, target ) );
		return this.into( target );
	}
	
	public < O extends RealType< O > & NativeType< O >, C  extends RealType< C > & NativeType< C > > RandomAccessibleInterval< O >
	intoArrayImg( final C computeType, final O outputType )
	{
		final RandomAccessibleInterval< ? > rai = Util.findImg( operation ).iterator().next();
		final ArrayImg< O, ? > target = new ArrayImgFactory< O >( outputType ).create( rai );
		this.params.compatible_iteration_order = Util.compatibleIterationOrder( Arrays.asList( rai, target ) );
		return this.into( new ArrayImgFactory< O >( outputType ).create( rai ), null, computeType, null );
	}
	
	public < O extends RealType< O > & NativeType< O >, C  extends RealType< C > > RandomAccessibleInterval< O >
	intoArrayImg( final O outputType )
	{
		return intoArrayImg( outputType.createVariable(), outputType );
	}
	
	/**
	 * Execute the computation and store the result into a newly created {@code Img} of the same {@code Type}
	 * (and kind, by {@code ImgFactory}) as one of the input images, using as the computation {@code Type}
	 * the type of that picked input image.
	 * 
	 * This approach is appropriate when e.g. all input images are of the same type, and operations aren't
	 * expected to overflow the {@code Type} or it doesn't matter whether they do.
	 * 
	 * @return A newly created {@code Img} with the result of the computation.
	 */
	@SuppressWarnings("unchecked")
	public < O extends RealType< O > & NativeType< O > > RandomAccessibleInterval< O >
	intoImg()
	{
		return intoImg( ( ( RandomAccessibleInterval< O > )Util.findImg( operation ).iterator().next() ).randomAccess().get().createVariable() );
	}
	
	/**
	 * Execute the computation and store the result into a newly created {@code Img} of the same kind,
	 * by {@code ImgFactory}, as one of the input images, but of type {@code outputType}, which is also
	 * used as the computation {@code Type}.
	 * 
	 * @param outputType The {@code Type} of the returned {@code Img} and used for computing.
	 * 
	 * @return A newly created {@code Img} with the result of the computation.
	 */
	public < O extends RealType< O > & NativeType< O > > RandomAccessibleInterval< O >
	intoImg( O outputType )
	{
		return intoImg( outputType.createVariable(), outputType );
	}
	
	/**
	 * Execute the computation and store the result into a newly created {@code Img} of the same kind,
	 * by {@code ImgFactory}, as one of the input images, but of type {@code outputType}, while using
	 * {@code computeType} for math operations, converting to the {@code outputType} to store the result.
	 * 
	 * @param computeType The {@code Type} used to perform mathematical operations.
	 * 
	 * @param outputType The {@code Type} of the returned {@code Img}.
	 * 
	 * @return A newly created {@code Img} with the result of the computation.
	 */
	public < O extends RealType< O > & NativeType< O >, C extends RealType< C > > RandomAccessibleInterval< O >
	intoImg( final C computeType, O outputType )
	{
		for ( final RandomAccessibleInterval< ? > rai : Util.findImg( operation ) )
		{
			if ( rai instanceof Img )
			{
				final Img< O > target = ( ( Img< ? > )rai ).factory().imgFactory( outputType ).create( rai ); // of compatible iteration order by definition
				return into( target, null, computeType, null );
			}
		}
		return intoArrayImg();
	}

	/**
	 * Execute the computation and store the result into the {@code target}.
	 * The computation is done using {@code Type}-based math, with the {@code Type}
	 * of the {@code target} defining the specific math implementation and numerical
	 * precision that will be used.
	 * 
	 * @param target The {@code RandomAccessibleInterval} into which to store the computation;
	 *               note its {@code Type} determines the precision of the computation and the specific
	 *               implementation of the mathematical operations.
	 * @return The {@code target}.
	 */
	public < O extends RealType< O > > RandomAccessibleInterval< O > into( final RandomAccessibleInterval< O > target )
	{
		return this.into( target, null );
	}
	
	/**
	 * Execute the mathematical operations and store the result into the given {@code RandomAccessibleInterval}.
	 * 
	 * @param target The {@code RandomAccessibleInterval} into which to store the computation;
	 *               note its {@code Type} determines the precision of the computation and the specific
	 *               implementation of the mathematical operations.
	 * 
	 * @param inConverter The {@code Converter} that transfers all input {@code Type} to the {@code Type}
	 *                  of the {@code target}; when null, will create one that uses double floating-point
	 *                  precision; but note that if the {@code Type} of an input {@code RandomAccessibleInterval}
	 *                  is the same as that of the {@code target}, the converter will not be used.
	 * 
	 * @return The {@code target}.
	 */
	public < O extends RealType< O > > RandomAccessibleInterval< O > into(
			final RandomAccessibleInterval< O > target,
			Converter< RealType< ? >, O > inConverter
			)
	{
		return into( target, inConverter, target.randomAccess().get().createVariable(), null );
	}

	/**
	 * Execute the mathematical operations and store the result into the given {@code RandomAccessibleInterval}.
	 * Takes into account whether all images involved in the computation are iterable in a compatible way.
	 * 
	 * @param target The {@code RandomAccessibleInterval} into which to store the computation.
	 * 
	 * @param inConverter The {@code Converter} that transfers all input {@code Type} to the {@code Type}
	 *                  of the {@code target}; when null, will create one that uses double floating-point
	 *                  precision; but note that if the {@code Type} of an input {@code RandomAccessibleInterval}
	 *                  is the same as that of the {@code target}, the converter will not be used.
	 * 
	 * @param computingType The {@code Type} that determines the precision of the computation and the specific
	 *                 implementation of the mathematical operations.
	 * 
	 * @param outConverter The {@code Converter} that transfers the {@code computingType} to the {@code Type}
	 *                 of the {@code target}; when null, a new one is created, which is the identity when the
	 *                 {@code computingType} equal the {@code Type} of the {@code target}, and a generic
	 *                 {@code RealType} converter that uses floating-point values (with {@code RealType#setReal(double)})
	 *                 created with {@code Util#genericRealTypeConverter()} is used.
	 * 
	 * @return The {@code target}.
	 */
	@SuppressWarnings("unchecked")
	public < O extends RealType< O >, C extends RealType< C > > RandomAccessibleInterval< O > into(
			final RandomAccessibleInterval< O > target,
			Converter< RealType< ? >, C > inConverter,
			final C computingType,
			Converter< C, O > outConverter
			)
	{
		if ( null == inConverter )
			inConverter = Util.genericRealTypeConverter();
		
		if ( null == outConverter )
		{
			final boolean are_same_type = computingType.getClass() == target.randomAccess().get().createVariable().getClass();
			if ( are_same_type )
			{
				outConverter = ( Converter< C, O > )new Converter< C, C >() {
					@Override
					public final void convert( final C comp, final C out) {
						out.set( comp );
					}
				};
			}
			else
			{
				outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
			}
		}

		// Recursive copy: initializes interval iterators and sets temporary computation holder
		final OFunction< C > f = this.operation.reInit(
				computingType,
				new HashMap< String, LetBinding< C > >(),
				inConverter, null );
		
		// Check compatible iteration order and dimensions
		if ( this.params.compatible_iteration_order && !this.params.contains_RandomAccessOnly )
		{
				// Evaluate function for every pixel
				for ( final O output : Views.iterable( target ) )
					outConverter.convert( f.eval(), output );
		}
		else
		{
			// Incompatible iteration order
			final Cursor< O > cursor = Views.iterable( target ).cursor();
			
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				outConverter.convert( f.eval( cursor ), cursor.get() );
			}
		}
		
		return target;
	}
	
	/** 
	 * View the result of the computations as a {@code RandomAccessibleInterval}, i.e. there is no target image,
	 * instead any pixel can be viewed as the result of the computation applied to it, dynamically.
	 * 
	 * Conversion between the input and computing type is done with a {@code Util#genericRealTypeConverter()},
	 * as is the conversion from computing to output type.
	 * 
	 * See also {@code ViewableFunction} and methods below related to {@code Compute#randomAccess()} and {@code Compute#cursor()}.
	 * The key difference is that, here, the computing and the output type can be different.
	 * 
	 * @param computingType The {@code Type} that defines the math to use.
	 * @param outputType The @{code Type} of the constructed and returned {@code RandomAccessibleInterval}.
	 * 
	 * return A {@code RandomAccessibleInterval} view of the result of the computations.
	 */
	public < O extends RealType< O >, C extends RealType< C > > RandomAccessibleInterval< O > view(
			final C computingType,
			final O outputType)
	{
		return view( null, computingType, outputType, null);
	}
	
	/** 
	 * View the result of the computations as a {@code RandomAccessibleInterval}, i.e. there is no target image,
	 * instead any pixel can be viewed as the result of the computation applied to it, dynamically.
	 * 
	 * See also {@code ViewableFunction} and methods below related to {@code Compute#randomAccess()} and {@code Compute#cursor()}.
	 * The key difference is that, here, the computing and the output type can be different.
	 * 
	 * @param inConverter_ To convert input images into the {@code computingType}. Can be null, defaults to a {@code Util#genericRealTypeConverter()}.
	 * @param computingType The {@code Type} that defines the math to use.
	 * @param outputType The @{code Type} of the constructed and returned {@code RandomAccessibleInterval}.
	 * @param outConverter_ To convert from the {@code computingType} to the {@code outputType}. Can be null, defaults to a {@code Util#genericRealTypeConverter()}.
	 * 
	 * return A {@code RandomAccessibleInterval} view of the result of the computations.
	 */
	@SuppressWarnings("unchecked")
	public < O extends RealType< O >, C extends RealType< C > > RandomAccessibleInterval< O > view(
			final Converter< RealType< ? >, C > inConverter_,
			final C computingType,
			final O outputType,
			final Converter< C, O > outConverter_)
	{
		final Converter< RealType< ? >, C > inConverter = null != inConverter_ ? inConverter_ : Util.genericRealTypeConverter();
		
		final Converter< C, O > outConverter;
		if ( null == outConverter_ )
		{
			final boolean are_same_type = computingType.getClass() == outputType.getClass();
			if ( are_same_type )
			{
				outConverter = ( Converter< C, O > )new Converter< C, C >() {
					@Override
					public final void convert( final C comp, final C out) {
						out.set( comp );
					}
				};
			}
			else
			{
				outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
			}
		}
		else
			outConverter = outConverter_;
		
		class RandomAccessCompute< PL extends Positionable & Localizable > extends AbstractWrappedPositionableLocalizable< PL > implements RandomAccess< O >
		{
			private final O result;
			private final OFunction< C > f;
			private final RandomAccessibleInterval< O > sourceInterval;

			public RandomAccessCompute( final O outputType, final RandomAccessibleInterval< O > sourceInterval ) {
				super( ( PL )sourceInterval.randomAccess() );
				this.result = outputType.createVariable();
				this.sourceInterval = sourceInterval;
				// Recursive copy: initializes interval iterators and sets temporary computation holder
				this.f = Compute.this.operation.reInit(
						computingType,
						new HashMap< String, LetBinding< C > >(),
						inConverter, null );
			}

			@Override
			public final O get() {
				outConverter.convert( f.eval( this ), result );
				return result;
			}

			@Override
			public Sampler< O > copy() {
				return new RandomAccessCompute< PL >( this.result, this.sourceInterval );
			}

			@Override
			public RandomAccess< O > copyRandomAccess() {
				return new RandomAccessCompute< PL >( this.result, this.sourceInterval );
			}
		}
				
		class RandomAccessIntervalCompute extends AbstractWrappedInterval< RandomAccessibleInterval< O > > implements RandomAccessibleInterval< O >
		{
			public RandomAccessIntervalCompute()
			{
				super( ( RandomAccessibleInterval< O > )Views.zeroMin( Util.findImg( operation ).iterator().next() ) );
			}

			@Override
			public RandomAccess< O > randomAccess()
			{
				return randomAccess( this.sourceInterval );
			}

			@SuppressWarnings("rawtypes")
			@Override
			public RandomAccess< O > randomAccess( final Interval interval )
			{	
				return new RandomAccessCompute( outputType.createVariable(), this.sourceInterval ); // Generic type inescrutable
			}
		}
		
		return new RandomAccessIntervalCompute();
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the same {@code Type} as the first image found,
	 * with mathematical operations using that same {@code Type}.
	 */
	public < O extends RealType< O > & NativeType< O > > RandomAccessibleInterval< O >
	parallelIntoArrayImg()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > first = ( RandomAccessibleInterval< O > )Util.findImg( operation ).iterator().next();
		final O outputType =  first.randomAccess().get().createVariable();
		final ArrayImg< O, ? > target = new ArrayImgFactory< O >( outputType ).create( first );
		return parallelInto( null, outputType.createVariable(), outputType, null, target );
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the given {@code Type}.
	 * 
	 * @param outputType The {@code Type} used for both computations and the output image.
	 */
	public < O extends RealType< O > & NativeType< O > > RandomAccessibleInterval< O >
	parallelIntoArrayImg( final O outputType )
	{
		return parallelIntoArrayImg( null, outputType.createVariable(), outputType, null );
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the given {@code Type}.
	 * Uses generic {@code RealType} converters where appropriate as provided by {@code Util#genericRealTypeConverter()}.
	 * 
	 * @param computeType The {@code Type} used for computations.
	 * @param outputType The {@code Type} used for the output image.
	 */
	public < O extends RealType< O > & NativeType< O >, C extends RealType< C > > RandomAccessibleInterval< O >
	parallelIntoArrayImg(
			final C computeType,
			final O outputType
			)
	{
		return parallelIntoArrayImg( null, computeType, outputType, null );
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the given {@code Type}.
	 * 
	 * @param inConverter From input to {@code computeType}, or when null, uses {@code Util#genericRealTypeConverter()}.
	 * @param computeType The {@code Type} used for computations.
	 * @param outputType The {@code Type} used for the output image.
	 * @param outConverter From {@code computeType} to {@code outputType}, or when null, uses {@code Util#genericRealTypeConverter()}.
	 */
	public < O extends RealType< O > & NativeType< O >, C extends RealType< C > > RandomAccessibleInterval< O >
	parallelIntoArrayImg(
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter
			)
	{
		final ArrayImg< O, ? > target = new ArrayImgFactory< O >( outputType ).create( Util.findImg( operation ).iterator().next() );
		return parallelInto( inConverter, computeType, outputType, outConverter, target );
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the given {@code Type}.
	 * Uses generic {@code RealType} converters where appropriate as provided by {@code Util#genericRealTypeConverter()}.
	 *
	 * @param target To store the result of the computation, with its {@code Type} defining both the output {@code Type} and the {@code Type} used for computations.
	 */
	public < O extends RealType< O > > RandomAccessibleInterval< O >
	parallelInto(
			final RandomAccessibleInterval< O > target
			)
	{
		final O type = net.imglib2.util.Util.getTypeFromInterval( target );
		return parallelInto( null, type.createVariable(), type, null, target );
	}
	
	/**
	 * Compute the result multithreaded into an {@code ArrayImg} of the given {@code Type}.
	 * 
	 * @param inConverter From input to {@code computeType}, or when null, uses {@code Util#genericRealTypeConverter()}.
	 * @param computeType The {@code Type} used for computations.
	 * @param outputType The {@code Type} used for the output image.
	 * @param outConverter From {@code computeType} to {@code outputType}, or when null, uses {@code Util#genericRealTypeConverter()}.
	 */
	public < O extends RealType< O >, C extends RealType< C > > RandomAccessibleInterval< O >
	parallelInto(
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter,
			final RandomAccessibleInterval< O > target
			)
	{
		final RandomAccessibleInterval< O > source = view( inConverter, computeType, outputType, outConverter );
		LoopBuilder.setImages( source, target ).forEachPixel( O::set );
		return target;
	}
	
	static public class Parameters {
		public boolean compatible_iteration_order;
		public List< RandomAccessibleInterval< ? > > images;
		public boolean contains_RandomAccessOnly;
	}
	
	static public Parameters validate( final IFunction f )
	{
		final Parameters p = new Parameters();
		
		final LinkedList< IFunction > ops = new LinkedList<>();
		ops.add( f );
		
		// child-parent map
		final HashMap< IFunction, IFunction > cp = new HashMap<>();
		cp.put( f, null );
		
		// Collect images to later check their iteration order
		final LinkedList< RandomAccessibleInterval< ? > > images = new LinkedList<>();
		
		// Collect Var instances to check that each corresponds to an upstream Let
		final ArrayList< Var > vars = new ArrayList<>();
		
		// Collect Let instances to check that their declared variables are used
		final HashSet< Let > lets = new HashSet<>();
		
		// Iterate into the nested operations, depth-first
		while ( ! ops.isEmpty() )
		{
			final IFunction  op = ops.removeFirst();
			
			if ( op instanceof ImgSource )
			{
				images.addFirst( ( ( ImgSource< ? > )op ).getRandomAccessibleInterval() );
			}
			else if ( op instanceof IUnaryFunction )
			{
				final IFunction first = ( ( IUnaryFunction )op ).getFirst();
				ops.addFirst( first );
				cp.put( first, op );
				
				if ( op instanceof IBinaryFunction )
				{
					final IFunction second = ( ( IBinaryFunction )op ).getSecond();
					ops.add( 1, second );
					cp.put( second, op );
					
					if ( op instanceof Let )
					{
						lets.add( ( Let )op );
					}
					
					if ( op instanceof ITrinaryFunction )
					{
						final IFunction third = ( ( ITrinaryFunction )op ).getThird();
						ops.add( 2, third );
						cp.put( third, op );
					}
				}
			}
			else if ( op instanceof Var )
			{
				final Var var = ( Var )op;
				vars.add( var );
			}
			else if ( op instanceof RandomAccessOnly )
			{
				p.contains_RandomAccessOnly = true;
			}
		}
		
		// Check Vars: are they all using names declared in upstream Lets
		final HashSet< Let > used = new HashSet<>();
		all: for ( final Var var : vars )
		{
			IFunction parent = var;
			while ( null != ( parent = cp.get( parent ) ) )
			{
				if ( parent instanceof Let )
				{
					final Let let = ( Let )parent;
					if ( let.getVarName() != var.getName() )
						continue;
					// Else, found: Var is in use
					used.add( let ); // might already be in used
					continue all;
				}
			}
			// No upstream Let found
			throw new RuntimeException( "The Var(\"" + var.getName() + "\") does not read from any upstream Let. " );
		}
		
		// Check Lets: are their declared variables used in downstream Vars?
		if ( lets.size() != used.size() )
		{
			lets.removeAll( used );
			String msg = "The Let-declared variable" + ( 1 == lets.size() ? "" : "s" );
			for ( final Let let : lets )
				msg += " \"" + let.getVarName() + "\"";
			msg += " " + ( 1 == lets.size() ? "is" : "are") + " not used by any downstream Var.";
			throw new RuntimeException( msg );
		}
		
		// Check ImgSource: if they are downstream of an If statement, they should be declared in a Let before that
		// TODO
		
		p.images = images;
		p.compatible_iteration_order = Util.compatibleIterationOrder( images );
		
		return p;
	}
	
	public < O extends RealType< O > > RandomAccess< O > randomAccess( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		return new FunctionRandomAccess< O >( this.operation, outputType, converter );
	}
	
	public < O extends RealType< O > > RandomAccess< O > randomAccess( final O outputType )
	{
		return new FunctionRandomAccess< O >( this.operation, outputType, Util.genericRealTypeConverter() );
	}
	
	/** Returns a {@link RandomAccess} with the same type as the first input image found. */
	public < O extends RealType< O > > RandomAccess< O > randomAccess()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > img = ( RandomAccessibleInterval< O > )Util.findFirstImg( operation );
		final O outputType = img.randomAccess().get().createVariable();
		return new FunctionRandomAccess< O >( this.operation, outputType, Util.genericRealTypeConverter() );
	}
	
	public < O extends RealType< O > > Cursor< O > cursor( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		if ( this.params.compatible_iteration_order )
			return new FunctionCursor< O >( this.operation, outputType, converter );
		return new FunctionCursorIncompatibleOrder< O >( this.operation, outputType, converter );
	}
	
	public < O extends RealType< O > > Cursor< O > cursor( final O outputType )
	{
		return this.cursor( outputType, Util.genericRealTypeConverter() );
	}
	
	/** Returns a {@link Cursor} with the same type as the first input image found. */
	public < O extends RealType< O > > Cursor< O > cursor()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > img = ( RandomAccessibleInterval< O > )Util.findFirstImg( operation );
		return this.cursor( img.randomAccess().get().createVariable() );
	}
	
	public < O extends RealType< O > > RandomAccess< O > randomAccessDouble( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		return new FunctionRandomAccessDouble< O >( this.operation, outputType, converter );
	}
	
	public < O extends RealType< O > > RandomAccess< O > randomAccessDouble( final O outputType )
	{
		return new FunctionRandomAccessDouble< O >( this.operation, outputType, Util.genericRealTypeConverter() );
	}
	
	/** Returns a {@link RandomAccess} with the same type as the first input image found. */
	public < O extends RealType< O > > RandomAccess< O > randomAccessDouble()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > img = ( RandomAccessibleInterval< O > )Util.findFirstImg( operation );
		final O outputType = img.randomAccess().get().createVariable();
		return new FunctionRandomAccessDouble< O >( this.operation, outputType, Util.genericRealTypeConverter() );
	}
	
	public < O extends RealType< O > > Cursor< O > cursorDouble( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		if ( this.params.compatible_iteration_order && !this.params.contains_RandomAccessOnly )
			return new FunctionCursorDouble< O >( this.operation, outputType, converter );
		return new FunctionCursorDoubleIncompatibleOrder< O >( this.operation, outputType, converter );
	}
	
	public < O extends RealType< O > > Cursor< O > cursorDouble( final O outputType )
	{
		return this.cursorDouble( outputType, Util.genericRealTypeConverter() );
	}
	
	/** Returns a {@link Cursor} with the same type as the first input image found. */
	public < O extends RealType< O > > Cursor< O > cursorDouble()
	{
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval< O > img = ( RandomAccessibleInterval< O > )Util.findFirstImg( operation );
		return this.cursorDouble( img.randomAccess().get().createVariable() );
	}
}
