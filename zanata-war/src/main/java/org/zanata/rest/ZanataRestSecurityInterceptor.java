package org.zanata.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.HttpUtil;

@SecurityPrecedence
@ServerInterceptor
@Slf4j
public class ZanataRestSecurityInterceptor implements PreProcessInterceptor {

    @Override
    public ServerResponse
            preProcess(HttpRequest request, ResourceMethod method)
                    throws Failure, WebApplicationException {

        String username = HttpUtil.getUsername(request);
        String apiKey = HttpUtil.getApiKey(request);
        if (StringUtils.isNotEmpty(username)|| StringUtils.isNotEmpty(apiKey)) {
            ZanataIdentity.instance().getCredentials().setUsername(username);
            ZanataIdentity.instance().setApiKey(apiKey);
            ZanataIdentity.instance().tryLogin();
            if (!SecurityFunctions.canAccessRestPath(ZanataIdentity.instance(),
                    request.getHttpMethod(), request.getPreprocessedPath())) {
                log.info(InvalidApiKeyUtil.getMessage(username, apiKey));
                return ServerResponse.copyIfNotServerResponse(Response.status(
                    Status.UNAUTHORIZED).entity(
                    InvalidApiKeyUtil.getMessage(username, apiKey))
                    .build());
            }
        }
        return null;
    }
}
