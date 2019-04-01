package com.pss.quarkus.spock.bytecode;

import com.pss.quarkus.spock.util.ClassUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.InjectionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InjectionOverride {

    private static final Logger LOGGER = Logger.getLogger(InjectionOverride.class);

    private static final Map<String, BeanSupplier> injections = new HashMap<>();

    private static final Set<String> enhancedNames = new HashSet<>();

    private static final Set<String> classNames = new HashSet<>();

    public static Object getInjection(String injection) {
        LOGGER.debugf("Getting injection for %s", injection);
        BeanSupplier supplier = injections.get(injection);
        if(supplier == null){
            LOGGER.debugf("Bean not found for %s", injection);
            if(LOGGER.isTraceEnabled()){
                injections.values()
                        .forEach((bean)-> LOGGER.tracef("Bean: %s", bean));
            }
            throw new InjectionException("Bean: " + injection + " not found");
        }
        return supplier.getBean();
    }

    public static boolean contains(String name){
        return injections.containsKey(name);
    }

    public static boolean containsBeanSupplier(String name){
       return enhancedNames.contains(name);
    }

    public static boolean containsClassName(String name){
        return classNames.contains(name);
    }

    public static void putInjection(Class clazz, BeanSupplier supplier){
        classNames.add(clazz.getName());
        String toSlash = ClassUtil.toSlash(clazz);
        injections.put(toSlash, supplier);
        enhancedNames.add(toSlash + "_Bean");
    }


}
