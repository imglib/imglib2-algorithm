package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;
import net.imglib2.Interval;
import net.imglib2.util.CloseableThreadLocal;

class ConcatenatedBlockProcessor< I, K, O > implements BlockProcessor< I, O >
{
	private final BlockProcessor< I, K > p0;

	private final BlockProcessor< K, O > p1;

	private Supplier< ConcatenatedBlockProcessor< I, K, O > > threadSafeSupplier;

	public ConcatenatedBlockProcessor(
			BlockProcessor< I, K > p0,
			BlockProcessor< K, O > p1 )
	{
		this.p0 = p0;
		this.p1 = p1;
	}

	private ConcatenatedBlockProcessor( ConcatenatedBlockProcessor< I, K, O > processor )
	{
		p0 = processor.p0.threadSafeSupplier().get();
		p1 = processor.p1.threadSafeSupplier().get();
		threadSafeSupplier = processor.threadSafeSupplier;
	}

	@Override
	public Supplier< ? extends BlockProcessor< I, O > > threadSafeSupplier()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( () -> new ConcatenatedBlockProcessor<>( this ) )::get;
		return threadSafeSupplier;
	}

	@Override
	public void setTargetInterval( final Interval interval )
	{
		p1.setTargetInterval( interval );
		p0.setTargetInterval( p1.getSourceInterval() );
	}

	@Override
	public long[] getSourcePos()
	{
		return p0.getSourcePos();
	}

	@Override
	public int[] getSourceSize()
	{
		return p0.getSourceSize();
	}

	@Override
	public Interval getSourceInterval()
	{
		return p0.getSourceInterval();
	}

	@Override
	public I getSourceBuffer()
	{
		return p0.getSourceBuffer();
	}

	@Override
	public void compute( final I src, final O dest )
	{
		p0.compute( src, p1.getSourceBuffer() );
		p1.compute( p1.getSourceBuffer(), dest );
	}
}
