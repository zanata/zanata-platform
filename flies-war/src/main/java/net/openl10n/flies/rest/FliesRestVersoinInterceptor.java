package net.openl10n.flies.rest;

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.FliesInit;
import net.openl10n.flies.service.impl.VersionManager;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;

@ServerInterceptor
public class FliesRestVersoinInterceptor implements PreProcessInterceptor
{
   public final String VERSION_NO = "Version-No";

   @Override
   public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException
   {
      String clientVer = request.getHttpHeaders().getRequestHeaders().getFirst(VERSION_NO);
      FliesInit fliesInit = (FliesInit) Component.getInstance(FliesInit.class, APPLICATION);
      VersionManager verManager = (VersionManager) Component.getInstance(VersionManager.class, APPLICATION);

      return verManager.checkVersion(clientVer, fliesInit.getVersion()) ? null : ServerResponse.copyIfNotServerResponse(Response.status(Status.PRECONDITION_FAILED).entity("Client Version " + clientVer + " /Server Version" + fliesInit.getVersion() + " MisMatch, Please get the latest client version").build());
   }

}
