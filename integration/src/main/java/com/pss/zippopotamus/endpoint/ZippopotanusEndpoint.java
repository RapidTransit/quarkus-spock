package com.pss.zippopotamus.endpoint;

import com.pss.zippopotamus.client.ZippopotamusClient;
import com.pss.zippopotamus.domain.ZippopotamusResult;
import com.pss.zippopotamus.service.ZippotamusService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/zip-code")
public class ZippopotanusEndpoint {

    @Inject
    ZippotamusService service;

    @Inject
    @RestClient
    ZippopotamusClient client;


    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public ZippopotamusResult get(String code){
        return Optional.ofNullable(service.find(code))
                .orElseGet(()-> {
                            ZippopotamusResult cities = client.getCities(code);
                            return service.persist(cities);
                        }

                        );
    }
}
