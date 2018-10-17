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
