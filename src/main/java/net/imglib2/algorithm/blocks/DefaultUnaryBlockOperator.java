package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

public class DefaultUnaryBlockOperator< S extends NativeType< S >, T extends NativeType< T > > implements UnaryBlockOperator< S, T >
{
	private final S sourceType;
	private final T targetType;
	private final BlockProcessor< ?, ? > blockProcessor;

	public DefaultUnaryBlockOperator( S sourceType, T targetType, BlockProcessor< ?, ? > blockProcessor )
	{
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.blockProcessor = blockProcessor;
	}

	@Override
	public < I, O > BlockProcessor< I, O > blockProcessor()
	{
		return Cast.unchecked( blockProcessor );
	}

	@Override
	public S getSourceType()
	{
		return sourceType;
	}

	@Override
	public T getTargetType()
	{
		return targetType;
	}

	@Override
	public UnaryBlockOperator< S, T > threadSafe()
	{
		final Supplier< ? extends BlockProcessor< ?, ? > > processorSupplier = blockProcessor.threadSafeSupplier();
		return new UnaryBlockOperator< S, T >()
		{
			@Override
			public < I, O > BlockProcessor< I, O > blockProcessor()
			{
				return Cast.unchecked( processorSupplier.get() );
			}

			@Override
			public S getSourceType()
			{
				return sourceType;
			}

			@Override
			public T getTargetType()
			{
				return targetType;
			}

			@Override
			public UnaryBlockOperator< S, T > threadSafe()
			{
				return this;
			}
		};
	}

}
