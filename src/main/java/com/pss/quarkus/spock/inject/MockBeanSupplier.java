package com.pss.quarkus.spock.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Qualifier;

import com.pss.quarkus.spock.state.SpecificationState;

public class MockBeanSupplier {

    private final Annotation[] qualifiers;

    private final Method method;

    private final Class<?> clazz;

    private Object bean;

    private MockBeanSupplier(Method method, Annotation[] qualifiers, Class<?> type) {
        this.method = method;
        this.qualifiers = qualifiers;
        this.clazz = type;
    }



    public Annotation[] getQualifiers() {
        return qualifiers;
    }

    public Method getMethod() {
        return method;
    }

    public Object getBean() {
        if (bean == null) {
            Objects.requireNonNull(SpecificationState.getSpecification(), "Specification State was not set");
            try {
                bean = method.invoke(SpecificationState.getSpecification());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return "MockBeanSupplier{" +
                "qualifiers=" + Arrays.toString(qualifiers) +
                ", method=" + method +
                ", clazz=" + clazz +
                ", bean=" + bean +
                '}';
    }

    public static MockBeanSupplier from(Method method) {
        List<Annotation> qualifier = new ArrayList<>();
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifier.add(annotation);
            }
        }
        Annotation[] qualifiers = qualifier.toArray(new Annotation[0]);
        Class<?> type = method.getReturnType();
        return new MockBeanSupplier(method, qualifiers, type);
    }
}
