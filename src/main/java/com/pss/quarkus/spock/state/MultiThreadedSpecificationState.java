package com.pss.quarkus.spock.state;

import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.ClientProxy;
import io.quarkus.runner.RuntimeRunner;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Not to sure about Spock's threading model yet, this was going to be a place holder for a thread local
 */
public class MultiThreadedSpecificationState {


    /**
     * There is some extra threads created so we might need an {@link InheritableThreadLocal} I don't know yet
     */
    private static final ThreadLocal<MultiThreadedSpecificationState> SPECIFICATION_STATES = new InheritableThreadLocal<>();


    private RuntimeRunner runner;

    private Object specification;
    private final SpecInfo info;
    private final Class clazz;

    public MultiThreadedSpecificationState(SpecInfo info, Class clazz) {
        this.info = info;
        this.clazz = clazz;
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

    public static Object unwrap(Object object){
        if(object instanceof ClientProxy){
            return ((ClientProxy) object).getContextualInstance();
        }
        return object;
    }




    public static MultiThreadedSpecificationState getInstance(){
        return SPECIFICATION_STATES.get();
    }

    public static void remove(){
        SPECIFICATION_STATES.remove();
    }

    public static void set(SpecInfo info){
        SPECIFICATION_STATES.set(new MultiThreadedSpecificationState(info, info.getReflection()));
    }

}
