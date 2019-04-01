package com.pss.quarkus.spock;

import io.quarkus.arc.ClientProxy;
import org.spockframework.runtime.model.SpecInfo;

/**
 * Not to sure about Spock's threading model yet
 */
public class SpecificationState {

    private static SpecInfo info;
    private static Class clazz;

    public static void setInfo(SpecInfo info) {
        SpecificationState.info = info;
    }

    public static void setClazz(Class clazz) {
        SpecificationState.clazz = clazz;
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
