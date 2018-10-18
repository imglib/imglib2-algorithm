package net.imglib2.algorithm.convolution.kernel;

import static org.junit.Assert.assertArrayEquals;

import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;

/**
 * Tests {@link ConvolverNativeType}, {@link ConvolverNumericType},
 * {@link DoubleConvolverRealType} and {@link FloatConvolverRealType}.
 *
 * @author Tobias Pietzsch
 */
public class ConvolverTest
{
	@Test
	public void testConvolverNativeType()
	{
		testConvolver( ConvolverNativeType::new );
	}

	@Test
	public void testConvolverNumericType()
	{
		testConvolver( ConvolverNumericType::new );
	}

	@Test
	public void testDoubleConvolverRealType()
	{
		testConvolver( DoubleConvolverRealType::new );
	}

	@Test
	public void testFloatConvolverRealType()
	{
		testConvolver( FloatConvolverRealType::new );
	}

	private void testConvolver( ConvolverConstructor< DoubleType > constructor )
	{
		final double[] kernel = { 1.0, 2.0, 3.0, 4.0 };
		final int center = 2;
		final double[] in = { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };
		final double[] out = new double[ kernel.length ];
		final Runnable convolver = constructor.create(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}

	private interface ConvolverConstructor< T >
	{
		Runnable create( final Kernel1D kernel, final RandomAccess< ? extends T > in, final RandomAccess< ? extends T > out, final int d, final long lineLength );
	}
}
