package net.imglib2.algorithm.blocks.convolve;

import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.OperandType;
import net.imglib2.type.NativeType;

class DoGProcessor< P > extends JoinedBlockProcessor< P, P, P, P >
{
	private final SubtractLoop< P > subtract;

	public < T extends NativeType< T > > DoGProcessor(
			final T type,
			final BlockProcessor< P, P > p0,
			final BlockProcessor< P, P > p1 )
	{
		super( type, type, type, p0, p1 );
		subtract = SubtractLoops.get( OperandType.of( type ) );
	}

	public DoGProcessor( final DoGProcessor< P > processor )
	{
		super( processor );
		subtract = processor.subtract;
	}

	@Override
	public BlockProcessor< P, P > independentCopy()
	{
		return new DoGProcessor<>( this );
	}

	@Override
	void reduce( final P dest0, final P dest1, final P dest )
	{
		subtract.apply( dest1, dest0, dest, destLength );
	}
}
