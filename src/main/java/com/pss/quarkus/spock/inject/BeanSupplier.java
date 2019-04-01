package com.pss.quarkus.spock.inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class BeanSupplier {

    private final String name;
    private final Method method;

    static Object specification;

    private Object bean;

    public BeanSupplier(String name, Method method) {
        this.name = name;
        this.method = method;
    }


    public String getName(){
        return name;
    }

    public Object getBean(){
        if(bean == null) {
            try {
                bean = method.invoke(specification);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }


    public Object getSpecification() {
        return specification;
    }

    public static void setSpecification(Object spec) {
        specification = spec;
    }
}
