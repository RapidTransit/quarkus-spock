package com.pss.quarkus.spock.exclude;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/endpoint")
public class HttpEndpoint {

    @Inject SimpleBean simpleBean;

    @GET
    public String get(){
        return "OK";
    }
}
