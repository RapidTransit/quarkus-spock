package com.pss.quarkus.spock.inject;

import io.quarkus.arc.InjectableBean;

import javax.enterprise.context.spi.CreationalContext;

public interface InjectableTestBean<T> extends InjectableBean<T> {

    T createBean(CreationalContext ctx);
}
