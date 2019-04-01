package com.pss.quarkus.spock.inject;

import com.pss.quarkus.spock.annotations.Mocks;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.Qualifiers;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.New;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InjectionMetadata {

    private final Set<Class> compatible;
    private final Class clazz;
    private final Set<Annotation> qualifiers;

    private InjectionMetadata(Set<Class> compatible, Class clazz, Set<Annotation> qualifiers) {
        this.compatible = compatible;
        this.clazz = clazz;
        this.qualifiers = qualifiers;
    }


    public boolean isCanidate(InjectableBean bean){
        return bean.getBeanClass().isAssignableFrom(clazz)
               && bean.getQualifiers().equals(qualifiers);
    }


    public Class getClazz() {
        return clazz;
    }

    public static InjectionMetadata fromMethod(Method method){

        // If the person would like to use interfaces instead
        Class type = Optional.of(method.getAnnotation(Mocks.class))
                .map(x -> x.implmentation())
                .filter(x -> x != void.class)
                .orElse(method.getReturnType());

        Set<Class> compatible = new HashSet<>();

        Class<?> returnType = method.getReturnType();
        while (returnType != null){
            compatible.add(returnType);
            for(Class inter : returnType.getInterfaces()){
                compatible.add(inter);
            }
            returnType = returnType.getSuperclass();
        }

        Set<Annotation> qualifier = new HashSet<>();
        for(Annotation annotation : method.getAnnotations()){
            if(annotation.annotationType().isAnnotationPresent(Qualifier.class)){
                qualifier.add(annotation);
            }
        }
        if(!qualifier.contains(New.Literal.INSTANCE)){
            qualifier.add(Any.Literal.INSTANCE);
        }

        return new InjectionMetadata(
                compatible,
                type,
                // Inject Default Qualifiers if the only annotation is @Any
                qualifier.size()== 1 && qualifier.contains(Any.Literal.INSTANCE) ?
                        Qualifiers.DEFAULT_QUALIFIERS
                        : qualifier);
    }
}
