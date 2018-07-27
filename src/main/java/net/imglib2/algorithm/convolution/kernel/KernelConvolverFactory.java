package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.LineConvolverFactory;
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
 * Used by {@link SeparableKernelConvolution} as {@link LineConvolverFactory}
 * for convolution. {@link ClassCopyProvider} is used to create copies
 * of the byte code of the actual convolvers. This improves their performance,
 * as the JIT can optimize the byte code to the individual use cases.
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

	public KernelConvolverFactory( Kernel1D kernel )
	{
		this.kernel = kernel;
	}

	@Override public long getBorderBefore()
	{
		return +kernel.max();
	}

	@Override public long getBorderAfter()
	{
		return -kernel.min();
	}

	@Override public Runnable getConvolver( RandomAccess< ? extends NumericType< ? > > in, RandomAccess< ? extends NumericType< ? > > out, int d, long lineLength )
	{
		NumericType< ? > targetType = out.get();
		NumericType< ? > sourceType = in.get();
		ClassCopyProvider< Runnable > provider = getProvider( sourceType, targetType );
		List< Class< ? > > key = Arrays.asList( in.getClass(), out.getClass(), sourceType.getClass(), targetType.getClass() );
		return provider.newInstanceForKey( key, kernel, in, out, d, lineLength );
	}

	private ClassCopyProvider< Runnable > getProvider( NumericType< ? > sourceType, NumericType< ? > targetType )
	{
		for ( Entry entry : factories )
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
			new Entry( ConvolverNumericType.class, null, NumericType.class )
	);

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
			if ( !targetClass.isInstance( targetType ) )
				return false;
			Class< ? > sourceClass = this.sourceClass == null ?
					targetType.getClass() : this.sourceClass;
			return sourceClass.isInstance( sourceType );
		}
	}
}
