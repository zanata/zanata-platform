package org.zanata.rest;


import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;

@Path("/test/data/sample")
public interface SampleProjectResource {


    @PUT
    @Path("/languages")
    Response makeSampleLanguages();

    @PUT
    @Path("/accounts/u/{username}/languages")
    Response userJoinsLanguageTeams(@PathParam("username") String username,
            @QueryParam("locales") Set<LocaleId> locales);

    @PUT
    @Path("/users")
    Response makeSampleUsers();

    @PUT
    @Path("/project")
    Response makeSampleProject();

    @DELETE
    Response deleteExceptEssentialData();
}
