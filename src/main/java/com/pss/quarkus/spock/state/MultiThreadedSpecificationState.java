package com.pss.quarkus.spock.state;

import org.spockframework.runtime.model.SpecInfo;

import io.quarkus.arc.ClientProxy;
import io.quarkus.runner.RuntimeRunner;

/**
 * Not to sure about Spock's threading model yet, this was going to be a place holder for a thread local
 */
public class MultiThreadedSpecificationState {

    /**
     * There is some extra threads created so we might need an {@link InheritableThreadLocal} I don't know yet
     */
    private static final ThreadLocal<MultiThreadedSpecificationState> SPECIFICATION_STATES = new InheritableThreadLocal<>();
    private final SpecInfo info;
    private final Class clazz;
    private RuntimeRunner runner;
    private Object specification;

    public MultiThreadedSpecificationState(SpecInfo info, Class clazz) {
        this.info = info;
        this.clazz = clazz;
    }


    public static MultiThreadedSpecificationState getInstance() {
        return SPECIFICATION_STATES.get();
    }

    public static void remove() {
        SPECIFICATION_STATES.remove();
    }

    public static void set(SpecInfo info) {
        SPECIFICATION_STATES.set(new MultiThreadedSpecificationState(info, info.getReflection()));
    }

    public SpecInfo getInfo() {
        return info;
    }

    public Class getClazz() {
        return clazz;
    }

    public Object getSpecification() {
        return specification;
    }

    public void setSpecification(Object specification) {
        this.specification = specification;
    }

}
