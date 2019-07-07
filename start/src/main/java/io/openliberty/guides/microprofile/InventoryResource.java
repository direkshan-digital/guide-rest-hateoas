package io.openliberty.guides.microprofile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("hosts")
public class InventoryResource {

    @Inject
    InventoryManager manager;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray handler() {
        return manager.getSystems(uriInfo.getAbsolutePath().toString());
    }

    @GET
    @Path("{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPropertiesForHost(@PathParam("hostname") String hostname) {
        return (hostname.equals("*")) ? manager.list() : manager.get(hostname);
    }
}