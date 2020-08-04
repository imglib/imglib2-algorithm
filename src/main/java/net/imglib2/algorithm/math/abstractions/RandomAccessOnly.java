package net.imglib2.algorithm.math.abstractions;

public interface RandomAccessOnly
{
	public default boolean isRandomAccessOnly()
	{
		return true;
	}
}
