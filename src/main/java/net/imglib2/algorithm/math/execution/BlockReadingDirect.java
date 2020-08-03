package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.RealType;

public class BlockReadingDirect< O extends RealType< O > > extends BlockReading< O, O >
{
	public BlockReadingDirect(
			final O scrap,
			final RandomAccessible< O > src,
			final long[][] corners,
			final byte[] signs
			)
	{
		super( scrap, null, src, corners, signs );
	}
	
	@Override
	public O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.ra.move( this.moves[ 0 ] );
		this.scrap.set( this.ra.get() );
		this.scrap.mul( this.signs[ 0 ] );
		for ( int i = 1; i < this.moves.length; ++i )
		{
			this.ra.move( this.moves[ i ] );
			this.tmp.set( this.ra.get() );
			// this.tmp.mul( this.signs[ i ] );
			// this.scrap.add( tmp );
			// Less method calls: theoretically faster, but branching
			if ( 1 == this.signs[ i ] )
				this.scrap.add( tmp );
			else
				this.scrap.sub( tmp );
		}
		return this.scrap;
	}
}