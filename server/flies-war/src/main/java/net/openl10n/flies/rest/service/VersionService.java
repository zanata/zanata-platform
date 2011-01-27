package net.openl10n.flies.rest.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.openl10n.flies.FliesInit;
import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.dto.VersionInfo;
import net.openl10n.flies.rest.service.VersionResource;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;


@Name("versionService")
@Path(VersionService.SERVICE_PATH)
public class VersionService implements VersionResource
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
   @Override
   @GET
   @Produces({ MediaTypes.APPLICATION_FLIES_VERSION_JSON, MediaTypes.APPLICATION_FLIES_VERSION_XML })
   public VersionInfo get()
   {
      VersionInfo entity = new VersionInfo(fliesInit.getVersion(), fliesInit.getBuildTimestamp());
      return entity;
   }
}
