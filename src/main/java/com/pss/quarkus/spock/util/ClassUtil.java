package com.pss.quarkus.spock.util;

public class ClassUtil {

    public static String toJvm(Class clazz){
       return  clazz.getName().replace('.', '/');
    }

}
