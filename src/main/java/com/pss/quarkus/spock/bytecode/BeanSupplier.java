package com.pss.quarkus.spock.bytecode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class BeanSupplier {

    //todo: Right now we just inject by name
    // private final Set<Object> metadata = new HashSet<>();

    private final String name;
    private final Method method;

    static Object specification;

    public BeanSupplier(String name, Method method) {
        this.name = name;
        this.method = method;
    }


    public String getName(){
        return name;
    }

    public Object getBean(){
        try {
            return method.invoke(specification);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public Object getSpecification() {
        return specification;
    }

    public static void setSpecification(Object spec) {
        specification = spec;
    }
}
