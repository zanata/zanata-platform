package org.zanata.rest.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import javax.inject.Named;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.util.VersionUtility;

@RequestScoped
@Named("versionService")
@Path(VersionResource.SERVICE_PATH)
public class VersionService implements VersionResource {

    private VersionInfo version;

    @PostConstruct
    public void postConstruct() {
        this.version = VersionUtility.getAPIVersionInfo();
    }

    @Override
    public Response get() {
        return Response.ok(version).build();
    }
}
