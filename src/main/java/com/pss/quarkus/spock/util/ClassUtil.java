package com.pss.quarkus.spock.util;

public class ClassUtil {

    public static String toSlash(Class clazz){
       return clazz.getName().replace('.', '/');
    }
}
