package com.disaster.locklayer.locklayer.infrastructure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LockLayer {
    String key() default "default_key";

    boolean reentryLock() default false;

    int expireTime() default 30;
}
