package com.pss.quarkus.spock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mocks {

    /**
     * If you are using an interface to mock, what is the implementation class, this is not working, this will take some
     * more byte code manipulation
     */
    Class implmentation() default void.class;
}
