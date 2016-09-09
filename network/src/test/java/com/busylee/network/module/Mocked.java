package com.busylee.network.module;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by busylee on 02.09.16.
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Mocked {

    /** The name. */
    String value() default "";
}
