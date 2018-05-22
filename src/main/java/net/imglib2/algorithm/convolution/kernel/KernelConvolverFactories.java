package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Arrays;
import java.util.List;

/**
 * Used by {@link SeparableKernelConvolution} to create the {@link ConvolverFactory}
 * for convolution. {@link ClassCopyProvider} is used to create copies
 * of the byte code of the actual convolvers. This improves their performance,
 * as the JIT can optimize the byte code to the individual use cases.
 *
 * @author Matthias Arzt
 */
public class KernelConvolverFactories
{

	private static final List< Entry > factories = Arrays.asList(
			new Entry( DoubleConvolverRealType.class, RealType.class, DoubleType.class ),
			new Entry( FloatConvolverRealType.class, RealType.class, RealType.class ),
			new Entry( ConvolverNativeType.class, null, NativeType.class ),
			new Entry( ConvolverNumericType.class, null, NumericType.class )
	);

	/**
	 * Returns a {@link ConvolverFactory} that can be used to convolve an image.
	 *
	 * @param sourceType supported type of the input {@link RandomAccess}
	 * @param targetType supported type of the output {@link RandomAccess}
	 */
	public static < T extends NumericType< ? > > ConvolverFactory< T, T > get( Kernel1D kernel, Object sourceType, T targetType )
	{
		for ( Entry factory : factories )
			if ( factory.supported( sourceType, targetType ) )
				return factory.get( kernel, targetType );
		throw new IllegalArgumentException( "Target type not supported" );
	}

	private static class Entry
	{

		private final ClassCopyProvider< Runnable > provider;

		private final Class< ? extends Type > sourceClass;

		private final Class< ? extends Type > targetClass;

		private Entry( Class< ? extends Runnable > convolverClass, Class< ? extends Type > sourceClass, Class< ? extends Type > targetClass )
		{
			this.provider = new ClassCopyProvider<>( convolverClass, Runnable.class );
			this.sourceClass = sourceClass;
			this.targetClass = targetClass;
		}

		private boolean supported( Object sourceType, Type< ? > targetType )
		{
			if ( targetClass.isInstance( targetType ) )
			{
				boolean compatible = sourceClass == null ?
						targetType.getClass().isInstance( targetType ) :
						sourceClass.isInstance( sourceType );
				if ( !compatible )
					throw new IncompatibleTypeException( sourceType, targetType.getClass().getCanonicalName() + " source required for convolving into a " + sourceClass.getCanonicalName() + " target" );
				return true;
			}
			return false;
		}

		public < T > ConvolverFactory< T, T > get( Kernel1D kernel, T targetType )
		{
			return new MyConvolverFactory(provider, kernel, targetType);
		}
	}

	private static class MyConvolverFactory<T> implements ConvolverFactory<T, T> {

		private final ClassCopyProvider<Runnable> provider;
		private final Kernel1D kernel;
		private final T targetType;

		private MyConvolverFactory( ClassCopyProvider< Runnable > provider, Kernel1D kernel, T targetType )
		{
			this.provider = provider;
			this.kernel = kernel;
			this.targetType = targetType;
		}

		@Override public long getBorderBefore()
		{
			return + kernel.max();
		}

		@Override public long getBorderAfter()
		{
			return - kernel.min();
		}

		@Override public Runnable getConvolver( RandomAccess< ? extends T > in, RandomAccess< ? extends T > out, int d, long lineLength )
		{
			List< Class< ? > > key = Arrays.asList( in.getClass(), out.getClass(), in.get().getClass(), out.get().getClass() );
			return provider.newInstanceForKey( key, kernel, in, out, d, lineLength, targetType );
		}
	}
}
