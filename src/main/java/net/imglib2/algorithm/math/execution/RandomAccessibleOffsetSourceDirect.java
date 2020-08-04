package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.RealType;

public class RandomAccessibleOffsetSourceDirect< O extends RealType< O > > extends RandomAccessibleOffsetSource< O, O >
{	
	public RandomAccessibleOffsetSourceDirect( final O scrap, final RandomAccessible< O > src, final long[] offset )
	{
		super( scrap, null, src, offset );
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.scrap.set( this.ra.get() );
		return this.scrap;
	}
}
