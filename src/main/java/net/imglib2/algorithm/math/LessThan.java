package net.imglib2.algorithm.math;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.type.numeric.RealType;

public final class LessThan extends Compare
{
	public LessThan( final Object o1, final Object o2) {
		super( o1, o2 );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean compare( final RealType t1, final RealType t2 )
	{
		return -1 == t1.compareTo( t2 );
	}

	@Override
	public LessThan copy() {
		final LessThan copy = new LessThan( this.a.copy(), this.b.copy() );
		copy.setScrap( this.scrap );
		return copy;
	}
}