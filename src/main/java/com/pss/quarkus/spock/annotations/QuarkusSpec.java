package com.pss.quarkus.spock.annotations;

import com.pss.quarkus.spock.QuarkusSpockExtension;
import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * # QuarkusSpec
 *
 * Add this annotation to your Spock Specification to bootstrap a Quarkus embedded application
 */
@ExtensionAnnotation(QuarkusSpockExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface QuarkusSpec {

    boolean substrate() default false;

    /**
     * Override the default log output location
     *
     * **Default** 'target/quarkus.log'
     *
     * @return a relative path including the file name
     */
    String logLocation() default "";
}
