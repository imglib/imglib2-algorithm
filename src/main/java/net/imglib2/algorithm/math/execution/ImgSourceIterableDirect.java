package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IImgSourceIterable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgSourceIterableDirect< O extends RealType< O > > implements OFunction< O >, IImgSourceIterable
{
	private final RandomAccessibleInterval< O > rai;
	private final Cursor< O > it;
	private final RandomAccess< O > ra;
	
	public ImgSourceIterableDirect( final RandomAccessibleInterval< O > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).cursor();
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

	@Override
	public boolean hasNext()
	{
		return this.it.hasNext();
	}

	@Override
	public Localizable localizable()
	{
		return this.it;
	}

	@Override
	public void localize(long[] position)
	{
		this.it.localize( position );
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
}
