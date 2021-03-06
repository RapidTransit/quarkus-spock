package com.pss.quarkus.spock.util;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.extension.IMethodInvocation;

import spock.lang.Specification;

public class CommonUtils {

    public static final MockUtil MOCK_UTIL = new MockUtil();

    public static String toJvm(Class clazz) {
        return clazz.getName().replace('.', '/');
    }

    public static <T> int indexOf(T[] array, T find) {
        int i = 0;
        for (T part : array) {
            if (Objects.equals(part, find)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static void doWithFields(IMethodInvocation invocation, BiConsumer<Specification, Object> consumer) {
        Specification instance = (Specification) invocation.getInstance();
        invocation.getSpec().getFields()
                .forEach(fieldInfo -> {
                    consumer.accept(instance, fieldInfo.readValue(instance));
                });
    }

    public static void detachMocks(IMethodInvocation invocation) {
        doWithFields(invocation, (spec, object) -> {
            if (MOCK_UTIL.isMock(object)) {
                MOCK_UTIL.detachMock(object);
            }
        });
    }

    public static void attachMocks(IMethodInvocation invocation) {
        doWithFields(invocation, (spec, object) -> {
            if (MOCK_UTIL.isMock(object)) {
                MOCK_UTIL.attachMock(object, spec);
            }
        });
    }
}
