package com.pss.quarkus.spock.repack;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Qualifier;

import com.pss.quarkus.spock.util.CommonUtils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;

/**
 * I can't/do not know how to exclude {@link io.quarkus.arc.deployment.ArcTestResourceProvider} or guarantee the loading
 * order of the ServiceLoader
 */
public class ArcTestResourceProvider {

    public static void inject(Object test) {
        Class<?> c = test.getClass();
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(Inject.class)) {
                    BeanManager beanManager = Arc.container().beanManager();
                    List<Annotation> qualifiers = new ArrayList<>();
                    for (Annotation a : f.getAnnotations()) {
                        if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                            qualifiers.add(a);
                        }
                    }
                    Set<Bean<?>> beans = beanManager.getBeans(f.getType(),
                            qualifiers.toArray(new Annotation[qualifiers.size()]));
                    Bean<?> bean = beanManager.resolve(beans);
                    CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
                    Object instance = beanManager.getReference(bean, f.getType(), ctx);
                    Object unwrapped = null;
                    if (instance instanceof ClientProxy) {
                        unwrapped = ((ClientProxy) instance).getContextualInstance();
                    }
                    f.setAccessible(true);
                    try {
                        if (CommonUtils.MOCK_UTIL.isMock(unwrapped)) {
                            f.set(test, unwrapped);
                        } else {
                            f.set(test, instance);
                        }

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            c = c.getSuperclass();
        }
    }
}
