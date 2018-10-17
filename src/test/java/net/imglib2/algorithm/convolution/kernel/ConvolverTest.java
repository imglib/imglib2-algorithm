package net.imglib2.algorithm.convolution.kernel;

import static org.junit.Assert.assertArrayEquals;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;

/**
 * @author Tobias Pietzsch
 */
public class ConvolverTest
{
	private final double[] kernel = { 1.0, 2.0, 3.0, 4.0 };
	private final int center = 2;

	private final double[] in = { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };

	@Test
	public void testConvolverNativeType()
	{
		final double[] out = new double[ kernel.length ];
		final ConvolverNativeType< DoubleType > convolver = new ConvolverNativeType<>(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}

	@Test
	public void testConvolverNumericType()
	{
		final double[] out = new double[ kernel.length ];
		final ConvolverNumericType< DoubleType > convolver = new ConvolverNumericType<>(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}

	@Test
	public void testDoubleConvolverRealType()
	{
		final double[] out = new double[ kernel.length ];
		final DoubleConvolverRealType convolver = new DoubleConvolverRealType(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}

	@Test
	public void testFloatConvolverRealType()
	{
		final double[] out = new double[ kernel.length ];
		final FloatConvolverRealType convolver = new FloatConvolverRealType(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}
}
