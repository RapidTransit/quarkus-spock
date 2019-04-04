package com.pss.quarkus.spock

import com.pss.quarkus.spock.annotations.Mocks
import com.pss.quarkus.spock.annotations.QuarkusSpec
import com.pss.zippopotamus.client.ZippopotamusClient
import com.pss.zippopotamus.domain.ZippopotamusResult
import com.pss.zippopotamus.service.ZippotamusService
import com.pss.zippopotamus.service.ZippotamusServiceImpl
import spock.lang.Specification
import static io.restassured.RestAssured.get
import javax.inject.Inject


/**
 * Examples
 */
@QuarkusSpec
class ZippopotamusSpec extends Specification {

    @Inject
    ZippopotamusClient client

    @Inject
    ZippotamusService service


    def "Mock Client"(){
        setup:
        def fake = new ZippopotamusResult(zipcode: "12345")
        service.persist(_) >> fake
        client.getCities(_) >> fake
        when:
        def response = get("/zip-code/12345")
        then:
        response.as(ZippopotamusResult) == fake
    }


    def "Make Sure Client is not Called"(){
        setup:
        def fake = new ZippopotamusResult(zipcode: "12345")
        service.find(_) >> fake
        when:
        def response = get("/zip-code/12345")
        then:
        response.as(ZippopotamusResult) == fake
        0 * client.getCities(_)
    }

    @Mocks
    ZippotamusServiceImpl service(){
        return Mock(ZippotamusServiceImpl)
    }

    @Mocks
    ZippopotamusClient client(){
        return Mock(ZippopotamusClient)
    }
}