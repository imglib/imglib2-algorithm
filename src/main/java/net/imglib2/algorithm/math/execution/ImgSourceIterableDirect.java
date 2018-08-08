package net.imglib2.algorithm.math.execution;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgSourceIterableDirect< O extends RealType< O > > implements OFunction< O >
{
	private final RandomAccessibleInterval< O > rai;
	private final Iterator< O > it;
	private final RandomAccess< O > ra;
	
	public ImgSourceIterableDirect( final RandomAccessibleInterval< O > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).iterator();
		this.ra = rai.randomAccess();
	}
	
	@Override
	public final O eval()
	{
		return this.it.next();
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		return this.ra.get();
	}
	
	public RandomAccessibleInterval< O > getRandomAccessibleInterval()
	{
		return this.rai;
	}
}
