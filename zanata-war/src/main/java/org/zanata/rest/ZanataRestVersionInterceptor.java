package org.zanata.rest;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.zanata.rest.service.RestUtils;
import org.zanata.service.impl.VersionManager;
import org.zanata.util.ServiceLocator;
import org.zanata.util.VersionUtility;

import java.io.IOException;

@ConstrainedTo(RuntimeType.SERVER)
@Provider
@HeaderDecoratorPrecedence
public class ZanataRestVersionInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
            throws IOException, WebApplicationException {
        MultivaluedMap<String, String> headers = context.getHeaders();
        String clientApiVer =
                headers.getFirst(RestConstant.HEADER_VERSION_NO);
        String serverApiVer = VersionUtility.getAPIVersionInfo().getVersionNo();
        VersionManager verManager =
                ServiceLocator.instance().getInstance(VersionManager.class);

        // NB checkVersion doesn't actually reject outdated versions yet
        return verManager.checkVersion(clientApiVer, serverApiVer) ?
                context.proceed() :
                RestUtils.copyIfNotServerResponse(Response
                        .status(Status.PRECONDITION_FAILED)
                        .entity("Client API Version '"
                                + clientApiVer
                                + "'  and Server API Version '"
                                + serverApiVer
                                +
                                "' do not match. Please update your Zanata client")
                        .build());
    }
}
