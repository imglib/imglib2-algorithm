package net.imglib2.algorithm.blocks;

/**
 * Specify in which precision should intermediate values be computed. (For
 * {@code AUTO}, the type that can represent the input/output type without
 * loss of precision is picked. That is, {@code FLOAT} for u8, i8, u16, i16,
 * i32, f32, and otherwise {@code DOUBLE} for u32, i64, f64.
 */
public enum ComputationType
{
	FLOAT, DOUBLE, AUTO
}
