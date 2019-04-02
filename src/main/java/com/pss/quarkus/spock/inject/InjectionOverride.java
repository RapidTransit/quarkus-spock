package com.pss.quarkus.spock.inject;

import com.pss.quarkus.spock.util.CommonUtils;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableBean;
import org.jboss.logging.Logger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class InjectionOverride {





    private static final Logger LOGGER = Logger.getLogger(InjectionOverride.class);

    private static final Set<String> jvmClassName = new HashSet<>();

    private static final Set<String> jvmInjectableBeanName = new HashSet<>();


    public static final Method INJECTABLE_BEANS;

    public static final Method GET_INJECTION;

    private static final Method CREATE;

    static {
        try {

            INJECTABLE_BEANS = InjectionOverride.class
                    .getDeclaredMethod("putInjectableBean", InjectableTestBean.class);

            GET_INJECTION = InjectionOverride.class
                    .getDeclaredMethod("getInjectableBean", InjectableTestBean.class, CreationalContext.class);

            CREATE = Contextual.class.getDeclaredMethod("create", CreationalContext.class);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private static final Map<String, MockBeanSupplier> beans = new HashMap<>();

    private static final Set<InjectableTestBean> injectableBeans = new HashSet<>();

    private static final Map<Class, List<MockBeanSupplier>> map = new HashMap<>();





    public static Object getInjectableBean(InjectableTestBean bean, CreationalContext ctx){
        LOGGER.debugf("Getting injection for %s", bean);
        if(beans.containsKey(bean.getIdentifier())){
            MockBeanSupplier supplier;
            if((supplier = beans.get(bean.getIdentifier())) == null){
                return bean.createBean(ctx);
            } else {
                return supplier.getBean();
            }
        } else {
            BeanManager beanManager = Arc.container().beanManager();
            List<BeanSupplierTuple> possibleMockSuppliers = map.getOrDefault(bean.getBeanClass(), Collections.emptyList())
                    .stream()
                    .flatMap(mockBeanSupplier -> beanManager.getBeans(bean.getBeanClass(), mockBeanSupplier.getQualifiers())
                            .stream()
                            .map(javaxBean -> new BeanSupplierTuple(mockBeanSupplier, javaxBean)))
                    .collect(Collectors.toList());
            if(possibleMockSuppliers.isEmpty()){
                beans.put(bean.getIdentifier(), null);
                return bean.createBean(ctx);
            } else if(possibleMockSuppliers.size() == 1){
                BeanSupplierTuple supplierTuple = possibleMockSuppliers.get(0);
                LOGGER.debugf("Found a Mock Supplier: %s", supplierTuple);
                beans.put(supplierTuple.bean.getName(), supplierTuple.supplier);
                return supplierTuple.supplier.getBean();
            } else {
                LOGGER.trace("*** Start List of possible Beans ***");
                possibleMockSuppliers.forEach(tuple -> {
                    LOGGER.tracef("Bean Supplier: %s", tuple);
                });
                LOGGER.trace("*** End List of possible Beans ***");
                throw new RuntimeException("Work this out later");
            }

        }
    }





    public static boolean containsJvmClassName(String name){
        return jvmClassName.contains(name);
    }

    public static boolean containsBeanSupplier(String name){
       return jvmInjectableBeanName.contains(name);
    }

    public static void putBeanSupplier(MockBeanSupplier supplier){
        map.computeIfAbsent(supplier.getMethod().getReturnType(),key-> new ArrayList<>()).add(supplier);
    }

    public static void putInjectableBean(InjectableTestBean bean){
        injectableBeans.add(bean);
    }


    private static class BeanSupplierTuple {
        private final MockBeanSupplier supplier;
        private final Bean<?> bean;

        private BeanSupplierTuple(MockBeanSupplier supplier, Bean<?> bean) {
            this.supplier = supplier;
            this.bean = bean;
        }

        @Override
        public String toString() {
            return "BeanSupplierTuple{" +
                    "supplier=" + supplier +
                    ", bean=" + bean +
                    '}';
        }
    }

}
