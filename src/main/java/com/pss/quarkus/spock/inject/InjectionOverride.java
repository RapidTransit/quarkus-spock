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

    private static final Set<String> jvmClassName = new HashSet<>();

    private static final Set<String> jvmInjectableBeanName = new HashSet<>();

    private static final Set<String> classNames = new HashSet<>();

    /**
     * todo: Start of injecting by metadata
     */
    private static final Map<InjectionMetadata, BeanSupplier> metadata = new HashMap<>();


    @SuppressWarnings("unused")
    public static Object getInjection(InjectableBean bean, CreationalContext ctx){
        LOGGER.debugf("Getting injection for %s", bean);
        return metadata.entrySet().stream()
                .filter(x-> x.getKey().isCanidate(bean))
                .findFirst()
                .map(x-> x.getValue().getBean())
                .orElseThrow(()-> {
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("*** Start List of beans ***");
                        metadata.keySet()
                                .forEach((key)-> LOGGER.tracef("Bean: %s", key));
                        LOGGER.trace("*** End List of beans ***");
                    }
                    return new InjectionException("Bean: " + bean.getName() + " not found");
                });

    }

    public static void putInjection(InjectionMetadata meta, BeanSupplier supplier){
        metadata.put(meta, supplier);
        classNames.add(meta.getClazz().getName());
        String toSlash = ClassUtil.toJvm(meta.getClazz());
        jvmClassName.add(toSlash);
        jvmInjectableBeanName.add(toSlash + "_Bean");
    }

    public static boolean containsJvmClassName(String name){
        return jvmClassName.contains(name);
    }

    public static boolean containsBeanSupplier(String name){
       return jvmInjectableBeanName.contains(name);
    }



}
