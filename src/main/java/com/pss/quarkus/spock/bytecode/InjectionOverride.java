package com.pss.quarkus.spock.bytecode;

import org.jboss.logging.Logger;

import javax.enterprise.inject.InjectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class InjectionOverride {

    private static final Logger LOGGER = Logger.getLogger(InjectionOverride.class);

    private static final Map<String, BeanSupplier<Object>> injections = new HashMap<>();

    public static Object getInjection(String injection) {
        LOGGER.debugf("Getting injection for %s", injection);
        BeanSupplier<Object> supplier = injections.get(injection);
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


    public static void putInjection(String name, BeanSupplier supplier){
        injections.put(name, supplier);
    }

    public interface BeanSupplier<T> extends Supplier<T> {

        String getName();

        Class<T> getType();

        default T getBean(){
            T bean = get();
            postProcess(bean);
            return bean;
        }

        void postProcess(T bean);
    }
}
