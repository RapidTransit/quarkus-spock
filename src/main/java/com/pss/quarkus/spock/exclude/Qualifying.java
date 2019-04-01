package com.pss.quarkus.spock.exclude;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Qualifying {
    enum Qualify {
        QUALIFY, ANOTHER_QUALIFY
    }

    Qualify value();
}
