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
	public void testDoubleConvolver()
	{
		testConvolver( DoubleConvolverRealType.class );
	}

	@Test
	public void testFloatConvolver()
	{
		testConvolver( FloatConvolverRealType.class );
	}

	@Test
	public void testNativeConvolver()
	{
		testConvolver( ConvolverNativeType.class );
	}

	@Test
	public void testNumericConvolver()
	{
		testConvolver( ConvolverNumericType.class );
	}

	public void testConvolver( Class< ? extends Runnable > expectedConvolverClass )
	{
		try
		{
			double[] halfKernel = { 3, 2, 1 };
			double[] in = { 5, 0, 1, 0, 0, 0, 0, 0, 0, 0 };
			double[] expected = { 8, 2, 1, 0, 0, 0 };
			int lineLength = in.length - 2 * ( halfKernel.length - 1 );
			double[] actual = new double[ lineLength ];

			Img< DoubleType > inImg = ArrayImgs.doubles( in, in.length );
			Img< DoubleType > outImg = ArrayImgs.doubles( actual, actual.length );
			Runnable convolver = expectedConvolverClass
					.getConstructor( Kernel1D.class, RandomAccess.class, RandomAccess.class, int.class, long.class )
					.newInstance( Kernel1D.symmetric( halfKernel ), inImg.randomAccess(), outImg.randomAccess(), 0, lineLength );
			convolver.run();
			assertArrayEquals( expected, actual, 0.0001 );
		}
		catch ( NoSuchMethodException e )
		{
			fail( "The class " + expectedConvolverClass.getSimpleName() + " misses the constructor needed for KernelConvolverFactory." );
		}
		catch ( IllegalAccessException | InstantiationException | InvocationTargetException e )
		{
			fail( "The constructor of class " + expectedConvolverClass.getSimpleName() + " must be public for class copying." );
		}
	}

	@Test
	public void test()
	{
		testFactoryTypeMatching( DoubleConvolverRealType.class, ArrayImgs.doubles( 1 ) );
		testFactoryTypeMatching( FloatConvolverRealType.class, ArrayImgs.bytes( 1 ) );
		testFactoryTypeMatching( ConvolverNativeType.class, ArrayImgs.argbs( 1 ) );
		ListImg< ? extends NumericType< ? > > image = createImageOfNumericType();
		testFactoryTypeMatching( ConvolverNumericType.class, image );
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
