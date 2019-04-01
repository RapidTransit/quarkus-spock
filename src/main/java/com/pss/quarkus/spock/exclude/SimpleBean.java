package com.pss.quarkus.spock.exclude;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SimpleBean {

    @Inject
    AnotherBean anotherBean;

    public String get(){
        return "OK";
    }
}
