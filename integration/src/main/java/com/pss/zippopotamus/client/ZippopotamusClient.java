package com.pss.zippopotamus.client;

import com.pss.zippopotamus.domain.ZippopotamusResult;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Path("/us")
@RegisterRestClient
public interface ZippopotamusClient {

    @GET
    @Path("/{code}")
    ZippopotamusResult getCities(String code);
}
