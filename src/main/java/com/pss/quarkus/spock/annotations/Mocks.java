package com.pss.quarkus.spock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mocks {

    /**
     * If you are using an interface to mock
     * @return
     */
    Class implmentation() default void.class;
}
