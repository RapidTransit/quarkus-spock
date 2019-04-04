package com.pss.zippopotamus;

import javax.enterprise.inject.Produces;

public class BeanFactory {

    @Produces
    @Qualifying(Qualifying.Qualify.QUALIFY)
    public ProducedBean get1() {
        return new ProducedBean();
    }

    @Produces
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    public ProducedBean get2() {
        return new ProducedBean();
    }
}
