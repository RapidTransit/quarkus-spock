package com.pss.quarkus.spock.exclude;

import com.pss.quarkus.spock.bytecode.InjectionOverride;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SimpleBean {

    @Inject AnotherBean anotherBean;

}
