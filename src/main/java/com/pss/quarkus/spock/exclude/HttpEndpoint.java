package com.pss.quarkus.spock.exclude;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/endpoint")
public class HttpEndpoint {

    @Inject SimpleBean simpleBean;

    @Qualifying(Qualifying.Qualify.QUALIFY)
    @Inject ProducedBean one;

    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    @Inject ProducedBean two;


    @GET
    public String get(){
        return "OK";
    }
}
