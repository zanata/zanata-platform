package net.openl10n.flies.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.openl10n.flies.FliesInit;
import net.openl10n.flies.rest.dto.VersionInfo;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;


@Name("versionService")
@Path(VersionService.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class VersionService
{
   public static final String SERVICE_PATH = "/version";
   @In
   private FliesInit fliesInit;

   public VersionService(FliesInit fliesInit)
   {
      this.fliesInit = fliesInit;
   }

   public VersionService()
   {

   }

   /**
    * Retrieve VersionInfo
    * 
    * @return VersionInfo
    */
   @GET
   public VersionInfo get()
   {
      VersionInfo entity = new VersionInfo(fliesInit.getVersion(), fliesInit.getBuildTimestamp());
      return entity;
   }

}
