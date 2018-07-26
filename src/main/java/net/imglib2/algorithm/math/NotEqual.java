package net.imglib2.algorithm.math;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.type.numeric.RealType;

public final class NotEqual extends Compare
{
	public NotEqual( final Object o1, final Object o2) {
		super( o1, o2 );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean compare( final RealType t1, final RealType t2 )
	{
		return 0 != t1.compareTo( t2 );
	}

	@Override
	public NotEqual copy() {
		final NotEqual copy = new NotEqual( this.a.copy(), this.b.copy() );
		copy.setScrap( this.scrap );
		return copy;
	}
}