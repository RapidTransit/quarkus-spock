package com.pss.quarkus.spock.exclude;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnotherBeanImpl implements AnotherBean {


    @Override
    public void doNothing() {

    }
}
