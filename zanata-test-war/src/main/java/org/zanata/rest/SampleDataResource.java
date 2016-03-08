package org.zanata.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface SampleDataResource {


    @PUT
    @Path("/languages")
    Response makeSampleLanguages();

    @PUT
    @Path("/languages/l/{locale}")
    Response addLanguage(@PathParam("locale") String localeId,
            @QueryParam("pluralForms")
            @DefaultValue("nplurals=2; plural=(n != 1);")
            String pluralForms);

    @PUT
    @Path("/accounts/u/{username}/languages")
    Response userJoinsLanguageTeams(@PathParam("username") String username,
            @QueryParam("locales") String localesCSV);

    @PUT
    @Path("/users")
    Response makeSampleUsers();

    @PUT
    @Path("/project")
    Response makeSampleProject();

    @DELETE
    Response deleteExceptEssentialData();

    /**
     * This dummy service can be used to simulate long running operation or throws exception.
     *
     * @param timeInMillis time used running this service
     * @param qualifiedExceptionClass exception to be thrown if not null
     * @return ok otherwise
     * @throws Throwable represented by qualifiedExceptionClass
     */
    @GET
    @Path("/dummy")
    Response dummyService(@QueryParam("timeUsedInMillis") long timeInMillis,
            @QueryParam("exception") String qualifiedExceptionClass) throws Throwable;
}
