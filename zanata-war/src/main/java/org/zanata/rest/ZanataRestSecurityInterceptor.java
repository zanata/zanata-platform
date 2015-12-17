package org.zanata.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.HttpUtil;

import java.io.IOException;

@Provider
@PreMatching
@SecurityPrecedence
@Slf4j
public class ZanataRestSecurityInterceptor implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {
        String username = HttpUtil.getUsername(context.getHeaders());
        String apiKey = HttpUtil.getApiKey(context.getHeaders());
        if (StringUtils.isNotEmpty(username) || StringUtils.isNotEmpty(apiKey)) {
            ZanataIdentity zanataIdentity = ZanataIdentity.instance();
            zanataIdentity.getCredentials().setUsername(username);
            zanataIdentity.setApiKey(apiKey);
            zanataIdentity.tryLogin();
            if (!SecurityFunctions.canAccessRestPath(zanataIdentity,
                    context.getMethod(), context.getUriInfo().getPath())) {
                log.info(InvalidApiKeyUtil.getMessage(username, apiKey));
                context.abortWith(Response.status(Status.UNAUTHORIZED)
                        .entity(InvalidApiKeyUtil.getMessage(username, apiKey))
                        .build());
            }
        }

    }
}
