package com.pss.quarkus.spock;

import io.quarkus.arc.ClientProxy;
import org.spockframework.runtime.model.SpecInfo;

public class SpecificationState {

    private static final ThreadLocal<SpecificationState> STATE = new ThreadLocal<>();

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
