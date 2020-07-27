package com.dhy.apiholder.demo;

import com.dhy.apiholder.ApiHolderUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface BaseUrlE {
    ServerConfig value();

    /**
     * append to value, auto check separator of '/'
     */
    String append() default "";

    /**
     * marke as root api for  {@link ApiHolderUtil#isRelease()}
     */
    boolean rootApi() default false;
}
