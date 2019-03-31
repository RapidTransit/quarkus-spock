package com.pss.quarkus.spock

import com.pss.quarkus.spock.exclude.SimpleBean
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named

@QuarkusSpec
class QuarkusSpockSpec extends Specification {

    @Inject
    SimpleBean bean1


    def "Test Injection"(){

        expect:
        bean1.getContextualInstance() != null
    }


    @Mocks
    SimpleBean mock(){
        return Mock(SimpleBean)
    }

}