package net.imglib2.algorithm.blocks.convert;

@FunctionalInterface
interface ConvertLoop< I, O >
{
	void apply( final I src, final O dest, final int length );
}
