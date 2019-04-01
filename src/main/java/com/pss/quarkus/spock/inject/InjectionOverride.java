package com.pss.quarkus.spock.inject;

import com.pss.quarkus.spock.util.ClassUtil;
import io.quarkus.arc.InjectableBean;
import org.jboss.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InjectionOverride {

    private static final Logger LOGGER = Logger.getLogger(InjectionOverride.class);

    private static final Map<String, BeanSupplier> injections = new HashMap<>();

    private static final Set<String> jvmClassName = new HashSet<>();

    private static final Set<String> jvmInjectableBeanName = new HashSet<>();

    private static final Set<String> classNames = new HashSet<>();

    /**
     * todo: Start of injecting by metadata
     */
    private static final Map<InjectionMetadata, BeanSupplier> metadata = new HashMap<>();


    public static Object getInjection(InjectableBean bean, CreationalContext ctx){
        return metadata.entrySet().stream()
                .filter(x-> x.getKey().isCanidate(bean))
                .findFirst()
                .map(x-> x.getValue().getBean())
                //.orElse(null);
                .orElseThrow(()-> new InjectionException("Bean: " + bean.getName() + " not found"));

    }

    public static void putInjection(InjectionMetadata meta, BeanSupplier supplier){
        metadata.put(meta, supplier);
        classNames.add(meta.getClazz().getName());
        String toSlash = ClassUtil.toJvm(meta.getClazz());
        jvmClassName.add(toSlash);
        jvmInjectableBeanName.add(toSlash + "_Bean");
    }

    @Deprecated
    public static Object getInjection(String injection) {
        LOGGER.debugf("Getting injection for %s", injection);
        BeanSupplier supplier = injections.get(injection);
        if(supplier == null){
            LOGGER.debugf("Bean not found for %s", injection);
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("*** Start List of beans ***");
                injections.values()
                        .forEach((bean)-> LOGGER.tracef("Bean: %s", bean));
                LOGGER.trace("*** End List of beans ***");
            }
            throw new InjectionException("Bean: " + injection + " not found");
        }
        return supplier.getBean();
    }

    public static boolean contains(String name){
        return jvmClassName.contains(name);
    }

    public static boolean containsBeanSupplier(String name){
       return jvmInjectableBeanName.contains(name);
    }

    public static void putInjection(Class clazz, BeanSupplier supplier){
        classNames.add(clazz.getName());
        String toSlash = ClassUtil.toJvm(clazz);
        injections.put(toSlash, supplier);
        jvmInjectableBeanName.add(toSlash + "_Bean");
    }


}
