package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;
import net.imglib2.Interval;
import net.imglib2.blocks.PrimitiveBlocks;

/**
 * A {@code BlockProcessor} computes values in a flattened primitive output
 * array from values in a flattened primitive input array.
 * <p>
 * Typically, {@code BlockProcessor} should not be used directly, but
 * wrapped in {@link UnaryBlockOperator} which has the ImgLib2 {@code Type}s
 * corresponding to {@code I}, {@code O}. This helps to avoid mistakes with
 * unchecked (primitive array) type casts.
 *
 * @param <I>
 * 		input primitive array type, e.g., float[]
 * @param <O>
 * 		output primitive array type, e.g., float[]
 */
public interface BlockProcessor< I, O >
{
	Supplier< ? extends BlockProcessor< I, O > > threadSafeSupplier();

	void setTargetInterval( Interval interval );

	long[] getSourcePos();

	int[] getSourceSize();

	// TODO: Its cumbersome to have both getSourcePos()/getSourceSize() *and* getSourceInterval()
	//       Only have getSourcePos()/getSourceSize() ?
	//       Have a modifiable SourceInterval class exposing getSourcePos()/getSourceSize() ?
	Interval getSourceInterval();

	I getSourceBuffer();

	/**
	 * Compute the {@code dest} array from the {@code src} array.
	 * <p>
	 * {@code src} and {@code dest} are expected to be flattened arrays of the
	 * dimensions specified in {@link #setTargetInterval} and computed in {@link
	 * #getSourceSize}, respectively. The typical sequence is:
	 * <nl>
	 * <li>A given target array {@code dest} with known flattened layout and min
	 * position should be computed.</li>
	 * <li>Call {@link #setTargetInterval}. This will compute the corresponding
	 * {@link #getSourceInterval source interval} (also available as {@link
	 * #getSourcePos}, {@link #getSourceSize}) including {@code
	 * BlockProcessor}-specific transformations, padding, etc.</li>
	 * <li>Fill a {@code src} array (either provided by {@link
	 * #getSourceBuffer}, or otherwise allocated) with the input data (see
	 * {@link PrimitiveBlocks#copy}).</li>
	 * <li>Call {@code compute(src, dest)} to compute the target array.</li>
	 * </nl>
	 * Note, that the {@code src} and {@code dest} arrays may be larger than
	 * implied by {@code setTargetInterval} and {@code getSourceSize}. In that
	 * case the trailing elements are ignored.
	 * <p>
	 * Typically, {@code BlockProcessor} should not be used directly, but
	 * wrapped in {@link UnaryBlockOperator} which has the ImgLib2 {@code Type}s
	 * corresponding to {@code I}, {@code O}.
	 *
	 * @param src
	 * 		flattened primitive array with input values
	 * @param dest
	 * 		flattened primitive array to fill with output values
	 */
	void compute( I src, O dest );

	/**
	 * Returns a {@code BlockProcessor concatenated} such that
	 * <pre>{@code
	 *   concatenated.compute(src, dest);
	 * }</pre>
	 * is equivalent to
	 * <pre>{@code
	 *   this.compute(src, tmp);
	 *   processor.compute(tmp, dest);
	 * }</pre>
	 */
	default < P > BlockProcessor< I, P > andThen( BlockProcessor< O, P > processor )
	{
		return new ConcatenatedBlockProcessor<>( this, processor );
	}
}
