package net.imglib2.algorithm.blocks.convert;

/**
 * How to clamp values when converting between {@link net.imglib2.algorithm.blocks.util.OperandType}.
 */
public enum ClampType
{
	/**
	 * don't clamp
	 */
	NONE,

	/**
	 * clamp to lower and upper bound
	 */
	CLAMP,

	/**
	 * clamp only to upper bound
	 */
	CLAMP_MAX;
}
