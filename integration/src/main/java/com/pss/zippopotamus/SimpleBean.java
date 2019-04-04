package com.pss.zippopotamus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SimpleBean {

    @Inject
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    AnotherBean anotherBean;

    public String get() {
        return "OK";
    }
}
