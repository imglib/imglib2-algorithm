package net.imglib2.algorithm.math;

import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.type.numeric.RealType;

public final class Equal extends Compare
{
	public Equal( final Object o1, final Object o2) {
		super( o1, o2 );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean compare( final RealType t1, final RealType t2 )
	{
		return 0 == t1.compareTo( t2 );
	}

	@Override
	public Equal copy() {
		final Equal copy = new Equal( this.a.copy(), this.b.copy() );
		copy.setScrap( this.scrap );
		return copy;
	}
}