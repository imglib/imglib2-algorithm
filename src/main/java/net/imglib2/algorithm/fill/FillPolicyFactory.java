package net.imglib2.algorithm.fill;

/**
 * Created by hanslovskyp on 4/21/16.
 */
public interface FillPolicyFactory< T > {

    FillPolicy< T > call( T t );

}
