package com.pss.quarkus.spock.exclude;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
public @interface Qualifying {
    Qualify value();

    enum Qualify {
        QUALIFY,
        ANOTHER_QUALIFY
    }
}
