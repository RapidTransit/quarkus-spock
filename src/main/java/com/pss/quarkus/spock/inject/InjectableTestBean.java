package com.pss.quarkus.spock.inject;

import javax.enterprise.context.spi.CreationalContext;

import io.quarkus.arc.InjectableBean;

/**
 * = Test Interface Enhancement
 *
 * This interface is swaps out {@link InjectableBean}, the {@link InjectableBean#create(CreationalContext)} is swapped
 * to {@link InjectableTestBean#createBean(CreationalContext)} and {@link InjectableBean#create(CreationalContext)} is
 * reimplemented to look for a mock
 * @param <T>
 */
public interface InjectableTestBean<T> extends InjectableBean<T> {

    T createBean(CreationalContext ctx);

}
