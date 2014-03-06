package org.zanata.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.zanata.security.ZanataIdentity;

@SecurityPrecedence
@ServerInterceptor
@Slf4j
public class ZanataRestSecurityInterceptor implements PreProcessInterceptor {

    @Override
    public ServerResponse
            preProcess(HttpRequest request, ResourceMethod method)
                    throws Failure, WebApplicationException {

        String username =
                HeaderHelper.getUserName(request);
        String apiKey =
                HeaderHelper.getApiKey(request);

        if (username != null && apiKey != null) {
            ZanataIdentity.instance().getCredentials().setUsername(username);
            ZanataIdentity.instance().setApiKey(apiKey);
            ZanataIdentity.instance().tryLogin();
            if (!ZanataIdentity.instance().isLoggedIn()) {
                log.info(
                        "Failed attempt to authenticate REST request for user {}",
                        username);
                return ServerResponse.copyIfNotServerResponse(Response.status(
                        Status.UNAUTHORIZED).build());
            }
        }
        return null;
    }
}
