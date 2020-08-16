package com.dhy.apiholder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface BaseUrl {
    String value();

    /**
     * append to value, auto check separator of '/'
     */
    String append() default "";
}
