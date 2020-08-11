package net.imglib2.algorithm.math.abstractions;

import net.imglib2.RandomAccessible;

public interface RandomAccessOnly< I >
{
	public default boolean isRandomAccessOnly()
	{
		return true;
	}
	
	public RandomAccessible< I > getRandomAccessible();
}
