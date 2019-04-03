package com.pss.quarkus.spock

import com.pss.quarkus.spock.annotations.Mocks
import com.pss.quarkus.spock.annotations.QuarkusSpec
import com.pss.quarkus.spock.exclude.*
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

import static io.restassured.RestAssured.get

@Stepwise
@QuarkusSpec
class QuarkusSpockSpec extends Specification {

    @Inject
    SimpleBean bean


    @Inject
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    AnotherBean anotherBeanWithQualifier


    @Inject
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    ProducedBean producedBean

    def "Test Injection"() {
        setup:
        bean.get() >> "MOCK"
        when:
        String result = bean.get()
        then:
        result == "MOCK"
    }


    def "Make Sure Mock Resets"() {
        when:
        String result = bean.get()
        then:
        result == null
    }

    def "Test Another Reset Injection"() {
        setup:
        bean.get() >> "SPOCK MOCK"
        when:
        String result = bean.get()
        then:
        result == "SPOCK MOCK"
    }

    def "Check qualifiers"() {
        expect:
        anotherBeanWithQualifier != null
    }


    def "Test Rest Service"() {
        when: "We Call the Back End"
        def req = get("/endpoint")

        then: "The response body should equal 'OK'"
        req.body().asString() == "OK"
    }

    @Mocks
    SimpleBean mock() {
        return Mock(SimpleBean)
    }

    @Mocks
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    AnotherBeanImpl mockAnother() {
        return Mock(AnotherBeanImpl)
    }

    @Mocks
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    ProducedBean getOne() {
        return Mock(ProducedBean)
    }
}