package net.imglib2.algorithm.math.abstractions;

import net.imglib2.Cursor;

public interface IImgSourceIterable< I >
{	
	public Cursor< I > getCursor();
}
