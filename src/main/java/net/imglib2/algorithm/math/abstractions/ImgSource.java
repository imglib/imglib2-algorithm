package net.imglib2.algorithm.math.abstractions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public interface ImgSource< I extends RealType< I > > extends IFunction
{
	public RandomAccessibleInterval< I > getRandomAccessibleInterval();
}
