package net.imglib2.algorithm.math.abstractions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public interface ImgSource< I extends RealType< I > >
{
	public RandomAccessibleInterval< I > getRandomAccessibleInterval();
}
