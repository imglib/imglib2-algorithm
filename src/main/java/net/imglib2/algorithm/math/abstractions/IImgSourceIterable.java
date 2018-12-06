package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Localizable;

public interface IImgSourceIterable
{
	public boolean hasNext();
	
	public Localizable localizable();
	
	public void localize( final long[] position );
}
