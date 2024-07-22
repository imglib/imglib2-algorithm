package net.imglib2.algorithm.blocks.convolve;

import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.util.OperandType;
import net.imglib2.type.NativeType;

class DoGProcessor< I, O > extends JoinedBlockProcessor< I, O, O, O >
{
	private final SubtractLoop< O > subtract;

	public < S extends NativeType< S >, T extends NativeType< T > > DoGProcessor(
			final S sourceType,
			final T targetType,
			final BlockProcessor< I, O > p0,
			final BlockProcessor< I, O > p1 )
	{
		super( sourceType, targetType, targetType, p0, p1 );
		subtract = SubtractLoops.get( OperandType.of( targetType ) );
	}

	public DoGProcessor( final DoGProcessor< I, O > processor )
	{
		super( processor );
		subtract = processor.subtract;
	}

	@Override
	public BlockProcessor< I, O > independentCopy()
	{
		return new DoGProcessor<>( this );
	}

	@Override
	void reduce( final O dest0, final O dest1, final O dest )
	{
		subtract.apply( dest1, dest0, dest, destLength );
	}
}
