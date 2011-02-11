package net.openl10n.flies.rest.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.dto.VersionInfo;
import net.openl10n.flies.util.VersionUtility;

import org.jboss.seam.annotations.Name;


@Name("versionService")
@Path(VersionService.SERVICE_PATH)
public class VersionService implements VersionResource
{
   public static final String SERVICE_PATH = "/version";
   
   private final VersionInfo version;

   public VersionService()
   {
      this(VersionUtility.getAPIVersionInfo());
   }

   VersionService(VersionInfo ver)
   {
      this.version = ver;
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
      return new VersionInfo(version);
   }
}
