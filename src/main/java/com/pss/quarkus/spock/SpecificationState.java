package com.pss.quarkus.spock;

import io.quarkus.arc.ClientProxy;
import org.spockframework.runtime.model.SpecInfo;

/**
 * Not to sure about Spock's threading model yet, this was going to be a place holder for a thread local
 */
public class SpecificationState {

    private Object specification;
    private final SpecInfo info;
    private final Class clazz;

    public SpecificationState(SpecInfo info, Class clazz) {
        this.info = info;
        this.clazz = clazz;
    }

    public SpecInfo getInfo() {
        return info;
    }

    public Class getClazz() {
        return clazz;
    }

    public static Object unwrap(Object object){
        if(object instanceof ClientProxy){
            return ((ClientProxy) object).getContextualInstance();
        }
        return object;
    }
}
