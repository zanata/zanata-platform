package org.zanata.rest;

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;
import org.zanata.service.impl.VersionManager;
import org.zanata.util.VersionUtility;


@ServerInterceptor
@HeaderDecoratorPrecedence
public class ZanataRestVersionInterceptor implements PreProcessInterceptor
{
   @Override
   public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException
   {
      String clientApiVer = request.getHttpHeaders().getRequestHeaders().getFirst(RestConstant.HEADER_VERSION_NO);
      String serverApiVer = VersionUtility.getAPIVersionInfo().getVersionNo();
      VersionManager verManager = (VersionManager) Component.getInstance(VersionManager.class, APPLICATION);

      return verManager.checkVersion(clientApiVer, serverApiVer) ? null : ServerResponse.copyIfNotServerResponse(Response.status(Status.PRECONDITION_FAILED).entity("Client API Version '" + clientApiVer + "'  and Server API Version '" + serverApiVer + "' do not match. Please update your Zanata client").build());
   }

}
