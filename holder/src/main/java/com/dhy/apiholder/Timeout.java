package com.dhy.apiholder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Timeout {
    long connect() default -1;

    long read();

    long write() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
