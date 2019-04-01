package com.pss.quarkus.spock

import com.pss.quarkus.spock.annotations.Mocks
import com.pss.quarkus.spock.annotations.QuarkusSpec
import com.pss.quarkus.spock.exclude.SimpleBean
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@Stepwise
@QuarkusSpec
class QuarkusSpockSpec extends Specification {

    @Inject
    SimpleBean bean


    def "Test Injection"(){
        setup:
        bean.get() >> "MOCK"
        when:
        String result = bean.get()
        then:
        result == "MOCK"
    }


    def "Make Sure Mock Resets"(){
        when:
        String result = bean.get()
        then:
        result == null
    }


    @Mocks
    SimpleBean mock(){
        return Mock(SimpleBean)
    }

}