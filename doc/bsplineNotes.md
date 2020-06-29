## BSpline notes

There are three kinds of BSpline interpolation.
1. **Precomputed coefficients** (fastest, high memory use)
2. **Lazily precomputed coefficients** (medium speed, medium memory use)
3. **On the fly interpolation** (slowest, low memory use)

### Recommendations

* Generally use precomputed coefficients (1)
* Use Lazily precomputed coefficients (2) for large images
  * Use the largest practical block size (128 cubed or 512 squared)
  * Smaller block size only for spare, small numbers of interpolations
  * Avoid blocks with less than 32 pixels per side
* Use on-the-fly interpolation (3) sparingly

Below we describe each method with code examples.


### 1 Precomputed coefficients

Recommended for small to medium sized images or when memory is not a concern.

The method precomputes BSpline coefficients and is substantially faster if get is called many times, but at the cost of memory - it allocates a RandomAccessibleInterval<T> the same size as the image to be interpolated. T defaults to DoubleType. Use it like this:

```
int splineOrder = ...;
boolean clipping = ...;
factory= new BSplineCoefficientsInterpolatorFactory<>( img, splineOrder, clipping );
realImg = Views.interpolate( img, factory);
```

here the factory constructor computes the coefficients - which is why it needs to be passed. Unfortunately, this means that the img argument to Views.interpolate is meaningless. For example,

```
realImgFoo = Views.interpolate( img, factory);
realImgBar = Views.interpolate( someOtherImg, factory);
```

both result in the same thing. We stuck with this to keep the api consistent. No one should be doing the bottom thing anyway.
Generating the coefficients

`BSplineDecomposition` and `BSplineCoefficientsInterpolator` give more control for those who need it.  For example:

```
/*
 * Compute the coefficients over an arbitrary interval
 */
BSplineDecomposition<T,S> decomp = new BSplineDecomposition<>( splineOrder,, extendedImg );
long[] min = Intervals.minAsLongArray( interval );
Img<S> coefficientsBase = coefficientFactory.create( interval );
coefficients = Views.translate( coefficientsBase, min );
decomp.accept( coefficients );

/*
 * Get a RealRandomAccess that uses the spline coefficients computed above
 * and using a custom extension of the coefficients
 */
RealRandomAccess interp = BSplineCoefficientsInterpolator.build(
     splineOrder,
     Views.extendMirrorSingle( coefficients),
     new FloatType());
```

### 2 Lazily precomputed coefficients

Recommended for large images or when memory is a concern.

The third method also precomputes BSpline coefficients, but does so over small blocks of a specified size on-demand, rather than computing all coefficients.

```
int splineOrder = ...;
int[] blockSize = ...
BSplineLazyCoefficientsInterpolatorFactory lazyFactory =
  new BSplineLazyCoefficientsInterpolatorFactory<>( img, splineOrder, blockSize );
RealRandomAccessible realImgLazy = Views.interpolate(
  Views.extendZero( img ), lazyFactory );
```

We generally recommend using the largest block size practical ( e.g. 128-cubed in 3D or 512 squared in 2d, or larger if possible).  Larger block sizes improve accuracy and speed (after precomputation).  Small block sizes could be useful when only interpolating at a small, sparse set of points, and the goal is to compute as few coefficients as possible.

### 3 On the fly interpolation

We generally do not recommend this approach since lazily computing coefficients gives
significant speed-ups with a controllable memory cost, for similar accuracy.
The code is most similar to current interpolation methods:

```
int splineOrder = ...;
boolean clipping = ...;
int radius = ...;
realImg = Views.interpolate( img, new BSplineInterpolatorFactory<>( splineOrder, clipping, radius ));
```

This computes the interpolation kernel on-the-fly at every get. The kernel size is defined by radius. clipping=true sets the interpolator to prevent over/underflow, just like the `ClampingNLinearInterpolator`.
