package com.pss.quarkus.spock.inject;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;

public class InjectionOverride {

    private static final Logger LOGGER = Logger.getLogger(InjectionOverride.class);

    private static final Map<String, MockBeanSupplier> beans = new HashMap<>();

    private static final Set<InjectableTestBean> injectableBeans = new HashSet<>();

    private static final Map<Class, List<MockBeanSupplier>> map = new HashMap<>();

    public static Object getInjectableBean(InjectableTestBean bean, CreationalContext ctx) {
        LOGGER.debugf("Getting injection for %s", bean);
        if (beans.containsKey(bean.getIdentifier())) {
            MockBeanSupplier supplier;
            if ((supplier = beans.get(bean.getIdentifier())) == null) {
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
            if (possibleMockSuppliers.isEmpty()) {
                beans.put(bean.getIdentifier(), null);
                return bean.createBean(ctx);
            } else if (possibleMockSuppliers.size() == 1) {
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



    public static void putBeanSupplier(MockBeanSupplier supplier) {
        map.computeIfAbsent(supplier.getMethod().getReturnType(), key -> new ArrayList<>()).add(supplier);
    }

    public static void putInjectableBean(InjectableTestBean bean) {
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
